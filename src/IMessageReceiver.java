import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IMessageReceiver extends Remote
{
    public boolean isAlive() throws RemoteException;
    public void addNode(String node) throws RemoteException;
    public void execute(String query) throws RemoteException;
}
