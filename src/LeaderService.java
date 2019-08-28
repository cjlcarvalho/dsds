import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.DatagramSocket;
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

                if (msgData.startsWith("IS_LEADER"))
                {
                    System.out.println("Received a request from: " + sender.toString().replace("/", ""));
                    byte[] res = "Y".getBytes();
                    DatagramPacket resPkt = new DatagramPacket(res, res.length, sender, senderPort);
                    socket.send(resPkt);

                    if (msgData.endsWith("A")) {
                        _member.addMember(sender.toString());
                    }
                }
                else if (msgData.startsWith("QUERY"))
                {
                    String query = msgData.substring(6, msgData.length());
                    System.out.println("Received a query: " + query);
                    
                    try {
                        _member.execute(query);
                        byte[] res = "Query executed!".getBytes();
                        DatagramPacket resPkt = new DatagramPacket(res, res.length, sender, senderPort);
                        socket.send(resPkt);
                    }
                    catch (Exception ex) {
                        byte[] res = "Unable to execute query!".getBytes();
                        DatagramPacket resPkt = new DatagramPacket(res, res.length, sender, senderPort);
                        socket.send(resPkt);

                        ex.printStackTrace();
                    }
                }
            }
        }
        catch (Exception ex)
        {
        }
    }

    int _port;
    Member _member;
}
