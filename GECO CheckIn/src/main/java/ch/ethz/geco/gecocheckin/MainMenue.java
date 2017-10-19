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

        final Button btn_scan = (Button) findViewById(R.id.btn_scan);
        final Button btn_rent = (Button) findViewById(R.id.btn_rent);
        btn_scan.setOnClickListener(new View.OnClickListener() { public void onClick(View v) { scan(); } } );
        btn_rent.setOnClickListener(new View.OnClickListener() { public void onClick(View v) { rent(); } } );
        //TODO: Action listener for 2 more buttons
    }

    /**
     * Open Scan view
     */
    private void scan(){
        Intent change = new Intent(getBaseContext(), Scan.class);
        Bundle b = new Bundle();
        b.putString("caller", ShowUserContent.class.getCanonicalName());
        change.putExtras(b);
        startActivity(change);
    }

    private void rent(){
        Intent change = new Intent(getBaseContext(), Rent.class);
        startActivity(change);
    }

    public void showResult(String res){
        //TODO: implement
    }
}
