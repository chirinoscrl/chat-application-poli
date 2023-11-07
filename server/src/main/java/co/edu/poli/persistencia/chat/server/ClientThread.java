package co.edu.poli.persistencia.chat.server;

import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

class ClientThread extends Thread {

    private static final Logger logger = Logger.getLogger(ClientThread.class);

    // La conexión del cliente
    private final Socket clientSocket;

    // La lista de clientes activos con su respectiva capa de escritura
    // PrintWriter se utiliza para enviar mensajes a los clientes conectados al servidor en una aplicación de chat de múltiples hilos.
    private final ConcurrentHashMap<String, PrintWriter> activeClientsWriters;

    private String clientNickname;

    /**
     * Inicializa una nueva instancia de la clase ClientThread con el socket de cliente especificado y los escritores de clientes activos.
     *
     * @param clientSocket El socket del cliente utilizado para comunicarse con el cliente.
     * @param activeClientsWriters El mapa de los escritores de clientes activos, donde la clave es el identificador único del cliente y el valor es el escritor utilizado para enviar mensajes al cliente.
     */
    public ClientThread(Socket clientSocket, ConcurrentHashMap<String, PrintWriter> activeClientsWriters) {
        this.clientSocket = clientSocket;
        this.activeClientsWriters = activeClientsWriters;
    }

    @Override
    public void run() {
        try {
            processClient();
        } catch (IOException e) {
            logger.info("An error occurred: " + e.getMessage());
        } finally {
            // Cierra el cliente y notifica a los demás clientes
            if (clientNickname != null) {
                activeClientsWriters.remove(clientNickname);
            }
            try {
                clientSocket.close();
            } catch (IOException ignored) {
            }

            sendActiveUsersToAllClients();
        }
    }

    /**
     * Procesa la comunicación con el cliente.
     *
     * @throws IOException Si ocurre un error de entrada/salida durante la comunicación con el cliente.
     */
    private void processClient() throws IOException {
        // Creando las capas de escritura y lectura para el cliente
        try (BufferedReader clientInputReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter clientOutputWriter = new PrintWriter(clientSocket.getOutputStream(), true)) {

            // Inicializando el nickname del cliente
            clientNickname = clientInputReader.readLine();

            // Esta parte se encarga de verificar si el nickname ya está en uso
            if (isNicknameInUse(clientOutputWriter)) return;

            // Inicio del ciclo que maneja los mensajes del cliente
            handleClientMessage(clientInputReader, clientOutputWriter);
        }
    }

    /**
     * Verifica si un nickname está siendo utilizado por otro cliente.
     *
     * @param clientOutputWriter El escritor para enviar mensajes al cliente.
     * @return True si el nickname ya está en uso, False en caso contrario.
     */
    private boolean isNicknameInUse(PrintWriter clientOutputWriter) {
        synchronized (activeClientsWriters) {
            if (activeClientsWriters.containsKey(clientNickname)) {
                clientOutputWriter.println("Nickname already in use. Disconnecting...");
                return true;
            } else {
                activeClientsWriters.put(clientNickname, clientOutputWriter);
                sendActiveUsersToAllClients();
            }
        }
        return false;
    }

    /**
     * Maneja los mensajes recibidos de un cliente.
     *
     * @param clientInputReader El lector de entrada para leer los mensajes del cliente.
     * @param clientOutputWriter El escritor de salida para enviar mensajes al cliente.
     * @throws IOException Si ocurre un error al leer o escribir en el flujo de entrada/salida.
     */
    private void handleClientMessage(BufferedReader clientInputReader, PrintWriter clientOutputWriter) throws IOException {
        String clientMessage;
        while ((clientMessage = clientInputReader.readLine()) != null) {
            logger.info("Client [" + clientNickname + "]: " + clientMessage);
            if (clientMessage.equalsIgnoreCase("chao")) {
                break; // salir del bucle y terminar la conexión
            } else if (clientMessage.startsWith("@")) {
                if (clientMessage.split(":")[1].trim().equalsIgnoreCase("chao")) {
                    break;
                } else {
                    sendPrivateMessage(clientNickname, clientMessage, clientOutputWriter);
                }
            }
        }
    }

    /**
     * Envia la lista de usuarios activos a todos los clientes conectados.
     */
    private void sendActiveUsersToAllClients() {
        // Crear un StringBuilder para construir la cadena que contendrá la lista de usuarios activos
        StringBuilder activeUsersList = new StringBuilder("Active Users: ");

        // Iterar sobre el conjunto de nombres de usuarios en activeClientsWriters
        for (String activeUserNickname : activeClientsWriters.keySet()) {
            // Añadir cada nombre de usuario y una coma a la cadena activeUsersList
            activeUsersList.append(activeUserNickname).append(", ");
        }

        // Si activeClientsWriters no está vacío, entonces habríamos añadido algunas comas adicionales al final de activeUsersList
        // Por lo tanto, eliminamos los dos últimos caracteres de activeUsersList
        if (!activeClientsWriters.isEmpty()) {
            activeUsersList.delete(activeUsersList.length() - 2, activeUsersList.length());
        }

        // Iterar sobre todos los PrintWriter en los valores de activeClientsWriters
        for (PrintWriter clientWriter : activeClientsWriters.values()) {
            // Para cada PrintWriter (que probablemente se utilice para enviar mensajes a un cliente), llamamos a su método println
            // para enviar la cadena activeUsersList a ese cliente
            clientWriter.println(activeUsersList);
        }
    }

    /**
     * Envia un mensaje privado desde el remitente al destinatario especificado.
     *
     * @param senderNickname   el apodo del remitente
     * @param message          el mensaje a enviar (en el formato ":destinatario:mensaje")
     * @param senderWriter     el PrintWriter del remitente utilizado para enviar el mensaje al remitente
     */
    private void sendPrivateMessage(String senderNickname, String message, PrintWriter senderWriter) {
        String[] messageParts = message.split(":", 2);
        String recipientNickname = messageParts[0].substring(1);

        // La verificación de que el usuario al que se desea enviar el mensaje existe
        if (messageParts.length > 1 && activeClientsWriters.containsKey(recipientNickname)) {
            String privateMessage = "[" + senderNickname + "(Private)]: " + messageParts[1];
            activeClientsWriters.get(recipientNickname).println(privateMessage);
            senderWriter.println(privateMessage);
        }
    }
}