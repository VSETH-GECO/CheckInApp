package ch.ethz.geco.gecocheckin;

import android.content.Intent;
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

public class SearchUser extends NetworkActivity {
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_user);

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
                this.userId = scanres.get("id").getAsInt();
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
        EditText seat = (EditText) findViewById(R.id.txt_seat);
        if (userName.getText().toString().length() > 0) {
            new Network("/lan/search/user/" + userName.getText().toString(), "GET", "", 5000, this).execute();
        } else if (seat.getText().toString().length() > 0) {
            new Network("/lan/search/seat/" + seat.getText().toString(), "GET", "", 5000, this).execute();
        } else if (userId != 0) {
            new Network("/lan/user/" + userId, "GET", "", 5000, this).execute();
        } else {
            Toast.makeText(this, "Bitte gibt einen Usernamen oder Platz an!", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Display data
     * @param userdata
     */
    private void showCont(JsonObject userdata) {
        TextView data = (TextView) findViewById(R.id.contentView);

        String status = "ERROR";

        //Generate Text from request to display
        try {
            //Check if user is over 18
            long unixbirthday = userdata.get("birthday").getAsLong() * 1000;
            Date birthday = new Date(unixbirthday);
            Calendar calendar = GregorianCalendar.getInstance();
            calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) - 18);
            boolean over18 = birthday.before(calendar.getTime());
            //is verifyed?
            boolean ver = userdata.get("sa_verified").getAsBoolean();

            //add Data to Screen
            data.setText("\nStatus: " + userdata.get("status").getAsString());
            data.append("\nUser-ID: " + userdata.get("id").getAsString());
            data.append("\nUsername: " + userdata.get("username").getAsString());
            data.append("\nName: " + userdata.get("first_name").getAsString() + " " + userdata.get("last_name").getAsString());
            data.append("\nGeburtstag: " + new SimpleDateFormat("dd.MM.yyyy").format(birthday) + " (über 18: " + (over18 ? "Ja" : "Nein") + ")");

            if (!userdata.get("seat").isJsonNull()) {
                data.append("\nSitzplatz: " + userdata.get("seat").getAsString());
            } else {
                data.append("\nSitzplatz: User hat noch keinen Sitzplatz!");
            }
            if (ver)
                data.append("\nVerifikation: OK!");
            else
                data.append("\nVerifikation: Nope");
            if (!userdata.get("legi_number").isJsonNull())
                data.append("\nLeginummer: " + userdata.get("legi_number").getAsString());
            else
                data.append("\nLeginummer:");
            data.append("\nFachverein: " + userdata.get("student_association").getAsString());

        } catch (Exception e) {
            e.printStackTrace();
            data.setText("Fehler beim darstellen der Daten. Bitte versuche es erneut!");
        }
    }

    /**
     * Get network result
     * @param res
     */
    public void showResult(String res) {
        if (res.length() == 0) {
            return;
        }
        JsonParser parser = new JsonParser();
        JsonObject jo = (JsonObject) parser.parse(res);
        if (jo.has("status")) {
            showCont(jo);
        }
    }
}
