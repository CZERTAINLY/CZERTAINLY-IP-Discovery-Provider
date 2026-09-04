package com.otilm.discovery.ip.service.v2;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.UnaryOperator;

/**
 * The runs this node is scanning, keyed by the {@code runId} Core assigns.
 *
 * <p>
 * Node-local and deliberately so: a running run's buffer cannot be shared, which is what makes running runs
 * single-replica. A stopped run holds nothing here — it is rebuilt from its replayed handle on whichever replica the
 * call reaches — so a miss is not automatically an error, and callers decide what a miss means.
 */
@Component
public class RunRegistry {

    private final Map<UUID, AtomicReference<RunHandle>> runs = new ConcurrentHashMap<>();

    /**
     * @return false if the run is already registered, which is a repeated initiate rather than a new run
     */
    public boolean register(UUID runId, RunHandle handle) {
        return runs.putIfAbsent(runId, new AtomicReference<>(handle)) == null;
    }

    public Optional<RunHandle> find(UUID runId) {
        AtomicReference<RunHandle> held = runs.get(runId);
        return held == null ? Optional.empty() : Optional.of(held.get());
    }

    /**
     * Applies a change to a run's handle atomically. The scan advances the cursor while a lifecycle call may be
     * stopping the run, so read-modify-write has to be a single step rather than a get followed by a put.
     *
     * @return the handle after the change, or empty if the run is not held here
     */
    public Optional<RunHandle> update(UUID runId, UnaryOperator<RunHandle> change) {
        AtomicReference<RunHandle> held = runs.get(runId);
        return held == null ? Optional.empty() : Optional.of(held.updateAndGet(change));
    }

    /**
     * @return true if this call removed the run, so a caller can tell a first cancel from a repeat
     */
    public boolean forget(UUID runId) {
        return runs.remove(runId) != null;
    }

    public int size() {
        return runs.size();
    }
}
