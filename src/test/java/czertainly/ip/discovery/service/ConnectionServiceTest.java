package czertainly.ip.discovery.service;

import czertainly.ip.discovery.dto.ConnectionResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.net.SocketTimeoutException;

@SpringBootTest
public class ConnectionServiceTest{

    @Autowired
    private ConnectionService connectionService;

    @Test
    public void testConnection() throws IOException {
        ConnectionResponse certificates = connectionService.getCertificates("https://google.com");
        Assertions.assertNotNull(certificates);
    }

    @Test
    public void testConnection_Fail() {
        Assertions.assertThrows(SocketTimeoutException.class, ()-> connectionService.getCertificates("https://localhost:124"));
    }
}