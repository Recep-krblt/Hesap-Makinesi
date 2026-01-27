package com.example.hesap_makiinasi;

import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.io.IOException;
import java.io.InputStream;

public class HelloApplication extends Application {
    @Override
    public void start(Stage mainStage) throws IOException {
        // --- 1. ANA UYGULAMA YÜKLEMESİ (Arka Planda Hazırlanır) ---
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));
        Scene mainScene = new Scene(fxmlLoader.load(), 420, 600);

        // Ana sahneye ikon ekleme
        InputStream iconStream = HelloApplication.class.getResourceAsStream("icon.png");
        Image appIcon = null;
        if (iconStream != null) {
            appIcon = new Image(iconStream);
            mainStage.getIcons().add(appIcon);
        }

        mainStage.setTitle("Hesap Makinesi");
        mainStage.setScene(mainScene);

        // --- 2. SPLASH SCREEN (AÇILIŞ EKRANI) OLUŞTURMA ---
        Stage splashStage = new Stage();

        // İkonu Görsel Nesnesine Çevir (Splash Screen için)
        ImageView splashIconView = new ImageView();
        if (appIcon != null) {
            splashIconView.setImage(appIcon);
            splashIconView.setFitWidth(150);  // İkon genişliği
            splashIconView.setFitHeight(150); // İkon yüksekliği
            splashIconView.setPreserveRatio(true);
        }

        // Alt Yazı Oluşturma
        Label titleLabel = new Label("HESAP MAKİNESİ");
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 24px; -fx-font-weight: bold;");

        // Düzen (VBox): İkon üstte, yazı altta
        VBox splashLayout = new VBox(20); // Elemanlar arası 20px boşluk
        splashLayout.getChildren().addAll(splashIconView, titleLabel);
        splashLayout.setAlignment(Pos.CENTER);
        splashLayout.setStyle("-fx-background-color: #2f3136; -fx-padding: 20; -fx-border-color" +
                ": #5865f2; -fx-border-width: 2;");

        // Sahne Ayarları
        Scene splashScene = new Scene(splashLayout, 300, 250);
        splashStage.setScene(splashScene);
        splashStage.initStyle(StageStyle.UNDECORATED); // Çerçevesiz pencere (X, -, [] yok)

        // Eğer ikon varsa görev çubuğunda da görünsün
        if (appIcon != null) {
            splashStage.getIcons().add(appIcon);
        }

        // --- 3. GÖSTERİM VE GEÇİŞ MANTIĞI ---

        // Önce Splash Screen'i göster
        splashStage.show();

        // 2 Saniye (Duration.seconds(2)) bekle, sonra ana ekranı aç
        PauseTransition delay = new PauseTransition(Duration.seconds(2));
        delay.setOnFinished(event -> {
            splashStage.close(); // Açılış ekranını kapat
            mainStage.show();    // Ana hesap makinesini aç
        });
        delay.play();
    }

    public static void main(String[] args) {
        launch();
    }
}