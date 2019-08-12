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
        Node node = new Node();

        if (leaderAddress.equals(InetAddress.getLocalHost().toString())) {
            System.out.println("I became the leader!");
            startLeaderService(node);
        } else {
            (new Thread(new Runnable() {
                public void run() {
                    try {
                        Registry registry = LocateRegistry.createRegistry(Settings.NODE_RMI_PORT);
                        registry.rebind("RmiClient", node);
                    } catch (RemoteException ex) {
                    }
                }
            })).start();

            IMessageReceiver leader = buildLeader(leaderAddress);

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
            Registry registry = LocateRegistry.getRegistry(Settings.NODE_RMI_PORT);
            registry.unbind("RmiClient");
            startLeaderService(node);
        }
    }

    public void startLeaderService(Node node) throws RemoteException
    {
        (new Thread(new LeaderService(Settings.LEADER_UDP_PORT, node))).start();
        Registry registry = LocateRegistry.createRegistry(Settings.LEADER_RMI_PORT);
        registry.rebind("RmiServer", node);
    }

    public IMessageReceiver buildLeader(String leaderAddress) throws Exception
    {
        Registry leader = LocateRegistry.getRegistry(leaderAddress, Settings.LEADER_RMI_PORT);
        IMessageReceiver leaderObj = (IMessageReceiver) leader.lookup("RmiServer");
        leaderObj.addNode(InetAddress.getLocalHost().toString());
        return leaderObj;
    }

}
