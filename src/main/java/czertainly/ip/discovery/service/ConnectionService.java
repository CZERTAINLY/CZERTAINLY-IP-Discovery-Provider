package czertainly.ip.discovery.service;

import java.io.IOException;

import czertainly.ip.discovery.dto.ConnectionResponse;

public interface ConnectionService {
	public ConnectionResponse getCertificates(String url) throws IOException;
}

