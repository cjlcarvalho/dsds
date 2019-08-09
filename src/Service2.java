public class Service
{
    final int LEADER_PORT = 3526;
    final int MESSAG_PORT = 4402;

    public Service(String netInterface) {
        _netInterface = netInterface;
        _updateNodes();
    }

    public void start() {
        while (true) {
            if (_isLeader()) {
                _processClientMessages(); // parallel
                _waitForLeaderRequests(); // parallel
            }
            else {
                _processLeaderMessages();
            }
        }
    }

    private void _waitForLeaderRequests() {
        
    }

    private void _processLeaderMessages() {
        _updateNodes();
        // wait for query
    }

    private void _lookForLeader() {
        InetAddress myIP = _getMyIP();

        if (myIP != null) {
            String hostAddress = myIP.getHostAddress();
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
                // clear node list
                // put leader on node list
            }
            catch (SocketTimeoutException ex) {
                // check if node list is not empty
                // if it is empty, then become the leader
                // if it is not empty, then put the second as the leader
                // check if the second is alive
            }
        }
    }

    private InetAddress _getMyIP() {
        NetworkInterface interf = NetworkInterface.getByName(_netInterface);
        Enumeration<InetAddress> enumIpAddresses = interf.getInetAddresses();

        InetAddress ipAddress = null;

        while (enumIpAddresses.hasMoreElements()) {
            InetAddress currAddress = enumIpAddresses.nextElement();

            if (currAddress instanceof Inet4Address) {
                ipAddress = currAddress;
                break;
            }
        }

        return ipAddress;
    }

    private String _netInterface;
    private List<Node> _availableNodes;
    private Node _selfNode;
}
