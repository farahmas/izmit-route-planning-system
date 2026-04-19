package packet;

public class Yasli extends Yolcu {
    public Yasli() {
        this.tip = "Yasli";
    }

    @Override
    public double indirimliUcret(double ucret) {
        return Math.round(ucret * 0.7 * 100.0) / 100.0;
    }
}