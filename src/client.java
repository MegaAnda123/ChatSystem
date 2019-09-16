import java.io.*;
import java.net.Socket;

public class client {

    public static void main(String[] args){

        client c = new client();

        c.run();

    }

    private void run() {

        try {

            // Create socket
            Socket socket = new Socket("localhost",6969);

            //Create Writer
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);

            // Send message
            String message = "";
            writer.println(message);
            System.out.println("Message sent: "+ message);

            // Create input reader
            InputStreamReader inReader = new InputStreamReader(socket.getInputStream());
            BufferedReader bufReader = new BufferedReader(inReader);
            // Receive message
            String response = bufReader.readLine();
            System.out.println("Server said: "+response);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
