import java.util.ArrayList;
import java.util.List;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class Service
{
    public Service() {
        _selfNode = new Node("localhost", "9811");
        
        _registeredNodes = new ArrayList<>();
        _registeredNodes.add(_selfNode);

        _networkManager = new NetworkManager();
    }

    public void start() {
        _findNodes();
    }

    private void _findNodes() {
        List<String> possibleHosts = _networkManager.discoverHostsOnNetwork("9811");

        for (String host : possibleHosts) {
        
        }

        // search for other nodes in the network
        // send your id for them
        // if there is not an available node in the network, become leader
        // if there is an available node in the network, ask if it is the leader
    }

    List<Node> _registeredNodes;
    Node _selfNode;
}
