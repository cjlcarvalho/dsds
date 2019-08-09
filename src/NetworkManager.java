import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.net.NetworkInterface;
import java.net.InetAddress;
import java.net.Inet4Address;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

public class NetworkManager
{
    final String NETWORK_INTERFACE_NAME = "wlp3s0";

    public static void main(String[] args) {
        try {
            List<String> hosts = (new NetworkManager()).discoverHostsOnNetwork(9031);
            for (String host : hosts) {
                System.out.println(host);
            }
        }
        catch (Exception ex) {
        }
    }

    public List<String> discoverHostsOnNetwork(int port) throws SocketException, IOException {
        List<String> availableHosts = new ArrayList<>();
        
        NetworkInterface interf = NetworkInterface.getByName(NETWORK_INTERFACE_NAME);
        Enumeration<InetAddress> enumIpAddresses = interf.getInetAddresses();

        InetAddress ipAddress = null;
        while (enumIpAddresses.hasMoreElements()) {
            InetAddress currAddress = enumIpAddresses.nextElement();

            if (currAddress instanceof Inet4Address) {
                ipAddress = currAddress;
                break;    
            }
        }

        if (ipAddress != null) {
            String hostAddress = ipAddress.getHostAddress();
            String network = hostAddress.substring(0, hostAddress.lastIndexOf(".") + 1);
            for (Integer i = 1; i <= 254; i++) {
                InetAddress targetAddress = InetAddress.getByName(network + i.toString());
                if (targetAddress.isReachable(20))
                    availableHosts.add(targetAddress.getHostAddress());
            }
        }

        return availableHosts;
    }

    private String _interfaceName;
}
