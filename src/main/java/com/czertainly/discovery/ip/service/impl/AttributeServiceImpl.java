package com.czertainly.discovery.ip.service.impl;

import com.czertainly.api.interfaces.connector.AttributesController;
import com.czertainly.api.model.client.attribute.RequestAttributeDto;
import com.czertainly.api.model.common.attribute.AttributeProperties;
import com.czertainly.api.model.common.attribute.AttributeType;
import com.czertainly.api.model.common.attribute.BaseAttribute;
import com.czertainly.api.model.common.attribute.DataAttribute;
import com.czertainly.api.model.common.attribute.content.AttributeContentType;
import com.czertainly.api.model.common.attribute.content.BooleanAttributeContent;
import com.czertainly.api.model.common.attribute.content.IntegerAttributeContent;
import com.czertainly.core.util.AttributeDefinitionUtils;
import com.czertainly.discovery.ip.service.AttributeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

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
        AttributeProperties ipProperties = new AttributeProperties();
        ipProperties.setLabel("IPs/Hostnames");
        ipProperties.setRequired(true);
        ipProperties.setReadOnly(false);
        ipProperties.setVisible(true);
        ipProperties.setList(false);
        ipProperties.setMulti(false);
        ip.setDescription("Multiple values can be given seperated by comma ','.");
        ip.setProperties(ipProperties);
        attributes.add(ip);

        DataAttribute port = new DataAttribute();
        port.setUuid("a9091e0d-f9b9-4514-b275-1dd52aa870ec");
        port.setName(ATTRIBUTE_PORT);
        port.setType(AttributeType.DATA);
        port.setContentType(AttributeContentType.INTEGER);
        AttributeProperties portProperties = new AttributeProperties();
        portProperties.setLabel("Ports");
        portProperties.setRequired(false);
        portProperties.setReadOnly(false);
        portProperties.setVisible(true);
        portProperties.setList(false);
        portProperties.setMulti(false);
        port.setContent(List.of(new IntegerAttributeContent(443)));
        port.setDescription("Multiple values can be given separated by comma ','.");
        port.setProperties(portProperties);
        attributes.add(port);

        DataAttribute allPorts = new DataAttribute();
        allPorts.setUuid("3c70d728-e8c3-40f9-b9b2-5d7256f89ef0");
        allPorts.setName(ATTRIBUTE_ALL_PORTS);
        allPorts.setType(AttributeType.DATA);
        allPorts.setContentType(AttributeContentType.BOOLEAN);
        AttributeProperties allPortProperties = new AttributeProperties();
        allPortProperties.setLabel("All Ports?");
        allPortProperties.setRequired(false);
        allPortProperties.setReadOnly(false);
        allPortProperties.setVisible(true);
        allPortProperties.setList(false);
        allPortProperties.setMulti(false);
        allPorts.setContent(List.of(new BooleanAttributeContent(false)));
        allPorts.setDescription("Check to discover certificates from all ports.");
        attributes.add(allPorts);

        logger.debug("Attributes constructed. {}", attributes);
        return attributes;
    }

    @Override
    public boolean validateAttributes(String kind, List<RequestAttributeDto> attributes) {
        AttributeDefinitionUtils.validateAttributes(getAttributes(kind), attributes);
        return true;
    }

}
