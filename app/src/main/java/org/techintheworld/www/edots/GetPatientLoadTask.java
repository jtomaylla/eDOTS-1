package org.techintheworld.www.edots;

import android.os.AsyncTask;
import android.util.Log;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import edots.models.Patient;
import edots.models.Project;

public class GetPatientLoadTask extends AsyncTask<String,String,Patient> {


    private Patient lstGeofence;

    @Override
    protected Patient doInBackground(String... params) {

        Patient p= null;

        String urlserver = params[0];
        final String NAMESPACE = urlserver+"/";
        final String URL=NAMESPACE+"EdotsWS/Service1.asmx";
        final String METHOD_NAME = "BuscarParticipante";
        final String SOAP_ACTION = NAMESPACE+METHOD_NAME;
        SoapObject request = new SoapObject(NAMESPACE, METHOD_NAME);

		request.addProperty("DocIdentidad", params[1]);
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.dotNet = true;

        envelope.setOutputSoapObject(request);

        HttpTransportSE transporte = new HttpTransportSE(URL);
        transporte.debug = true;
        try
        {
            transporte.call(SOAP_ACTION, envelope);

            SoapObject resSoap =(SoapObject)envelope.getResponse();

            SoapObject resSoap2 = (SoapObject) resSoap.getProperty(0);

            Log.v("The object we got is", resSoap2.getProperty(0).toString());


            SoapObject ic = (SoapObject)resSoap.getProperty(0);

            String patientID = ic.getProperty(0).toString();
            String name = ic.getProperty(1).toString();
            String fathersName = ic.getProperty(2).toString();
            String mothersName = ic.getProperty(3).toString();
            Long nationalID = Long.valueOf(ic.getProperty(5).toString());
            Date birthDate = new Date();
            Integer sexInt = Integer.parseInt(ic.getProperty(7).toString());
            String sex = "null";
            if (sexInt == 0){
                sex = "Female";}
            else{
                sex = "Male";}
            Project testProject = new Project();
            Project testProject2 = new Project();
            ArrayList<Project> enrolledProjects = new ArrayList<Project>(Arrays.asList(testProject, testProject2));

            Log.v("patient data", patientID+name+fathersName+mothersName+nationalID);

            p = new Patient(name, birthDate, nationalID, sex, enrolledProjects, mothersName, fathersName, patientID);
//
//            p.latitud = ic.getProperty(3).toString();
//            p.longitud = ic.getProperty(4).toString();
//            p.radio = ic.getProperty(5).toString();
//            p.duracionexpiracion = ic.getProperty(6).toString();
//            p.tipotransicion = Integer.parseInt(ic.getProperty(7).toString());
//            lstGeofence[i] = p;
//
//            if (resSoap.getPropertyCount()>0){
//                resul = lstGeofence;
//            }

            Log.v("patient object:", p.toString());

        }
        catch (Exception e)
        {
            e.printStackTrace();
            p = null;
        }

        return p;
    }

}

