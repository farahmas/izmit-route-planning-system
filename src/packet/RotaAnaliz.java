package packet;

import java.util.List;
import java.util.Map;

public class RotaAnaliz {
    public static class Sonuc {
        public double ucret;
        public int sure;
        public int aktarma;

        public Sonuc(double ucret, int sure, int aktarma) {
            this.ucret = ucret;
            this.sure = sure;
            this.aktarma = aktarma;
        }
    }

    public static Sonuc analizEt(List<String> yol, Map<String, Durak> duraklar, Yolcu yolcu) {
        double toplamUcret = 0;
        int toplamSure = 0;
        int aktarmaSayisi = 0;

        for (int i = 0; i < yol.size() - 1; i++) {
            String from = yol.get(i);
            String to = yol.get(i + 1);
            Durak durak = duraklar.get(from);

            boolean bulundu = false;
            for (Map<String, Object> next : durak.getNextStops()) {
                if (next.get("stopId").equals(to)) {
                    double ucret = ((Number) next.get("ucret")).doubleValue();
                    int sure = ((Number) next.get("sure")).intValue();
                    toplamUcret += yolcu.indirimliUcret(ucret);
                    toplamSure += sure;
                    bulundu = true;
                }
            }

            String fromTip = duraklar.get(from).getType();
            String toTip = duraklar.get(to).getType();
            if (!fromTip.equals(toTip)) {
                toplamSure += 2;
                toplamUcret += 0.5;
                aktarmaSayisi++;
            }

            if (!bulundu) {
                aktarmaSayisi++;
            }
        }

        return new Sonuc(toplamUcret, toplamSure, aktarmaSayisi);
    }
}
