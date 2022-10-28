import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Cliente {
    public static final int PUERTO = 4030;
    public static final String SERVIDOR = "localhost";

    public static void main(String[] args) throws IOException {
        Socket socket = null;
        PrintWriter escritor = null;
        BufferedReader lector = null;

        System.out.println("Cliente ...");
        try {
            socket = new Socket(SERVIDOR, PUERTO);
            escritor = new PrintWriter(socket.getOutputStream(), true);
            lector = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e){
            System.err.println("Exception: " + e.getMessage());
            System.exit(-1);
        }
        
        BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
        System.out.println("P:");
        ProtocoloCliente.diffieHellmann(stdIn,lector,escritor);
        System.out.println("G:");
        ProtocoloCliente.diffieHellmann(stdIn,lector,escritor);
        System.out.println("G2x");
        ProtocoloCliente.diffieHellmann(stdIn,lector,escritor);

        escritor.close();
        lector.close();
        socket.close();
        stdIn.close();

    }

}
