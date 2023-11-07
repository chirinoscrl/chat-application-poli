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
    private final Consumer<String> messageHandler; // Nuevo consumer para procesar los mensajes

    /**
     * Inicializa una nueva instancia de la clase ChatClient.
     *
     * @param host              El nombre de anfitrión o dirección IP del servidor al que se va a conectar.
     * @param port              El número de puerto del servidor.
     * @param nickname          El apodo que se usará para el usuario actual.
     * @param activeUserHandler Una función de Consumer que manejará las actualizaciones de los usuarios activos en el chat.
     * @param messageHandler    Una función Consumer que manejará los mensajes entrantes del chat.
     * @throws IOException      Si se produce un error de IO mientras se conecta al servidor.
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
     * @return verdadero si el socket está conectado, falso de lo contrario.
     */
    public boolean isConnected() {
        return socket.isConnected();
    }

    /**
     * Envía un mensaje utilizando la conexión establecida.
     *
     * @param msg el mensaje a enviar
     * @throws IOException si ocurre un error de entrada/salida durante el envío del mensaje
     */
    public void sendMessage(String msg) throws IOException {
        buffWriter.println(msg);
        System.out.println("Sent message: " + msg);
    }

    /**
     * Lee mensajes de texto enviados por el servidor y realiza distintas acciones según el contenido del mensaje.
     *
     * @throws IOException si ocurre un error de entrada/salida durante la lectura de los mensajes
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