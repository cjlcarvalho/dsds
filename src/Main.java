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
import java.rmi.Naming;
import java.net.MalformedURLException;

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

    public void run () throws Exception
    {
        InetAddress broadcast = InetAddress.getByName("192.168.1.255");

        DatagramSocket socket = new DatagramSocket();
        socket.setBroadcast(true);
        socket.setSoTimeout(1000);

        byte[] isLeader = "IS_LEADER".getBytes();
        DatagramPacket req = new DatagramPacket(isLeader, isLeader.length, broadcast, Settings.LEADER_UDP_PORT);
        socket.send(req);

        byte[] msg = new byte[1];
        DatagramPacket resp = new DatagramPacket(msg, msg.length);

        try {
            Node node = new Node();

            socket.receive(resp);
            String leaderAddress = resp.getAddress().toString().replace("/", "");

            System.out.println(leaderAddress);

            Registry leader = LocateRegistry.getRegistry(leaderAddress, Settings.LEADER_RMI_PORT);
            IMessageReceiver leaderInstance = (IMessageReceiver)leader.lookup("RmiServer");
            leaderInstance.addNode(InetAddress.getLocalHost().toString());

            (new Thread(new Runnable() {
                public void run() {
                    while (true) {
                        try {
                            leaderInstance.isAlive();
                        } catch (RemoteException ex) {
                            try {
                                node.updateLeader();
                                if (node.isLeader()) {
                                    _registry.unbind("RmiClient");
                                    startLeaderService(node);
                                }
                            } catch (RemoteException _ex) {
                            } catch (NotBoundException _ex) {
                            }
                        }
                    }
                }
            })).start();

            _registry = LocateRegistry.createRegistry(Settings.NODE_RMI_PORT);
            _registry.rebind("RmiClient", node);
        } catch (SocketTimeoutException ex) {
            System.out.println("I became the leader!");

            Node node = new Node();
            node.addNode(InetAddress.getLocalHost().toString());
            startLeaderService(node);
        }
    }

    public void startLeaderService(Node node) throws RemoteException
    {
        (new Thread(new LeaderService(Settings.LEADER_UDP_PORT, node))).start();
        _registry = LocateRegistry.createRegistry(Settings.LEADER_RMI_PORT);
        _registry.rebind("RmiServer", node);
    }

    Registry _registry;

}
