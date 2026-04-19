package packet;

public class Ogrenci extends Yolcu {
    public Ogrenci() {
        this.tip = "Ogrenci";
    }

    @Override
    public double indirimliUcret(double ucret) {
        return Math.round(ucret * 0.5 * 100.0) / 100.0;
    }
}
