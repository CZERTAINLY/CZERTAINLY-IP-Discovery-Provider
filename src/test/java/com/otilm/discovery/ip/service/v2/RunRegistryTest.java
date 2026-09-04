package com.otilm.discovery.ip.service.v2;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

class RunRegistryTest {

    private final RunRegistry registry = new RunRegistry();

    private static RunHandle handle(long cursor) {
        return new RunHandle(RunHandle.RunState.RUNNING, cursor, cursor, "digest", cursor, 0L, Map.of());
    }

    @Test
    void holdsARunUnderTheIdCoreAssigned() {
        UUID runId = UUID.randomUUID();

        Assertions.assertTrue(registry.register(runId, handle(0)));
        Assertions.assertEquals(handle(0), registry.find(runId).orElseThrow());
        Assertions.assertEquals(1, registry.size());
    }

    /** A second initiate for a run already held is a repeat, not a new run, and the caller has to be able to tell. */
    @Test
    void refusesToRegisterTheSameRunTwice() {
        UUID runId = UUID.randomUUID();
        registry.register(runId, handle(0));

        Assertions.assertFalse(registry.register(runId, handle(99)));
        Assertions.assertEquals(handle(0), registry.find(runId).orElseThrow(), "the first handle must survive");
    }

    /**
     * A miss is not an error here. A stopped run holds nothing on this node — it is rebuilt from its replayed handle
     * on whichever replica the call reaches — so the registry reports absence and lets the caller decide.
     */
    @Test
    void reportsAnUnknownRunAsAbsent() {
        Assertions.assertEquals(Optional.empty(), registry.find(UUID.randomUUID()));
        Assertions.assertEquals(Optional.empty(), registry.update(UUID.randomUUID(), h -> h));
        Assertions.assertFalse(registry.forget(UUID.randomUUID()));
    }

    @Test
    void appliesAChangeAndReturnsTheResult() {
        UUID runId = UUID.randomUUID();
        registry.register(runId, handle(0));

        RunHandle updated = registry.update(runId, h -> h.withState(RunHandle.RunState.STOPPED)).orElseThrow();

        Assertions.assertEquals(RunHandle.RunState.STOPPED, updated.state());
        Assertions.assertEquals(RunHandle.RunState.STOPPED, registry.find(runId).orElseThrow().state());
    }

    @Test
    void tellsAFirstForgetFromARepeat() {
        UUID runId = UUID.randomUUID();
        registry.register(runId, handle(0));

        Assertions.assertTrue(registry.forget(runId));
        Assertions.assertFalse(registry.forget(runId));
        Assertions.assertEquals(0, registry.size());
    }

    /**
     * The scan advances the cursor while a lifecycle call may be stopping the run, so the update has to be one step.
     * A get-then-put would lose increments under exactly this interleaving.
     */
    @Test
    void losesNoConcurrentAdvanceOfTheCursor() throws Exception {
        UUID runId = UUID.randomUUID();
        registry.register(runId, handle(0));
        int advances = 500;

        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Callable<Object>> work = IntStream
                    .range(0, advances)
                    .<Callable<Object>>mapToObj(i -> () -> registry
                            .update(runId,
                                    h -> new RunHandle(h.state(), h.cursorIndex() + 1, h.sequenceHighWater(),
                                            h.targetsDigest(), h.targetsProcessed(), h.targetsFailed(),
                                            h.yieldByResource())))
                    .toList();
            executor.invokeAll(work);
        }

        Assertions.assertEquals(advances, registry.find(runId).orElseThrow().cursorIndex());
    }
}
