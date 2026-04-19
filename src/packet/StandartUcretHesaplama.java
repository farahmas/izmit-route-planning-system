package packet;

public class StandartUcretHesaplama implements UcretHesaplamaStrategy {

    @Override
    public double ucretHesapla(Yolcu yolcu, double temelUcret, String aracTipi) {
        if (aracTipi.equalsIgnoreCase("taksi")) {
            return temelUcret;
        } else {
            return yolcu.indirimliUcret(temelUcret); 
        }
    }
}
