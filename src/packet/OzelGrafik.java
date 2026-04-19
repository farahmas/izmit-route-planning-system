package packet;

import java.util.List;
import java.util.Map;

import com.mxgraph.model.mxCell;
import com.mxgraph.view.mxGraph;

public class OzelGrafik extends mxGraph {

   
    public OzelGrafik() {
        super();
    }


    public void rotaCiz(Object parent, Map<String, Object> vertexMap, List<String> rota, List<String> aracTurleri) {
        double x = 20;
        double y = 50;

        for (int i = 0; i < rota.size(); i++) {
            String durakId = rota.get(i);
            String etiket = durakId;
            if (i == 0) etiket += " (Başlangıç)";
            else if (i == rota.size() - 1) etiket += " (Hedef)";

            Object vertex = insertVertex(parent, null, etiket, x, y, 100, 40);
            vertexMap.put(durakId, vertex);
            x += 130;

            if (i > 0) {
                String arac = aracTurleri.get(i - 1).toLowerCase();
                String renk = switch (arac) {
                    case "tram", "tramvay" -> "green";
                    case "bus", "otobus" -> "blue";
                    case "taksi" -> "orange";
                    case "transfer" -> "red";
                    default -> "gray";
                };
                String stil = arac.equals("transfer") ?
                    "strokeColor=" + renk + ";dashed=1;dashPattern=3 3;strokeWidth=2" :
                    "strokeColor=" + renk + ";strokeWidth=3";

                insertEdge(parent, null, arac, vertexMap.get(rota.get(i - 1)), vertex, stil);
            }
        }
    }
}
