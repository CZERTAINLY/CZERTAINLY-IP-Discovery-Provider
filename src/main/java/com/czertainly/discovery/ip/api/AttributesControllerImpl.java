package com.czertainly.discovery.ip.api;

import com.czertainly.api.exception.ValidationException;
import com.czertainly.api.interfaces.connector.AttributesController;
import com.czertainly.api.model.client.attribute.RequestAttributeDto;
import com.czertainly.api.model.common.attribute.v2.BaseAttribute;
import com.czertainly.discovery.ip.service.AttributeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/discoveryProvider/{kind}/attributes")
public class AttributesControllerImpl implements AttributesController {

    private static final Logger logger = LoggerFactory.getLogger(AttributesControllerImpl.class);
    private AttributeService attributeService;

    @Autowired
    public void setAttributeService(AttributeService attributeService) {
        this.attributeService = attributeService;
    }

    @Override
    public List<BaseAttribute> listAttributeDefinitions(@PathVariable String kind) {
        logger.info("Request to get the attribute for discovery for kind {}", kind);
        return attributeService.getAttributes(kind);
    }


    @Override
    public void validateAttributes(@PathVariable String kind, @RequestBody List<RequestAttributeDto> attributes) throws ValidationException {
        attributeService.validateAttributes(kind, attributes);
    }

}
