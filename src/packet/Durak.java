package packet;

import java.util.List;
import java.util.Map;

public class Durak {
    private String id;
    private String name;
    private String type;
    private double lat;
    private double lon;
    private List<Map<String, Object>> nextStops;
    private Map<String, Object> transfer;
    

    private MesafeHesaplamaStrategy mesafeHesaplayici = new HaversineMesafe();

    public Durak(String id, String name, String type, double lat, double lon,
                 List<Map<String, Object>> nextStops, Map<String, Object> transfer) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.lat = lat;
        this.lon = lon;
        this.nextStops = nextStops;
        this.transfer = transfer;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getType() { return type; }
    public double getLat() { return lat; }
    public double getLon() { return lon; }
    public List<Map<String, Object>> getNextStops() { return nextStops; }
    public Map<String, Object> getTransfer() { return transfer; }

    public boolean hasTransfer() {
        return transfer != null && transfer.containsKey("transferStopId");
    }

    public String getTransferStopId() {
        return hasTransfer() ? (String) transfer.get("transferStopId") : null;
    }

    public int getTransferSure() {
        return hasTransfer() ? ((Number) transfer.get("transferSure")).intValue() : 0;
    }

    public double getTransferUcret() {
        return hasTransfer() ? ((Number) transfer.get("transferUcret")).doubleValue() : 0.0;
    }

    public void setMesafeHesaplayici(MesafeHesaplamaStrategy strategy) {
        this.mesafeHesaplayici = strategy;
    }

    public double mesafeHesapla(double kullaniciLat, double kullaniciLon) {
        return mesafeHesaplayici.mesafeHesapla(kullaniciLat, kullaniciLon, this.lat, this.lon);
    }
}
