module com.example.hesap_makiinasi {
    requires javafx.controls;
    requires javafx.fxml;
    requires exp4j;

    opens com.example.hesap_makiinasi to javafx.fxml;
    exports com.example.hesap_makiinasi;
}
