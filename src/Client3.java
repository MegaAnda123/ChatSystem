import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class Client3 {
    Socket socket;
    ReceiveStringClient receiver = new ReceiveStringClient(this);
    boolean connected = false;

    public static void main(String[] args) throws IOException, InterruptedException {
        Client3 a = new Client3();
        a.start();
    }

    public void start() throws IOException, InterruptedException {
        while (connected==false) {

            try {
                socket = new Socket("localhost", 8888);
                connected=true;
            } catch (ConnectException e) {
                System.out.println("Failed to connect");
                TimeUnit.SECONDS.sleep(1);
            }
        }
        Scanner reader = new Scanner(System.in);
        PrintWriter pr = new PrintWriter(socket.getOutputStream());
        receiver.start();


        while (true) {
            String str = reader.nextLine();
            pr.println(str);
            pr.flush();
        }
    }

    public Socket getSocket() {
        return socket;
    }

    public class ReceiveStringClient extends Thread {
        Client3 client;

        ReceiveStringClient(Client3 client) {
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
                        System.out.println(out);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

