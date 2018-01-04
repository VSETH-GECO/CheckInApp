package ch.ethz.geco.gecocheckin;

import android.content.Intent;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by felunka on 19.10.2017.
 */

public class Network extends AsyncTask<String, String, String> {

    private String serverurl;
    private String apikey;
    private int timeout;
    private String requestType;
    private Loading loading;
    private String content;
    private AppCompatActivity origin;
    private NetworkActivity target;

    /**
     * Initilize all vars
     * @param requestType POST or GET
     * @param content POST content for request
     * @param origin Calling class
     * @param target Class to open when done with result
     */
    Network(String urlex, String requestType, String content, int timeout, AppCompatActivity origin, NetworkActivity target) {
        this.timeout = timeout;
        this.requestType = requestType;
        this.content = content;
        this.origin = origin;
        this.target = target;
        this.loading = new Loading(this.origin, this.target);
        this.apikey = PreferenceManager.getDefaultSharedPreferences(origin.getBaseContext()).getString("saved_api_key", "error");
        this.serverurl = PreferenceManager.getDefaultSharedPreferences(origin.getBaseContext()).getString("saved_server_ip", "error");
        this.serverurl += urlex;
        if (this.apikey.contains("error") || this.serverurl.contains("error")) {
            Toast.makeText(origin, "Fehler! Bitte App resetten!", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Displays loading animation and starts request
     */
    @Override
    protected void onPreExecute() {
        this.loading.show();
        super.onPreExecute();
    }

    /**
     * Opens Network connection and returns answer
     * @param params
     * @return
     */
    protected String doInBackground(String... params) {
        HttpURLConnection c = null;
        try {
            //Open network connection to Host
            System.out.println("Connect to " + this.serverurl + " with apikey " + this.apikey);
            URL u = new URL(this.serverurl);
            c = (HttpURLConnection) u.openConnection();
            c.setRequestMethod(this.requestType);
            c.setUseCaches(false);
            c.setAllowUserInteraction(false);
            c.setConnectTimeout(timeout);
            c.setReadTimeout(timeout);
            c.setRequestProperty("Content-Type","application/json");
            //API Key for Authorisation
            c.addRequestProperty("X-API-KEY", this.apikey);

            c.connect();

            //Content only for POST
            if (this.requestType.equals("POST")) {
                String str =  this.content;
                byte[] outputInBytes = str.getBytes("UTF-8");
                OutputStream os = c.getOutputStream();
                os.write( outputInBytes );
            }

            //Get HTTP response Code
            int status = c.getResponseCode();
            System.out.println("HTTP Status: " + status);

            //Read result if response 200
            switch (status) {
                case 200:
                case 201:
                    BufferedReader br = new BufferedReader(new InputStreamReader(c.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line+"\n");
                    }
                    br.close();
                    System.out.println("returns: " + sb.toString());
                    return sb.toString();
            }

        } catch (MalformedURLException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (c != null) {
                try {
                    c.disconnect();
                } catch (Exception ex) {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        //Return error Message
        return "Fehler! Verbindung konnte nicht hergestellt werden! Bitte prüfe deine Internetverbindung, Serveradresse und API Key.";
    }

    /**
     * Send result to target, hide loading animation and switch to target Activity
     * @param a HTTP result
     */
    @Override
    protected void onPostExecute(String a){
        //System.out.println(a);
        //TODO: switch to target
        if ( !a.contains("Fehler") ) {
            this.target.showResult(a);
            this.loading.done();
        } else {
            Toast.makeText(this.target, a, Toast.LENGTH_LONG).show();
            this.loading.done();
        }

        //TODO: switch to target?
    }
}
