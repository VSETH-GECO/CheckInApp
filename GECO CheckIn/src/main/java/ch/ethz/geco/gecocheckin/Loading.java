package ch.ethz.geco.gecocheckin;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by felunka on 19.10.2017.
 */

public class Loading {

    private AppCompatActivity dialogTarget;
    private AppCompatActivity afterLoadingTarget;
    private ProgressDialog dialog;

    /**
     *
     * @param dialogTarget
     * @param afterLoadingTarget
     */
    Loading(AppCompatActivity dialogTarget, AppCompatActivity afterLoadingTarget) {
        this.dialogTarget = dialogTarget;
        this.afterLoadingTarget = afterLoadingTarget;
    }

    public void show(){
        dialog = new ProgressDialog(this.dialogTarget); // this = YourActivity
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setMessage("Lade Daten...");
        dialog.setIndeterminate(true);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    public void done(){
        dialog.hide();
    }
}
