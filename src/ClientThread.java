import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientThread extends Thread{

    public int port;
    public String server;
    public String id;
    public medidor medidor;

    public ClientThread(int port, String server, String id,medidor medidor) {
        this.port = port;
        this.server = server;
        this.id = id;
        this.medidor= medidor;
    }

    @Override
    public void run() {
        Socket socket = null;
        PrintWriter escritor = null;
        BufferedReader lector = null;

        try {
            socket = new Socket(server, port);
            escritor = new PrintWriter(socket.getOutputStream(), true);
            lector = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e){
            System.err.println("Exception: " + e.getMessage());
            System.exit(-1);
        }

        ClientProtocol clientProtocol = new ClientProtocol(id,medidor);
        try {
            clientProtocol.protocol(lector,escritor);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        escritor.close();
        try {
            lector.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            socket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
