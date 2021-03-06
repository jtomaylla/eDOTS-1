package org.techintheworld.www.edots;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import edots.models.Patient;
import edots.models.Schema;
import edots.models.Visit;
import edots.tasks.GetPatientContactLoadTask;
import edots.tasks.GetPatientLoadTask;
import edots.tasks.GetPatientSchemaLoadTask;
import edots.tasks.NewPromoterPatientUploadTask;
import edots.utils.OfflineStorageManager;


/*
 * Written by Ankit
 *
 * Controller file
 *      Associated View: activity_get_patient.xml
 *      Accesses Models: Patient, Visit,
 *
 * Used to query the database for patients and visits, by parsing the national ID input.
 * Also renders the queried patient data.
 */

public class GetPatientActivity extends Activity {

    private Patient currentPatient;
    private String promoterId;
    private Spinner spnPatient;
    private Context c = this;
    JSONArray object;

    @Override
    // TODO: needs comments!!
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_patient);

        // fetch promoterID from SharedPreferences
        SharedPreferences sPrefs = PreferenceManager.getDefaultSharedPreferences(c.getApplicationContext());
        promoterId = sPrefs.getString(getString(R.string.key_userid), "");

        // Makes sure that the keyboard doesn't automatically rise
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);


        spnPatient = (Spinner) findViewById(R.id.patient_spinner);
        loadPatientSpinner();
        testFunction();
        try {
            OfflineStorageManager sm = new OfflineStorageManager(this);
            String patient_file = getString(R.string.patient_data_filename);
            object = new JSONArray(sm.getStringFromLocal(patient_file));
        }
        catch(JSONException e1){
            e1.printStackTrace();
        }
        spnPatient.setOnItemSelectedListener(new OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                setButtons(false);
                int index = arg0.getSelectedItemPosition();

                if (index > 0) {
                    try {
                        currentPatient = new Patient(object.getJSONObject(index-1).toString());
                        Log.v("GetPatientActivity.java: The patient that we loaded is", currentPatient.toString());
                        fillTable();
                    } catch (NullPointerException e1) {
                        e1.printStackTrace();
                    } catch (JSONException e1) {
                        e1.printStackTrace();
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
            
        });
        try {
            setButtons(false);
            currentPatient = new Patient(getIntent().getExtras().getString("Patient"));
            fillTable();
        }
        catch (Exception e){
            Log.v("There is no patient already", "There is no patient already");
        }
    }

    // TODO: delete @Nishant
    public void testFunction() {
        GetPatientContactLoadTask result = new GetPatientContactLoadTask();
        result.execute(getString(R.string.server_url), "30C85C6A-D30E-48D2-949B-0004965E626F");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_get_patient, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * @author lili
     * @param view
     * when the search button is pressed
     */
    public void btnSearchClicked(View view) {
        hideKeyboard();
        loadPatient(view);
    }


    /**
     * @author Brendan
     * @param nationalid the DNI of the desired person who is being looked up
     * @return the Patient object of the person with the specified DNI, if exists
     * else it will be null
     * @throws JSONException
     * A function that looks up a DNI and checks if that DNI is found locally
     * and if not, attempts to find a patient with that DNI on the webservice
     */
    public Patient lookupPatient(String nationalid) throws JSONException{

        setButtons(false);
        currentPatient = null;

        // TODO: Check if Patient is already stored locally first
        JSONArray arr;
        try {
            OfflineStorageManager sm = new OfflineStorageManager(this);
            String patient_file = getString(R.string.patient_data_filename);

            // load list of patients from file patient_data
            arr = new JSONArray(sm.getStringFromLocal(patient_file));
            // look at all patients
            for (int i = 0; i < arr.length(); i++){
                JSONObject obj = arr.getJSONObject(i);
                Patient p = new Patient(obj.toString());
                // this ensures that they have a NationalId
                if (p.getNationalID() == nationalid) {
                    currentPatient = p;
                    Log.e("GetPatientActivity", "Patient Found is" + p.toString());
                }
            }
        }
        catch(NullPointerException e1){
            e1.printStackTrace();
        }

        // Instantiate a loader task and load the given patient via nationalid
        if (currentPatient == null) {
            GetPatientLoadTask newP = new GetPatientLoadTask();
            AsyncTask p = newP.execute(getString(R.string.server_url), nationalid);


            // parse the result, and return it
            try {
                currentPatient = (Patient) p.get();
                Log.v("GetPatientActivity: The patient loaded is", currentPatient.toString());
                //ArrayList<Visit> visits = currentPatient.getPatientHistory(this);
                //Log.v("GetPatientActivity.java: The patient visits that we got are", visits.toString());
                //Log.v("Patient that we got is", currentPatient.toString());

            } catch (InterruptedException e1) {
                e1.printStackTrace();
            } catch (ExecutionException e1) {
                e1.printStackTrace();
            } catch (NullPointerException e1){
                Log.e("GetPatientActivity: LoadPatient", "NullPointerException");
            }
        }
        return currentPatient;

    }

    /**
     * @author lili
     * load the project a patient is currently enrolled in into the patient object
     */
    public void loadPatientSchema(){
        //Schema currentSchema = new Schema();
        Log.v("GetPatientActivity.java: About to start loading schema", "About to start loading schema");
        GetPatientSchemaLoadTask schemaLoader = new GetPatientSchemaLoadTask();
        try{
            Schema currentSchema = schemaLoader.execute(getString(R.string.server_url), currentPatient.getPid()).get().get(0);
            Log.v("GetPatientActivity.java: The patient schema we just loaded was", currentSchema.toString());
            currentPatient.setEnrolledSchema(currentSchema);
            return;
        } catch (InterruptedException e1) {
            Log.v("GetPatientActivity.java: Could not load schema", "Could not load schema");
            e1.printStackTrace();
        } catch (ExecutionException e1) {
            Log.v("GetPatientActivity.java: Could not load schema", "Could not load schema");
            e1.printStackTrace();
        } catch (NullPointerException e1){
            Log.v("GetPatientActivity.java: Could not load schema", "Could not load schema");
            Log.e("null pointer exception","");
        }
        
        currentPatient.setEnrolledSchema(new Schema());
        
        return;
        

    }

    /**
     * @author Ankit
     * @param val bool to indicate whether buttons being turned off or on
     *
     * Turns on or off the buttons on the page to disallow accidental clicks when no patient is loaded.
     */
    public void setButtons(boolean val){
        
        // Toggle the buttons as needed
        Button historyBtn = (Button) findViewById(R.id.history_button);
        historyBtn.setEnabled(val);
        Button newVisitBtn = (Button) findViewById(R.id.new_visit_button);
        newVisitBtn.setEnabled(val);
        Button changeSchemaBtn = (Button) findViewById(R.id.change_schema_button);
        changeSchemaBtn.setEnabled(val);
        
        // If val is false, then clear the text fields.
        if (!val){
            TextView patientname = (TextView) findViewById(R.id.patientname);
            TextView nationalid = (TextView) findViewById(R.id.nationalid);
            TextView dob = (TextView) findViewById(R.id.dob);
            TextView sex = (TextView) findViewById(R.id.sex);

            patientname.setText("");
            nationalid.setText("");
            dob.setText("");
            sex.setText("");
        }
    }


    /**
     * @author Ankit
     * This function fills the text fields with the attributes of currentPatient
     */
    public void fillTable(){
        // TODO: clear existing patient data when searched again
        hideKeyboard();
        setButtons(false);
        if (currentPatient == null){
            return;
        }
        TextView patientname = (TextView) findViewById(R.id.patientname);
        TextView nationalid = (TextView) findViewById(R.id.nationalid);
        TextView dob = (TextView) findViewById(R.id.dob);
        TextView sex = (TextView) findViewById(R.id.sex);

        SimpleDateFormat parser =new SimpleDateFormat("dd/MM/yyyy");
        patientname.setText("");
        nationalid.setText("");
        dob.setText("");
        sex.setText("");


        patientname.setText(currentPatient.getName());
        if (currentPatient.getNationalID() != null) {
            nationalid.setText(currentPatient.getNationalID().toString());
        }
        if (currentPatient.getBirthDate() != null) {
            dob.setText(parser.format(currentPatient.getBirthDate()));
        }
        if (currentPatient.getSex() != null) {
            sex.setText(currentPatient.getSex());
        }

        setButtons(true);


    }

    /**
     * @author Ankit
     * @return boolean representing whether the inputs are all valid.
     */
    // TODO: needs cleanup
    public boolean validateInput() {
        
        //return true;
        
        EditText editor = (EditText) findViewById(R.id.nationalid_input);
        return editor.getText().toString().trim().length() != 0;
        
        /*String nationalID = editor.getText().toString();
        
        

        boolean validated = !nationalID.isEmpty();
        if (validated){
            Log.v("GetpatientActivity", "This patient is validated");
        }
        else{
            Log.v("GetPatientActivity", "This patient is not validated");
        }
        return validated;
        */
        
    }

    /**
     * @author Ankit
     * @param view the current view passed in with the onClick
     *
     * Called by the onClick method on Search - this calls the functions that make the queries for that patient
     *             and the function that fills the table.
     */
    // TODO: consider factorizing into subfunctions; consider rewriting the function comment above
    public void loadPatient(View view) {

        // clear the entered text and make new hint to search for new patient
        setButtons(false);
        
        
        // get the nationalID that was entered
        EditText editText = (EditText) findViewById(R.id.nationalid_input);
        String pid = editText.getText().toString();

        // make sure that the input is validated before continuing
        if (!validateInput()){
            return;
        }
        
        // blank out out the nationalid field
        editText.setText("", TextView.BufferType.EDITABLE);
        
        // lookup the given patient;
        try {
            
            currentPatient = lookupPatient(pid);
            loadPatientSchema();
        }
        catch(JSONException e1){
            e1.printStackTrace();
        }
        
        // alert to register patient if not found
        if (currentPatient == null){
            patientNotFoundAlert();
        }
        
        // alert ot add a patient to the list of patients for the promoter if found
        else {
            fillTable();
            // TODO: needs comments!
            try {
                OfflineStorageManager sm = new OfflineStorageManager(this);
                String patient_file = getString(R.string.patient_data_filename);
                // look at all patients
                boolean already_found = false;
                for (int i = 0; i < object.length(); i++) {
                    JSONObject obj = object.getJSONObject(i);
                    Patient p = new Patient(obj.toString());
                    if (currentPatient.getNationalID().equals(p.getNationalID())){
                        already_found = true;
                    }
                }
                if (!already_found) {
                    patientNotListedAlert(this);
                }
            }
            catch(Exception e){
                Log.e("GetPatientActivity: loadPatient", "unable to load patient_data");
            }
        }
    }

    /**
     * @author Brendan
     * Alert for when patient not found on server
     * Prompts user to register patient if they click add patient
     */
    private void patientNotFoundAlert(){
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle(this.getString(R.string.patient_not_found));
        alertDialog.setMessage(this.getString(R.string.patient_not_found_new_patient));
        alertDialog.setButton(-3, this.getString(R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        // TODO: pass in the DNI that they already entered
        alertDialog.setButton(-1, this.getString(R.string.add_patient), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                switchNewPatientDataActivity();
            }
        });
        alertDialog.show();
    }

    /**
     * @author Brendan
     * Alert for when patient is not listed as one of the patients for a promoter
     * If user presses add patient button, then adds to list of patients for logged-in promoter
     */
    private void patientNotListedAlert(Context c) {
        final Context context = c;
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle(this.getString(R.string.patient_not_listed));
        alertDialog.setMessage(this.getString(R.string.patient_not_listed_add_patient));
        alertDialog.setButton(-3, this.getString(R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alertDialog.setButton(-1, this.getString(R.string.add_patient), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Log.e("GetPatientActivity: loadPatient", currentPatient.getPid());
                NewPromoterPatientUploadTask npu = new NewPromoterPatientUploadTask();

                try {
                    npu.execute(getString(R.string.server_url), currentPatient.getPid(), promoterId, "0").get();
                    OfflineStorageManager sm = new OfflineStorageManager(context);
                    if (sm.CanUpdateLocalStorage()){
                        sm.UpdateLocalStorage();
                    }
                } catch (Exception e1) {
                    Log.e("GetPatientActivity: loadPatient", "ExecutionException Probably11");
                }
            }
        });
        alertDialog.show();
    }


    /**
     * switch to CheckFingerPrintActivity
     */
    public void switchCheckFingerPrint(View view) {
        if (currentPatient != null){
            Intent intent = new Intent(this, CheckFingerPrintActivity.class);
            intent.putExtra("Patient", currentPatient.toString());
            startActivity(intent);
        }
    }

    /**
     * switch to MedicalHistoryActivity
     */
    public void switchMedicalHistoryActivity(View view) {
        if (currentPatient != null){
            Intent intent = new Intent(this, MedicalHistoryActivity.class);
            intent.putExtra("Patient", currentPatient.toString());
            startActivity(intent);
        }

    }

    /**
     * switch to NewVisitActivity
     */
    public void switchNewVisitActivity(View view) {
        if (currentPatient != null) {
            Intent intent = new Intent(this, NewVisitActivity.class);
            intent.putExtra("Patient", currentPatient.toString());
            startActivity(intent);
        }
    }


    /**
     * switch to switchChangeSchemaActivity
     */
    public void switchChangeSchemaActivity(View view) {
        if (currentPatient != null) {
            Intent intent = new Intent(this, ChangeSchemaActivity.class);
            intent.putExtra("Patient", currentPatient.toString());
            startActivity(intent);
        }
    }
    
    /**
     * switch to NewPatientActivity
     */
    public void switchNewPatientDataActivity() {
            Intent intent = new Intent(this, NewPatientDataActivity.class);
            startActivity(intent);
    }

    /**
     * @author lili
     * hide keyboard
     */
    private void hideKeyboard() {
        // Check if no view has focus:
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    /**
     * @author Brendan
     * Loads the Patient Spinner by loading all patients from the JSON file patient_data
     */
    private void loadPatientSpinner(){
        JSONArray array;
        try {
            // load list of patients from file patient_data
            OfflineStorageManager sm = new OfflineStorageManager(this);
            String patient_file = getString(R.string.patient_data_filename);
            array = new JSONArray(sm.getStringFromLocal(patient_file));

            String[] patients = new String[array.length()+1];
            patients[0] = getString(R.string.get_patient_select_patient);
            // look at all patients
            for (int i = 0; i < array.length(); i++){
                JSONObject obj = array.getJSONObject(i);
                Patient p = new Patient(obj.toString());
                patients[i+1] = p.getName() + " " + p.getFathersName() + " " + p.getMothersName();
            }
            ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(
                    this, android.R.layout.simple_spinner_item, patients);
            spinnerArrayAdapter.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item );
            spnPatient.setAdapter(spinnerArrayAdapter);
        }
        catch(JSONException e1){
            e1.printStackTrace();
        }
        catch(NullPointerException e1){
            e1.printStackTrace();
        }

    }


}
