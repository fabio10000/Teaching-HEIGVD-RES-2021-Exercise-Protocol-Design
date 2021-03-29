
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SingleThreadedServer {
    final static Logger LOG = Logger.getLogger(SingleThreadedServer.class.getName());

    int port;

    /**
     * Constructor
     * @param port the port to listen on
     */
    public SingleThreadedServer(int port) {
        this.port = port;
    }

    private void sendResponse(PrintWriter out, String result, String status, String description) {
        out.println(result + " " + status + " " + description);
        out.flush();
    }

    private void sendBadRequest(PrintWriter out) {
        sendResponse(out, "NULL", "400", "Bad Request");
    }

    private void sendNotFound(PrintWriter out) {
        sendResponse(out, "NULL", "404", "Not Found");
    }

    private void sendUnprocessable(PrintWriter out) {
        sendResponse(out, "NULL", "422", "Unprocessable entity");
    }

    private void sendOk(PrintWriter out, double result) {
        sendResponse(out, String.valueOf(result), "200", "OK" );
    }

    private boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        try {
            double d = Double.parseDouble(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    /**
     * This method initiates the process. The server creates a socket and binds
     * it to the previously specified port. It then waits for clients in a infinite
     * loop. When a client arrives, the server will read its input line by line
     * and send back the data converted to uppercase. This will continue until
     * the client sends the "QUIT" command.
     */
    public void serveClients() {
        ServerSocket serverSocket;
        Socket clientSocket = null;
        BufferedReader in = null;
        PrintWriter out = null;

        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
            return;
        }

        while (true) {
            try {

                LOG.log(Level.INFO, "Waiting (blocking) for a new client on port {0}", port);
                clientSocket = serverSocket.accept();
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                out = new PrintWriter(clientSocket.getOutputStream());
                String line;
                boolean shouldRun = true;

                out.println("ADD 2;SUB 2;MUL 2;DIV 2;");
                out.flush();
                LOG.info("Reading until client sends QUIT or closes the connection...");

                while( (shouldRun) && (line = in.readLine()) != null) {
                    if (line.equalsIgnoreCase("quit")) {
                        shouldRun = false;
                        out.println("> " + line.toUpperCase());
                    } else {
                        out.println("> " + line.toUpperCase());
                        String[] args = line.toUpperCase().split(" ");
                        if (args.length == 3 && isNumeric(args[1]) && isNumeric(args[2])) {
                            double left = Double.parseDouble(args[1]);
                            double right = Double.parseDouble(args[2]);
                            switch (args[0]) {
                                case "ADD":
                                    sendOk(out, left + right);
                                    break;
                                case "SUB":
                                    sendOk(out, left - right);
                                    break;
                                case "MUL":
                                    sendOk(out, left * right);
                                    break;
                                case "DIV":
                                    if (right == 0) {
                                        sendUnprocessable(out);
                                    } else {
                                        sendOk(out, left / right);
                                    }
                                    break;
                                default:
                                    sendNotFound(out);
                                    break;
                            }
                        } else {
                            sendBadRequest(out);
                        }
                    }
                    out.flush();
                }

                LOG.info("Cleaning up resources...");
                clientSocket.close();
                in.close();
                out.close();

            } catch (IOException ex) {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException ex1) {
                        LOG.log(Level.SEVERE, ex1.getMessage(), ex1);
                    }
                }

                if (out != null) {
                    out.close();
                }

                if (clientSocket != null) {
                    try {
                        clientSocket.close();
                    } catch (IOException ex1) {
                        LOG.log(Level.SEVERE, ex1.getMessage(), ex1);
                    }
                }

                LOG.log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
    }
}
