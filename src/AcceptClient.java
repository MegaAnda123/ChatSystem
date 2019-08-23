import java.io.IOException;

class AcceptClient extends Thread {
    private Server server;

    public AcceptClient(Server server) {
        this.server = server;
    }

    @Override
    public void run() {
        try {
            this.server.addClient();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
