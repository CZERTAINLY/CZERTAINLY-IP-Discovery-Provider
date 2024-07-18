package com.czertainly.discovery.ip.service.impl;

import com.czertainly.api.exception.NotFoundException;
import com.czertainly.api.model.connector.discovery.DiscoveryRequestDto;
import com.czertainly.api.model.core.discovery.DiscoveryStatus;
import com.czertainly.discovery.ip.dao.DiscoveryHistory;
import com.czertainly.discovery.ip.repository.DiscoveryHistoryRepository;
import com.czertainly.discovery.ip.service.DiscoveryHistoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class DiscoveryHistoryServiceImpl implements DiscoveryHistoryService {

	private static final Logger logger = LoggerFactory.getLogger(DiscoveryHistoryServiceImpl.class);

	@Autowired
	public void setDiscoveryHistoryRepository(DiscoveryHistoryRepository discoveryHistoryRepository) {
		this.discoveryHistoryRepository = discoveryHistoryRepository;
	}

	private DiscoveryHistoryRepository discoveryHistoryRepository;

	@Override
	public DiscoveryHistory addHistory(DiscoveryRequestDto request) {
		logger.debug("Adding a new entry to the database for the discovery with name {}", request.getName());
		DiscoveryHistory modal = new DiscoveryHistory();
		modal.setUuid(UUID.randomUUID().toString());
		modal.setName(request.getName());
		modal.setStatus(DiscoveryStatus.IN_PROGRESS);
		discoveryHistoryRepository.save(modal);
		return modal;
	}

	@Override
	public DiscoveryHistory getHistoryById(Long id) throws NotFoundException {
		logger.debug("Finding the discovery history record for ID {}", id);
		return discoveryHistoryRepository.findById(id).orElseThrow(() -> new NotFoundException(DiscoveryHistoryServiceImpl.class, id));
	}

	@Override
	public DiscoveryHistory getHistoryByUuid(String uuid) throws NotFoundException {
		logger.debug("Finding the discovery history record for uuid {}", uuid);
		return discoveryHistoryRepository.findByUuid(uuid).orElseThrow(() -> new NotFoundException(DiscoveryHistoryServiceImpl.class, uuid));
	}
	
	public void setHistory(DiscoveryHistory history) {
		discoveryHistoryRepository.save(history);
	}

	@Override
	public void deleteHistory(DiscoveryHistory history) {
		discoveryHistoryRepository.delete(history);
	}
}
