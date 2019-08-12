import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IMessageReceiver extends Remote
{
    boolean isLeader() throws RemoteException;
    boolean isAlive() throws RemoteException;
    void addNode(String node) throws RemoteException;
    void updateLeader() throws RemoteException;
    void execute(String query) throws RemoteException;
}
