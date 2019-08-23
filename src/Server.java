import com.sun.istack.internal.Nullable;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;


public class Server {
    ServerSocket serverSocket;
    int ClientNumber = 0;
    ArrayList<Client> Clients = new ArrayList<>();
    AcceptClient ClientListener = new AcceptClient(this);
    ReceiveString MessageReceiver = new ReceiveString(this);

    public void start() throws IOException, InterruptedException {
        serverSocket = new ServerSocket(8888);
        ClientListener.start();
        MessageReceiver.start();

        if (Clients.size() ==0) {
            System.out.println("no clients connected");
        }
    }

    public void sendString(Client client, String msg) throws IOException {
        PrintWriter printWriter = new PrintWriter(client.getSocket().getOutputStream());
        printWriter.println(msg);
        printWriter.flush();
    }

    public String receiveString(Socket client) throws IOException {
        InputStreamReader in = new InputStreamReader(client.getInputStream());
        BufferedReader bf = new BufferedReader(in);
        String out = bf.readLine();
        sendStringToAllClients(Clients,out,null);
        System.out.println(out);
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

    public void sendStringToAllClients(ArrayList<Client> Clients, String msg, @Nullable Client exclude) throws IOException {
        for (Client client : Clients) {
            sendString(client, client.getName() + ": " + msg);
        }
    }
}

