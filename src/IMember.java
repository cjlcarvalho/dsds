import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IMember extends Remote
{
    public boolean isAlive() throws RemoteException;
    public void addMember(String node) throws RemoteException;
    public void execute(String query) throws Exception;
    public void executeAsMember(int queriesExecuted, String query) throws Exception;
}
