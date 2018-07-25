package ch.ethz.geco.gecocheckin;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class Scan extends AppCompatActivity {

    private ProgressDialog dialog;
    private boolean debug;
    private String caller;

    /**
     * Setup scanner
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.debug = PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getBoolean("saved_debug_status", false);

        Bundle b = getIntent().getExtras();
        this.caller = null;
        if(b != null) {
            this.caller = b.getString("caller");
        }

        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
        integrator.setPrompt("");
        integrator.setCameraId(0);
        integrator.setBeepEnabled(true);
        integrator.setBarcodeImageEnabled(false);
        integrator.setOrientationLocked(true);
        integrator.initiateScan();

    }

    /**
     * Works with resuklt when scan is finished
     * @param requestCode
     * @param resultCode
     * @param data
     */
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

                Intent change = null;
                try {
                    change = new Intent(getBaseContext(), Class.forName(this.caller));
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                Bundle b = new Bundle();
                b.putString("scan", scanres);
                change.putExtras(b);
                startActivity(change);
                finish();
            }
        }
        else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}

