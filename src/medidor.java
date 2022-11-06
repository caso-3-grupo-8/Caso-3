import java.util.ArrayList;

public class medidor {

    private long tfirma;
    private long tmac;
    private long tcifrado;
    private long tgy;
    private int contador=0;
    private ArrayList<Long> listafirma= new ArrayList<Long>();
    private ArrayList<Long> listamac= new ArrayList<Long>();
    private ArrayList<Long> listacifrado= new ArrayList<Long>();
    private ArrayList<Long> listagy= new ArrayList<Long>();

    public medidor(long tfirma, long tmac,long tcifrado,long tgy){

        this.tcifrado=tcifrado;
        this.tfirma=tfirma;
        this.tgy=tgy;
        this.tmac=tmac;
    }

    public synchronized void ejecutar(long tf, long tm,long tc,long tg){
      tfirma +=tf;
      tmac+=tm;
      tcifrado+=tc;
      tgy+=tg;
      contador+=1;
      listafirma.add(tf);
      listamac.add(tm);
      listacifrado.add(tc);
      listagy.add(tg);
    
    }

    public long getTfirma() {
        return tfirma;
    }

    public void setTfirma(long tfirma) {
        this.tfirma = tfirma;
    }

    public long getTmac() {
        return tmac;
    }

    public void setTmac(long tmac) {
        this.tmac = tmac;
    }

    public long getTcifrado() {
        return tcifrado;
    }

    public void setTcifrado(long tcifrado) {
        this.tcifrado = tcifrado;
    }

    public long getTgy() {
        return tgy;
    }

    public void setTgy(long tgy) {
        this.tgy = tgy;
    }

    public int getContador() {
        return contador;
    }

    public void setContador(int contador) {
        this.contador = contador;
    }

    public ArrayList<Long> getListafirma() {
        return listafirma;
    }

    public void setListafirma(ArrayList<Long> listafirma) {
        this.listafirma = listafirma;
    }

    public ArrayList<Long> getListamac() {
        return listamac;
    }

    public void setListamac(ArrayList<Long> listamac) {
        this.listamac = listamac;
    }

    public ArrayList<Long> getListacifrado() {
        return listacifrado;
    }

    public void setListacifrado(ArrayList<Long> listacifrado) {
        this.listacifrado = listacifrado;
    }

    public ArrayList<Long> getListagy() {
        return listagy;
    }

    public void setListagy(ArrayList<Long> listagy) {
        this.listagy = listagy;
    }

    
    
    
}
