package ch.ethz.geco.gecocheckin;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class MainMenue extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menue);

        final Button btn_scan = (Button) findViewById(R.id.btn_scan);
        final Button btn_finduser = (Button) findViewById(R.id.btn_findUser);
        final Button btn_rent = (Button) findViewById(R.id.btn_rent);
        final Button btn_findSeat = (Button) findViewById(R.id.btn_findSeat);
        final Button btn_reset = (Button) findViewById(R.id.btn_reset);
        btn_scan.setOnClickListener(new View.OnClickListener() { public void onClick(View v) { scan(); } } );
        btn_finduser.setOnClickListener(new View.OnClickListener() { public void onClick(View v) { finduser(); } } );
        btn_rent.setOnClickListener(new View.OnClickListener() { public void onClick(View v) { rent(); } } );
        btn_findSeat.setOnClickListener(new View.OnClickListener() { public void onClick(View v) { findSeat(); } } );
        btn_reset.setOnClickListener(new View.OnClickListener() { public void onClick(View v) { reset(); } } );
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

    /**
     * Open FindUser view
     */
    private void finduser(){
        Intent change = new Intent(getBaseContext(), SearchUser.class);
        startActivity(change);
    }

    /**
     * Open Rent view
     */
    private void rent(){
        Intent change = new Intent(getBaseContext(), Rent.class);
        startActivity(change);
    }

    /**
     * Open Seat view
     */
    private void findSeat(){
        Intent change = new Intent(getBaseContext(), SearchSeat.class);
        startActivity(change);
    }

    /**
     * Reset App values
     */
    private void reset(){
        PreferenceManager.getDefaultSharedPreferences(getBaseContext()).edit().putString("saved_api_key", "default").commit();
        PreferenceManager.getDefaultSharedPreferences(getBaseContext()).edit().putString("saved_server_ip", "default").commit();
        Intent change = new Intent(getBaseContext(), Start.class);
        startActivity(change);
        finish();
    }


}
