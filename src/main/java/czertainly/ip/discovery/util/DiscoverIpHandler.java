package czertainly.ip.discovery.util;

import com.czertainly.api.model.common.RequestAttributeDto;
import com.czertainly.api.model.connector.discovery.DiscoveryRequestDto;
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
		String kind = getAttributeValue(request.getAttributes(), "kind").toString();
		String ip = getAttributeValue(request.getAttributes(), "ip").toString();
		String port = getAttributeValue(request.getAttributes(), "port").toString();
		Boolean allPort = getAttributeValue(request.getAttributes(), "allPorts") == "Yes";
		
		List<String> urls = getUrl(ip, kind, port, allPort);
		return urls;
	}
	
	private static Object getAttributeValue(List<RequestAttributeDto> attributes, String attributeName) {
		for(RequestAttributeDto attribute: attributes) {
			if (attribute.getName().equals(attributeName)) {
				return attribute.getValue();
			}
		}
		return "";
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
