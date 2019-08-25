import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;

public class ReceiveString extends Thread {
    private Server server;
    private ArrayList<Client> Clients;

    public ReceiveString(Server server) {
        this.server = server;
    }

    @Override
    public void run() {
        this.Clients = server.getClients();
        while (true) {
            try {
                for (Client client : this.Clients) {
                    try {
                        if (client.getSocket().getInputStream().available() != 0) {
                            try {
                                System.out.println(this.server.receiveString(client));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (ConcurrentModificationException e) {
                e.printStackTrace();
            }
        }
    }
}
