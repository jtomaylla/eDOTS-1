package edots.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import org.techintheworld.www.edots.R;

import java.util.concurrent.ExecutionException;

import edots.models.Login;
import edots.tasks.LoginTask;

public class AccountLogin {

    // TODO: refactor dialog back into promoter login activity

    public static String login(String username, String password, String locale, String locale_name, Context c) {
        // TODO: check internet connection

        LoginTask runner = new LoginTask();
        AsyncTask<String, String, Login> loginAsyncTask;
        String response = "";

        // Get the server from the settings
        SharedPreferences mPreferences = PreferenceManager.getDefaultSharedPreferences(c.getApplicationContext());
        SharedPreferences.Editor editor = mPreferences.edit();
        String url = "http://demo.sociosensalud.org.pe";
        Log.i("login", "OnClick_url:" + url);

        loginAsyncTask = runner.execute(username, password, locale, url);

        try {
            Login login = loginAsyncTask.get();
            response = login.Message;
            editor.putString(c.getString(R.string.promoter_id), username);
            editor.putString(c.getString(R.string.key_userid), String.valueOf(login.UserID));
            editor.putString(c.getString(R.string.login_locale), locale);
            editor.putString(c.getApplicationContext().getString(R.string.login_locale_name), locale_name);



            Log.i("login", "OnClick_response:" + response);
            if (response.equals(c.getString(R.string.session_init_key)) || response.equals(c.getString(R.string.password_expired_key))) {

                editor.commit();
            }
            else{
                ProgressDialog.Builder loginProgress = new ProgressDialog.Builder(c);

                loginProgress.setTitle("Login Error");
                loginProgress.setMessage("Your username or password was incorrect or invalid");
                loginProgress.show();
            }


        } catch (InterruptedException e1) {
            response = e1.getMessage();
        } catch (ExecutionException e2) {
            response = e2.getMessage();
        } catch (Exception e3) {
            response = e3.getMessage();
        }
        return response;
    }

}