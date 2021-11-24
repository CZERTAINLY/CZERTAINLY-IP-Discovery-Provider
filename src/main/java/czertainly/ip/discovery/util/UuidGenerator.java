package czertainly.ip.discovery.util;

import java.util.UUID;

public class UuidGenerator {
	private UuidGenerator() {
		throw new IllegalAccessError("Utility Class");
	}
	
	public static String uuid() {
		return UUID.randomUUID().toString();
	}
}
