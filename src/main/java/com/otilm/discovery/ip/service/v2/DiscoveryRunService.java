package com.otilm.discovery.ip.service.v2;

import com.otilm.api.exception.ValidationException;
import com.otilm.api.model.connector.discovery.v2.DiscoveryDrainRequestDto;
import com.otilm.api.model.connector.discovery.v2.DiscoveryInitiateRequestDto;
import com.otilm.api.model.connector.discovery.v2.DiscoveryInitiateResponseDto;
import com.otilm.api.model.connector.discovery.v2.DiscoveryResultsResponseDto;
import com.otilm.api.model.connector.discovery.v2.DiscoveryRunRequestDto;
import com.otilm.api.model.connector.discovery.v2.DiscoveryRunState;
import com.otilm.api.model.connector.discovery.v2.DiscoveryStatusResponseDto;
import com.otilm.api.model.connector.discovery.v2.DiscoveryStopResponseDto;
import com.otilm.api.model.core.auth.Resource;
import com.otilm.discovery.ip.api.v2.NodeAtCapacityException;
import com.otilm.discovery.ip.api.v2.UnknownRunException;
import com.otilm.discovery.ip.service.ConnectionService;
import com.otilm.discovery.ip.util.TargetEnumeration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The run lifecycle: initiate, status, results, stop, resume, cancel.
 *
 * <p>
 * Every call replays the run's whole configuration, so nothing here is read from storage. What the node keeps is the
 * live run — its buffer and its scan — and only for as long as it is scanning or has items nobody has taken.
 */
@Service
public class DiscoveryRunService {

    private static final Logger logger = LoggerFactory.getLogger(DiscoveryRunService.class);

    /** What a scan of this connector can produce. A run asking for anything else is refused rather than shortchanged. */
    private static final Set<Resource> SUPPORTED = EnumSet.of(Resource.CERTIFICATE, Resource.CRYPTOGRAPHIC_KEY);

    private final RunRegistry registry;
    private final BufferBudget budget;
    private final DiscoveryAttributeService attributes;
    private final ConnectionService connectionService;
    private final ExecutorService scans = Executors.newVirtualThreadPerTaskExecutor();

    public DiscoveryRunService(RunRegistry registry, BufferBudget budget, DiscoveryAttributeService attributes,
            ConnectionService connectionService) {
        this.registry = registry;
        this.budget = budget;
        this.attributes = attributes;
        this.connectionService = connectionService;
    }

    /**
     * Starts a run, or recognises one already started.
     *
     * <p>
     * The contract requires the repeat to be answered idempotently, and it has to be answered without starting a
     * second scan: a second scan would renumber from 1, and Core's cursor filter would drop every item it re-emitted
     * without reporting anything wrong.
     */
    public DiscoveryInitiateResponseDto initiate(DiscoveryInitiateRequestDto request) {
        UUID runId = request.getRunId();
        var known = registry.find(runId);
        if (known.isPresent()) {
            logger.info("Run {} is already tracked; answering the repeated initiate without starting a second scan",
                    runId);
            return accepted(known.get());
        }

        requireSupported(request.getResources());
        TargetEnumeration targets = enumerate(request);
        int parallelism = attributes.readParallelExecutions(request.getAttributes());

        if (!budget.open(runId)) {
            throw new NodeAtCapacityException("this node is already scanning as many runs as it can feed");
        }
        RunHandle handle = RunHandle.initial(targets.digest());
        if (!registry.register(runId, handle)) {
            // Lost a race with a concurrent initiate for the same run: that one owns the scan, and this one answers
            // as the repeat it turned out to be.
            budget.close(runId);
            return accepted(registry.find(runId).orElseThrow(() -> new UnknownRunException(runId)));
        }

        start(runId, targets, handle, parallelism);
        return accepted(handle);
    }

    public DiscoveryStatusResponseDto status(DiscoveryRunRequestDto request) {
        UUID runId = request.getRunId();
        registry.find(runId).orElseThrow(() -> new UnknownRunException(runId));
        registry.touch(runId);

        DiscoveryStatusResponseDto response = new DiscoveryStatusResponseDto();
        response.setState(registry.state(runId).orElse(DiscoveryRunState.RUNNING));
        response.setHighestSequence(registry.buffer(runId).map(ResultBuffer::highestSequence).orElse(0L));
        // Progress is deliberately absent until there is something to report: Core keeps the last progress it was
        // given, and cannot tell an empty object from an omitted one.
        return response;
    }

    /**
     * Serves the items after the given cursor, and drops what the cursor says Core already has.
     *
     * <p>
     * Discarding first is what makes a repeat cheap, and the buffer decides what a cursor below its watermark means —
     * a late or redelivered drain is transport, not a defect.
     */
    public DiscoveryResultsResponseDto results(DiscoveryDrainRequestDto request) {
        UUID runId = request.getRunId();
        registry.find(runId).orElseThrow(() -> new UnknownRunException(runId));
        registry.touch(runId);
        ResultBuffer buffer = registry.buffer(runId).orElseThrow(() -> new UnknownRunException(runId));

        buffer.discardThrough(request.getAfterSequence());
        ResultBuffer.Page page = buffer
                .page(request.getAfterSequence(), request.getMaxItems() == null ? 500 : request.getMaxItems(),
                        request.getMaxBytes() == null ? 5L * 1024 * 1024 : request.getMaxBytes());

        DiscoveryResultsResponseDto response = new DiscoveryResultsResponseDto();
        response.setItems(page.items());
        response.setHighestSequence(page.highestSequence());
        response.setMore(page.more());
        return response;
    }

    /**
     * Stops the scan and answers with the checkpoint. The scan is interrupted rather than quiesced, so this returns
     * inside the control envelope even when every probe is stalled or parked on backpressure.
     */
    public DiscoveryStopResponseDto stop(DiscoveryRunRequestDto request) {
        UUID runId = request.getRunId();
        registry.find(runId).orElseThrow(() -> new UnknownRunException(runId));
        registry.touch(runId);

        registry.runner(runId).ifPresent(ScanRunner::stop);
        registry.setState(runId, DiscoveryRunState.STOPPED);
        RunHandle stopped = registry
                .update(runId, handle -> handle.withState(RunHandle.RunState.STOPPED))
                .orElseThrow(() -> new UnknownRunException(runId));

        DiscoveryStopResponseDto response = new DiscoveryStopResponseDto();
        response.setMeta(stopped.encode());
        return response;
    }

    /**
     * Resumes a stopped run this node still holds. A run it does not hold has to be rebuilt from its replayed
     * checkpoint, which is not implemented yet, so it answers 404 rather than pretending.
     */
    public DiscoveryInitiateResponseDto resume(DiscoveryRunRequestDto request) {
        UUID runId = request.getRunId();
        RunHandle handle = registry.find(runId).orElseThrow(() -> new UnknownRunException(runId));
        registry.touch(runId);

        if (registry.state(runId).orElse(DiscoveryRunState.RUNNING) == DiscoveryRunState.RUNNING) {
            logger.info("Run {} is already running; the resume is a no-op", runId);
            return accepted(handle);
        }

        requireSupported(request.getResources());
        TargetEnumeration targets = enumerate(request);
        int parallelism = attributes.readParallelExecutions(request.getAttributes());

        RunHandle running = registry
                .update(runId, current -> current.withState(RunHandle.RunState.RUNNING))
                .orElseThrow(() -> new UnknownRunException(runId));
        registry.setState(runId, DiscoveryRunState.RUNNING);
        start(runId, targets, running, parallelism);
        return accepted(running);
    }

    /** Forgets the run and everything it held. A later call finds nothing, which is the contract's expected answer. */
    public void cancel(DiscoveryRunRequestDto request) {
        UUID runId = request.getRunId();
        if (!registry.release(runId)) {
            throw new UnknownRunException(runId);
        }
        logger.info("Run {} cancelled and forgotten", runId);
    }

    private void start(UUID runId, TargetEnumeration targets, RunHandle handle, int parallelism) {
        ResultBuffer buffer = new ResultBuffer(runId, budget, handle.sequenceHighWater());
        ScanRunner runner = new ScanRunner(runId, targets, buffer, registry, connectionService, parallelism);
        registry.attach(runId, runner, buffer);
        scans.submit(() -> {
            try {
                if (runner.scan()) {
                    registry.setState(runId, DiscoveryRunState.COMPLETED);
                    logger.info("Run {} scanned every target", runId);
                }
            } catch (Exception e) {
                registry.setState(runId, DiscoveryRunState.FAILED);
                logger.error("Run {} failed: {}", runId, e.getMessage(), e);
            }
        });
    }

    /**
     * The targets come from the request every time rather than from the checkpoint. The checkpoint carries a digest
     * of them instead, so a resumed run can prove the enumeration it is about to continue is the one it left.
     */
    private TargetEnumeration enumerate(com.otilm.api.model.connector.discovery.v2.DiscoveryV2ScopedRequestDto request) {
        List<String> hosts = attributes.readHosts(request.getAttributes());
        List<String> ports = attributes.readPorts(request.getAttributes());
        return TargetEnumeration.of(String.join(",", hosts), String.join(",", ports), false);
    }

    /**
     * Read from {@code resources} on the request, never inferred from {@code resourceAttributes}: that map omits any
     * resource with no attributes of its own, so inferring from it would silently narrow the run's scope.
     */
    private static void requireSupported(List<Resource> resources) {
        if (resources == null || resources.isEmpty()) {
            throw new ValidationException("resources is required and must name at least one resource type");
        }
        List<String> unsupported = resources.stream().filter(r -> !SUPPORTED.contains(r)).map(Resource::getCode)
                .toList();
        if (!unsupported.isEmpty()) {
            throw new ValidationException("this connector does not discover " + unsupported + "; supported: "
                    + SUPPORTED.stream().map(Resource::getCode).toList());
        }
    }

    private static DiscoveryInitiateResponseDto accepted(RunHandle handle) {
        DiscoveryInitiateResponseDto response = new DiscoveryInitiateResponseDto();
        response.setMeta(handle.encode());
        // Stop is honoured per run rather than declared once: this connector can always stop, because its scan is
        // interruptible.
        response.setStoppable(true);
        return response;
    }
}
