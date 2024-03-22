public enum PortasEIPs {

    // Portas
    PORTA_GATEWAY(new DescritorIPPorta("127.0.0.1", 1000)),
    PORTA_QUEUE_MANAGER(new DescritorIPPorta("127.0.0.1", 3300)),
    PORTA_DOWNLOAD_MANAGER(new DescritorIPPorta("127.0.0.1", 3570));

    private final DescritorIPPorta descritor;

    PortasEIPs(DescritorIPPorta descritor) {
        this.descritor = descritor;
    }

    public String getIP() {
        return descritor.getIP();
    }

    public int getPorta() {
        return descritor.getPorta();
    }

    public void printINIT(String name) {
        System.out.println("[" + this + "] " + name + " ready.");
    }

    @Override
    public String toString() {
        return descritor.toString();
    }
}
