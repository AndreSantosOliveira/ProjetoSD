public enum PortasEIPs {

    // Portas
    PORTA_GATEWAY("127.0.0.1", 1000, "gateway"),
    PORTA_QUEUE_MANAGER("127.0.0.1", 3300, ""),
    PORTA_DOWNLOAD_MANAGER("127.0.0.1", 3570, "");

    private final String ip, rmiName;
    private final int portaFinal;

    PortasEIPs(String ip, int porta, String rmiName) {
        this.ip = ip;
        this.portaFinal = porta;
        this.rmiName = rmiName;
    }

    public String getIP() {
        return ip;
    }

    public int getPorta() {
        return portaFinal;
    }

    public String getRMIName() {
        return rmiName;
    }

    public void printINIT(String name) {
        System.out.println("[" + this + "] " + name + " ready.");
    }

    @Override
    public String toString() {
        return ip + ':' + portaFinal;
    }
}
