public class Settings
{
    static final int LEADER_UDP_PORT = 12341;
    static final int LEADER_RMI_PORT = 12342;
    static final int MEMBER_RMI_PORT = 12343;

    static final String LEADER_RMI_NAME = "RmiLeader";
    static final String MEMBER_RMI_NAME = "RmiMember";

    static final String DB_URI = "jdbc:postgresql://localhost:5432/postgres";
    static final String DB_USER = "postgres";
    static final String DB_PASSWD = "root";
}
