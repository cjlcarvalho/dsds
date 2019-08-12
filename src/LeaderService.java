import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.DatagramSocket;
import java.util.Base64;
import java.rmi.RemoteException;
import java.io.IOException;
import java.net.InetSocketAddress;

public class LeaderService implements Runnable
{
    public LeaderService(int port, Node node)
    {
        _port = port;
        _node = node;
    }

    public void run()
    {
        try {
            DatagramSocket socket = new DatagramSocket(null);
            socket.setReuseAddress(true);
            socket.bind(new InetSocketAddress(12931));

            byte[] msg = new byte[256];
            DatagramPacket msgPkt = new DatagramPacket(msg, msg.length);

            while (true) {
                socket.receive(msgPkt);

                InetAddress sender = msgPkt.getAddress();
                int senderPort = msgPkt.getPort();

                String msgData = Base64.getEncoder().encodeToString(msgPkt.getData());

                if (msgData.equals("IS_LEADER")) {
                    byte[] res = "Y".getBytes();
                    DatagramPacket resPkt = new DatagramPacket(res, res.length, sender, senderPort);
                    socket.send(resPkt);
                } else if (msgData.startsWith("QUERY")) {
                    String query = msgData.replace("QUERY", "");
                    _node.execute(query);
                }
            }
        } catch (RemoteException ex) {
        } catch (IOException ex) {
        }
    }

    int _port;
    Node _node;
}
