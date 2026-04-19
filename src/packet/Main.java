package packet; 

import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {
    public static void main(String[] args) {
        System.setProperty("prism.forceGPU", "true"); 
        System.setProperty("javafx.embed.singleThread", "true"); 

        launch(args); 
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            GirisEkrani girisEkrani = new GirisEkrani();
            girisEkrani.start(primaryStage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
