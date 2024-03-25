public enum PortasEIPs {

    // Portas
    MULTICAST(new DescritorIPPorta("230.0.0.1", 6900)),
    GATEWAY(new DescritorIPPorta("127.0.0.1", 1000)),
    QUEUE_MANAGER(new DescritorIPPorta("127.0.0.1", 3300)),
    BARREL_MANAGER(new DescritorIPPorta("127.0.0.1", 4200)),
    DOWNLOAD_MANAGER(new DescritorIPPorta("127.0.0.1", 3570));

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
