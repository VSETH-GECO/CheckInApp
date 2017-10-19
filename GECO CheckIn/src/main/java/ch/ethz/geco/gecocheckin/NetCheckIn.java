package ch.ethz.geco.gecocheckin;

import android.os.AsyncTask;
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


public class NetCheckIn extends AsyncTask<String, String, String> {

    private String ticketscan;
    private String surl;
    private String key;
    private int timeout;
    private ShowUserContent from;


    NetCheckIn(ShowUserContent from, String surl, String key, String ticketscan, int timeout) {
        this.surl = surl;
        this.key = key;
        this.ticketscan = ticketscan;
        this.timeout = timeout;
        this.from = from;
    }

    @Override
    protected void onPreExecute() {
        this.from.showLoading();
        super.onPreExecute();
    }


    protected String doInBackground(String... params) {
        HttpURLConnection c = null;
        try {
            System.out.println(this.ticketscan);


            JsonParser parser = new JsonParser();
            try {
                JsonObject jo = (JsonObject)parser.parse(this.ticketscan);
                jo.addProperty("type", "checkin");
                this.ticketscan = jo.toString();
            } catch (Exception e) {
                //Toast.makeText(from, "Ticket hat ein ungültiges Format!", Toast.LENGTH_LONG).show();
                return "Fehler! Ticket hat ein ungültiges Format!";
            }


            
            System.out.println("Connect to " + this.surl + " with key " + this.key + " sending: " + this.ticketscan);
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
        if ( !a.contains("OK") ) {
            Toast.makeText(from, a, Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(from, "User erfolgreich eingechecked!", Toast.LENGTH_LONG).show();
            from.done();
        }
        super.onPostExecute(a);
    }
}
