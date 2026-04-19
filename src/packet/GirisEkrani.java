package packet;

import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.view.mxGraph;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.embed.swing.SwingNode;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.util.*;

public class GirisEkrani extends Application {

    private Pane grafPanel;
    private UlasimAgi ag;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("İzmit Rota Planlama Sistemi");

        BorderPane root = new BorderPane();
        GridPane inputGrid = new GridPane();
        inputGrid.setPadding(new Insets(20));
        inputGrid.setVgap(10);
        inputGrid.setHgap(10);
        inputGrid.setAlignment(Pos.CENTER);

        Label titleLabel = new Label("İzmit Rota Planlama Sistemi");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: darkblue;");

        Label baslangicLabel = new Label("Başlangıç Konumu Enlem:");
        TextField baslangicField = new TextField();
        
        Label lonLabel = new Label("Başlangıç Konumu Boylam:");
        TextField lonField = new TextField();

        Label varisLabel = new Label("Hedef Durak:");
        ComboBox<String> varisBox = new ComboBox<>();

        Label yolcuLabel = new Label("Yolcu Tipi:");
        ComboBox<String> yolcuBox = new ComboBox<>();
        yolcuBox.getItems().addAll("Genel", "Öğrenci", "Öğretmen", "Yaşlı");
        

        Label odemeLabel = new Label("Ödeme Yöntemi:");
        ComboBox<String> odemeBox = new ComboBox<>();
        odemeBox.getItems().addAll("Nakit", "Kredi Kartı", "KentKart");
        

        Label filtreLabel = new Label("Rota Filtresi:");
        ComboBox<String> filtreBox = new ComboBox<>();
        filtreBox.getItems().addAll("En Kısa", "En Hızlı", "Minimum Aktarma");
        

        Label aracLabel = new Label("Taşıt Türü:");
        ComboBox<String> aracBox = new ComboBox<>();
        aracBox.getItems().addAll("Hepsi", "Sadece Otobüs", "Sadece Tramvay", "Otobüs + Tramvay", "Taksi + Toplu Taşıma");
        

        Button hesaplaButton = new Button("Rota Hesapla");
        hesaplaButton.setStyle("-fx-background-color: darkblue; -fx-text-fill: white;");

        inputGrid.add(titleLabel, 0, 0, 2, 1);
        inputGrid.add(baslangicLabel, 0, 1);
        inputGrid.add(baslangicField, 1, 1);
        inputGrid.add(lonLabel, 0, 2);
        inputGrid.add(lonField, 1, 2);
        inputGrid.add(varisLabel, 0, 3);
        inputGrid.add(varisBox, 1, 3);
        inputGrid.add(yolcuLabel, 0, 4);
        inputGrid.add(yolcuBox, 1, 4);
        inputGrid.add(odemeLabel, 0, 5);
        inputGrid.add(odemeBox, 1, 5);
        inputGrid.add(filtreLabel, 0, 6);
        inputGrid.add(filtreBox, 1, 6);
        inputGrid.add(aracLabel, 0, 7);
        inputGrid.add(aracBox, 1, 7);
        inputGrid.add(hesaplaButton, 1, 8);

        TabPane tabPane = new TabPane();
        Tab haritaTab = new Tab("Harita");
        Tab grafTab = new Tab("Graf");
        haritaTab.setClosable(false);
        grafTab.setClosable(false);

        WebView webView = new WebView();
        WebEngine webEngine = webView.getEngine();
        webEngine.setJavaScriptEnabled(true);
        haritaTab.setContent(webView);

        grafPanel = new Pane();
        grafPanel.setPrefSize(1800, 600);
        grafTab.setContent(new ScrollPane(grafPanel));

        tabPane.getTabs().addAll(haritaTab, grafTab);
        root.setLeft(inputGrid);
        root.setCenter(tabPane);

        ag = new UlasimAgi("resources/veriseti.json");
        for (String id : ag.getDuraklar().keySet()) {
            Durak d = ag.getDurakById(id);
            varisBox.getItems().add(id + " - " + d.getName() + " (" + d.getType() + ")");
        }

        hesaplaButton.setOnAction(e -> {
            try {
                double lat = Double.parseDouble(baslangicField.getText());
                double lon = Double.parseDouble(lonField.getText());
                String hedef = varisBox.getValue().split(" - ")[0];
                String yolcuTipi = yolcuBox.getValue();
                String odemeTipi = odemeBox.getValue();
                String filtre = filtreBox.getValue();
                String aracTuru = aracBox.getValue();

                String baslangic = ag.enYakinDurak(lat, lon);
                if (baslangic == null) {
                    throw new Exception("En yakın durak bulunamadı!");
                }

                Yolcu yolcu = switch (yolcuTipi) {
                    case "Öğrenci" -> new Ogrenci();
                    case "Öğretmen" -> new Ogretmen();
                    case "Yaşlı" -> new Yasli();
                    default -> new GenelYolcu();
                };

                Odeme odeme = switch (odemeTipi) {
                    case "Kredi Kartı" -> new KrediKarti();
                    case "KentKart" -> new KentKart();
                    default -> new Nakit();
                };

                UlasimAgi.RotaSonucu sonuc = ag.rotaHesapla(baslangic, hedef, yolcu, filtre, aracTuru, lat, lon);

                if (sonuc.rota.size() == 1 && sonuc.rota.get(0).startsWith("Rota bulunamadı")) {
                    throw new Exception("Belirtilen duraklar arasında rota bulunamadı!");
                }

                Durak hedefDurak = ag.getDurakById(hedef);
                String url = "https://www.google.com/maps/dir/" + lat + "," + lon + "/" +
                        hedefDurak.getLat() + "," + hedefDurak.getLon();
                Timeline delay = new Timeline(new KeyFrame(Duration.seconds(1.5), ev -> {
                    try {
                        if (webEngine.getLoadWorker().isRunning()) {
                            webEngine.getLoadWorker().cancel();
                        }
                        webEngine.load(url);
                    } catch (Exception ignored) {
                        System.out.println("❗ Harita yüklenemedi.");
                    }
                }));
                delay.setCycleCount(1);
                delay.play();

                ag.tumDuraklariVeRotaCiz(grafPanel, sonuc.rota, sonuc.aracTurleri);

                Dialog<Void> dialog = new Dialog<>();
                dialog.setTitle("Rota Bilgisi");
                dialog.setHeaderText("📍 Rota, Süre, Ücret ve Ödeme Detayları");

                StringBuilder sb = new StringBuilder();

                Durak baslangicDurak = ag.getDurakById(baslangic);
                double baslangicMesafe = baslangicDurak.mesafeHesapla(lat, lon);
                sb.append(String.format("🚶 Başlangıç Noktası → %s (%.2f km)\n\n", baslangicDurak.getName(), baslangicMesafe));

                if (baslangicMesafe > 3.0 || aracTuru.equals("Taksi + Toplu Taşıma")) {
                    Taksi taksi = new Taksi();
                    double taksiUcreti = taksi.ucretHesapla(baslangicMesafe, yolcu.getTip());
                    sb.append(String.format("🚕 Taksi Kullanımı Gerekli!\n"));
                    sb.append(String.format("   Mesafe: %.2f km\n", baslangicMesafe));
                    sb.append(String.format("   Ücret: %.2f TL\n\n", taksiUcreti));
                }

                sb.append("📍 Rota: ").append(String.join(" → ", sonuc.rota)).append("\n\n");
                sb.append("⏱ Toplam Süre: ").append(sonuc.toplamSure).append(" dakika\n");
                sb.append("💰 Toplam Ücret: ").append(String.format("%.2f TL", sonuc.toplamUcret)).append("\n");
                sb.append("🧾 Ödeme: ").append(odeme.odemeYap(sonuc.toplamUcret)).append("\n\n");

                if (!sonuc.aracTurleri.isEmpty()) {
                    sb.append("🚗 Kullanılan Taşıtlar: ");
                    for (String arac : sonuc.aracTurleri) {
                        String ad = switch (arac.toLowerCase()) {
                            case "taksi" -> "🚕 Taksi";
                            case "bus", "otobüs" -> "🚌 Otobüs";
                            case "tram", "tramvay" -> "🚋 Tramvay";
                            case "transfer" -> "🔁 Aktarma";
                            default -> "➡️ Diğer";
                        };
                        sb.append(ad).append(" → ");
                    }
                    sb.setLength(sb.length() - 3);
                    sb.append("\n\n");
                }

                sb.append("📋 Adım Adım Detaylar:\n");
                for (int i = 0; i < sonuc.adimlar.size(); i++) {
                    RotaAdimi adim = sonuc.adimlar.get(i);
                    Durak from = ag.getDurakById(adim.from);
                    Durak to = ag.getDurakById(adim.to);

                    String simge = switch (adim.aracTuru.toLowerCase()) {
                        case "taksi" -> "🚕";
                        case "bus", "otobüs" -> "🚌";
                        case "tram", "tramvay" -> "🚋";
                        case "transfer" -> "🔁";
                        default -> "➡️";
                    };

                    sb.append(String.format("%s %s → %s | 🕒 %d dk | 📏 %.1f km | 💸 %.2f TL\n",
                            simge,
                            from != null ? from.getName() : adim.from,
                            to != null ? to.getName() : adim.to,
                            adim.sure,
                            adim.mesafe,
                            adim.ucret));
                }


                TextArea textArea = new TextArea(sb.toString());
                textArea.setWrapText(true);
                textArea.setEditable(false);
                textArea.setPrefSize(600, 400);

                ScrollPane scrollPane = new ScrollPane(textArea);
                scrollPane.setFitToWidth(true);
                scrollPane.setPrefSize(620, 420);

                dialog.getDialogPane().setContent(scrollPane);
                dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
                dialog.showAndWait();

            } catch (Exception ex) {
                ex.printStackTrace();
                Alert hata = new Alert(Alert.AlertType.ERROR);
                hata.setTitle("Hata");
                hata.setHeaderText("İşlem Hatası");
                hata.setContentText("Bir hata oluştu: " + ex.getMessage());
                hata.show();
            }
        });

        Scene scene = new Scene(root, 1400, 700);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
