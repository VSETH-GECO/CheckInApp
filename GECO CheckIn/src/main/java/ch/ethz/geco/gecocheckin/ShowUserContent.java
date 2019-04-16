package ch.ethz.geco.gecocheckin;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
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

import ch.ethz.geco.g4j.impl.DefaultGECoClient;
import ch.ethz.geco.g4j.obj.GECoClient;
import ch.ethz.geco.g4j.obj.LanUser;
import reactor.core.publisher.Mono;

public class ShowUserContent extends AppCompatActivity {

    private LanUser user;
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
        checkin_btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                checkIn();
            }
        });

        //init values
        this.status = 0;
        int userId = -1;

        //get result of qr code
        Bundle b = getIntent().getExtras();
        String extra = "";
        if (b != null) {
            extra = b.getString("scan");
        }
        try {
            //parse qr to json object
            JsonParser parser = new JsonParser();
            JsonObject scanres = (JsonObject) parser.parse(extra);
            userId = scanres.get("id").getAsInt();
            this.validateString = scanres.get("verification").getAsString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        //validate Ticket and get Userdata
        String apiKey = PreferenceManager.getDefaultSharedPreferences(this.getBaseContext()).getString("saved_api_key", "error");
        GECoClient client = new DefaultGECoClient(apiKey);

        Mono<LanUser> monoLanUser = client.getLanUserByID((long) userId);
        monoLanUser.doOnError(Throwable::printStackTrace).subscribe(lanUser -> {
            this.user = lanUser;
            runOnUiThread(() -> {
                showCont(lanUser);
                Loading.instance.done();
            });
        });
        new Loading(this).show();
    }

    /**
     * Present Userdata, calculate userage and verify SA
     */
    private void showCont(LanUser user) {
        TextView data = (TextView) findViewById(R.id.contentView);

        String status = "ERROR";

        //Generate Text from request to display
        try {
            this.status = 1;
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
                this.status = 2;
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

            //Confirm SA and Age
            if (!user.isVerified()) {
                this.status = 4;
                if(user.getStudentAssoc().isPresent())
                    confirmSA(user.getLANPackage(), user.getStudentAssoc().get());
                else
                    confirmSA(user.getLANPackage(), "none");
            } else if (!over18 && this.status != 5) {
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
     *
     * @param aPackage Package the user has booked
     * @param student_association SA the user has choosen
     */
    private void confirmSA(String aPackage, String student_association) {
        if (aPackage.equals("External")) {
            this.user.setVerification(true, "").subscribe(lanUser -> {
                this.user = lanUser;
                runOnUiThread(() -> {
                    showCont(lanUser);
                    Loading.instance.done();
                });
            });
            new Loading(this).show();
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
     * @param id legi ID from tge dialog
     */
    private void getLegiIDFromDialog(String id) {
        //Legi Format regexp
        String regex = "[0-9]{2}-[0-9]{3}-[0-9]{3}";
        if (id.matches(regex)) {
            this.user.setVerification(true, id).subscribe(lanUser -> {
                this.user = lanUser;
                runOnUiThread(() -> {
                    showCont(lanUser);
                    Loading.instance.done();
                });
            });
            new Loading(this).show();
        } else {
            Toast.makeText(ShowUserContent.this, "Legi-Nummer ist nicht richtig formatiert. Bitte versuche es erneut!", Toast.LENGTH_LONG).show();
            if (this.user.getStudentAssoc().isPresent())
                confirmSA(this.user.getLANPackage(), this.user.getStudentAssoc().get());
        }
    }

    /**
     * Confirm the checkin and execute
     */
    private void checkIn() {
        if (this.status == 1) {
            this.status = 5;
            this.user.checkin(this.validateString).subscribe(lanUser -> {
                this.user = lanUser;
                runOnUiThread(() -> {
                    showCont(lanUser);
                    Loading.instance.done();
                    new AlertDialog.Builder(ShowUserContent.this)
                            .setTitle("Check In")
                            .setMessage("Der User wurde erfolgreich eingechecked!")
                            .setIcon(0)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_info)
                            .show();
                });
            });
            new Loading(this).show();
        } else {
            switch (this.status) {
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
        new AlertDialog.Builder(ShowUserContent.this)
                .setTitle("Altersprüfung")
                .setMessage("User ist nicht über 18! Ist eine Erlaubnis der Eltern vorhanden?")
                .setIcon(0)
                .setPositiveButton("Jup", (dialog, which) -> status = 1)
                .setNegativeButton("Nope", (dialog, which) -> {
                    Toast.makeText(ShowUserContent.this, "Checkin abgebrochen!", Toast.LENGTH_LONG).show();
                    Intent change = new Intent(getBaseContext(), Scan.class);
                    startActivity(change);
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

}
