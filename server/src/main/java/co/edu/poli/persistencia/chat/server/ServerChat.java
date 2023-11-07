package co.edu.poli.persistencia.chat.server;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

/**
 * La clase ServerChat representa un servidor de chat que espera por conexiones de clientes.
 *
 * @author Autor
 * @version 1.0
 * @since 2023.11.06
 */
public class ServerChat {

    private static final Logger logger = Logger.getLogger(ServerChat.class);

    private static final int PORT = 8888;

    // Una lista de los PrintWriter para cada cliente conectado
    private final ConcurrentHashMap<String, PrintWriter> clientWriters = new ConcurrentHashMap<>();

    private final ServerSocket serverSocket;

    /**
     * Constructor de la clase ServerChat.
     *
     * @param port El número de puerto en el que el servidor escucha las conexiones entrantes.
     * @throws IOException Si ocurre un error de entrada/salida al abrir el socket del servidor
     */
    public ServerChat(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        logger.info("Server is running...");
    }

    /**
     * Este método espera por conexiones entrantes de clientes y las atiende en sus propios hilos.
     *
     * @throws IOException si ocurre un error de entrada/salida al aceptar las conexiones de clientes
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

    /**
     * Método principal que inicializa el servidor e inicia a atender a los clientes
     *
     * @param args cualquier argumento de línea de comando pasado al programa
     */
    public static void main(String[] args) {
        try {
            ServerChat server = new ServerChat(PORT);
            server.serveClients();
        } catch (IOException e) {
            logger.info("Server error: " + e.getMessage());
        }
    }
}