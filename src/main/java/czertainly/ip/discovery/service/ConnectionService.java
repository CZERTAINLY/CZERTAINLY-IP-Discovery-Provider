package czertainly.ip.discovery.service;

import czertainly.ip.discovery.dto.ConnectionResponse;

import java.io.IOException;

public interface ConnectionService {
	public ConnectionResponse getCertificates(String url) throws IOException;
}

