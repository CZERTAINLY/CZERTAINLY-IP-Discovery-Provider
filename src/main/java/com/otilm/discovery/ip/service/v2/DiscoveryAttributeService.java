package com.otilm.discovery.ip.service.v2;

import com.otilm.api.model.client.connector.v2.attribute.AttributeCallbackRequestDto;
import com.otilm.api.model.client.connector.v2.attribute.AttributeCallbackResponseDto;
import com.otilm.api.model.client.connector.v2.attribute.AttributeDefinitionsDto;
import com.otilm.api.model.common.attribute.common.BaseAttribute;

import java.util.List;
import java.util.UUID;

/**
 * The v2 attribute schema and its registry.
 *
 * <p>
 * Deliberately shares nothing with the v1 {@code AttributeService}: that one reaches the discovery history and the
 * certificate repository, and the v2 surface stores nothing. The two schemas are also different shapes — v1 splits
 * comma-separated strings, v2 takes lists — so a shared reader would have to serve both and would serve neither well.
 */
public interface DiscoveryAttributeService {

    /** The schema that configures a run as a whole. Every attribute this connector defines is run-level. */
    List<BaseAttribute> listRunAttributes();

    /** The registry: all definitions, or only those named. */
    AttributeDefinitionsDto listDefinitions(List<UUID> uuids);

    BaseAttribute getDefinition(UUID uuid);

    AttributeCallbackResponseDto callback(AttributeCallbackRequestDto request);
}
