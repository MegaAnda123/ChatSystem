import java.io.*;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.concurrent.TimeUnit;


public class Server {
    ServerSocket serverSocket;
    int ClientNumber = 0;
    ArrayList<Client> Clients = new ArrayList<>();
    AcceptClient ClientListener = new AcceptClient(this);
    ReceiveString MessageReceiver = new ReceiveString(this);

    public static void main(String[] args) throws IOException, InterruptedException {
        Server a = new Server();
        a.start();
    }

    public void start() throws IOException, InterruptedException {
        serverSocket = new ServerSocket(42069);
        ClientListener.start();
        MessageReceiver.start();

        while (Clients.size() == 0) {
            System.out.println("no clients connected");
            TimeUnit.SECONDS.sleep(1);
        }
    }

    public void sendString(Client client, String msg) throws IOException {
        PrintWriter printWriter = new PrintWriter(client.getSocket().getOutputStream());
        printWriter.println(msg);
        printWriter.flush();
    }

    public String receiveString(Client client) throws IOException {
        InputStreamReader in = new InputStreamReader(client.getSocket().getInputStream());
        BufferedReader bf = new BufferedReader(in);
        String out = "";
        while (client.getSocket().getInputStream().available() != 0) {
            out += bf.readLine();
        }
        System.out.println("Message length" + out.length());
        System.out.println(out);
        sendStringToAllClients(Clients,out,client);
        return out;
    }

    public void addClient() throws IOException {
        ClientNumber++;
        Clients.add(new Client("Client" + ClientNumber,serverSocket.accept()));
        System.out.println("Client" + ClientNumber + " connected");
        ClientListener.run();
    }
    public ArrayList<Client> getClients() {
        return this.Clients;
    }

    public void sendStringToAllClients(ArrayList<Client> Clients, String msg, Client sender) throws IOException {
        for (Client client : Clients) {
                sendString(client, sender.getName() + ": " + msg);
        }
    }

    private int getDataType(Client client) throws IOException {
        int i;
        InputStreamReader in = new InputStreamReader(client.getSocket().getInputStream());
        BufferedReader bf = new BufferedReader(in);
        try {
            i = Integer.parseInt(bf.readLine());
        } catch (NumberFormatException e) {
            e.printStackTrace();
            i = 0;
        }

        return i;
    }

    private void dataReceived(Client client, int dataType) throws IOException {
        System.out.println(dataType);
        final int String = 1;
        switch (dataType) {
            case String:
                receiveString(client);
                break;
        }
    }
}

