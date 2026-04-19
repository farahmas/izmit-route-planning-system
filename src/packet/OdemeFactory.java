package packet;

public class OdemeFactory {
    public static Odeme createOdeme(String tip) {
        return switch (tip.toLowerCase()) {
            case "nakit" -> new Nakit();
            case "kredi kartı", "kredi" -> new KrediKarti();
            case "kentkart" -> new KentKart();
            default -> throw new IllegalArgumentException("Geçersiz ödeme tipi: " + tip);
        };
    }
}
