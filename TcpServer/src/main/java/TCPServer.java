public class TCPServer {
    public static void main(String[] args) {
        System.setProperty("java.util.logging.SimpleFormatter.format", "%5$s %n");

        SingleThreadedServer single = new SingleThreadedServer(2424);
        single.serveClients();
    }
}
