import java.util.UUID;

public class Node
{
    public Node(String address, String port) {
        _address = address;
        _port = port;
        _id = _generateId();
        _isLeader = false;
    }

    public Node(String address, String port, String id, boolean isLeader) {
        _address = address;
        _port = port;
        _id = id;
        _isLeader = isLeader;
    }

    public String getAddress() {
        return _address;
    }

    public String getPort() {
        return _port;
    }

    public String getId() {
        return _id;
    }

    public boolean isLeader() {
        return _isLeader;
    }
    
    public void setLeadership(boolean isLeader) {
        _isLeader = isLeader;
    }

    private String _generateId() {
        return UUID.randomUUID().toString();
    }

    private String _address;
    private String _port;
    private String _id;
    private boolean _isLeader;
}
