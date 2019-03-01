package ch.ethz.geco.gecocheckin;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by felunka on 19.10.2017.
 */

public class Loading {

    public static Loading instance;

    private AppCompatActivity dialogTarget;
    private ProgressDialog dialog;

    /**
     * Initialize vars
     * @param dialogTarget
     */
    Loading(AppCompatActivity dialogTarget) {
        this.dialogTarget = dialogTarget;
        Loading.instance = this;
    }

    /**
     * Show the Loading dialog
     */
    public void show(){
        dialog = new ProgressDialog(this.dialogTarget);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setMessage("Lade Daten...");
        dialog.setIndeterminate(true);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    /**
     * Hide dialog and change Activity
     */
    public void done(){
        dialog.hide();
    }
}
