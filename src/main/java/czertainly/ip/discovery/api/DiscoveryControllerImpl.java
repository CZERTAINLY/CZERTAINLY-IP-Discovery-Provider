package czertainly.ip.discovery.api;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.czertainly.api.exception.NotFoundException;
import com.czertainly.api.interfaces.DiscoveryController;
import com.czertainly.api.model.discovery.DiscoveryProviderDto;
import czertainly.ip.discovery.dao.DiscoveryHistory;
import czertainly.ip.discovery.service.DiscoveryHistoryService;
import czertainly.ip.discovery.service.DiscoveryService;

/**
 * @author Pradeep Saminathan
 * Controller class for all certificate discovery operations.
 * The certificate discovery including all the methods
 */
@RestController
public class DiscoveryControllerImpl implements DiscoveryController {
	
	private static final Logger logger = LoggerFactory.getLogger(DiscoveryControllerImpl.class);
	
	@Autowired
	private DiscoveryService discoveryService;
	
	@Autowired
	private DiscoveryHistoryService discoveryHistoryService;

	@Override
	public DiscoveryProviderDto discoverCertificate(@RequestBody DiscoveryProviderDto request) throws IOException, NotFoundException {
		logger.info("Initiating certificate discovery for the given inputs");
		DiscoveryHistory history;
		if(request.getId() == null || request.getId() == 0) {
			history = discoveryHistoryService.addHistory(request);
			discoveryService.discoverCertificate(request, history);
		}else {
			history = discoveryHistoryService.getHistoryById(request.getId());
		}
		
		return discoveryService.getProviderDtoData(request, history);
		
	}
}
