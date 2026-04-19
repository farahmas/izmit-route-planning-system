package packet;
public abstract class Yolcu {
    protected String tip;

    public String getTip() {
        return tip;
    }

    public abstract double indirimliUcret(double ucret);

    public double odemeYap(double ucret) {
        double indirimliTutar = indirimliUcret(ucret);
        logla(indirimliTutar);
        return indirimliTutar;
    }

    protected void logla(double tutar) {
        System.out.printf("%s yolcu için hesaplanan ücret: %.2f TL%n", getTip(), tutar);
    }
}

