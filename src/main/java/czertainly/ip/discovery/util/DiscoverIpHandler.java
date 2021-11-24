package czertainly.ip.discovery.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.net.util.SubnetUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.czertainly.api.model.AttributeDefinition;
import com.czertainly.api.model.discovery.DiscoveryProviderDto;

public class DiscoverIpHandler {
	private static final Logger logger = LoggerFactory.getLogger(DiscoverIpHandler.class);
	
	private DiscoverIpHandler() {
		throw new IllegalStateException("Utility Class");
	}
	
	public static List<String> getAllIp(DiscoveryProviderDto request){
		logger.debug("Discovering the IP");
		String discoveryType = getAttributeValue(request.getAttributes(), "discoveryType").toString();
		String ip = getAttributeValue(request.getAttributes(), "ip").toString();
		String port = getAttributeValue(request.getAttributes(), "port").toString();
		Boolean allPort = getAttributeValue(request.getAttributes(), "allPorts") == "Yes";
		
		List<String> urls = getUrl(ip, discoveryType, port, allPort);
		return urls;
	}
	
	private static Object getAttributeValue(List<AttributeDefinition> attributes, String attributeName) {
		for(AttributeDefinition attribute: attributes) {
			if (attribute.getName().equals(attributeName)) {
				return attribute.getValue();
			}
		}
		return "";
	}
	
	private static List<String> getIps(String ip, String discoveryType) {
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
	
	private static List<String> getUrl(String ips, String discoveryType, String ports, Boolean allPorts) {
		List<String> allIP = getIps(ips, discoveryType);
		List<String> urls = new ArrayList<>();
		String[] port;
		if(!allPorts) {
			port = ports.split(",");
		}else {
			String portString = "";
			for(Integer i=0; i < 65535; i++) {
				portString += i.toString()+",";
			}
			port = portString.split(",");
		}
		
		for(String ip: allIP) {
			for (String prt: port) {
				urls.add("https://" + ip + ":" + prt);
			}
		}
		return urls;
		
	}
}
