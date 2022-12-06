package com.czertainly.discovery.ip.util;

import com.czertainly.api.model.common.attribute.content.BaseAttributeContent;
import com.czertainly.api.model.connector.discovery.DiscoveryRequestDto;
import com.czertainly.core.util.AttributeDefinitionUtils;
import com.czertainly.discovery.ip.service.impl.AttributeServiceImpl;
import org.apache.commons.net.util.SubnetUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DiscoverIpHandler {
	private static final Logger logger = LoggerFactory.getLogger(DiscoverIpHandler.class);
	
	private DiscoverIpHandler() {
		throw new IllegalStateException("Utility Class");
	}
	
	public static List<String> getAllIp(DiscoveryRequestDto request){
		logger.debug("Discovering the IP");

		String kind = AttributeDefinitionUtils.getAttributeContentValue("kind", request.getAttributes(), BaseAttributeContent.class);
		String ip = AttributeDefinitionUtils.getAttributeContentValue(AttributeServiceImpl.ATTRIBUTE_DISCOVERY_IP, request.getAttributes(), BaseAttributeContent.class);
		String ports = AttributeDefinitionUtils.getAttributeContentValue(AttributeServiceImpl.ATTRIBUTE_PORT, request.getAttributes(), BaseAttributeContent.class);
		Boolean allPort = AttributeDefinitionUtils.getAttributeContentValue(AttributeServiceImpl.ATTRIBUTE_ALL_PORTS, request.getAttributes(), BaseAttributeContent.class);

		if (allPort == null) {
			allPort = false;
		}

		if (ports == null) {
			ports = "443";
		}

		return getUrl(ip, kind, ports, allPort);
	}
	
	private static List<String> getIps(String ip, String kind) {
		List<String> allIP = new ArrayList<>();
			for(String indIp: ip.split(",")) {
				if(!indIp.contains("/")) {
					allIP.add(indIp);
				}
				else {
					SubnetUtils utils = new SubnetUtils(indIp);
					allIP.addAll(Arrays.asList(utils.getInfo().getAllAddresses()));
				}
			}
		return allIP;
	}
	
	private static List<String> getUrl(String ips, String kind, String ports, Boolean allPorts) {
		List<String> allIP = getIps(ips, kind);
		List<String> urls = new ArrayList<>();
		String[] port;
		if(!allPorts) {
			port = ports.split(",");
		}else {
			StringBuilder portString = new StringBuilder();
			for(int i = 0; i < 65535; i++) {
				portString.append(i).append(",");
			}
			port = portString.toString().split(",");
		}
		
		for(String ip: allIP) {
			for (String prt: port) {
				urls.add("https://" + ip + ":" + prt);
			}
		}
		return urls;
		
	}
}
