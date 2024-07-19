package com.czertainly.discovery.ip.service;

import com.czertainly.api.model.client.attribute.RequestAttributeDto;
import com.czertainly.api.model.common.attribute.v2.content.BooleanAttributeContent;
import com.czertainly.api.model.common.attribute.v2.content.IntegerAttributeContent;
import com.czertainly.api.model.common.attribute.v2.content.StringAttributeContent;
import com.czertainly.api.model.connector.discovery.DiscoveryDataRequestDto;
import com.czertainly.api.model.connector.discovery.DiscoveryRequestDto;
import com.czertainly.discovery.ip.dao.DiscoveryHistory;
import com.czertainly.discovery.ip.service.impl.AttributeServiceImpl;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.List;

@SpringBootTest
@Transactional
@Rollback
@ActiveProfiles(profiles = "non-async")
public class DiscoveryServiceTest {

    @Autowired
    private DiscoveryService discoveryService;

    private DiscoveryRequestDto discoveryProviderDtoTest;
    private DiscoveryDataRequestDto discoveryProviderDtoTestExists;
    private DiscoveryHistory discoveryHistory;

    @BeforeEach
    public void setUp() {
        discoveryProviderDtoTest = new DiscoveryRequestDto();
        discoveryProviderDtoTest.setName("test123");

        discoveryProviderDtoTestExists = new DiscoveryDataRequestDto();
        discoveryProviderDtoTestExists.setName("test123");
        discoveryProviderDtoTestExists.setPageNumber(0);
        discoveryProviderDtoTestExists.setItemsPerPage(100);

        RequestAttributeDto kind = new RequestAttributeDto();
        kind.setUuid("72f1ce7d-3e63-458c-8954-2b950240ca33");
        kind.setName("kind");
        kind.setContent(List.of(new StringAttributeContent("IP/Hostname")));

        RequestAttributeDto ip = new RequestAttributeDto();
        ip.setUuid("1b6c48ad-c1c7-4c82-91ef-3b61bc9f52ac");
        ip.setName(AttributeServiceImpl.DATA_ATTRIBUTE_DISCOVERY_IP_NAME);
        ip.setContent(List.of(new StringAttributeContent("google.com")));

        RequestAttributeDto port = new RequestAttributeDto();
        port.setUuid("a9091e0d-f9b9-4514-b275-1dd52aa870ec");
        port.setName(AttributeServiceImpl.DATA_ATTRIBUTE_PORT_NAME);
        port.setContent(List.of(new IntegerAttributeContent(443)));

        RequestAttributeDto allPorts = new RequestAttributeDto();
        allPorts.setUuid("3c70d728-e8c3-40f9-b9b2-5d7256f89ef0");
        allPorts.setName(AttributeServiceImpl.DATA_ATTRIBUTE_ALL_PORTS_NAME);
        allPorts.setContent(List.of(new BooleanAttributeContent(false)));
        discoveryProviderDtoTest.setAttributes(Arrays.asList(kind, ip, port, allPorts));

        discoveryHistory = new DiscoveryHistory();
        discoveryHistory.setName("test");
    }

    @Test
    public void getProviderDtoDataTest(){
        Assertions.assertAll(() -> discoveryService.getProviderDtoData(discoveryProviderDtoTestExists, discoveryHistory));
    }

    @Test
    public void discoveryTest() {
        Assertions.assertAll(() -> discoveryService.discoverCertificate(discoveryProviderDtoTest, discoveryHistory));
    }
}
