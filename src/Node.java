import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;
import java.net.UnknownHostException;

public class Node extends UnicastRemoteObject
{
    public Node() throws RemoteException, UnknownHostException
    {
        _localhost = InetAddress.getLocalHost().toString();
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

    public void addNode(Node node) throws RemoteException
    {
        if (!_currentNodes.contains(node)) {
            System.out.println("adding node");
            _currentNodes.add(node);

            for (Node n : _currentNodes)
                if (!n.equals(this))
                    n.updateNodes(_currentNodes);
        }
    }

    public void updateNodes(List<Node> nodes) throws RemoteException
    {
        _currentNodes = nodes;
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
