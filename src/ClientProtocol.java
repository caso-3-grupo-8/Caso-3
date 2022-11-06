import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.security.PublicKey;
import java.security.SecureRandom;

public class ClientProtocol {
    public SecurityFunctions f;
    public String id;
    public medidor medidor;

    public ClientProtocol(String id,medidor medidor) {
        this.f = new SecurityFunctions();
        this.id = id;
        this.medidor= medidor;
    }

    public void protocol(BufferedReader pIn, PrintWriter pOut) throws Exception {
        String stringG;
        String stringP;
        String stringGx;
        String firma;

        /* 1. We send a SECURE INIT message */
        pOut.println("SECURE INIT");

        if ((stringG = pIn.readLine()) != null) System.out.println("Client: " + id + " found G: " + stringG);
        if ((stringP = pIn.readLine()) != null) System.out.println("Client: " + id + " found P: " + stringP);
        if ((stringGx = pIn.readLine()) != null) System.out.println("Client: " + id + " found Gx: " + stringGx);
        if ((firma = pIn.readLine()) != null) System.out.println("Client: " + id + " found signature: " + firma);

        /* 3. We receive G, P and Gx parameters */
        assert stringG != null;
        BigInteger G = new BigInteger(stringG);
        assert stringP != null;
        BigInteger P = new BigInteger(stringP);
        assert stringGx != null;
        BigInteger Gx = new BigInteger(stringGx);

        /* 4. We verify ciphered message with public key */
        
        PublicKey publicaServidor = f.read_kplus("datos_asim_srv.pub","Client: ");
        String msj = G+","+P+","+Gx;    

        assert firma != null;
        long tiniciofirma = System.nanoTime();
        boolean auth = f.checkSignature(publicaServidor, str2byte(firma), msj);
        long tiempofirma = System.nanoTime()-tiniciofirma;
        System.out.print("Tiempo firma: "+tiempofirma+", para id: "+id);


        if(auth){
            /* 5. We send an OK message */
            System.out.println("Client " + id + " found signature successful, OK message sent.");
            pOut.println("OK");

            SecureRandom r = new SecureRandom();
            int integerX = Math.abs(r.nextInt());
            BigInteger x = BigInteger.valueOf(integerX);
            long tiniciogy = System.nanoTime();
            BigInteger gy = G2X(G, x, P);
            long tiempogy = System.nanoTime()-tiniciogy;
            System.out.print("Tiempo Gy: "+tiempogy+", para id: "+id);

            /* 6b. We send Gy */
            pOut.println(gy);

            /* 7a. We calculate master key, generate symmetric key and iv1*/
            BigInteger llave_maestra = calcular_llave_maestra(Gx,x,P);
            String str_llave = llave_maestra.toString();
            System.out.println("Client " + id + " found llave maestra: " + str_llave);

            SecretKey sk_srv = f.csk1(str_llave);
            long tiniciomac = System.nanoTime();
            SecretKey sk_mac = f.csk2(str_llave);
            long tiempomac = System.nanoTime()-tiniciomac;
            System.out.print("Tiempo mac: "+tiempomac+", para id: "+id);
            byte[] iv1 = generateIvBytes();
 
            /* 8. We send C(K_AB1, <consulta>) and HMAC(K_AB2, <consulta>)*/
            String consulta = id;
            String str_iv1 = byte2str(iv1);
            IvParameterSpec ivSpec1 = new IvParameterSpec(iv1);

            long tinicioc = System.nanoTime();
            byte[] byteC = f.senc(consulta.getBytes(), sk_srv, ivSpec1, "Client: "+ id);
            long tiempocifrado = System.nanoTime()-tinicioc;
            System.out.print("Tiempo cifrado: "+tiempocifrado+"para id: "+id);
            medidor.ejecutar(tiempofirma, tiempomac, tiempocifrado, tiempogy);

            String c = byte2str(byteC);
            byte[] byteHMAC = f.hmac(consulta.getBytes(), sk_mac);
            String hmac = byte2str(byteHMAC);

            pOut.println(c);
            System.out.println("Client " + id + " C: " + c);
            pOut.println(hmac);
            System.out.println("Client " + id + "HMAC: " + hmac);
            pOut.println(str_iv1);
            System.out.println("Client " + id + " iv1: " + str_iv1);

            /* 11. We receive C(K_AB1, <rta>) and HMAC(K_AB2, <rta>)*/
            String verified;
            if ((verified = pIn.readLine()) != null) System.out.println("Client " + id + " did server check: " + verified);
            assert verified != null;
            if (verified.equals("OK")){
                String stringCiphered;
                String stringHMAC;
                String StringIv2;

                /* 11. We receive C(K_AB1, <rta>) and HMAC(K_AB2, <rta>)*/
                if ((stringCiphered = pIn.readLine()) != null) System.out.println("Client " + id + " C(K_AB1, <ans>): " + stringG);
                if ((stringHMAC = pIn.readLine()) != null) System.out.println("Client " + id + " HMAC(K_AB2, <rta>): " + stringP);
                if ((StringIv2 = pIn.readLine()) != null) System.out.println("Client " + id + " iv2: " + stringGx);

                assert StringIv2 != null;
                byte[] iv2 = str2byte(StringIv2);
                IvParameterSpec ivSpec2 = new IvParameterSpec(iv2);

                /* 12. We verify C(K_AB1, <rta>) and HMAC(K_AB2, <rta>)*/
                assert stringCiphered != null;
                byte[] descifrado = f.sdec(str2byte(stringCiphered), sk_srv, ivSpec2);
                assert stringHMAC != null;
                boolean verificar = f.checkInt(descifrado, sk_mac, str2byte(stringHMAC));
                System.out.println("Client " + id + " Integrity check:" + verificar);

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
            System.out.println("Client " + id + " found signature unsuccessful, ERROR message sent.");
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
        StringBuilder ret = new StringBuilder();
        for (byte value : b) {
            String g = Integer.toHexString(((char) value) & 0x00ff);
            ret.append(g.length() == 1 ? "0" : "").append(g);
        }
        return ret.toString();
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
