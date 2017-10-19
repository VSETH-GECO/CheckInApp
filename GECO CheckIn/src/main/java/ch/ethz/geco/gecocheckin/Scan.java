package ch.ethz.geco.gecocheckin;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class Scan extends NetworkActivity {

    private String qrticketscan;
    private ProgressDialog dialog;
    private boolean debug;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.debug = PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getBoolean("saved_debug_status", false);

        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
        integrator.setPrompt("");
        integrator.setCameraId(0);
        integrator.setBeepEnabled(true);
        integrator.setBarcodeImageEnabled(false);
        integrator.setOrientationLocked(true);
        integrator.initiateScan();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null){
            if(result.getContents()==null){
                Toast.makeText(this, "Ticketscan abgebrochen!", Toast.LENGTH_LONG).show();
                Intent change = new Intent(getBaseContext(), MainMenue.class);
                startActivity(change);
            }
            else {
                if ( this.debug )
                    Toast.makeText(this, result.getContents(), Toast.LENGTH_LONG).show();

                String scanres = result.getContents();
                this.qrticketscan = scanres;
                String key = PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getString("saved_api_key", "error");
                String urls = PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getString("saved_server_ip", "error");

                if ( key.contains("error") || urls.contains("error") ) {
                    Toast.makeText(this, "Fehler! Bitte App resetten!", Toast.LENGTH_LONG).show();
                }
                //TODO: remove this:
                //new Net(urls, key, scanres, 5000, this).execute();
                //TODO: edit scanres (add info); target=ShowUserContent
                new Network(urls, key, "POST", scanres, 5000, this, this);
            }
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    //TODO: move to ShowUserContent
    public void showResult(String res) {
        if ( this.debug )
            Toast.makeText(this, res, Toast.LENGTH_LONG).show();
        Intent change = new Intent(getBaseContext(), ShowUserContent.class);
        Bundle b = new Bundle();
        b.putString("ticketdata", res);
        b.putString("scan", this.qrticketscan);
        change.putExtras(b);
        startActivity(change);
    }


    /*public void confirmDel(){
        new AlertDialog.Builder(Scan.this)
                .setTitle("Sicher?")
                .setMessage("Es wird der API-Key und Server Adresse gelöscht! ")
                .setIcon(0)
                .setPositiveButton("Jup", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent change = new Intent(getBaseContext(), Start.class);
                        PreferenceManager.getDefaultSharedPreferences(getBaseContext()).edit().putString("saved_api_key", "default").commit();
                        PreferenceManager.getDefaultSharedPreferences(getBaseContext()).edit().putString("saved_server_ip", "default").commit();
                        startActivity(change);
                    }
                })
                .setNegativeButton("Nope", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // do nothing
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }*/

}

