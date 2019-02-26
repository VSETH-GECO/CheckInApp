package ch.ethz.geco.gecocheckin;

import android.content.Intent;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import ch.ethz.geco.g4j.impl.DefaultGECoClient;
import ch.ethz.geco.g4j.obj.GECoClient;
import ch.ethz.geco.g4j.obj.LanUser;
import ch.ethz.geco.g4j.obj.Seat;

public class SearchUser extends AppCompatActivity {
    private GECoClient client;
    private LanUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_user);

        this.client = new DefaultGECoClient(PreferenceManager.getDefaultSharedPreferences(this.getBaseContext()).getString("saved_api_key", "error"));

        //Setup Button action listener
        final Button btn_go = (Button) findViewById(R.id.btn_go);
        btn_go.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                search();
            }
        });
        final Button btn_scan = (Button) findViewById(R.id.btn_scan);
        btn_scan.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent change = new Intent(getBaseContext(), Scan.class);
                Bundle b = new Bundle();
                b.putString("caller", Rent.class.getCanonicalName());
                change.putExtras(b);
                startActivity(change);
            }
        });


        //read result if ticket got scanned
        Bundle b = getIntent().getExtras();
        String extra = "";
        if (b != null) {
            extra = b.getString("scan");
            try {
                //parse qr to json object
                JsonParser parser = new JsonParser();
                JsonObject scanres = (JsonObject) parser.parse(extra);
                this.user = this.client.getLanUserByID(scanres.get("id").getAsLong());
                search();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Search for user. Request depends on form used
     */
    private void search(){
        EditText userName = (EditText) findViewById(R.id.txt_username);
        EditText seatEditText = (EditText) findViewById(R.id.txt_seat);
        if (userName.getText().toString().length() > 0) {
            this.user = this.client.getLanUserByName(userName.getText().toString());
        } else if (seatEditText.getText().toString().length() > 0) {
            Seat seat = this.client.getSeatByName(seatEditText.getText().toString());
            if(seat.getLanUserID().isPresent())
                this.user = this.client.getLanUserByID(seat.getLanUserID().get());
        }

        if (this.user != null) {
            showCont();
        } else {
            Toast.makeText(this, "Bitte gibt einen Usernamen oder Platz an!", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Display data
     */
    private void showCont() {
        TextView data = (TextView) findViewById(R.id.contentView);

        String status = "ERROR";

        //Generate Text from request to display
        try {
            //Check if user is over 18
            long unixBirthDay = user.getBirthDay() * 1000;
            Date birthday = new Date(unixBirthDay);
            Calendar calendar = GregorianCalendar.getInstance();
            calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) - 18);
            boolean over18 = birthday.before(calendar.getTime());

            //add Data to Screen
            data.setText(String.format("\nStatus: %s", user.getStatus()));
            data.append(String.format("\nUser-ID: %s", user.getID()));
            data.append(String.format("\nUsername: %s", user.getUserName()));
            data.append(String.format("\nName: %s", user.getFirstName()));
            data.append(String.format("\nGeburtstag: %s (über 18: %s)", new SimpleDateFormat("dd.MM.yyyy").format(birthday), (over18 ? "Ja" : "Nein")));

            if (user.getSeatName().isPresent()) {
                data.append(String.format("\nSitzplatz: %s", user.getSeatName().get()));
            } else {
                data.append("\nSitzplatz: User hat noch keinen Sitzplatz!");
            }
            data.append(String.format("\nVerifikation: %s!", (user.isVerified() ? "Ja" : "Nein")));
            if (user.getLegiNumber().isPresent())
                data.append(String.format("\nLeginummer: %s", user.getLegiNumber().get()));
            else
                data.append("\nLeginummer:");
            data.append(String.format("\nPaket: %s", user.getLANPackage()));
            if (user.getStudentAssoc().isPresent())
                data.append(String.format("\nFachverein: %s", user.getStudentAssoc().get()));
            else
                data.append("\nFachverein: none");

        } catch (Exception e) {
            e.printStackTrace();
            data.setText("Fehler beim darstellen der Daten. Bitte versuche es erneut!");
        }
    }
}
