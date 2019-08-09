
public class Main
{
    final int LEADER_PORT = 3526;
    final int MESSAG_PORT = 4402;

    public static void main(String[] args) {
        (new Main()).start();
    }

    public void start() {
        
        while (true) {
            if (isLeader())
                _service = new LeaderService(LEADER_PORT, MESSAG_PORT);
            else
                _service = new NodeService(MESSAG_PORT);

            try {
                _service.run();
            }
            catch (LeaderException ex) {
                System.out.println("Leader has disconnect. Let's select a new one!");
            }
        }
    }

    public boolean isLeader() {
        InetAddress ip = _getMyIP();

        if (myIP != null) {
            String hostAddress = ip.getHostAddress();
            String broadcastAddress = hostAddress.substring(0, hostAddress.lastIndexOf(".") + 1) + "0";

            MulticastSocket socket = new MulticastSocket(LEADER_PORT);
            InetAddress broadcastGroup = InetAddress.getByName(broadcastAddress);
            socket.joinGroup(broadcastGroup);
            socket.setSoTimeout(100);

            byte[] request = "IS_LEADER".getBytes();
            DatagramPacket requestPacket = new DatagramPacket(request, request.length);
            socket.send(requestPacket);

            byte[] response = new byte[256];
            DatagramPacket responsePacket = new DatagramPacket(response, response.length);

            try {
                socket.receive(responsePacket);
                InetAddress leaderAddress = responsePacket.getAddress();

                byte[] reqNodes = "REQ_NODES".getBytes();
                DatagramPacket reqNodesPacket = new DatagramPacket(reqNodes, reqNodes.length);
                socket.send(reqPacket);

                byte[] respNodes = new byte[256];
                DatagramPacket respNodesPacket = new DatagramPacket(respNodes, respNodes.length);
                socket.receive(respNodesPacket);

                int nodeNum = Integer.parseInt(Base64.getEncoder().encodeToString(respNodesPacket.getData()));

                while (nodeNum > 0) {
                    socket.receive(respNodesPacket);

                    _knownNodes.add(new Node(Base64.getEncoder().encodeToString(respNodesPacket.getData())));

                    nodeNum--;
                }

                return false;
            }
            catch (SocketTimeoutException ex) {
                _knownNodes.add(new Node(ip.getHostAddress()));
                return true;
            }
        }
    }

    private List<Node> _knownNodes;
    private Node _selfNode;
    private Thread _service;
}
