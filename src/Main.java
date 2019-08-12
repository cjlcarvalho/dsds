import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;

public class Main
{
    public static void main(String[] args)
    {
        try
        {
            (new Main()).run();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    public String getLeaderAddress() throws Exception
    {
        InetAddress broadcast = InetAddress.getByName("192.168.1.255");

        DatagramSocket socket = new DatagramSocket();
        socket.setBroadcast(true);
        socket.setSoTimeout(1000);

        byte[] isLeader = "IS_LEADER".getBytes();
        DatagramPacket request = new DatagramPacket(isLeader, isLeader.length, broadcast,
                                                    Settings.LEADER_UDP_PORT);
        socket.send(request);

        byte[] resp = new byte[1];
        DatagramPacket response = new DatagramPacket(resp, resp.length);

        try
        {
            socket.receive(response);
            return response.getAddress().toString().replace("/", "");
        }
        catch (SocketTimeoutException ex)
        {
            return InetAddress.getLocalHost().toString();
        }
    }

    public void run () throws Exception
    {
        String leaderAddress = getLeaderAddress();
        Member member = new Member();

        if (leaderAddress.equals(InetAddress.getLocalHost().toString()))
        {
            System.out.println("I became the leader!");
            startLeaderService(member);
        }
        else
        {
            startMemberService(member, leaderAddress);
            System.out.println("I became the leader!");
            startLeaderService(member);
        }
    }

    public void startLeaderService(Member member) throws RemoteException
    {
        (new Thread(new LeaderService(Settings.LEADER_UDP_PORT, member))).start();
        Registry registry = LocateRegistry.createRegistry(Settings.LEADER_RMI_PORT);
        registry.rebind(Settings.LEADER_RMI_NAME, member);
    }

    public IMember buildLeader(String leaderAddress) throws Exception
    {
        Registry leader = LocateRegistry.getRegistry(leaderAddress, Settings.LEADER_RMI_PORT);
        IMember leaderObj = (IMember) leader.lookup(Settings.LEADER_RMI_NAME);
        leaderObj.addMember(InetAddress.getLocalHost().toString());
        return leaderObj;
    }

    public void startMemberService(Member member, String leaderAddress) throws Exception
    {
        MemberService memberService = new MemberService(member);
        (new Thread(memberService)).start();

        IMember leader = buildLeader(leaderAddress);
        System.out.println("current leader: " + leaderAddress);

        while (true)
        {
            try
            {
                leader.isAlive();
            }
            catch (RemoteException ex)
            {
                try
                {
                    leaderAddress = getLeaderAddress();

                    if (leaderAddress.equals(InetAddress.getLocalHost().toString()))
                        break;
                    else
                    {
                        leader = buildLeader(leaderAddress);
                        System.out.println("current leader: " + leaderAddress);
                    }
                }
                catch (Exception _ex)
                {
                }
            }
        }

        memberService.stop();
    }

}
