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
            String broadcastAddress = hostAddress.substring(0, hostAddress.lastIndexOf(".") + 1) + "0";

            MulticastSocket socket = new MulticastSocket(port);
            InetAddress broadcastGroup = InetAddress.getByName(broadcastAddress);
            socket.joinGroup(broadcastGroup);
            socket.setSoTimeout(100);

            byte[] request = "IS_LEADER".getBytes();
            DatagramPacket requestPacket = new DatagramPacket(request, request.length);
            
            socket.send(requestPacket);
            
            byte[] response = new byte[256];
            DatagramPacket responsePacket = new DatagramPacket(response, response.length);

            try {
                socket.receive(responsePacket);

                InetAddress leaderAddress = responsePacket.getAddress();
                

                // receive the response from the leader (which is its id and you can get the IP from the packet)
                // receive number of hosts registered on the leader
                // add hosts to availableHosts
            }
            catch (SocketTimeoutException ex) {
                // become the leader
            }
        }

        return availableHosts;
    }

    private String _interfaceName;
}
