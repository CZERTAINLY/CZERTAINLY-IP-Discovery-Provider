package com.otilm.discovery.ip.service.v2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * What the undrained results of every live run are allowed to occupy on this node.
 *
 * <p>
 * The buffer is the only true state the connector holds, so it is the only thing that can exhaust it. Production
 * blocks rather than drops when a bound is reached — safe because Core drains from initiate onward rather than at
 * completion, so a drain is always coming — and a wait that outlives the configured window fails the run naming the
 * limit. Silent truncation is never an option: Core would fail the run anyway, with a worse message.
 *
 * <p>
 * One lock covers both the per-run and the aggregate accounting. Two would deadlock against each other, and the
 * critical section is a map lookup and some arithmetic while a probe is a network round trip.
 */
@Component
public class BufferBudget {

    private static final Logger logger = LoggerFactory.getLogger(BufferBudget.class);

    private final ReentrantLock lock = new ReentrantLock();
    private final Condition spaceFreed = lock.newCondition();
    private final Map<UUID, Holding> holdings = new HashMap<>();

    private final int maxRuns;
    private final long maxItemsPerRun;
    private final long maxBytesPerRun;
    private final long maxTotalBytes;
    private final long backpressureWaitMs;

    private long totalBytes;
    private int waiting;

    public BufferBudget(@Value("${discovery.buffer.max-runs}") int maxRuns,
            @Value("${discovery.buffer.max-items-per-run}") long maxItemsPerRun,
            @Value("${discovery.buffer.max-bytes-per-run}") long maxBytesPerRun,
            @Value("${discovery.buffer.max-total-bytes}") long maxTotalBytes,
            @Value("${discovery.buffer.backpressure-wait-ms}") long backpressureWaitMs) {
        // Rejected at construction rather than at the first insert: a zero or negative bound reads as "no limit" and
        // would quietly remove the protection this class exists to provide.
        require(maxRuns > 0, "discovery.buffer.max-runs", maxRuns);
        require(maxItemsPerRun > 0, "discovery.buffer.max-items-per-run", maxItemsPerRun);
        require(maxBytesPerRun > 0, "discovery.buffer.max-bytes-per-run", maxBytesPerRun);
        require(maxTotalBytes > 0, "discovery.buffer.max-total-bytes", maxTotalBytes);
        require(backpressureWaitMs > 0, "discovery.buffer.backpressure-wait-ms", backpressureWaitMs);
        this.maxRuns = maxRuns;
        this.maxItemsPerRun = maxItemsPerRun;
        this.maxBytesPerRun = maxBytesPerRun;
        this.maxTotalBytes = maxTotalBytes;
        this.backpressureWaitMs = backpressureWaitMs;
    }

    private static void require(boolean condition, String property, Number value) {
        if (!condition) {
            throw new IllegalArgumentException(property + " must be positive, was " + value);
        }
    }

    private static final class Holding {
        private long items;
        private long bytes;
    }

    /**
     * Admits a run, or refuses it because this node already holds as many as it can feed. Refusing at initiate is the
     * point: a run accepted beyond the cap would be starved by the others rather than told it cannot run.
     *
     * @return false when the cap is reached, or when the run is already admitted
     */
    public boolean open(UUID runId) {
        lock.lock();
        try {
            if (holdings.size() >= maxRuns || holdings.containsKey(runId)) {
                return false;
            }
            holdings.put(runId, new Holding());
            return true;
        } finally {
            lock.unlock();
        }
    }

    public void close(UUID runId) {
        lock.lock();
        try {
            Holding holding = holdings.remove(runId);
            if (holding != null) {
                totalBytes -= holding.bytes;
                spaceFreed.signalAll();
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Reserves room for one item, blocking while there is none.
     *
     * @throws BufferLimitExceededException if the item can never fit, or if the wait outlives the configured window
     * @throws InterruptedException if the scan is stopped while waiting, which is how a stop reaches a blocked probe
     */
    public void acquire(UUID runId, long bytes) throws InterruptedException {
        if (bytes > maxBytesPerRun) {
            throw new BufferLimitExceededException("a single item of " + bytes
                    + " bytes cannot fit discovery.buffer.max-bytes-per-run of " + maxBytesPerRun);
        }
        lock.lock();
        try {
            Holding holding = holdings.get(runId);
            if (holding == null) {
                throw new BufferLimitExceededException("run " + runId + " holds no buffer budget");
            }
            long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(backpressureWaitMs);
            while (blocked(holding, bytes)) {
                waiting++;
                try {
                    // awaitNanos reports what is left of the window; at or below zero the drain never came.
                    if (spaceFreed.awaitNanos(deadline - System.nanoTime()) <= 0) {
                        throw new BufferLimitExceededException("waited " + backpressureWaitMs
                                + " ms for the drain to free buffer space, limited by " + limitReached(holding, bytes));
                    }
                } finally {
                    waiting--;
                }
            }
            holding.items++;
            holding.bytes += bytes;
            totalBytes += bytes;
        } finally {
            lock.unlock();
        }
    }

    /** Returns what a drain has taken, waking whatever production is waiting on it. */
    public void release(UUID runId, long items, long bytes) {
        lock.lock();
        try {
            Holding holding = holdings.get(runId);
            if (holding == null) {
                return;
            }
            holding.items -= items;
            holding.bytes -= bytes;
            totalBytes -= bytes;
            spaceFreed.signalAll();
        } finally {
            lock.unlock();
        }
    }

    /** True while any producer is parked on a bound, which is what a run reports as its backpressured phase. */
    public boolean isBackpressured() {
        lock.lock();
        try {
            return waiting > 0;
        } finally {
            lock.unlock();
        }
    }

    public int openRuns() {
        lock.lock();
        try {
            return holdings.size();
        } finally {
            lock.unlock();
        }
    }

    private boolean blocked(Holding holding, long bytes) {
        return holding.items + 1 > maxItemsPerRun || holding.bytes + bytes > maxBytesPerRun
                || totalBytes + bytes > maxTotalBytes;
    }

    private String limitReached(Holding holding, long bytes) {
        if (holding.items + 1 > maxItemsPerRun) {
            return "discovery.buffer.max-items-per-run of " + maxItemsPerRun;
        }
        if (holding.bytes + bytes > maxBytesPerRun) {
            return "discovery.buffer.max-bytes-per-run of " + maxBytesPerRun;
        }
        return "discovery.buffer.max-total-bytes of " + maxTotalBytes;
    }

    /** The run cannot continue and must say which bound stopped it, rather than truncating its results. */
    public static class BufferLimitExceededException extends RuntimeException {
        public BufferLimitExceededException(String message) {
            super(message);
        }
    }

    void logHolding(UUID runId) {
        lock.lock();
        try {
            Holding holding = holdings.get(runId);
            if (holding != null) {
                logger.debug("Run {} holds {} items, {} bytes; node holds {} bytes", runId, holding.items,
                        holding.bytes, totalBytes);
            }
        } finally {
            lock.unlock();
        }
    }
}
