package com.otilm.discovery.ip.service.v2.impl;

import com.otilm.api.model.client.connector.v2.attribute.AttributeCallbackRequestDto;
import com.otilm.api.model.common.attribute.common.AttributeType;
import com.otilm.api.model.common.attribute.common.BaseAttribute;
import com.otilm.api.model.common.attribute.common.DataAttribute;
import com.otilm.api.model.common.attribute.common.properties.DataAttributeProperties;
import com.otilm.api.model.common.attribute.v3.DataAttributeV3;
import com.otilm.discovery.ip.api.v2.AttributeCallbackNotSupportedException;
import com.otilm.discovery.ip.api.v2.AttributeDefinitionNotFoundException;
import com.otilm.discovery.ip.service.impl.AttributeServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.info.BuildProperties;

import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

class DiscoveryAttributeServiceImplTest {

    private final DiscoveryAttributeServiceImpl attributeService =
            new DiscoveryAttributeServiceImpl(buildProperties());

    private static BuildProperties buildProperties() {
        Properties properties = new Properties();
        properties.setProperty("version", "2.20.0-SNAPSHOT");
        return new BuildProperties(properties);
    }

    private BaseAttribute definition(String name) {
        return attributeService
                .listRunAttributes()
                .stream()
                .filter(attribute -> name.equals(attribute.getName()))
                .findFirst()
                .orElseThrow(() -> new AssertionError(name + " is not published"));
    }

    private static DataAttributeProperties propertiesOf(BaseAttribute attribute) {
        return ((DataAttribute) attribute).getProperties();
    }

    /**
     * {@code BaseAttributeDeserializer} reads the schema version off the wire and falls back to 2 when the field is
     * absent, so a definition that lost its version would be silently read as a v2 one by Core rather than rejected.
     */
    @Test
    void publishesEveryDefinitionAtSchemaVersionThree() {
        for (BaseAttribute attribute : attributeService.listRunAttributes()) {
            Assertions.assertEquals(3, attribute.getVersion(), attribute.getName() + " must be a v3 definition");
        }
    }

    /** Core refuses both at registration on an attribute that is not a list, so the connector must never send one. */
    @Test
    void neverMarksAnAttributeMultiSelectOrExtensibleWithoutMakingItAList() {
        for (BaseAttribute attribute : attributeService.listRunAttributes()) {
            if (attribute.getType() != AttributeType.DATA) {
                continue;
            }
            DataAttributeProperties properties = propertiesOf(attribute);
            if (properties.isMultiSelect() || properties.isExtensibleList()) {
                Assertions
                        .assertTrue(properties.isList(),
                                attribute.getName() + " is multiSelect or extensible but not a list");
            }
        }
    }

    /**
     * The v2 attributes are a redesign, not a rename. Sharing a UUID with a v1 attribute would put a different shape
     * behind an identity Core has already stored content against.
     */
    @Test
    void sharesNoIdentityWithTheV1Schema() {
        Set<String> v1Uuids = Set
                .of(AttributeServiceImpl.INFO_ATTRIBUTE_IP_HOSTNAME_UUID,
                        AttributeServiceImpl.DATA_ATTRIBUTE_DISCOVERY_IP_UUID,
                        AttributeServiceImpl.DATA_ATTRIBUTE_PORT_UUID,
                        AttributeServiceImpl.DATA_ATTRIBUTE_ALL_PORTS_UUID,
                        AttributeServiceImpl.DATA_ATTRIBUTE_PARALLEL_EXECUTIONS_UUID);
        Set<String> v1Names = Set
                .of(AttributeServiceImpl.INFO_ATTRIBUTE_IP_HOSTNAME_NAME,
                        AttributeServiceImpl.DATA_ATTRIBUTE_DISCOVERY_IP_NAME,
                        AttributeServiceImpl.DATA_ATTRIBUTE_PORT_NAME,
                        AttributeServiceImpl.DATA_ATTRIBUTE_ALL_PORTS_NAME,
                        AttributeServiceImpl.DATA_ATTRIBUTE_PARALLEL_EXECUTIONS_NAME);

        for (BaseAttribute attribute : attributeService.listRunAttributes()) {
            Assertions.assertFalse(v1Uuids.contains(attribute.getUuid()), attribute.getName() + " reuses a v1 UUID");
            Assertions.assertFalse(v1Names.contains(attribute.getName()), attribute.getName() + " reuses a v1 name");
        }
    }

    @Test
    void publishesDistinctIdentities() {
        List<BaseAttribute> published = attributeService.listRunAttributes();

        Assertions
                .assertEquals(published.size(),
                        published.stream().map(BaseAttribute::getUuid).collect(Collectors.toSet()).size());
        Assertions
                .assertEquals(published.size(),
                        published.stream().map(BaseAttribute::getName).collect(Collectors.toSet()).size());
    }

    /**
     * Content on an extensible list is a suggestion set, not a default and not a permitted set, so ports ships the
     * common choices and hosts ships none — there is no host every deployment would want.
     */
    @Test
    void offersPortSuggestionsAndNoHostSuggestions() {
        DataAttributeV3 ports = (DataAttributeV3) definition(DiscoveryAttributeServiceImpl.DATA_ATTRIBUTE_PORTS_NAME);
        List<String> suggested = ports.getContent().stream().map(content -> (String) content.getData()).toList();

        Assertions.assertTrue(suggested.contains("443"), suggested.toString());
        Assertions.assertTrue(suggested.contains("1-65535"), "the all-ports scan must stay one click: " + suggested);

        DataAttributeV3 hosts = (DataAttributeV3) definition(DiscoveryAttributeServiceImpl.DATA_ATTRIBUTE_HOSTS_NAME);
        Assertions.assertNull(hosts.getContent());
    }

    /** Not a list, so unlike hosts and ports it can carry a genuine default the form comes up holding. */
    @Test
    void keepsARealDefaultAndItsRangeOnTheParallelismAttribute() {
        DataAttributeV3 parallelism = (DataAttributeV3) definition(
                DiscoveryAttributeServiceImpl.DATA_ATTRIBUTE_PARALLEL_EXECUTIONS_NAME);

        Assertions.assertFalse(parallelism.getProperties().isList());
        Assertions
                .assertEquals(DiscoveryAttributeServiceImpl.PARALLEL_EXECUTIONS_MIN,
                        parallelism.getContent().get(0).getData());
        Assertions.assertEquals(1, parallelism.getConstraints().size(), "the 1..100 range must be published");
    }

    @Test
    void hostsIsRequiredAndPortsIsNot() {
        Assertions
                .assertTrue(propertiesOf(definition(DiscoveryAttributeServiceImpl.DATA_ATTRIBUTE_HOSTS_NAME))
                        .isRequired());
        Assertions
                .assertFalse(propertiesOf(definition(DiscoveryAttributeServiceImpl.DATA_ATTRIBUTE_PORTS_NAME))
                        .isRequired());
    }

    // --- the registry ---

    @Test
    void returnsEveryDefinitionWhenNoneIsNamed() {
        Assertions
                .assertEquals(attributeService.listRunAttributes().size(),
                        attributeService.listDefinitions(null).getDefinitions().size());
        Assertions
                .assertEquals(attributeService.listRunAttributes().size(),
                        attributeService.listDefinitions(List.of()).getDefinitions().size());
    }

    @Test
    void returnsOnlyTheDefinitionsNamed() {
        UUID ports = UUID.fromString(DiscoveryAttributeServiceImpl.DATA_ATTRIBUTE_PORTS_UUID);

        List<BaseAttribute> returned = attributeService.listDefinitions(List.of(ports)).getDefinitions();

        Assertions.assertEquals(1, returned.size());
        Assertions.assertEquals(DiscoveryAttributeServiceImpl.DATA_ATTRIBUTE_PORTS_NAME, returned.get(0).getName());
    }

    @Test
    void carriesTheConnectorVersionWithTheDefinitions() {
        Assertions.assertEquals("2.20.0-SNAPSHOT", attributeService.listDefinitions(null).getConnectorVersion());
    }

    @Test
    void resolvesADefinitionByItsUuid() {
        UUID hosts = UUID.fromString(DiscoveryAttributeServiceImpl.DATA_ATTRIBUTE_HOSTS_UUID);

        Assertions
                .assertEquals(DiscoveryAttributeServiceImpl.DATA_ATTRIBUTE_HOSTS_NAME,
                        attributeService.getDefinition(hosts).getName());
    }

    @Test
    void refusesAnUnknownDefinition() {
        UUID unknown = UUID.fromString("00000000-0000-4000-8000-000000000000");

        Assertions
                .assertThrows(AttributeDefinitionNotFoundException.class,
                        () -> attributeService.getDefinition(unknown));
    }

    /** Nothing here declares a callback, so the honest answer is a refusal rather than an empty response. */
    @Test
    void refusesACallbackForAnAttributeThatDeclaresNone() {
        AttributeCallbackRequestDto request = new AttributeCallbackRequestDto();
        request.setAttributeName(DiscoveryAttributeServiceImpl.DATA_ATTRIBUTE_HOSTS_NAME);

        AttributeCallbackNotSupportedException thrown = Assertions
                .assertThrows(AttributeCallbackNotSupportedException.class, () -> attributeService.callback(request));

        Assertions
                .assertTrue(thrown.getMessage().contains(DiscoveryAttributeServiceImpl.DATA_ATTRIBUTE_HOSTS_NAME),
                        thrown.getMessage());
    }
}
