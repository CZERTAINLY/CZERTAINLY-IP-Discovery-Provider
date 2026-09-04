package com.otilm.discovery.ip.service;

import com.otilm.api.model.client.attribute.RequestAttribute;
import com.otilm.api.model.common.attribute.common.BaseAttribute;

import java.util.List;

public interface AttributeService {
    List<BaseAttribute> getAttributes(String kind);

    /**
     * The same schema as {@link #getAttributes(String)}, without a kind. Discovery v2 has no kind: the run-level
     * attributes configure a run as a whole, and the resource types it targets are named separately.
     */
    List<BaseAttribute> getRunAttributes();

    boolean validateAttributes(String kind, List<RequestAttribute> attributes);
}
