package com.czertainly.discovery.ip.service.impl;

import com.czertainly.api.interfaces.connector.AttributesController;
import com.czertainly.api.model.common.attribute.AttributeDefinition;
import com.czertainly.api.model.common.attribute.AttributeType;
import com.czertainly.api.model.common.attribute.RequestAttributeDto;
import com.czertainly.api.model.common.attribute.content.BaseAttributeContent;
import com.czertainly.core.util.AttributeDefinitionUtils;
import com.czertainly.discovery.ip.service.AttributeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AttributeServiceImpl implements AttributeService {
	private static final Logger logger = LoggerFactory.getLogger(AttributesController.class);

	public static final String ATTRIBUTE_DISCOVERY_IP = "ip";
	public static final String ATTRIBUTE_PORT = "port";
	public static final String ATTRIBUTE_ALL_PORTS = "allPorts";
    
	@Override
	public List<AttributeDefinition> getAttributes(String kind) {
		List<AttributeDefinition> attributes = new ArrayList<>();

        AttributeDefinition ip = new AttributeDefinition();
        ip.setUuid("1b6c48ad-c1c7-4c82-91ef-3b61bc9f52ac");
        ip.setName(ATTRIBUTE_DISCOVERY_IP);
        ip.setLabel("IPs/Hostnames");
        ip.setType(AttributeType.STRING);
        ip.setRequired(true);
        ip.setReadOnly(false);
        ip.setVisible(true);
        ip.setList(false);
        ip.setMultiSelect(false);
        ip.setDescription("Multiple values can be given seperated by comma ','.");
        attributes.add(ip);
        
        AttributeDefinition port = new AttributeDefinition();
        port.setUuid("a9091e0d-f9b9-4514-b275-1dd52aa870ec");
        port.setName(ATTRIBUTE_PORT);
        port.setLabel("Ports");
        port.setType(AttributeType.STRING);
        port.setRequired(false);
        port.setReadOnly(false);
        port.setVisible(true);
        port.setList(false);
        port.setMultiSelect(false);
        port.setContent(new BaseAttributeContent<>(443));
        port.setDescription("Multiple values can be given seperated by comma ','.");
        attributes.add(port);

        AttributeDefinition allPorts = new AttributeDefinition();
        allPorts.setUuid("3c70d728-e8c3-40f9-b9b2-5d7256f89ef0");
        allPorts.setName(ATTRIBUTE_ALL_PORTS);
        allPorts.setLabel("All Ports?");
        allPorts.setType(AttributeType.BOOLEAN);
        allPorts.setRequired(false);
        allPorts.setReadOnly(false);
        allPorts.setVisible(true);
        allPorts.setList(false);
        allPorts.setMultiSelect(false);
        allPorts.setContent(new BaseAttributeContent<>(false));
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
