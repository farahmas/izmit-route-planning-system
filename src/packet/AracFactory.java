package packet;

public class AracFactory {
    public static Arac getArac(String tip) {
        return switch (tip.toLowerCase()) {
            case "bus", "otobüs" -> new Otobus();
            case "tram", "tramvay" -> new Tramvay();
            case "taksi" -> new Taksi();
            default -> throw new IllegalArgumentException("Geçersiz araç tipi: " + tip);
        };
    }
}
