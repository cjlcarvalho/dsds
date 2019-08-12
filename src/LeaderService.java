import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.DatagramSocket;
import java.util.Base64;
import java.rmi.RemoteException;
import java.io.IOException;
import java.net.InetSocketAddress;

public class LeaderService implements Runnable
{
    public LeaderService(int port, Member member)
    {
        _port = port;
        _member = member;
    }

    public void run()
    {
        try
        {
            DatagramSocket socket = new DatagramSocket(null);
            socket.setReuseAddress(true);
            socket.bind(new InetSocketAddress(_port));

            byte[] msg = new byte[256];
            DatagramPacket msgPkt = new DatagramPacket(msg, msg.length);

            while (true)
            {
                socket.receive(msgPkt);

                InetAddress sender = msgPkt.getAddress();
                int senderPort = msgPkt.getPort();

                String msgData = new String(msgPkt.getData(), 0, msgPkt.getLength());

                if (msgData.equals("IS_LEADER"))
                {
                    byte[] res = "Y".getBytes();
                    DatagramPacket resPkt = new DatagramPacket(res, res.length, sender, senderPort);
                    socket.send(resPkt);
                }
                else if (msgData.startsWith("QUERY"))
                {
                    String query = msgData.replace("QUERY", "");
                    _member.execute(query);
                }
            }
        }
        catch (RemoteException ex)
        {
        }
        catch (IOException ex)
        {
        }
    }

    int _port;
    Member _member;
}
