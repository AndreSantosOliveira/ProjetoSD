public class DescritorIPPorta {

    private String ip, rmiName;
    private final int porta;

    public DescritorIPPorta(String ip, int porta) {
        this.ip = ip;
        this.porta = porta;
    }

    public DescritorIPPorta(String ip, int porta, String rmiName) {
        this.ip = ip;
        this.porta = porta;
        this.rmiName = rmiName;
    }

    public String getIP() {
        return ip;
    }

    public int getPorta() {
        return porta;
    }

    public String getRMIName() {
        return rmiName;
    }

    @Override
    public String toString() {
        return ip + ':' + porta;
    }

}
