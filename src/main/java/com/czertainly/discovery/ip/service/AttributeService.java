package com.czertainly.discovery.ip.service;

import com.czertainly.api.model.client.attribute.RequestAttributeDto;
import com.czertainly.api.model.common.attribute.v2.BaseAttribute;
import com.czertainly.api.model.common.attribute.v2.content.IntegerAttributeContent;

import java.util.List;

public interface AttributeService {
    List<BaseAttribute> getAttributes(String kind);

    boolean validateAttributes(String kind, List<RequestAttributeDto> attributes);

    List<IntegerAttributeContent> getUrlsCount(String ip, String port);
}
