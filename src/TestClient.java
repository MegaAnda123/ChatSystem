import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.DatagramPacket;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class TestClient {
    Socket serverSocket;
    OutputStream outStream;
    InputStream inStream;
    Boolean connected = false;

    public static void main(String[] args) throws IOException, InterruptedException {
        TestClient a = new TestClient();
        a.start();
    }

    public void start() throws IOException, InterruptedException {

        while (connected==false) {
            try {
                serverSocket = new Socket("83.243.160.197", 8888);
                outStream = serverSocket.getOutputStream();
                inStream = serverSocket.getInputStream();
                connected=true;
            } catch (ConnectException e) {
                System.out.println("Failed to connect");
                TimeUnit.SECONDS.sleep(1);
            }
        }

        while (true) {
            Scanner reader = new Scanner(System.in);
            System.out.println("Press enter");
            String str = reader.nextLine();
            sendConditionedString(outStream, str);
        }
    }

    public static String fixedLengthString(String string, int length) {
        return String.format("%-"+length+"s", string);
    }

    public void sendConditionedString(OutputStream outStream, String string) {
        PrintWriter pr = new PrintWriter(outStream);
        int outPackets = (string.length()/1000)+1;
        pr.println(fixedLengthString("string message@" + outPackets ,1069));
        pr.flush();
        System.out.println(string.length());
        System.out.println(outPackets);

        String[] strings = new String[outPackets];
        for (int i=0; i< outPackets; i++) {
            if(string.length() > (i+1)*1000) {
                strings[i] = (i + "@" + string.substring(i*1000, ((i + 1) * 1000)));
            } else {
                strings[i] = (i + "@" + string.substring(i*1000));
            }
        }

        for (int i=0; i< outPackets; i++) {
            pr.println(strings[i]);
            pr.flush();
        }
    }
}
