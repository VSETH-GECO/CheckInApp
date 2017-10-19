package ch.ethz.geco.gecocheckin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainMenue extends NetworkActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menue);

        final Button button = (Button) findViewById(R.id.btn_scan);
        button.setOnClickListener(new View.OnClickListener() { public void onClick(View v) { scan(); } } );
        //TODO: Action listener for 3 more buttons
    }

    /**
     * Open Scan view
     */
    private void scan(){
        //TODO: implement
        Intent change = new Intent(getBaseContext(), Scan.class);
        startActivity(change);
    }

    public void showResult(String res){
        //TODO: implement
    }
}
