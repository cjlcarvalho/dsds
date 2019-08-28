import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;
import java.util.Scanner;

public class App
{

    public static void main(String[] args)
    {
        if (args.length < 1)
            System.out.println("Please specify your IPv4 address");
        else
            (new App(args[0])).start();
    }

    public App(String myIP)
    {
        _myIP = myIP;
    }

    public void start()
    {
        System.out.println("### Database Synchronization Distributed System ###\n\n");
        System.out.println("Your IPv4 address: " + _myIP);
        
        while (true)
        {
            System.out.print("Specify your query [EXIT to close program]: ");

            Scanner in = new Scanner(System.in);
            String s = in.nextLine();

            if (s.equals("EXIT")) {
                break;
            }
            else {
                if (!s.endsWith(";"))
                    s = s + ";";

                System.out.println("Sending query: " + s);
                    
                try 
                {
                    String output = sendQuery(s);
                    System.out.println("Server reply: " + output);
                }
                catch (Exception ex)
                {
                    System.out.println("Couldn't communicate with a leader node on your network");
                    break;
                }
            }
        }
    }

    private String sendQuery(String query) throws Exception
    {
        updateLeaderNode();

        System.out.println("Your leader is: " + _leaderIP.toString());
        
        DatagramSocket socket = new DatagramSocket();
        socket.setSoTimeout(5000);

        query = "QUERY " + query;
        
        byte[] queryBytes = query.getBytes();
        DatagramPacket request = new DatagramPacket(queryBytes, queryBytes.length, _leaderIP, 12341);
        
        socket.send(request);
        
        byte[] resp = new byte[512];
        DatagramPacket response = new DatagramPacket(resp, resp.length);
        
        try
        {
            socket.receive(response);
            String msgData = new String(response.getData(), 0, response.getLength());
            return msgData;
        }
        catch (SocketTimeoutException ex)
        {
            throw ex;
        }
    }

    private void updateLeaderNode() throws Exception
    {
        InetAddress broadcast = InetAddress.getByName(_myIP.substring(0, _myIP.lastIndexOf(".")) + ".255");

        DatagramSocket socket = new DatagramSocket();
        socket.setBroadcast(true);
        socket.setSoTimeout(1000);

        byte[] isLeader = "IS_LEADER".getBytes();
        DatagramPacket request = new DatagramPacket(isLeader, isLeader.length, broadcast,
                                                    12341);
        socket.send(request);

        byte[] resp = new byte[1];
        DatagramPacket response = new DatagramPacket(resp, resp.length);

        try
        {
            socket.receive(response);
            _leaderIP = response.getAddress();
        }
        catch (SocketTimeoutException ex)
        {
            throw ex;
        }
    }

    private String _myIP;
    private InetAddress _leaderIP;
}
