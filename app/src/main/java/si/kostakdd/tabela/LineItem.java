package si.kostakdd.tabela;

import org.json.JSONException;
import org.json.JSONObject;

public class LineItem {
    public String inv_st,opis,geoLokacija,geoCoord,row_num;

    public JSONObject Json;

    public LineItem(int i, String JSONstring) throws JSONException {

        this.Json = new JSONObject(JSONstring);
        this.inv_st = this.Json.getString("Inv_št");
        this.row_num = i + ".";
        this.opis = this.Json.getString("Naziv_osn_sred") + " (" +  this.inv_st + ")";
        this.geoCoord = this. Json.getString("GEOCoord");
        this.geoLokacija = this.Json.getString("GEOLokacija");

    }
}
