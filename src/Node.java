import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;
import java.net.UnknownHostException;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.NotBoundException;

public class Node extends UnicastRemoteObject implements IMessageReceiver
{
    public Node() throws RemoteException, UnknownHostException
    {
        this(InetAddress.getLocalHost().toString());
    }

    public Node(String host) throws RemoteException, UnknownHostException
    {
        _localhost = host;
        if (_localhost.contains("/"))
            _localhost = _localhost.substring(_localhost.indexOf("/") + 1, _localhost.length());

        _currentNodes = new ArrayList<String>();
        _logfile = UUID.randomUUID().toString().replace("-", "") + ".txt";
    }

    public boolean equals(Node other)
    {
        return _localhost.equals(other._localhost);
    }

    public boolean isAlive() throws RemoteException
    {
        return true;
    }

    public void addNode(String node) throws RemoteException
    {
        if (node.contains("/"))
            node = node.substring(node.indexOf("/") + 1, node.length());

        if (!_currentNodes.contains(node)) {
            System.out.println("adding node: " + node);
            _currentNodes.add(node);
        }
    }

    public void execute(String query) throws RemoteException
    {
        System.out.println("executing query");

        for (String node : _currentNodes) {
            try {
                Registry nodeR = LocateRegistry.getRegistry(node, Settings.NODE_RMI_PORT);
                IMessageReceiver nodeO = (IMessageReceiver) nodeR.lookup("RmiClient");
                nodeO.execute(query);
            } catch (NotBoundException ex) {
                System.out.println("couldn't communicate with node: " + node);
            }
        }

        updateLog(query);
    }

    public void updateLog(String query)
    {
        // TODO: update log
    }

    List<String> _currentNodes;
    String _localhost;
    String _logfile;
}
