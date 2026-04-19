package packet;

import java.util.List;

public interface RotaFiltreStrategy {
    boolean guncelle(List<String> mevcut, List<String> yeni, int mevcutSure, int yeniSure, int mevcutAktarma, int yeniAktarma);
}
