package ch.ethz.geco.gecocheckin;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
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

    private JsonObject scanres;
    private JsonObject ticketdata;
    private int userId;
    private int status;
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

        //checkin button action listener
        final FloatingActionButton checkin_btn = (FloatingActionButton) findViewById(R.id.checkin_btn);
        checkin_btn.setOnClickListener(new View.OnClickListener() { public void onClick(View v) { checkIn(); } } );

        //init values
        this.status = 0;
        this.userId = -1;

        //get result of qr code
        Bundle b = getIntent().getExtras();
        String extra = "";
        if (b != null) {
            extra = b.getString("scan");
        }
        try {
            //parse qr to json object
            JsonParser parser = new JsonParser();
            this.scanres = (JsonObject) parser.parse(extra);
            this.userId = this.scanres.get("id").getAsInt();
            this.validateString = this.scanres.get("verification").getAsString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        //validate Ticket and get Userdata
        new Network("/lan/user/" + this.userId, "GET", "", 5000, this).execute();
    }

    /**
     * Present Userdata, calculate userage and verify SA
     */
    private void showCont() {
        TextView data = (TextView) findViewById(R.id.contentView);

        String status = "ERROR";

        //Generate Text from request to display
        try {
            this.status = 1;
            //Check if user is over 18
            long unixbirthday = this.ticketdata.get("birthday").getAsLong() * 1000;
            Date birthday = new Date(unixbirthday);
            Calendar calendar = GregorianCalendar.getInstance();
            calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR) - 18);
            boolean over18 = birthday.before(calendar.getTime());
            //is verifyed?
            boolean ver = this.ticketdata.get("sa_verified").getAsBoolean();

            //add Data to Screen
            data.setText("\nStatus: " + this.ticketdata.get("status").getAsString());
            data.append("\nUser-ID: " + this.ticketdata.get("id").getAsString());
            data.append("\nUsername: " + this.ticketdata.get("username").getAsString());
            data.append("\nName: " + this.ticketdata.get("first_name").getAsString() + " " + this.ticketdata.get("last_name").getAsString());
            data.append("\nGeburtstag: " + new SimpleDateFormat("dd.MM.yyyy").format(birthday) + " (über 18: " + (over18 ? "Ja" : "Nein") + ")");

            if (!this.ticketdata.get("seat").isJsonNull()) {
                data.append("\nSitzplatz: " + this.ticketdata.get("seat").getAsString());
            } else {
                data.append("\nSitzplatz: User hat noch keinen Sitzplatz!");
                this.status = 2;
            }
            if (ver)
                data.append("\nVerifikation: OK!");
            else
                data.append("\nVerifikation: Nope");
            if (!this.ticketdata.get("legi_number").isJsonNull())
                data.append("\nLeginummer: " + this.ticketdata.get("legi_number").getAsString());
            else
                data.append("\nLeginummer:");
            data.append("\nPaket: " + this.ticketdata.get("package").getAsString());
            String jsonSAcont = "none";
            try {
                jsonSAcont = this.ticketdata.get("student_association").getAsString();
            }
            catch (UnsupportedOperationException e) {
                System.out.println("Not Fachverein set.");
            }

            data.append("\nFachverein: " + jsonSAcont);

            //Confirm SA and Age
            if (!ver) {
                this.status = 4;
                confirmSA(this.ticketdata.get("package").getAsString(), jsonSAcont);
            } else if (!over18) {
                this.status = 3;
                confirmAge();
            }

        } catch (Exception e) {
            e.printStackTrace();
            data.setText("Fehler beim darstellen der Daten. Bitte versuche es erneut!");
        }
    }

    /**
     * Dialog to confirm SA and get Legi ID
     * @param aPackage
     * @param student_association
     */
    private void confirmSA(String aPackage, String student_association) {
        if(aPackage.equals("External")){
            JsonObject post = new JsonObject();
            post.addProperty("sa_verified", true);
            post.addProperty("legi_number", "none");
            new Network("/lan/user/" + this.userId + "/verify", "PATCH", post.toString(), 5000, this).execute();
            return;
        }

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
        //Legi Format regexp
        String regex = "[0-9]{2}-[0-9]{3}-[0-9]{3}";
        if(id.matches(regex)){
            JsonObject post = new JsonObject();
            post.addProperty("sa_verified", true);
            post.addProperty("legi_number", id);
            new Network("/lan/user/" + this.userId + "/verify", "PATCH", post.toString(), 5000, this).execute();
        } else {
            Toast.makeText(ShowUserContent.this, "Legi-Nummer ist nicht richtig formatiert. Bitte versuche es erneut!", Toast.LENGTH_LONG).show();
            confirmSA(this.ticketdata.get("package").getAsString(), this.ticketdata.get("student_association").getAsString());
        }
    }

    /**
     * Confirm the checkin and execute
     */
    private void checkIn() {
        if (this.status == 1) {
            this.status = 5;
            JsonObject post = new JsonObject();
            post.addProperty("checkin_string", this.validateString);
            new Network("/lan/user/" + this.userId + "/checkin", "PATCH", post.toString(), 5000, this).execute();
        } else {
            switch (this.status){
                case 2:
                    Toast.makeText(ShowUserContent.this, "User hat keinen Sitzplatz!", Toast.LENGTH_LONG).show();
                    break;
                case 3:
                    Toast.makeText(ShowUserContent.this, "Alter des Users ist nicht bestätigt!", Toast.LENGTH_LONG).show();
                    break;
                case 4:
                    Toast.makeText(ShowUserContent.this, "User ist nicht verifiziert!", Toast.LENGTH_LONG).show();
                    break;
                case 5:
                    Toast.makeText(ShowUserContent.this, "Checkin fehlgeschlagen!", Toast.LENGTH_LONG).show();
                    break;
                default:
                    Toast.makeText(ShowUserContent.this, "Es gab einen Fehler!", Toast.LENGTH_LONG).show();
                    break;
            }
        }
    }

    /**
     * Dialog Box to confirm, that the user has permission
     */
    private void confirmAge() {
        if(!this.ticketdata.get("status").getAsString().equalsIgnoreCase("user has been checked in!")) {
            new AlertDialog.Builder(ShowUserContent.this)
                    .setTitle("Altersprüfung")
                    .setMessage("User ist nicht über 18! Ist eine Erlaubnis der Eltern vorhanden?")
                    .setIcon(0)
                    .setPositiveButton("Jup", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            status = 1;
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
    }

    /**
     * Get data/response from Network Object
     * @param res
     */
    public void showResult(String res) {
        JsonParser parser = new JsonParser();
        JsonObject jo = (JsonObject) parser.parse(res);
        if (!jo.has("code") || jo.get("code").toString().equals("200")) {
            if(!jo.has("code")){
                this.ticketdata = jo;
            }
            showCont();
        } else {
            switch (this.status){
                case 4:
                    Toast.makeText(ShowUserContent.this, "SA konnte nicht aktualisiert werden!", Toast.LENGTH_LONG).show();
                    break;
                case 5:
                    Toast.makeText(ShowUserContent.this, "Checkin fehlgeschlagen!", Toast.LENGTH_LONG).show();
                    break;
                default:
                    Toast.makeText(ShowUserContent.this, "Es gab einen Fehler bei der Anfrage!", Toast.LENGTH_LONG).show();
                    break;
            }
        }
    }

}
