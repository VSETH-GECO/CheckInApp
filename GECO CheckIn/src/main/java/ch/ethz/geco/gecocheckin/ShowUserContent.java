package ch.ethz.geco.gecocheckin;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class ShowUserContent extends NetworkActivity {

    private String scanres;
    private boolean error;
    private String ticketdata;
    private int userId;
    private String validateString;

    /**
     * Create view
     *
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
        if (b != null) {
            this.scanres = b.getString("scan");
        }
        try {
            JsonParser parser = new JsonParser();
            JsonObject s = (JsonObject) parser.parse(this.scanres);
            this.userId = s.get("id").getAsInt();
            this.validateString = s.get("verification").getAsString();
        } catch (Exception e) {
            e.printStackTrace();
        }


        //validate Ticket and get Userdata
        //TODO: add query parm to scanres
        new Network("/lan/user/" + this.userId, "GET", "", 5000, this, this).execute();
    }

    /**
     * Present Userdata and calculate userage
     */
    private void showCont() {
        TextView data = (TextView) findViewById(R.id.contentView);

        String status = "ERROR";

        //Generate Text from request to display
        try {
            JsonParser parser = new JsonParser();
            JsonObject jo = (JsonObject) parser.parse(ticketdata);

            //Check if user is over 18
            long unixbirthday = jo.get("birthday").getAsLong() * 1000;
            Date birthday = new Date(unixbirthday);
            Calendar calendar = GregorianCalendar.getInstance();
            calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) - 18);
            boolean over18 = birthday.before(calendar.getTime());
            boolean ver = jo.get("sa_verified").getAsBoolean();

            data.setText("\nStatus: " + jo.get("status").getAsString());
            data.append("\nUser-ID: " + jo.get("id").getAsString());
            data.append("\nUsername: " + jo.get("username").getAsString());
            data.append("\nName: " + jo.get("fist_name").getAsString() + " " + jo.get("last_name").getAsString());
            data.append("\nGeburtstag: " + new SimpleDateFormat("dd.MM.yyyy").format(birthday) + " (über 18: " + (over18 ? "Ja" : "Nein") + ")");

            if (!jo.get("seat").isJsonNull())
                data.append("\nSitzplatz: " + jo.get("seat").getAsString());
            else
                data.append("\nSitzplatz: User hat noch keinen Sitzplatz!");
            if (ver)
                data.append("\nVerifikation: OK!");
            else
                data.append("\nVerifikation: Nope");
            if (!jo.get("legi_number").isJsonNull())
                data.append("\nLeginummer: " + jo.get("legi_number").getAsString());
            else
                data.append("\nLeginummer:");
            data.append("\nPaket: " + jo.get("package").getAsString());
            data.append("\nFachverein: " + jo.get("student_association").getAsString());


            status = jo.get("status").getAsString();


            //Confirm age if User ist under 18
            if (!over18)
                confirmAge();
            //Confirm SA
            if (!ver) {
                confirmSA(jo.get("package").getAsString(), jo.get("student_association").getAsString());
            }

        } catch (Exception e) {
            e.printStackTrace();
            data.setText("Fehler beim darstellen der Daten. Bitte versuche es erneut!");
        }


        if (status.contains("ERROR")) {
            this.error = true;
        } else {
            this.error = false;
        }
    }

    /**
     * Dialog to confirm SA and get Legi ID
     * @param aPackage
     * @param student_association
     */
    private void confirmSA(String aPackage, String student_association) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Bitte prüfe die folgenden Angaben:");
        builder.setMessage(aPackage + "\n" + student_association);


        final EditText input = new EditText(this);

        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setHint("Legi-Nummer...");
        builder.setView(input);


        builder.setPositiveButton("Jup", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                System.out.println("legi " + input.getText().toString());
                getLegiIDFromDialog(input.getText().toString());
            }
        });
        builder.setNegativeButton("Nope", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                Toast.makeText(ShowUserContent.this, "Checkin abgebrochen!", Toast.LENGTH_LONG).show();
                Intent change = new Intent(getBaseContext(), Scan.class);
                startActivity(change);
            }
        });

        builder.show();
    }

    /**
     * Get result from user input and process
     * @param id
     */
    private void getLegiIDFromDialog(String id){
        System.out.println("legi@method: " + id);
        JsonObject post = new JsonObject();
        post.addProperty("sa_verified", true);
        post.addProperty("legi_number", id);
        new Network("/lan/user/" + this.userId + "/verify", "PATCH", post.toString(), 5000, this, this).execute();
    }

    /**
     * Confirm the checkin
     *
     * @param view
     */
    private void checkIn(View view) {
        if (!error) {
            JsonObject post = new JsonObject();
            post.addProperty("checkin_string", this.validateString);
            new Network("/lan/user/" + this.userId + "/checkin", "POST", this.scanres, 5000, this, new MainMenue()).execute();
        } else {
            Toast.makeText(this, "User kann nicht eingeckecked werden. Siehe Status.", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Dialog Box to confirm, that the user has permission
     */
    private void confirmAge() {
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


    public void showResult(String res) {
        //TODO: chatch error?
        if (this.ticketdata.equals("")) {
            this.ticketdata = res;
            this.showCont();
        } else {
            //TODO: If positiv: User has been checked in; move to main menue
        }
    }
}
