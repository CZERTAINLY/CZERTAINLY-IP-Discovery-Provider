package czertainly.ip.discovery.api;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.czertainly.api.exception.ValidationException;
import com.czertainly.api.interfaces.AttributesController;
import com.czertainly.api.model.AttributeDefinition;
import czertainly.ip.discovery.service.AttributeService;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/v1/discoveryProvider/{kind}/attributes")
public class AttributesControllerImpl implements AttributesController {
	
	private static final Logger logger = LoggerFactory.getLogger(AttributesControllerImpl.class);
	
	@Autowired
	private AttributeService attributeService;

    @Override
    public List<AttributeDefinition> listAttributeDefinitions(@PathVariable String kind){
    	logger.info("Request to get the attribute for discovery for kind {}", kind);
        return attributeService.getAttributes(kind);
    }

    
    @Override
    public boolean validateAttributes(@PathVariable String kind, @RequestBody List<AttributeDefinition> attributes) throws ValidationException {
        return attributeService.validateAttributes(kind, attributes);
    }
}
