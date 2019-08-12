import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface IMessageReceiver extends Remote
{
    boolean isLeader() throws RemoteException;
    boolean isAlive() throws RemoteException;
    void addNode(Node node) throws RemoteException;
    void updateNodes(List<Node> nodes) throws RemoteException;
    void updateLeader() throws RemoteException;
    void execute(String query) throws RemoteException;
}
