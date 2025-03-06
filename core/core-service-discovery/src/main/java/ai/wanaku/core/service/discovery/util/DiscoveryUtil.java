package ai.wanaku.core.service.discovery.util;

import java.net.InetAddress;
import java.net.UnknownHostException;

public final class DiscoveryUtil {

    private DiscoveryUtil() {}

    /**
     * Resolve the address that will be registered in the service registry
     * @return the address as a string
     */
    public static String resolveRegistrationAddress() {
        InetAddress address = null;
        try {
            address = InetAddress.getLocalHost();
            return address.getHostAddress();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }
}
