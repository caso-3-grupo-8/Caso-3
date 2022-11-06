import java.util.Scanner;

public class ClientMain {

    public static final int PUERTO = 4030;
    public static final String SERVIDOR = "localhost";

    public static void main(String[] args){

        Scanner input = new Scanner(System.in);

        System.out.print("Ingrese el número de clientes a enviar: ");
        int number = input.nextInt();

        ClientThread[] clientThreads = new ClientThread[number];

        for (int i = 0; i < number; i++) {
            ClientThread clientThread = new ClientThread(PUERTO, SERVIDOR, String.valueOf(i));
            clientThreads[i] = clientThread;
        }

        for (ClientThread ct:
             clientThreads) {
            System.out.println("====================================");
            ct.run();
        }
    }
}
