package ch.ethz.geco.gecocheckin;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class ShowUserContent extends AppCompatActivity {

    private String scanres;
    private ProgressDialog dialog;
    private boolean error;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_user_content);

        TextView data = (TextView) findViewById(R.id.contentView);

        Bundle b = getIntent().getExtras();
        String ticketdata = "";
        this.scanres = "";
        if(b != null) {
            ticketdata = b.getString("ticketdata");
            this.scanres = b.getString("scan");
        }

        String status = "ERROR";

        try {
            JsonParser parser = new JsonParser();
            JsonObject jo = (JsonObject) parser.parse(ticketdata);

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

            data.append("\nSitzplatz: " + jo.get("seat").getAsString());
            data.append("\n\nEssen Freitag: " + (jo.get("dinner_friday").getAsBoolean() ? "Ja" : "Nein"));
            data.append("\nEssen Samstag: " + (jo.get("dinner_saturday").getAsBoolean() ? "Ja" : "Nein"));
            data.append("\nEssen Sonntag: " + (jo.get("dinner_sunday").getAsBoolean() ? "Ja" : "Nein"));

            status = jo.get("status").getAsString();

            if ( !over18 ) {
                confirmAge();
            }
        }
        catch (Exception e) {
            data.setText("Fehler beim darstellen der Daten. Bitte versuche es erneut!");
        }


        if ( status.contains("ERROR") ) {
            error = true;
        } else {
            error = false;
        }
    }

    public void checkIn(View view) {
        if ( !error ) {
            String key = PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getString("saved_api_key", "error");
            String urls = PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getString("saved_server_ip", "error");
            new NetCheckIn(this, urls, key, this.scanres, 5000).execute();
        } else {
            Toast.makeText(this, "User kann nicht eingeckecked werden. Siehe Status.", Toast.LENGTH_LONG).show();
        }
    }

    public void done() {
        dialog.hide();
        Intent change = new Intent(getBaseContext(), Scan.class);
        startActivity(change);
    }


    public void showLoading(){
        dialog = new ProgressDialog(ShowUserContent.this);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setMessage("Lade Daten...");
        dialog.setIndeterminate(true);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }


    public void confirmAge(){
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


}
