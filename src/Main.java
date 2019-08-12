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
        socket.setSoTimeout(5000);

        byte[] isLeader = "IS_LEADER".getBytes();
        DatagramPacket req = new DatagramPacket(isLeader, isLeader.length, broadcast, 4445);
        socket.send(req);

        byte[] msg = new byte[1];
        DatagramPacket resp = new DatagramPacket(msg, msg.length);

        try {
            Node node = new Node();

            socket.receive(resp);
            String leaderAddress = resp.getAddress().toString();
            if (leaderAddress.startsWith("/"))
                leaderAddress = leaderAddress.replace("/", "");

            Registry leader = LocateRegistry.getRegistry(leaderAddress, 12930);
            IMessageReceiver leaderInstance = (IMessageReceiver)leader.lookup("rmiServer");

            System.out.println("leader registered");
            leaderInstance.addNode(InetAddress.getLocalHost().toString());
            System.out.println("node added");

            (new Thread(new Runnable() {
                public void run() {
                    while (true) {
                        try {
                            leaderInstance.isAlive();
                        } catch (RemoteException ex) {
                            try {
                                node.updateLeader();
                                if (node.isLeader()) {
                                    _registry.unbind("rmiClient");
                                    startLeaderService(node);
                                }
                            } catch (RemoteException _ex) {
                            } catch (NotBoundException _ex) {
                            }
                        }
                    }
                }
            })).start();

            _registry = LocateRegistry.createRegistry(12932);
            _registry.rebind("rmiClient", node);
        } catch (SocketTimeoutException ex) {
            System.out.println("I became the leader!");
            Node node = new Node();
            node.addNode(InetAddress.getLocalHost().toString());
            startLeaderService(node);
        }
    }

    public void startLeaderService(Node node) throws RemoteException
    {
        (new Thread(new LeaderService(4445, node))).start();
        _registry = LocateRegistry.createRegistry(12930);
        _registry.rebind("rmiServer", node);
    }

    Registry _registry;

}
