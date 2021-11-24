package czertainly.ip.discovery.api;

import java.util.ArrayList;
import java.util.List;

import czertainly.ip.discovery.EndpointsListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.czertainly.api.interfaces.InfoController;
import com.czertainly.api.model.connector.FunctionGroupCode;
import com.czertainly.api.model.connector.InfoResponse;

@RestController
@RequestMapping("/v1")
public class InfoControllerImpl implements InfoController {
    private static final Logger logger = LoggerFactory.getLogger(InfoControllerImpl.class);

    @Autowired
    private EndpointsListener endpointsListener;

    @Override
    public List<InfoResponse> listSupportedFunctions() {
    	logger.info("Listing the end points for IP Discovery");
    	List<String> types = List.of("IP-Hostname");
    	List<InfoResponse> functions = new ArrayList<>(); 
        functions.add(new InfoResponse(types, FunctionGroupCode.DISCOVERY_PROVIDER, endpointsListener.getEndpoints()));
        logger.debug("Functions of the connector is obtained. Value is {}", functions.toString());
        return functions;
    }
}