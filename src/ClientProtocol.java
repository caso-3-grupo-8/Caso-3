import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Random;

public class ClientProtocol {
    private final static String PADDING = "AES/CBC/PKCS5Padding";
    public final static String ALGORITMO = "AES";
    public SecurityFunctions f;

    public ClientProtocol() {
        this.f = new SecurityFunctions();
    }

    public byte[] str2byte( String ss)
    {
        // Encapsulamiento con hexadecimales
        byte[] ret = new byte[ss.length()/2];
        for (int i = 0 ; i < ret.length ; i++) {
            ret[i] = (byte) Integer.parseInt(ss.substring(i*2,(i+1)*2), 16);
        }
        return ret;
    }

    public BigInteger diffieHellman(BigInteger G, BigInteger P, BigInteger Gx){
        Random rand = new Random();
        BigInteger x = new BigInteger(1064, rand);
        BigInteger y = Gx.mod(P);
        return y.modPow(x,P);
    }
    public void protocol(BufferedReader stdIn, BufferedReader pIn, PrintWriter pOut) throws Exception {
        f = new SecurityFunctions();

        String stringG;
        String stringP;
        String stringGx;
        String firma;
        pOut.println("SECURE INIT");
        if ((stringG = pIn.readLine()) != null) System.out.println("G: " + stringG);
        if ((stringP = pIn.readLine()) != null) System.out.println("P: " + stringP);
        if ((stringGx = pIn.readLine()) != null) System.out.println("Gx: " + stringGx);
        if ((firma = pIn.readLine()) != null) System.out.println("Mensaje Cifrado: " + firma);

        assert stringG != null;
        BigInteger G = new BigInteger(stringG);
        assert stringP != null;
        BigInteger P = new BigInteger(stringP);
        assert stringGx != null;
        BigInteger Gx = new BigInteger(stringGx);

        PublicKey publicaServidor = f.read_kplus("datos_asim_srv.pub","Client: ");
        String msj = G.toString()+","+P.toString()+","+Gx;

        assert firma != null;
        boolean auth = f.checkSignature(publicaServidor, str2byte(firma), msj);


        if(auth){
            pOut.println("OK");

            //TODO seguir con los pasos del protocolo

        }
        else{
            pOut.println("ERROR");
        }



        //BigInteger masterKey = diffieHellman(G,P,Gx); TODO Revisar cómo lo hacen ellos en el servidor





    }
}
