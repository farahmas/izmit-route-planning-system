package packet;

public class GenelYolcu extends Yolcu {
    public GenelYolcu() {
        this.tip = "Genel";
    }

    @Override
    public double indirimliUcret(double ucret) {
        return ucret;
    }
}