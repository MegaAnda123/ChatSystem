import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class GUIClient {
    Socket socket;
    ReceiveStringClient receiver = new ReceiveStringClient(this);
    boolean connected = false;
    private String newMessage;
    boolean GUI = false;
    PrintWriter pr;
    ClientGUI clientGUI;

    public void start(boolean GUIon, ClientGUI clientGUI) throws IOException, InterruptedException {
        GUI = GUIon;
        if (clientGUI != null) {
            this.clientGUI = clientGUI;
        }
        while (connected==false) {

            try {
                socket = new Socket("83.243.160.197", 42069);
                connected=true;
            } catch (ConnectException e) {
                System.out.println("Failed to connect");
                TimeUnit.SECONDS.sleep(1);
            }
        }
        Scanner reader = new Scanner(System.in);
        pr = new PrintWriter(socket.getOutputStream());
        receiver.start();


        while (GUI==false) {
            String str = reader.nextLine();
            pr.println(str);
            pr.flush();
        }
    }

    public Socket getSocket() {
        return socket;
    }

    public class ReceiveStringClient extends Thread {
        GUIClient client;

        ReceiveStringClient(GUIClient client) {
            this.client = client;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    if (client.getSocket().getInputStream().available() != 0) {
                        InputStreamReader in = new InputStreamReader(client.getSocket().getInputStream());
                        BufferedReader bf = new BufferedReader(in);
                        String out = bf.readLine();
                        clientGUI.displayNewMessage(out);
                        System.out.println(out);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void sendNewMessage(String msg) {
        pr.println(msg);
        pr.flush();
    }
}

