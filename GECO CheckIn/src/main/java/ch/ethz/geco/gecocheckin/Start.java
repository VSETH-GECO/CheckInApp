package ch.ethz.geco.gecocheckin;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

public class Start extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        String key = PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getString("saved_api_key", "default");
        System.out.println("keyvalue: " + key);
        if ( !key.equals("default")  ) {
            Intent change = new Intent(getBaseContext(), Scan.class);
            startActivity(change);
        }


        final Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                EditText keyeingabe = (EditText)findViewById(R.id.keyeingabe);
                EditText server_ip = (EditText) findViewById(R.id.server);
                CheckBox debug = (CheckBox) findViewById(R.id.debug_box) ;

                PreferenceManager.getDefaultSharedPreferences(getBaseContext()).edit().putBoolean("saved_debug_status", debug.isChecked()).commit();
                PreferenceManager.getDefaultSharedPreferences(getBaseContext()).edit().putString("saved_api_key", keyeingabe.getText().toString()).commit();
                PreferenceManager.getDefaultSharedPreferences(getBaseContext()).edit().putString("saved_server_ip", server_ip.getText().toString()).commit();
                Intent change = new Intent(getBaseContext(), Scan.class);
                startActivity(change);
            }
        });
    }

}


