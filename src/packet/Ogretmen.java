package packet;

public class Ogretmen extends Yolcu {
    public Ogretmen() {
        this.tip = "Ogretmen";
    }

    @Override
    public double indirimliUcret(double ucret) {
        return Math.round(ucret * 0.75 * 100.0) / 100.0;
    }
}
