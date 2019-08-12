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

public class Member extends UnicastRemoteObject implements IMember
{
    public Member() throws RemoteException, UnknownHostException
    {
        _currentMembers = new ArrayList<String>();
        _logfile = UUID.randomUUID().toString().replace("-", "") + ".txt";
    }

    public boolean isAlive() throws RemoteException
    {
        return true;
    }

    public void addMember(String host) throws RemoteException
    {
        if (host.contains("/"))
            host = host.substring(host.indexOf("/") + 1, host.length());

        if (!_currentMembers.contains(host)) {
            System.out.println("adding host: " + host);
            _currentMember.add(host);
        }
    }

    public void execute(String query) throws RemoteException
    {
        System.out.println("executing query");

        for (String member : _currentMembers) {
            try {
                Registry memberR = LocateRegistry.getRegistry(member, Settings.MEMBER_RMI_PORT);
                IMember memberO = (IMember) nodeR.lookup("RmiMember");
                memberO.execute(query);
            } catch (NotBoundException ex) {
                System.out.println("couldn't communicate with member: " + member);
            }
        }

        updateLog(query);
    }

    public void updateLog(String query)
    {
        // TODO: update log
    }

    List<String> _currentMembers;
    String _logfile;
}
