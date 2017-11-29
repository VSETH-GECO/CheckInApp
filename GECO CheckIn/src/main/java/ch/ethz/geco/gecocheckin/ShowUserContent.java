package ch.ethz.geco.gecocheckin;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class ShowUserContent extends NetworkActivity {

    private String scanres;
    private boolean error;
    private String ticketdata;
    private int userId;

    /**
     * Create view
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_user_content);

        this.ticketdata = "";
        this.userId = -1;

        //get result of qr code
        Bundle b = getIntent().getExtras();
        this.scanres = "";
        if(b != null) {
            this.scanres = b.getString("scan");
        }
        try {
            JSONObject s = new JSONObject(this.scanres);
            this.userId = s.getInt("id");
        } catch (JSONException e) {
            e.printStackTrace();
        }


        //read API setting and validate
        String key = PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getString("saved_api_key", "error");
        String urls = PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getString("saved_server_ip", "error");

        if ( key.contains("error") || urls.contains("error") ) {
            Toast.makeText(this, "Fehler! Bitte App resetten!", Toast.LENGTH_LONG).show();
        }
        //validate Ticket and get Userdata
        //TODO: add query parm to scanres
        new Network(urls+"/lan/user/"+this.userId, key, "GET", "", 5000, this, this).execute();
    }

    /**
     * Present Userdata and calculate userage
     */
    private void showCont(){
        TextView data = (TextView) findViewById(R.id.contentView);

        String status = "ERROR";

        //Generate Text from request to display
        try {
            JsonParser parser = new JsonParser();
            JsonObject jo = (JsonObject) parser.parse(ticketdata);

            //Check if user is over 18
            long unixbirthday = jo.get("birthday").getAsLong()*1000;
            Date birthday = new Date(unixbirthday);
            Calendar calendar = GregorianCalendar.getInstance();
            calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) - 18);
            boolean over18 = birthday.before(calendar.getTime());

            data.setText("\nStatus: " + jo.get("status").getAsString());
            data.append("\nUser-ID: " + jo.get("id").getAsString());
            data.append("\nUsername: " + jo.get("username").getAsString());
            data.append("\nName: " + jo.get("fist_name").getAsString() + " " + jo.get("last_name").getAsString());
            data.append("\nGeburtstag: " + new SimpleDateFormat("dd.MM.yyyy").format(birthday) + " (über 18: " + (over18 ? "Ja" : "Nein") + ")");

            //data.append("\nSitzplatz: " + jo.get("seat").getAsString());

            status = jo.get("status").getAsString();

            //Confirm age if User ist under 18
            if ( !over18 ) {
                confirmAge();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            data.setText("Fehler beim darstellen der Daten. Bitte versuche es erneut!");
        }


        if ( status.contains("ERROR") ) {
            this.error = true;
        } else {
            this.error = false;
        }
    }

    /**
     * Confirm the checkin
     * @param view
     */
    private void checkIn(View view) {
        if ( !error ) {
            String key = PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getString("saved_api_key", "error");
            String urls = PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getString("saved_server_ip", "error");
            //TODO: add checkin to scanres
            new Network(urls, key, "POST", this.scanres, 5000, this, new MainMenue()).execute();
        } else {
            Toast.makeText(this, "User kann nicht eingeckecked werden. Siehe Status.", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Dialog Box to confirm, that the user has permission
     */
    private void confirmAge(){
        new AlertDialog.Builder(ShowUserContent.this)
                .setTitle("Altersprüfung")
                .setMessage("User ist nicht über 18! Ist eine Erlaubnis der Eltern vorhanden?")
                .setIcon(0)
                .setPositiveButton("Jup", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        //nothing
                    }
                })
                .setNegativeButton("Nope", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(ShowUserContent.this, "Checkin abgebrochen!", Toast.LENGTH_LONG).show();
                        Intent change = new Intent(getBaseContext(), Scan.class);
                        startActivity(change);
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    //TODO: confirmFachverein() missing

    public void showResult(String res) {
        if(this.ticketdata.equals("")) {
            this.ticketdata = res;
            this.showCont();
        } else {
            //TODO: If positiv: User has been checked in; move to main menue
        }
    }
}
