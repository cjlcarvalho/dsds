import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.rmi.NotBoundException;
import java.sql.PreparedStatement;

public class Member extends UnicastRemoteObject implements IMember
{
    public Member() throws Exception
    {
        _currentMembers = new ArrayList<String>();
        _logfile = UUID.randomUUID().toString().replace("-", "") + ".txt";
        _queriesExecuted = 0;
    }

    public boolean isAlive() throws RemoteException
    {
        return true;
    }

    public void addMember(String host) throws RemoteException
    {
        if (host.contains("/"))
            host = host.substring(host.indexOf("/") + 1, host.length());

        if (!_currentMembers.contains(host))
        {
            System.out.println("adding host: " + host);
            _currentMembers.add(host);
        }
    }

    public void execute(String query) throws Exception
    {
        System.out.println("executing query");

        String q = query.substring(6, query.length());

        _executeSQL(q);

        _queriesExecuted++;

        for (String member : _currentMembers)
        {
            try
            {
                Registry memberR = LocateRegistry.getRegistry(member, Settings.MEMBER_RMI_PORT);
                IMember memberO = (IMember) memberR.lookup(Settings.MEMBER_RMI_NAME);
                memberO.executeAsMember(_queriesExecuted, q);
            }
            catch (NotBoundException ex)
            {
                System.out.println("couldn't communicate with member: " + member);
            }
        }

        _updateLog(q);
    }

    public void executeAsMember(int queriesExecuted, String query) throws Exception
    {
        // se o valor atual - valor anterior > 1, então houve algum erro
        // pedir últimas queries ao líder a partir do número que parou

        _queriesExecuted = queriesExecuted;

        _executeSQL(query);

        _updateLog(query);
    }

    private void _updateLog(String query) throws IOException
    {
        File file = new File(_logfile);
        FileWriter fr = new FileWriter(file, true);
        fr.write(_queriesExecuted.toString() + " | " + query);
        fr.close();
    }

    private void _executeSQL(String query) throws Exception
    {
        PreparedStatement pstm = DBConnection.getConnection().prepareStatement(query);
        try
        {
            pstm.executeQuery();
        }
        catch (Exception ex)
        {
            System.out.println("erro durante execução de query sql");
        }
    }

    List<String> _currentMembers;
    String _logfile;
    Integer _queriesExecuted;
}
