package packet;

public class Taksi extends Arac {
    private final double acilisUcreti = 10.0;

    public Taksi() {
        super("Taksi", 4.0); 
    }

    @Override
    public double ucretHesapla(double mesafe, String yolcuTipi) {
        
        double toplamUcret = acilisUcreti + (mesafe * birimUcret);
        return Math.round(toplamUcret * 100.0) / 100.0;
    }
}
