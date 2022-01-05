package czertainly.ip.discovery.service.impl;

import com.czertainly.api.model.connector.discovery.DiscoveryDataRequestDto;
import com.czertainly.api.model.connector.discovery.DiscoveryProviderDto;
import com.czertainly.api.model.connector.discovery.DiscoveryRequestDto;
import com.czertainly.api.model.core.discovery.DiscoveryStatus;
import czertainly.ip.discovery.dao.Certificate;
import czertainly.ip.discovery.dao.DiscoveryHistory;
import czertainly.ip.discovery.dto.ConnectionResponse;
import czertainly.ip.discovery.repository.CertificateRepository;
import czertainly.ip.discovery.service.ConnectionService;
import czertainly.ip.discovery.service.DiscoveryHistoryService;
import czertainly.ip.discovery.service.DiscoveryService;
import czertainly.ip.discovery.util.DiscoverIpHandler;
import czertainly.ip.discovery.util.MetaDefinitions;
import czertainly.ip.discovery.util.X509ObjectToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class DiscoveryServiceImpl implements DiscoveryService {

	private static final Logger logger = LoggerFactory.getLogger(DiscoveryServiceImpl.class);

	@Autowired
	private ConnectionService connectionService;

	@Autowired
	private CertificateRepository certificateRepository;

	@Autowired
	private DiscoveryHistoryService discoveryHistoryService;
	
	@Override
	public DiscoveryProviderDto getProviderDtoData(DiscoveryDataRequestDto request, DiscoveryHistory history) {
		DiscoveryProviderDto dto = new DiscoveryProviderDto();
		dto.setUuid(history.getUuid());
		dto.setName(history.getName());
		dto.setStatus(history.getStatus());
		dto.setMeta(MetaDefinitions.deserialize(history.getMeta()));
		int totalCertificateSize = certificateRepository.findByDiscoveryId(history.getId()).size();
		dto.setTotalCertificatesDiscovered(totalCertificateSize);
		if (history.getStatus() == DiscoveryStatus.IN_PROGRESS) {
			dto.setCertificateData(new ArrayList<>());
			dto.setTotalCertificatesDiscovered(0);
		}
		else {
				Pageable page = PageRequest.of(request.getStartIndex(), request.getEndIndex());
				dto.setCertificateData(certificateRepository.findAllByDiscoveryId(history.getId(), page).stream().map(Certificate::mapToDto).collect(Collectors.toList()));
		}
		return dto;
	}
	
	@Override
	@Async
	public void discoverCertificate(DiscoveryRequestDto request, DiscoveryHistory history) {
		logger.info("Discovery initiated for the request with name {}", request.getName());
		List<String> urls = DiscoverIpHandler.getAllIp(request);
		List<String> successUrls = new ArrayList<>();
		List<String> failedUrls = new ArrayList<>();
		List<String> allCerts = new ArrayList<>();
		for (String url : urls) {
			logger.debug("Discovering certificate for " + url);
			try {
				ConnectionResponse connection = connectionService.getCertificates(url);
				logger.debug("Connection to the url success. Certificates obtained");
				X509Certificate[] certificates = connection.getCertificates();
				for (X509Certificate certificate : certificates) {
					createCertificateEntry(certificate, history.getId(),url);
					allCerts.add("Certificate");
				}
				successUrls.add(url);
			} catch (Exception e) {
				logger.error("Unable to connect to the URL " + url);
				logger.error(e.getMessage());
				failedUrls.add(url);
			}
		}
		logger.info("Discovery {} has total of {} certificates from {} sources", request.getName(), allCerts.size(), urls.size());
		history.setStatus(DiscoveryStatus.COMPLETED);
		
		Map<String, Object> meta = new LinkedHashMap<>();
		meta.put("totalUrls", urls.size());
		meta.put("successUrls", successUrls.size());
		meta.put("failedUrls", failedUrls.size());
		history.setMeta(MetaDefinitions.serialize(meta));
		discoveryHistoryService.setHistory(history);
		logger.info("Discovery Completed. Name of the discovery is {}", request.getName());
	}
	
	private void createCertificateEntry(X509Certificate certificate, Long discoveryId, String discoverySource) {
		Map<String, Object> meta = new HashMap<>();
		meta.put("discoverySource",discoverySource);
		Certificate cert = new Certificate();
		cert.setDiscoveryId(discoveryId);
		cert.setMeta(MetaDefinitions.serialize(meta));
		cert.setBase64Content(X509ObjectToString.toPem(certificate));
		cert.setUuid(UUID.randomUUID().toString());
		certificateRepository.save(cert);
	}
	
}
