package co.edu.poli.persistencia.chat.client;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Controlador inicial para el cliente del chat.
 *
 * @author Autor
 * @version 1.0
 * @since 2023.11.06
 */
public class ChatClientInitController {

    private static final Logger logger = LogManager.getLogger(ChatClientInitController.class);

    // Elementos FXML:
    @FXML
    private TextField ipField;

    @FXML
    private TextField portField;

    @FXML
    private TextField nicknameField;

    @FXML
    private TextField messageInput;

    @FXML
    private Button connectButton;

    @FXML
    private Label statusLabel;

    @FXML
    private TextArea chatArea;

    // Cliente del chat:
    ChatClient client;

    // Lista Observable para mantener a los usuarios activos:
    @FXML
    private ComboBox<String> activeUsersList;

    // Apodo del usuario:
    String nickname;

    // ObservableList to hold the active users
    ObservableList<String> activeUsers;

    /**
     * Inicializa la lista activeUsers y la establece como los elementos para la ListView activeUsersList.
     * Este método normalmente es llamado después de que el archivo FXML ha sido cargado y el controlador ha sido creado.
     */
    @FXML
    protected void initialize() {
        activeUsers = FXCollections.observableArrayList();
        activeUsersList.setItems(activeUsers);
    }

    /**
     * Manejador de eventos para el botón conectar.
     * Este método establece una conexión con el servidor de chat utilizando el apodo provisto.
     * Una vez que se realiza una conexión exitosa, actualiza los elementos de la interface del usuario en consecuencia,
     * y comienza un nuevo hilo para continuar leyendo los mensajes entrantes del servidor.
     * Si la conexión falla, habilita el botón conectar nuevamente y muestra el mensaje de error en la etiqueta de estado.
     *
     * @throws IOException Si hay un error durante el proceso de conexión.
     */
    @FXML
    protected void onConnectButtonClick() throws IOException {
        try {
            nickname = nicknameField.getText();
            String ip = ipField.getText();
            int port = Integer.parseInt(portField.getText());
            client = new ChatClient(ip, port, nickname,
                    users -> Platform.runLater(() -> updateActiveUsers(users)),
                    msg -> Platform.runLater(() -> chatArea.appendText(msg + "\n")));

            // Deshabilita el botón de conexión y cambia la etiqueta de estado después de la conexión
            connectButton.setDisable(true);
            statusLabel.setText("Usuario conectado");
            chatArea.appendText(String.format("Te acabas de conectar...\n"));

            new Thread(() -> {
                try {
                    while (true) {
                        client.readMessages();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        } catch (IOException e) {
            // Revisa el error, habilita el botón de conexión y muestra el error en la etiqueta de estado si la conexión falla
            connectButton.setDisable(false);
            statusLabel.setText("Falló la conexión: " + e.getMessage());
        }
    }

    /**
     * Manejador de eventos para el botón enviar.
     * Este método envía un mensaje al servidor de chat utilizando el nombre de usuario seleccionado y el mensaje ingresado.
     * Si el nombre de usuario y mensaje no son null ni están vacíos, el método añadirá el nombre de usuario al mensaje.
     * Si el cliente está conectado, el método enviará el mensaje al servidor a través del cliente.
     * Finalmente, el método limpiará el campo de entrada de mensajes.
     *
     * @param actionEvent El evento que ha disparado el método.
     * @throws IOException Si ocurre un error durante el proceso de envío del mensaje.
     */
    public void onSendButtonClick(ActionEvent actionEvent) throws IOException {
        String targetNickname = activeUsersList.getValue(); // Obtén el nombre de usuario seleccionado
        String message = messageInput.getText();

        if (targetNickname != null && !targetNickname.isEmpty() && !message.isEmpty()) {
            message = "@" + targetNickname + ": " + message;
        }

        if(client != null && client.isConnected()){
            client.sendMessage(message);
        }

        messageInput.clear();
    }

    /**
     * Actualiza la lista de usuarios activos.
     *
     * @param userList Una cadena que representa la lista de usuarios activos separados por comas.
     */
    public void updateActiveUsers(String userList) {
        Platform.runLater(() -> {
            // Guarda el usuario seleccionado
            String previousUser = activeUsersList.getValue();

            activeUsers.clear();
            String[] users = userList.split(", ");

            // Creando un ArrayList a partir del array users. Esto permitirá
            // utilizar el método remove() del ArrayList
            ArrayList<String> activeUsersList = new ArrayList<>(Arrays.asList(users));

            // Removiendo el nickname del ArrayList
            activeUsersList.remove(nickname);

            // Agregar de vuelta los usuarios a activeUsers
            activeUsers.addAll(activeUsersList);

            // Coloca de nuevo el usuario anteriormente seleccionado
            if(activeUsers.contains(previousUser)) {
                this.activeUsersList.setValue(previousUser);
            }

            String activeUsersStr = String.join(", ", activeUsersList);
            chatArea.appendText(String.format("Usuarios conectados: %s\n", activeUsersStr));

            // Log Entry for Active Users Update
            logger.info(String.format("Active user list updated: %s", activeUsersStr));
        });
    }
}