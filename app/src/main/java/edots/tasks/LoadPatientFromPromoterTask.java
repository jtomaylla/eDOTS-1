package edots.tasks;

import android.os.AsyncTask;
import android.util.Log;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.util.ArrayList;

import edots.models.Patient;

/**
 * Created by jfang on 1/12/15.
 */
public class LoadPatientFromPromoterTask extends AsyncTask<String, String, ArrayList<String>> {

    protected ArrayList<String> doInBackground(String... params) {
        Patient p = null;

        int count = params.length;
        if (count != 2) {
            return null;
        }
        String urlserver = params[0];
        final String NAMESPACE = urlserver + "/";
        final String URL = NAMESPACE + "EdotsWS/Service1.asmx";
        final String METHOD_NAME = "ListadoPacientesUsuario";
        final String SOAP_ACTION = NAMESPACE + METHOD_NAME;
        SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);

        request.addProperty("CodigoUsuario", params[1]);
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.dotNet = true;

        envelope.setOutputSoapObject(request);

        HttpTransportSE transporte = new HttpTransportSE(URL);
        transporte.debug = true;

        try {
            transporte.call(SOAP_ACTION, envelope);

            SoapObject resSoap = (SoapObject) envelope.getResponse();
            if (resSoap.getPropertyCount() == 0) {
                Log.v("QUERY ERROR:", "This is not a valid person");
                return null;
            }
            int num_patients = resSoap.getPropertyCount();
            ArrayList<String> patients = new ArrayList<String>();


            for (int i = 0; i < num_patients; i++) {
                SoapObject ic = (SoapObject) resSoap.getProperty(i);
                String p_id = ic.getProperty(0).toString();
                patients.add(p_id);
            }

            return patients;


        } catch (Exception e) {
            e.printStackTrace();
            p = null;
        }

        return null;


    }

}
