package czertainly.ip.discovery.api;

import com.czertainly.api.exception.NotFoundException;
import com.czertainly.api.interfaces.connector.DiscoveryController;
import com.czertainly.api.model.connector.discovery.DiscoveryDataRequestDto;
import com.czertainly.api.model.connector.discovery.DiscoveryProviderDto;
import com.czertainly.api.model.connector.discovery.DiscoveryRequestDto;
import czertainly.ip.discovery.dao.DiscoveryHistory;
import czertainly.ip.discovery.service.DiscoveryHistoryService;
import czertainly.ip.discovery.service.DiscoveryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

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
	public DiscoveryProviderDto discoverCertificate(@RequestBody DiscoveryRequestDto request) throws IOException, NotFoundException {
		logger.info("Initiating certificate discovery for the given inputs");
		DiscoveryHistory history;
		history = discoveryHistoryService.addHistory(request);
		discoveryService.discoverCertificate(request, history);
		DiscoveryDataRequestDto dto = new DiscoveryDataRequestDto();
		dto.setName(request.getName());
		return discoveryService.getProviderDtoData(dto, history);
		
	}

	@Override
	public DiscoveryProviderDto getDiscovery(String uuid, DiscoveryDataRequestDto request) throws IOException, NotFoundException {
		DiscoveryHistory history = discoveryHistoryService.getHistoryByUuid(uuid);
		return discoveryService.getProviderDtoData(request, history);
	}
}
