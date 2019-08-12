import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.registry.LocateRegistry;
import java.net.UnknownHostException;
import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.Remote;

public class Main
{
    public static void main(String[] args)
    {
        try {
            (new Main()).run();
        } catch (Exception ex) {
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
        DatagramPacket req = new DatagramPacket(isLeader, isLeader.length, broadcast,
                                                Settings.LEADER_UDP_PORT);
        socket.send(req);

        byte[] msg = new byte[1];
        DatagramPacket resp = new DatagramPacket(msg, msg.length);

        try {
            socket.receive(resp);
            return resp.getAddress().toString().replace("/", "");
        } catch (SocketTimeoutException ex) {
            return InetAddress.getLocalHost().toString();
        }
    }

    public void run () throws Exception
    {
        String leaderAddress = getLeaderAddress();
        Member member = new Member();

        if (leaderAddress.equals(InetAddress.getLocalHost().toString())) {
            System.out.println("I became the leader!");
            startLeaderService(member);
        } else {
            (new Thread(new Runnable() {
                public void run() {
                    try {
                        Registry registry = LocateRegistry.createRegistry(Settings.MEMBER_RMI_PORT);
                        registry.rebind("RmiMember", member);
                    } catch (RemoteException ex) {
                    }
                }
            })).start();

            IMember leader = buildLeader(leaderAddress);

            while (true) {
                try {
                    leader.isAlive();
                    System.out.println("the leader is alive!");
                } catch (RemoteException ex) {
                    try {
                        leaderAddress = getLeaderAddress();

                        if (leaderAddress.equals(InetAddress.getLocalHost().toString()))
                            break;
                        else
                            leader = buildLeader(leaderAddress);
                    } catch (Exception _ex) {
                    }
                }
            }

            System.out.println("I became the leader!");
            leader = null;
            Registry registry = LocateRegistry.getRegistry(Settings.MEMBER_RMI_PORT);
            registry.unbind("RmiMember");
            startLeaderService(member);
        }
    }

    public void startLeaderService(Member member) throws RemoteException
    {
        (new Thread(new LeaderService(Settings.LEADER_UDP_PORT, member))).start();
        Registry registry = LocateRegistry.createRegistry(Settings.LEADER_RMI_PORT);
        registry.rebind("RmiLeader", member);
    }

    public IMember buildLeader(String leaderAddress) throws Exception
    {
        Registry leader = LocateRegistry.getRegistry(leaderAddress, Settings.LEADER_RMI_PORT);
        IMember leaderObj = (IMessageReceiver) leader.lookup("RmiLeader");
        leaderObj.addMember(InetAddress.getLocalHost().toString());
        return leaderObj;
    }

}
