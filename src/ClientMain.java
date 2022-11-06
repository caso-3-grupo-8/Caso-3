import java.util.Scanner;

public class ClientMain {

    public static final int PUERTO = 4030;
    public static final String SERVIDOR = "localhost";

    public static void main(String[] args){

        Scanner input = new Scanner(System.in);

        System.out.print("Ingrese el número de clientes a enviar: ");
        int number = input.nextInt();

        ClientThread[] clientThreads = new ClientThread[number];
        medidor medidor= new medidor(0, 0, 0, 0);

        for (int i = 0; i < number; i++) {
            ClientThread clientThread = new ClientThread(PUERTO, SERVIDOR, String.valueOf(i),medidor);
            clientThreads[i] = clientThread;
        }

        for (ClientThread ct:
             clientThreads) {
            System.out.println("===================================");
            ct.run();
        }
        long tfirma= medidor.getTfirma()/number;
        long tmac= medidor.getTmac()/number;
        long tgy= medidor.getTgy()/number;
        long tcifrado= medidor.getTcifrado()/number;

        System.out.print("Tiempos promedio: \nTiempo firma: "+tfirma+"\nTiempo mac: "+
        tmac+"\nTiempo Gy: "+tgy+"\nTiempo cifrado: "+ tcifrado+"\nContador: "+medidor.getContador()+"\n\n"+medidor.getListafirma()
        +"\n\n"+medidor.getListamac()+"\n\n"+medidor.getListagy()+"\n\n"+medidor.getListacifrado());



    }
}
