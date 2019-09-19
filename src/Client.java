import java.net.Socket;
import java.sql.Connection;

public class Client {
    private final String name;
    private final String password;
    private Socket socket;
    private boolean available = true;
    private boolean hasPassword;
    private int timeOut = 10;
    private DBConnector DB;
    private Connection con;

    public Client(String name, String password) {
        this.name = name;
        this.password = password;
        if(password.equals("")) {
            hasPassword = false;
        } else {
            hasPassword = true;
        }
        DB = new DBConnector();
        try {
            con = DB.getConnection();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public Socket getSocket() {
        return socket;
    }

    public String getName() {
        return name;
    }

    public boolean getAvailable() {
        return available;
    }

    public void setAvailable(boolean bool) {
        available = bool;
    }

    public boolean getHasPassword() {
        return hasPassword;
    }

    public String getPassword() {
        String hash = DB.getPassword(con,name);

        return hash;
    }

    public void setTimeOut(int timeOut) {
        this.timeOut = timeOut;
    }

    public int getTimeOut() {
        return timeOut;
    }

    public void decrementTimeOut() {
        timeOut--;
    }
}
