package czertainly.ip.discovery.service;

import com.czertainly.api.exception.NotFoundException;
import com.czertainly.api.model.discovery.DiscoveryProviderDto;
import czertainly.ip.discovery.dao.DiscoveryHistory;

public interface DiscoveryHistoryService {
	public DiscoveryHistory addHistory(DiscoveryProviderDto request);
	public DiscoveryHistory getHistoryById(Long id) throws NotFoundException;
	public DiscoveryHistory getHistoryByUuid(String uuid) throws NotFoundException;
	public void setHistory(DiscoveryHistory history);
}
