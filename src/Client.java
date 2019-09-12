import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;

public class Client {
    private final String name;
    private final Socket socket;

    public Client(String name, Socket socket) throws IOException {
        this.name = name;
        this.socket = socket;
    }

    public Socket getSocket() {
        return socket;
    }

    public String getName() {
        return name;
    }
}
