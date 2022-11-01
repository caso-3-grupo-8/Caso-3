import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
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

            /* 7a. We calculate master key, generate symmetric key and iv1*/
            BigInteger llave_maestra = calcular_llave_maestra(Gx,x,P);
            String str_llave = llave_maestra.toString();
            System.out.println("Client found llave maestra: " + str_llave);

            SecretKey sk_srv = f.csk1(str_llave);
            SecretKey sk_mac = f.csk2(str_llave);
            byte[] iv1 = generateIvBytes();

            /* 8. We send C(K_AB1, <consulta>) and HMAC(K_AB2, <consulta>)*/
            String consulta = "10";
            String str_iv1 = byte2str(iv1);
            IvParameterSpec ivSpec1 = new IvParameterSpec(iv1);
            byte[] byteC = f.senc(consulta.getBytes(), sk_srv, ivSpec1, "client: ");
            String c = byte2str(byteC);
            byte[] byteHMAC = f.hmac(consulta.getBytes(), sk_mac);
            String hmac = byte2str(byteHMAC);

            pOut.println(c);
            System.out.println("Client C: " + c);
            pOut.println(hmac);
            System.out.println("Client HMAC: " + hmac);
            pOut.println(str_iv1);
            System.out.println("Client iv1: " + str_iv1);

            /* 10. We receive C(K_AB1, <rta>) and HMAC(K_AB2, <rta>)*/
            String verified;
            if ((verified = pIn.readLine()) != null) System.out.println("server check: " + verified);
            assert verified != null;
            if (verified.equals("OK")){
                String stringCiphered;
                String stringHMAC;
                String StringIv2;

                if ((stringCiphered = pIn.readLine()) != null) System.out.println("C(K_AB1, <ans>): " + stringG);
                if ((stringHMAC = pIn.readLine()) != null) System.out.println("HMAC(K_AB2, <rta>): " + stringP);
                if ((StringIv2 = pIn.readLine()) != null) System.out.println("iv2: " + stringGx);

                assert StringIv2 != null;
                byte[] iv2 = str2byte(StringIv2);
                IvParameterSpec ivSpec2 = new IvParameterSpec(iv2);

                assert stringCiphered != null;
                byte[] descifrado = f.sdec(str2byte(stringCiphered), sk_srv, ivSpec2);
                assert stringHMAC != null;
                boolean verificar = f.checkInt(descifrado, sk_mac, str2byte(stringHMAC));
                System.out.println("Client Integrity check:" + verificar);

                if(verificar){
                    pOut.println("OK");
                }
                else {
                    pOut.println("ERROR");
                }
            }
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

    public String byte2str( byte[] b )
    {
        // Encapsulamiento con hexadecimales
        String ret = "";
        for (int i = 0 ; i < b.length ; i++) {
            String g = Integer.toHexString(((char)b[i])&0x00ff);
            ret += (g.length()==1?"0":"") + g;
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
