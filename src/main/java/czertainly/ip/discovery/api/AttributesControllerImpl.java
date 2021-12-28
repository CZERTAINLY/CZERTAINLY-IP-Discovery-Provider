package czertainly.ip.discovery.api;

import com.czertainly.api.exception.ValidationException;
import com.czertainly.api.interfaces.connector.AttributesController;
import com.czertainly.api.model.common.AttributeDefinition;
import com.czertainly.api.model.common.RequestAttributeDto;
import czertainly.ip.discovery.service.AttributeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public boolean validateAttributes(@PathVariable String kind, @RequestBody List<RequestAttributeDto> attributes) throws ValidationException {
        return attributeService.validateAttributes(kind, attributes);
    }
}
