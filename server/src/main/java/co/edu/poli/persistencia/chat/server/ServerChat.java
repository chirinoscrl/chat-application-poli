package co.edu.poli.persistencia.chat.server;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

public class ServerChat {

    private static final Logger logger = Logger.getLogger(ServerChat.class);

    private static final int PORT = 8888;

    private final ConcurrentHashMap<String, PrintWriter> clientWriters = new ConcurrentHashMap<>();

    private final ServerSocket serverSocket;

    /**
     * Constructor para la clase ServerChat.
     *
     * @param port El número de puerto en el que el servidor escuchará las conexiones entrantes.
     * @throws IOException Si se produce un error de entrada/salida al abrir el socket del servidor.
     */
    public ServerChat(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        logger.info("Server is running...");
    }

    /**
     * Este método espera conexiones entrantes de clientes y las atiende en hilos separados.
     *
     * @throws IOException si ocurre un error de I/O mientras se aceptan las conexiones del cliente.
     */
    public void serveClients() throws IOException {
        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();
                new ClientThread(clientSocket, clientWriters).start();
                logger.info("A client has successfully connected");
            } catch (IOException e) {
                logger.info("Client error: " + e.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        try {
            ServerChat server = new ServerChat(PORT);
            server.serveClients();
        } catch (IOException e) {
            logger.info("Server error: " + e.getMessage());
        }
    }
}