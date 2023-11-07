package co.edu.poli.persistencia.chat.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Representa una aplicación cliente de chat.
 *
 * Esta clase extiende la clase Application de JavaFX y es responsable de lanzar y ejecutar
 * la aplicación cliente de chat.
 *
 * La aplicación comienza cargando un archivo FXML ("init-view.fxml") que contiene la interfaz de usuario inicial
 * para el cliente de chat. La interfaz de usuario se muestra en un escenario y se establece el título del escenario en "Chat!".
 *
 * Ejemplo de uso:
 *
 *     ChatClientApplication clientApplication = new ChatClientApplication();
 *     clientApplication.launch();
 */
public class ChatClientApplication extends Application {

    /**
     * Sobrescribe el método start de la clase Application.
     * Se encarga de cargar la interfaz de usuario inicial y mostrarla en un escenario.
     *
     * @param stage la ventana principal en la que se mostrará el contenido de la UI
     * @throws IOException si no se puede cargar el archivo FXML
     */
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(ChatClientApplication.class.getResource("init-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("Chat!");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Método principal que inicia la aplicación.
     *
     * @param args argumentos de línea de comandos
     */
    public static void main(String[] args) {
        launch();
    }
}