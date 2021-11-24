package czertainly.ip.discovery.service;

import com.czertainly.api.exception.NotFoundException;
import com.czertainly.api.model.AttributeDefinition;
import com.czertainly.api.model.discovery.DiscoveryProviderDto;
import czertainly.ip.discovery.dao.DiscoveryHistory;
import czertainly.ip.discovery.repository.CertificateRepository;
import czertainly.ip.discovery.repository.DiscoveryHistoryRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;

import javax.transaction.Transactional;
import java.io.IOException;
import java.util.Arrays;

@SpringBootTest
@Transactional
@Rollback
@ActiveProfiles(profiles = "non-async")
public class DiscoveryServiceTest {

    @Autowired
    private DiscoveryService discoveryService;

    private DiscoveryProviderDto discoveryProviderDtoTest;
    private DiscoveryHistory discoveryHistory;

    @BeforeEach
    public void setUp() {
        discoveryProviderDtoTest = new DiscoveryProviderDto();
        discoveryProviderDtoTest.setName("test123");
        discoveryProviderDtoTest.setConnectorUuid("123456");

        AttributeDefinition discoveryType = new AttributeDefinition();
        discoveryType.setId("72f1ce7d-3e63-458c-8954-2b950240ca33");
        discoveryType.setName("discoveryType");
        discoveryType.setValue("IP/Hostname");

        AttributeDefinition ip = new AttributeDefinition();
        ip.setId("1b6c48ad-c1c7-4c82-91ef-3b61bc9f52ac");
        ip.setName("ip");
        ip.setValue("google.com");

        AttributeDefinition port = new AttributeDefinition();
        port.setId("a9091e0d-f9b9-4514-b275-1dd52aa870ec");
        port.setName("port");
        port.setValue("443");

        AttributeDefinition allPorts = new AttributeDefinition();
        allPorts.setId("3c70d728-e8c3-40f9-b9b2-5d7256f89ef0");
        allPorts.setName("allPorts");
        allPorts.setValue("No");
        discoveryProviderDtoTest.setAttributes(Arrays.asList(discoveryType, ip, port, allPorts));

        discoveryHistory = new DiscoveryHistory();
        discoveryHistory.setName("test");
    }

    @Test
    public void getProviderDtoDataTest(){
        Assertions.assertAll(() -> discoveryService.getProviderDtoData(discoveryProviderDtoTest, discoveryHistory));
    }

    @Test
    public void discoveryTest() {
        Assertions.assertAll(() -> discoveryService.discoverCertificate(discoveryProviderDtoTest, discoveryHistory));
    }
}
