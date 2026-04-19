package packet;

import java.util.List;

public class EnHizliFiltre implements RotaFiltreStrategy {
    @Override
    public boolean guncelle(List<String> mevcut, List<String> yeni, int mevcutSure, int yeniSure, int mevcutAktarma, int yeniAktarma) {
        return mevcut == null || yeniSure < mevcutSure;
    }
}
