/*package ch.ethz.geco.gecocheckin;

import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Net extends AsyncTask<String, String, String> {

    private String surl;
    private String key;
    private int timeout;
    private Scan scan;
    private String ticketscan;

    Net(String surl, String key, String ticketscan, int timeout, Scan scan) {
        this.surl = surl;
        this.key = key;
        this.timeout = timeout;
        this.scan = scan;
        this.ticketscan = ticketscan;
    }

    @Override
    protected void onPreExecute() {
        this.scan.showLoading();
        super.onPreExecute();
    }


    protected String doInBackground(String... params) {
        HttpURLConnection c = null;
        try {
            System.out.println(this.ticketscan);
            JsonParser parser = new JsonParser();
            try {
                JsonObject jo = (JsonObject)parser.parse(this.ticketscan);
                jo.addProperty("type", "query");
                this.ticketscan = jo.toString();
            } catch (Exception e) {
                //Toast.makeText(scan, "Ticket hat ein ungültiges Format!", Toast.LENGTH_LONG).show();
                return "Fehler! Ticket hat ein ungültiges Format!";
            }

            System.out.println("Added query to Data: " + this.ticketscan);
            System.out.println("Connect to " + this.surl + " with key " + this.key);
            URL u = new URL(this.surl);
            c = (HttpURLConnection) u.openConnection();
            c.setRequestMethod("POST");
            c.setUseCaches(false);
            c.setAllowUserInteraction(false);
            c.setConnectTimeout(timeout);
            c.setReadTimeout(timeout);
            c.setRequestProperty("Authorization", "Token token="+this.key);
            c.setRequestProperty("Content-Type","application/json");

            c.connect();

            String str =  this.ticketscan;
            byte[] outputInBytes = str.getBytes("UTF-8");
            OutputStream os = c.getOutputStream();
            os.write( outputInBytes );

            int status = c.getResponseCode();
            System.out.println("HTTP Status: " + status);

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
        return "Fehler! Verbindung konnte nicht hergestellt werden! Bitte prüfe deine Internetverbindung, Serveradresse und API Key";
    }

    @Override
    protected void onPostExecute(String a){
        System.out.println(a);
        if ( !a.contains("Fehler") ) {
            scan.showResult(a);
        } else {
            Toast.makeText(scan, a, Toast.LENGTH_LONG).show();
            scan.hideDialog();
        }

    }
}*/
