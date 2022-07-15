package com.czertainly.discovery.ip.api;

import com.czertainly.api.exception.NotFoundException;
import com.czertainly.api.interfaces.connector.DiscoveryController;
import com.czertainly.api.model.connector.discovery.DiscoveryDataRequestDto;
import com.czertainly.api.model.connector.discovery.DiscoveryProviderDto;
import com.czertainly.api.model.connector.discovery.DiscoveryRequestDto;
import com.czertainly.discovery.ip.service.DiscoveryHistoryService;
import com.czertainly.discovery.ip.service.DiscoveryService;
import com.czertainly.discovery.ip.dao.DiscoveryHistory;
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
	public void setDiscoveryService(DiscoveryService discoveryService) {
		this.discoveryService = discoveryService;
	}
	@Autowired
	public void setDiscoveryHistoryService(DiscoveryHistoryService discoveryHistoryService) {
		this.discoveryHistoryService = discoveryHistoryService;
	}

	private DiscoveryService discoveryService;
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
	public DiscoveryProviderDto getDiscovery(String uuid, DiscoveryDataRequestDto request) throws NotFoundException {
		DiscoveryHistory history = discoveryHistoryService.getHistoryByUuid(uuid);
		return discoveryService.getProviderDtoData(request, history);
	}

	@Override
	public void deleteDiscovery(String uuid) throws IOException, NotFoundException {
		discoveryService.deleteDiscovery(uuid);
	}
}
