package com.czertainly.discovery.ip.service;

import com.czertainly.api.exception.NotFoundException;
import com.czertainly.api.model.connector.discovery.DiscoveryDataRequestDto;
import com.czertainly.api.model.connector.discovery.DiscoveryProviderDto;
import com.czertainly.api.model.connector.discovery.DiscoveryRequestDto;
import com.czertainly.discovery.ip.dao.DiscoveryHistory;

import java.io.IOException;

public interface DiscoveryService {
	public void discoverCertificate(DiscoveryRequestDto request, DiscoveryHistory history) throws IOException, NotFoundException;

	public DiscoveryProviderDto getProviderDtoData(DiscoveryDataRequestDto request, DiscoveryHistory history);

    void deleteDiscovery(String uuid) throws NotFoundException;
}
