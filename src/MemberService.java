import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.RemoteException;

public class MemberService implements Runnable
{
    public MemberService(Member member)
    {
        _member = member;
    }

    public void run()
    {
        try
        {
            Registry registry = LocateRegistry.createRegistry(Settings.MEMBER_RMI_PORT);
            registry.rebind("RmiMember", _member);
        }
        catch (RemoteException ex)
        {
        }
    }

    public void stop() throws Exception
    {
        Registry registry = LocateRegistry.getRegistry(Settings.MEMBER_RMI_PORT);
        registry.unbind("RmiMember");
    }

    Member _member;
}
