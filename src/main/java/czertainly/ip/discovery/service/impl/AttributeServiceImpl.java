package czertainly.ip.discovery.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.czertainly.api.interfaces.AttributesController;
import com.czertainly.api.model.AttributeDefinition;
import com.czertainly.api.model.BaseAttributeDefinitionTypes;
import czertainly.ip.discovery.service.AttributeService;
import com.czertainly.core.util.AttributeDefinitionUtils;

@Service
public class AttributeServiceImpl implements AttributeService{
	private static final Logger logger = LoggerFactory.getLogger(AttributesController.class);

	public static final String ATTRIBUTE_DISCOVERY_ip = "ip";
	public static final String ATTRIBUTE_PORTS = "ports";
	public static final String ATTRIBUTE_ALL_PORTS = "allPorts";
	
	@Value("${discovery.portOptions}")
	private ArrayList<String> supportedPortOptions;
    
	@Override
	public List<AttributeDefinition> getAttributes(String kind) {
		List<AttributeDefinition> attributes = new ArrayList<>();

        AttributeDefinition ip = new AttributeDefinition();
        ip.setUuid("1b6c48ad-c1c7-4c82-91ef-3b61bc9f52ac");
        ip.setName("ip");
        ip.setLabel("IP/Hostname");
        ip.setType(BaseAttributeDefinitionTypes.STRING);
        ip.setRequired(true);
        ip.setReadOnly(false);
        ip.setVisible(true);
        ip.setDescription("Multiple values can be given seperated by comma ','.");
        attributes.add(ip);
        
        AttributeDefinition port = new AttributeDefinition();
        port.setUuid("a9091e0d-f9b9-4514-b275-1dd52aa870ec");
        port.setName("port");
        port.setLabel("Port");
        port.setType(BaseAttributeDefinitionTypes.STRING);
        port.setRequired(false);
        port.setReadOnly(false);
        port.setVisible(true);
        port.setValue("443");
        port.setDescription("Multiple values can be given seperated by comma ','.");
        attributes.add(port);
        
        AttributeDefinition allPorts = new AttributeDefinition();
        allPorts.setUuid("3c70d728-e8c3-40f9-b9b2-5d7256f89ef0");
        allPorts.setName("allPorts");
        allPorts.setLabel("All Ports?");
        allPorts.setType(BaseAttributeDefinitionTypes.LIST);
        allPorts.setRequired(false);
        allPorts.setReadOnly(false);
        allPorts.setVisible(true);
        allPorts.setValue(supportedPortOptions);
        allPorts.setDescription("Select to discover certificates from all ports.");
        attributes.add(allPorts);
        logger.debug("Attributes constructed. {}", attributes.toString());
        return attributes;
	}

	@Override
	public boolean validateAttributes(String kind, List<AttributeDefinition> attributes) {
		AttributeDefinitionUtils.validateAttributes(getAttributes(kind), attributes);
        return true;
	}
	
}
