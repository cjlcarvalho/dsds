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
        _currentNodes = new ArrayList<Node>();
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

        System.out.println("adding node: " + node);
        _currentNodes.add(node);
    }

    public void execute(String query) throws RemoteException
    {
        System.out.println("executing query as leader");

        for (String node : _currentNodes) {
            Registry nodeR = LocateRegistry.getRegistry(leaderAddress, Settings.NODE_RMI_PORT);
            IMessageReceiver nodeO = (IMessageReceiver)leader.lookup("RmiClient");
            nodeO.execute(query);
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
