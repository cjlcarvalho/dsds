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
        _currentNodes = new ArrayList<Node>();
        _logfile = UUID.randomUUID().toString().replace("-", "") + ".txt";
    }

    public boolean equals(Node other)
    {
        return _localhost.equals(other._localhost);
    }

    public boolean isLeader() throws RemoteException
    {
        return _currentNodes.get(0).equals(this);
    }

    public boolean isAlive() throws RemoteException
    {
        return true;
    }

    public void addNode(String node) throws RemoteException
    {
        try {
            Node nodeInst = null;
            if (!node.equals(_localhost)) {
                Registry r = LocateRegistry.getRegistry(node, Settings.NODE_RMI_PORT);
                nodeInst = (Node) r.lookup("RmiClient");
            }
            else {
                nodeInst = this;
            }

            if (!_currentNodes.contains(nodeInst)) {
                System.out.println("adding node");
                _currentNodes.add(nodeInst);

                for (Node n : _currentNodes)
                    if (!n.equals(this))
                        n.addNode(node);
            }
        } catch (NotBoundException ex) {
        
        }
    }

    public void updateLeader() throws RemoteException
    {
        _currentNodes.remove(0);
    }

    public void execute(String query) throws RemoteException
    {
        System.out.println("executing query as leader");

        if (isLeader())
            for (Node node : _currentNodes)
                if (!node.equals(this))
                    node.execute(query);

        updateLog(query);
    }

    public void updateLog(String query)
    {
        // TODO: update log
    }

    List<Node> _currentNodes;
    String _localhost;
    String _logfile;
}
