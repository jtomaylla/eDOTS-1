package edots.models;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * @author JN Fang
 * @author Ankit Gupta
 * @since 2015-01-13
 *
 *
 * Model for Promoters.
 *
 */
public class Promoter extends Object {
    private String PromoterId;
    private String Locale;
    private ArrayList<String> patient_ids;
    //TODO: add projects they are qualified to administer

    public Promoter(){

    }

    /**
     * Constructor for Promoter
     *
     * @param u PromoterId of promoter
     * @param l Locale of promoter
     * @param p NOTUSED
     * @param pt ids of the patient of that promoter
     */
    public Promoter(String u,String l, String p, ArrayList<String> pt){
        PromoterId = u;
        Locale = l;
        patient_ids=pt;
    }

    /**
     * @param JSONString JSON encoding of the Promoter object
     */
    public Promoter(String JSONString) {
        try {
            JSONObject n = new JSONObject(JSONString);
            PromoterId = n.get("PromoterId").toString();
            Locale = n.get("Locale").toString();
            patient_ids = new ArrayList<String>();
            JSONArray arry = new JSONArray(n.get("patient_ids").toString());
            for (int i = 0; i < arry.length(); i++) {
                patient_ids.add(arry.getString(i));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @return a JSON serialization of the Promoter object
     */
    public String toString(){
        JSONObject temp = new JSONObject();
        try {
            temp.put("PromoterId", getPromoterId());
            temp.put("Locale", getLocale());
            temp.put("patient_ids", new JSONArray(getPatientIds()));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return temp.toString();

    }

    public ArrayList<String> getPatientIds(){return patient_ids;}

    public String getPromoterId(){
        return PromoterId;
    }

    public String getLocale(){
        return Locale;
    }

    public void setPatientIds(ArrayList<String> p_ids){ patient_ids = p_ids;}

    public void setPromoterId(String u){
        PromoterId =u;
    }


    public void setLocale(String l){
        Locale =l;
    }


}
