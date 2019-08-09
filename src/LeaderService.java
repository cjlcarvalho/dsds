
public class LeaderService extends Thread
{
    public LeaderService(int leaderPort) {
        _leaderPort = leaderPort;
    }

    public void run() {
        // wait for leader requests on leader port
    }

    private int _leaderPort;
}
