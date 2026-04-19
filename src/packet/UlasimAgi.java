package packet;

import java.io.*;
import java.util.*;

import com.google.gson.*;
import com.mxgraph.view.mxGraph;
import com.mxgraph.swing.mxGraphComponent;
import javafx.embed.swing.SwingNode;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Priority;
import javafx.geometry.Insets;

public class UlasimAgi {
    private Map<String, Durak> duraklar;
    private Map<String, List<String>> graf;

    public UlasimAgi(String veriDosyasi) {
        duraklar = new HashMap<>();
        graf = new HashMap<>();
        yukleVeri(veriDosyasi);
    }

    private void yukleVeri(String dosyaAdi) {
        try (InputStream inputStream = new FileInputStream(dosyaAdi)) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder jsonBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBuilder.append(line);
            }

            Gson gson = new Gson();
            JsonObject jsonObject = gson.fromJson(jsonBuilder.toString(), JsonObject.class);
            JsonArray duraklarArray = jsonObject.getAsJsonArray("duraklar");

            for (JsonElement element : duraklarArray) {
                JsonObject durakJson = element.getAsJsonObject();
                String id = durakJson.get("id").getAsString();
                String name = durakJson.get("name").getAsString();
                String type = durakJson.get("type").getAsString();
                double lat = durakJson.get("lat").getAsDouble();
                double lon = durakJson.get("lon").getAsDouble();
                List<Map<String, Object>> nextStops = new ArrayList<>();

                if (durakJson.has("nextStops")) {
                    JsonArray nextStopsArray = durakJson.getAsJsonArray("nextStops");
                    for (JsonElement stopElement : nextStopsArray) {
                        nextStops.add(gson.fromJson(stopElement, Map.class));
                    }
                }

                Map<String, Object> transfer = null;
                if (durakJson.has("transfer") && !durakJson.get("transfer").isJsonNull()) {
                    transfer = gson.fromJson(durakJson.get("transfer"), Map.class);
                }

                Durak durak = new Durak(id, name, type, lat, lon, nextStops, transfer);
                duraklar.put(id, durak);
                graf.put(id, new ArrayList<>());

                for (Map<String, Object> stop : nextStops) {
                    graf.get(id).add((String) stop.get("stopId"));
                }

                if (transfer != null && transfer.containsKey("transferStopId")) {
                    graf.get(id).add((String) transfer.get("transferStopId"));
                }
            }
        } catch (IOException e) {
            System.out.println("Hata: " + e.getMessage());
        }
    }

    public Map<String, Durak> getDuraklar() {
        return duraklar;
    }

    public Map<String, List<String>> getGraf() {
        return this.graf;
    }

    public List<String> getDurakIdVeIsimListesi() {
        List<String> liste = new ArrayList<>();
        for (Durak d : duraklar.values()) {
            liste.add(d.getId() + " - " + d.getName());
        }
        return liste;
    }

    public String enYakinDurak(double lat, double lon) {
        return duraklar.values().stream()
                .min(Comparator.comparingDouble(d -> d.mesafeHesapla(lat, lon)))
                .map(Durak::getId)
                .orElse(null);
    }

    public Durak getDurakById(String id) {
        return duraklar.get(id);
    }

    public Object getDuraklarListesi() {
        return duraklar.values().stream().toList();
    }

    public RotaSonucu rotaHesapla(String baslangic, String hedef, Yolcu yolcu, String filtre, String aracTuru, double baslangicLat, double baslangicLon) {
        Queue<List<String>> queue = new LinkedList<>();
        queue.offer(List.of(baslangic));

        List<String> enIyiYol = null;
        List<String> enIyiAraclar = null;
        double enDusukUcret = Double.MAX_VALUE;
        int enKisaSure = Integer.MAX_VALUE;
        int enAzAktarma = Integer.MAX_VALUE;
        List<RotaAdimi> enIyiAdimlar = null;

        RotaFiltreStrategy strategy = switch (filtre) {
            case "En Hızlı" -> new EnHizliFiltre();
            case "Minimum Aktarma" -> new MinimumAktarmaFiltre();
            default -> new EnKisaFiltre();
        };

        while (!queue.isEmpty()) {
            List<String> yol = queue.poll();
            String son = yol.get(yol.size() - 1);

            if (son.equals(hedef)) {
                double ucret = 0;
                int sure = 0;
                int aktarma = 0;
                String oncekiAracTuru = "";
                List<String> aracTurleri = new ArrayList<>();
                List<RotaAdimi> adimlar = new ArrayList<>();

                for (int i = 0; i < yol.size() - 1; i++) {
                    String from = yol.get(i);
                    String to = yol.get(i + 1);
                    Durak d1 = duraklar.get(from);
                    Durak d2 = duraklar.get(to);

                    boolean bulundu = false;
                    if (d1.getNextStops() != null) {
                        for (Map<String, Object> next : d1.getNextStops()) {
                            if (next.get("stopId").equals(to)) {
                                double mesafe = ((Number) next.get("mesafe")).doubleValue();
                                int rawSure = ((Number) next.get("sure")).intValue();
                                double rawUcret = ((Number) next.get("ucret")).doubleValue();
                                String arac = d1.getType();

                                if (!oncekiAracTuru.isEmpty() && !oncekiAracTuru.equals(arac)) {
                                    aktarma++;
                                }

                                double indirimliUcret = yolcu.indirimliUcret(rawUcret);
                                ucret += indirimliUcret;
                                sure += rawSure;
                                oncekiAracTuru = arac;
                                aracTurleri.add(arac);
                                adimlar.add(new RotaAdimi(from, to, mesafe, rawSure, indirimliUcret, arac));
                                bulundu = true;
                                break;
                            }
                        }
                    }

                    if (!bulundu && d1.hasTransfer() && d1.getTransferStopId().equals(to)) {
                        int transferSure = d1.getTransferSure();
                        double transferUcret = d1.getTransferUcret();
                        ucret += transferUcret;
                        sure += transferSure;
                        aktarma++;
                        aracTurleri.add("transfer");
                        adimlar.add(new RotaAdimi(from, to, 0.0, transferSure, transferUcret, "transfer"));
                        oncekiAracTuru = "transfer";
                    }
                }

                Durak ilkDurak = duraklar.get(yol.get(0));
                double mesafeIlkDuraga = ilkDurak.mesafeHesapla(baslangicLat, baslangicLon);

                boolean taksiGerekli = false;

                if (aracTuru.equals("Taksi + Toplu Taşıma")) {
                    taksiGerekli = true; 
                } else if (mesafeIlkDuraga > 3.0) {
                    taksiGerekli = true;
                }

                if (taksiGerekli) {
                    int taksiSure = (int) (mesafeIlkDuraga / 0.5);
                    double taksiUcret = yolcu.indirimliUcret(10 + mesafeIlkDuraga * 4.0);
                    sure += taksiSure;
                    ucret += taksiUcret;
                    aracTurleri.add(0, "taksi");
                    adimlar.add(0, new RotaAdimi("Başlangıç", yol.get(0), mesafeIlkDuraga, taksiSure, taksiUcret, "taksi"));
                }

                boolean guncelle = strategy.guncelle(enIyiYol, yol, enKisaSure, sure, enAzAktarma, aktarma);
                if (guncelle) {
                    enIyiYol = yol;
                    enIyiAraclar = aracTurleri;
                    enDusukUcret = ucret;
                    enKisaSure = sure;
                    enAzAktarma = aktarma;
                    enIyiAdimlar = adimlar;
                }
                continue;
            }

            for (String next : graf.getOrDefault(son, new ArrayList<>())) {
                if (!yol.contains(next)) {
                    if (!aracTuru.equals("Hepsi") && !uygunArac(duraklar.get(next).getType(), aracTuru)) {
                        continue;
                    }
                    List<String> yeniYol = new ArrayList<>(yol);
                    yeniYol.add(next);
                    queue.offer(yeniYol);
                }
            }
        }

        if (enIyiYol == null) {
            return new RotaSonucu(List.of("Ulaşım rotası bulunamadı."), 0, 0, new ArrayList<>(), new ArrayList<>());
        }

        return new RotaSonucu(enIyiYol, enDusukUcret, enKisaSure, enIyiAraclar, enIyiAdimlar);
    }

    private boolean uygunArac(String tip, String secim) {
        return switch (secim) {
            case "Sadece Otobüs" -> tip.equals("bus") || tip.equals("otobüs");
            case "Sadece Tramvay" -> tip.equals("tram") || tip.equals("tramvay");
            case "Otobüs + Tramvay", "Taksi + Toplu Taşıma" -> tip.equals("bus") || tip.equals("tram") || 
                                                               tip.equals("otobüs") || tip.equals("tramvay");
            default -> true;
        };
    }
    
    public static class RotaSonucu {
        public List<String> rota;
        public double toplamUcret;
        public int toplamSure;
        public List<String> aracTurleri;
        public List<RotaAdimi> adimlar;

        public RotaSonucu(List<String> rota, double toplamUcret, int toplamSure, List<String> aracTurleri, List<RotaAdimi> adimlar) {
            this.rota = rota;
            this.toplamUcret = toplamUcret;
            this.toplamSure = toplamSure;
            this.aracTurleri = aracTurleri;
            this.adimlar = adimlar;
        }
    }

    public void tumDuraklariVeRotaCiz(Pane panel, List<String> rota, List<String> aracTurleri) {
        panel.getChildren().clear();

        if (rota == null || rota.size() < 1) {
            panel.getChildren().add(new Label("❗ Görsel graf çizimi için yeterli durak yok."));
            return;
        }

        mxGraph graph = new mxGraph();
        Object parent = graph.getDefaultParent();
        Map<String, Object> vertexMap = new HashMap<>();

        graph.getModel().beginUpdate();
        try {
            Object kisiVertex = graph.insertVertex(parent, null, "👤 Başlangıç Noktası", 0, 0, 160, 50);
            vertexMap.put("kisi", kisiVertex);

            for (int i = 0; i < rota.size(); i++) {
                String id = rota.get(i);
                Durak d = getDurakById(id);
                if (d == null) continue;

                String arac = (i > 0 && aracTurleri.size() >= i) ? aracTurleri.get(i - 1).toLowerCase() : "";
                String simge = switch (arac) {
                    case "bus" -> "🚌";
                    case "tram" -> "🚋";
                    case "taksi" -> "🚕";
                    case "transfer" -> "🔁";
                    default -> "➡️";
                };

                String etiket = simge + " " + d.getName();
                if (i == rota.size() - 1) etiket = "🔴 " + d.getName();

                Object v = graph.insertVertex(parent, null, etiket, 0, 0, 160, 50);
                vertexMap.put(id, v);

                if (i == 0) {
                    String ilkArac = (aracTurleri.size() > 0) ? aracTurleri.get(0).toLowerCase() : "";
                    Object edge = graph.insertEdge(parent, null, "", kisiVertex, v);
                    if (ilkArac.equals("taksi")) {
                        graph.setCellStyle("strokeColor=orange", new Object[]{edge});
                    } else {
                        graph.setCellStyle("strokeColor=black", new Object[]{edge});
                    }
                }

                if (i >= 1) {
                    Object prev = vertexMap.get(rota.get(i - 1));
                    Object edge = graph.insertEdge(parent, null, "", prev, v);

                    int offset = (aracTurleri.size() == rota.size()) ? 0 : 1;
                    String prevArac = ((i - offset) >= 0 && (i - offset) < aracTurleri.size())
                                      ? aracTurleri.get(i - offset).toLowerCase()
                                      : "";

                    String renk = switch (prevArac) {
                        case "bus" -> "green";
                        case "tram" -> "blue";
                        case "transfer" -> "red;dashed=1";
                        case "taksi" -> "orange";
                        default -> "gray";
                    };

                    graph.setCellStyle("strokeColor=" + renk, new Object[]{edge});
                }
            }
        } finally {
            graph.getModel().endUpdate();
        }

        com.mxgraph.layout.mxCircleLayout layout = new com.mxgraph.layout.mxCircleLayout(graph);
        layout.setRadius(200);
        layout.execute(parent);

        mxGraphComponent graphComponent = new mxGraphComponent(graph);
        graphComponent.setConnectable(false);
        graphComponent.getGraph().setAllowDanglingEdges(false);
        graphComponent.setMinimumSize(new java.awt.Dimension(800, 600));
        graphComponent.setPreferredSize(new java.awt.Dimension(1000, 700));

        SwingNode swingNode = new SwingNode();
        javax.swing.SwingUtilities.invokeLater(() -> swingNode.setContent(graphComponent));

        VBox box = new VBox();
        box.setPadding(new Insets(10));
        box.setSpacing(10);
        box.getChildren().addAll(new Label("🔗 Görsel Rota Grafiği"), swingNode);
        VBox.setVgrow(swingNode, Priority.ALWAYS);

        panel.getChildren().add(box);
    }

}
