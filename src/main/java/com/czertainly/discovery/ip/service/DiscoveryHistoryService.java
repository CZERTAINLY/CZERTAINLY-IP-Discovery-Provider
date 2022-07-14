package com.czertainly.discovery.ip.service;

import com.czertainly.api.exception.NotFoundException;
import com.czertainly.api.model.connector.discovery.DiscoveryRequestDto;
import com.czertainly.discovery.ip.dao.DiscoveryHistory;

public interface DiscoveryHistoryService {
	public DiscoveryHistory addHistory(DiscoveryRequestDto request);
	public DiscoveryHistory getHistoryById(Long id) throws NotFoundException;
	public DiscoveryHistory getHistoryByUuid(String uuid) throws NotFoundException;
	public void setHistory(DiscoveryHistory history);
	void deleteHistory(DiscoveryHistory history);
}
