package co.edu.poli.persistencia.chat.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.function.Consumer;

/**
 * La clase ChatClient es responsable de establecer una conexión con un servidor de chat y manejar los mensajes entrantes y salientes.
 *
 * @author Autor
 * @version 1.0
 * @since 2023.11.06
 */
public class ChatClient {

    private final Socket socket;
    private final BufferedReader buffReader;
    private final PrintWriter buffWriter;

    private final Consumer<String> activeUserHandler;

    private final Consumer<String> messageHandler;

    /**
     * Inicializa una nueva instancia de la clase ChatClient.
     *
     * @param host              El nombre del host o la dirección IP del servidor al que se va a conectar.
     * @param port              El número de puerto del servidor.
     * @param nickname          El apodo que se utilizará para el usuario actual.
     * @param activeUserHandler Una función Consumer que manejará las actualizaciones de usuarios activos en el chat.
     * @param messageHandler    Una función Consumer que manejará los mensajes entrantes del chat.
     * @throws IOException      Si hay un error de E/S al conectarse con el servidor.
     */
    public ChatClient(String host,
                      int port,
                      String nickname,
                      Consumer<String> activeUserHandler,
                      Consumer<String> messageHandler
    ) throws IOException {
        socket = new Socket(host, port);
        buffWriter = new PrintWriter(socket.getOutputStream(), true);
        buffReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        buffWriter.println(nickname);
        this.activeUserHandler = activeUserHandler;
        this.messageHandler = messageHandler;
        System.out.println("Connected to server.");
    }

    /**
     * Devuelve si el socket está actualmente conectado.
     *
     * @return verdadero si el socket está conectado, falso en caso contrario.
     */
    public boolean isConnected() {
        return socket.isConnected();
    }

    /**
     * Enviar un mensaje utilizando la conexión establecida.
     *
     * @param msg el mensaje a enviar
     * @throws IOException si ocurre un error en el envío del mensaje.
     */
    public void sendMessage(String msg) throws IOException {
        buffWriter.println(msg);
        System.out.println("Sent message: " + msg);
    }

    /**
     * Lee los mensajes de texto enviados por el servidor y realiza diferentes acciones según el contenido del mensaje.
     *
     * @throws IOException si ocurre un error durante la lectura de los mensajes.
     */
    public void readMessages() throws IOException {
        String serverMsg;
        while ((serverMsg = buffReader.readLine()) != null) {
            System.out.println(serverMsg);
            if (serverMsg.startsWith("Active Users: ")) {
                activeUserHandler.accept(serverMsg.substring(14));
            } else {
                messageHandler.accept(serverMsg); // Procesa el mensaje
            }
        }
    }
}