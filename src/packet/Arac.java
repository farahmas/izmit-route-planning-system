package packet;

public abstract class Arac {
    protected String aracTipi;
    protected double birimUcret;

    public Arac(String aracTipi, double birimUcret) {
        this.aracTipi = aracTipi;
        this.birimUcret = birimUcret;
    }

    public double ucretHesapla(double mesafe, String yolcuTipi) {
       
        if (this instanceof Taksi) {
            return Math.round(mesafe * birimUcret * 100.0) / 100.0;
        }
        
        double oran = switch (yolcuTipi.toLowerCase()) {
            case "ogrenci" -> 0.5;  
            case "ogretmen" -> 0.75; 
            case "yasli" -> 0.7;    
            default -> 1.0;         
        };

        return Math.round(mesafe * birimUcret * oran * 100.0) / 100.0;
    }

    public String getAracTipi() {
        return aracTipi;
    }
}
