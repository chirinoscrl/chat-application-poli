module co.edu.poli.persistencia.chat.client {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.logging;
    requires org.apache.logging.log4j;

    opens co.edu.poli.persistencia.chat.client to javafx.fxml;
    exports co.edu.poli.persistencia.chat.client;
}