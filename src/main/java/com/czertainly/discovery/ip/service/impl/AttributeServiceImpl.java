package com.czertainly.discovery.ip.service.impl;

import com.czertainly.api.interfaces.connector.AttributesController;
import com.czertainly.api.model.client.attribute.RequestAttributeDto;
import com.czertainly.api.model.common.attribute.v2.AttributeType;
import com.czertainly.api.model.common.attribute.v2.BaseAttribute;
import com.czertainly.api.model.common.attribute.v2.DataAttribute;
import com.czertainly.api.model.common.attribute.v2.callback.AttributeCallback;
import com.czertainly.api.model.common.attribute.v2.callback.AttributeCallbackMapping;
import com.czertainly.api.model.common.attribute.v2.callback.AttributeValueTarget;
import com.czertainly.api.model.common.attribute.v2.content.AttributeContentType;
import com.czertainly.api.model.common.attribute.v2.content.BooleanAttributeContent;
import com.czertainly.api.model.common.attribute.v2.content.IntegerAttributeContent;
import com.czertainly.api.model.common.attribute.v2.content.StringAttributeContent;
import com.czertainly.api.model.common.attribute.v2.properties.DataAttributeProperties;
import com.czertainly.core.util.AttributeDefinitionUtils;
import com.czertainly.discovery.ip.service.AttributeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class AttributeServiceImpl implements AttributeService {
    public static final String ATTRIBUTE_DISCOVERY_IP = "ip";
    public static final String ATTRIBUTE_PORT = "port";
    public static final String ATTRIBUTE_ALL_PORTS = "allPorts";
    private static final Logger logger = LoggerFactory.getLogger(AttributesController.class);

    @Override
    public List<BaseAttribute> getAttributes(String kind) {
        List<BaseAttribute> attributes = new ArrayList<>();

        DataAttribute ip = new DataAttribute();
        ip.setUuid("1b6c48ad-c1c7-4c82-91ef-3b61bc9f52ac");
        ip.setName(ATTRIBUTE_DISCOVERY_IP);
        ip.setType(AttributeType.DATA);
        ip.setContentType(AttributeContentType.STRING);
        DataAttributeProperties ipProperties = new DataAttributeProperties();
        ipProperties.setLabel("IPs/Hostnames");
        ipProperties.setRequired(true);
        ipProperties.setReadOnly(false);
        ipProperties.setVisible(true);
        ipProperties.setList(false);
        ipProperties.setMultiSelect(false);
        ip.setDescription("Multiple values can be given seperated by comma ','.");
        ip.setProperties(ipProperties);
        attributes.add(ip);

        DataAttribute port = new DataAttribute();
        port.setUuid("a9091e0d-f9b9-4514-b275-1dd52aa870ec");
        port.setName(ATTRIBUTE_PORT);
        port.setType(AttributeType.DATA);
        port.setContentType(AttributeContentType.STRING);
        DataAttributeProperties portProperties = new DataAttributeProperties();
        portProperties.setLabel("Ports");
        portProperties.setRequired(false);
        portProperties.setReadOnly(false);
        portProperties.setVisible(true);
        portProperties.setList(false);
        portProperties.setMultiSelect(false);
        port.setContent(List.of(new StringAttributeContent("443", "443")));
        port.setDescription("Multiple values can be given separated by comma ','.");
        port.setProperties(portProperties);
        attributes.add(port);

        DataAttribute allPorts = new DataAttribute();
        allPorts.setUuid("3c70d728-e8c3-40f9-b9b2-5d7256f89ef0");
        allPorts.setName(ATTRIBUTE_ALL_PORTS);
        allPorts.setType(AttributeType.DATA);
        allPorts.setContentType(AttributeContentType.BOOLEAN);
        DataAttributeProperties allPortProperties = new DataAttributeProperties();
        allPortProperties.setLabel("All Ports?");
        allPortProperties.setRequired(false);
        allPortProperties.setReadOnly(false);
        allPortProperties.setVisible(true);
        allPortProperties.setList(false);
        allPortProperties.setMultiSelect(false);
        allPorts.setProperties(allPortProperties);
        allPorts.setContent(List.of(new BooleanAttributeContent(false)));
        allPorts.setDescription("Check to discover certificates from all ports.");
        attributes.add(allPorts);

        DataAttribute countUrls = new DataAttribute();
        countUrls.setUuid("4b2d1819-db39-4c83-9ee8-6d9e4c3bcc9c");
        countUrls.setName("urlsCount");
        countUrls.setType(AttributeType.DATA);
        countUrls.setContentType(AttributeContentType.INTEGER);
        DataAttributeProperties countUrlsProperties = new DataAttributeProperties();
        countUrlsProperties.setLabel("Total URLs");
        countUrlsProperties.setRequired(false);
        countUrlsProperties.setReadOnly(true);
        countUrlsProperties.setVisible(true);
        countUrlsProperties.setList(false);
        countUrlsProperties.setMultiSelect(false);
        countUrls.setProperties(countUrlsProperties);
        countUrls.setDescription("Check to discover count of all Urls");

        Set<AttributeCallbackMapping> mappings = new HashSet<>();
        mappings.add(new AttributeCallbackMapping(ATTRIBUTE_DISCOVERY_IP + ".data", ATTRIBUTE_DISCOVERY_IP, AttributeValueTarget.REQUEST_PARAMETER));
        mappings.add(new AttributeCallbackMapping(ATTRIBUTE_PORT + ".data", ATTRIBUTE_PORT, AttributeValueTarget.REQUEST_PARAMETER));

        AttributeCallback attributeCallback = new AttributeCallback();
        attributeCallback.setCallbackContext("/v1/discoveryProvider/IP-Hostname/attributes/urlsCount");
        attributeCallback.setCallbackMethod("GET");
        attributeCallback.setMappings(mappings);

        countUrls.setAttributeCallback(attributeCallback);
        attributes.add(countUrls);

        logger.debug("Attributes constructed. {}", attributes);
        return attributes;
    }

    @Override
    public boolean validateAttributes(String kind, List<RequestAttributeDto> attributes) {
        AttributeDefinitionUtils.validateAttributes(getAttributes(kind), attributes);
        return true;
    }

    @Override
    public List<IntegerAttributeContent> getUrlsCount(String ip, String port) {
        int portsCount = 1;
        int urlsCount = ip.split(",").length;
        if (port != null && !port.isEmpty()) {
            portsCount = port.split(",").length;
        }
        urlsCount = urlsCount * portsCount;

        return List.of(new IntegerAttributeContent(urlsCount));
    }

}
