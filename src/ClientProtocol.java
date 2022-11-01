import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Random;

public class ClientProtocol {
    private final static String PADDING = "AES/CBC/PKCS5Padding";
    public final static String ALGORITMO = "AES";
    public SecurityFunctions f;

    public ClientProtocol() {
        this.f = new SecurityFunctions();
    }

    public void protocol(BufferedReader stdIn, BufferedReader pIn, PrintWriter pOut) throws Exception {
        String stringG;
        String stringP;
        String stringGx;
        String firma;

        /* 1. We send a SECURE INIT message */
        pOut.println("SECURE INIT");

        if ((stringG = pIn.readLine()) != null) System.out.println("G: " + stringG);
        if ((stringP = pIn.readLine()) != null) System.out.println("P: " + stringP);
        if ((stringGx = pIn.readLine()) != null) System.out.println("Gx: " + stringGx);
        if ((firma = pIn.readLine()) != null) System.out.println("Mensaje Cifrado: " + firma);

        /* 3. We receive G, P and Gx parameters */
        assert stringG != null;
        BigInteger G = new BigInteger(stringG);
        assert stringP != null;
        BigInteger P = new BigInteger(stringP);
        assert stringGx != null;
        BigInteger Gx = new BigInteger(stringGx);

        /* 4. We verify ciphered message with public key */
        PublicKey publicaServidor = f.read_kplus("datos_asim_srv.pub","Client: ");
        String msj = G.toString()+","+P.toString()+","+Gx;

        assert firma != null;
        boolean auth = f.checkSignature(publicaServidor, str2byte(firma), msj);


        if(auth){
            /* 5. We send an OK message */
            System.out.println("Client found signature successful, OK message sent.");
            pOut.println("OK");

            SecureRandom r = new SecureRandom();
            int integerX = Math.abs(r.nextInt());
            BigInteger x = BigInteger.valueOf((long) integerX);
            BigInteger gy = G2X(G, x, P);

            /* 6b. We send Gy */
            pOut.println(gy.toString());

            /* 7a. We calculate master key */
            BigInteger llave_maestra = calcular_llave_maestra(Gx,x,P);
            String str_llave = llave_maestra.toString();
            System.out.println("Client found llave maestra: " + str_llave);

            /* 5. We generate symmetric key and iv1 */
            SecretKey sk_srv = f.csk1(str_llave);
            SecretKey sk_mac = f.csk2(str_llave);
            byte[] iv1 = generateIvBytes();
            

        }
        else{
            /* 5. We send an ERROR message */
            System.out.println("Client found signature unsuccessful, ERROR message sent.");
            pOut.println("ERROR");
        }
    }

    /* From this line forward are the functions they provided. */
    /* Can't import theirs because we would have to create a new SrvThread object. */

    public byte[] str2byte( String ss)
    {
        // Encapsulamiento con hexadecimales
        byte[] ret = new byte[ss.length()/2];
        for (int i = 0 ; i < ret.length ; i++) {
            ret[i] = (byte) Integer.parseInt(ss.substring(i*2,(i+1)*2), 16);
        }
        return ret;
    }

    private BigInteger G2X(BigInteger base, BigInteger exponente, BigInteger modulo) {
        return base.modPow(exponente,modulo);
    }

    private BigInteger calcular_llave_maestra(BigInteger base, BigInteger exponente, BigInteger modulo) {
        return base.modPow(exponente, modulo);
    }

    private byte[] generateIvBytes() {
        byte[] iv = new byte[16];
        new SecureRandom().nextBytes(iv);
        return iv;
    }
}
