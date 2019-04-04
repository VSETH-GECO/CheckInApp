package ch.ethz.geco.gecocheckin;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ch.ethz.geco.g4j.impl.DefaultGECoClient;
import ch.ethz.geco.g4j.obj.BorrowedItem;
import ch.ethz.geco.g4j.obj.GECoClient;
import ch.ethz.geco.g4j.obj.LanUser;
import ch.ethz.geco.g4j.obj.Seat;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class Rent extends AppCompatActivity {

    private ArrayAdapter<String> adapter;
    private LanUser user;
    private HashMap<Integer, Long> itemid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rent);

        //Setup Button action listener
        final Button btn_go = (Button) findViewById(R.id.btn_go);
        btn_go.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                search();
            }
        });
        final Button btn_save = (Button) findViewById(R.id.btn_save);
        btn_save.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                save();
            }
        });
        final Button btn_scan = (Button) findViewById(R.id.btn_scan);
        btn_scan.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent change = new Intent(getBaseContext(), Scan.class);
                Bundle b = new Bundle();
                b.putString("caller", Rent.class.getCanonicalName());
                change.putExtras(b);
                startActivity(change);
            }
        });

        //setup Listview with datastructure and adapter
        final ListView listView = (ListView) findViewById(R.id.viw_list);
        ArrayList<String> lst = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, android.R.id.text1, lst);

        //hashmap to translate list position to item id in database
        itemid = new HashMap<>();

        //assing list adapter
        listView.setAdapter(adapter);

        //setup action listener for list item tapping
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {

                new AlertDialog.Builder(Rent.this)
                        .setTitle("Löschen")
                        .setMessage("Soll Gegenstand gelöscht werden?")
                        .setIcon(0)
                        .setPositiveButton("Jup", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                delete(position);
                                adapter.remove(adapter.getItem(position));
                            }
                        })
                        .setNegativeButton("Nope", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                //nothing
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }

        });

        //read result if ticket got scanned
        Bundle b = getIntent().getExtras();
        String extra = "";
        if (b != null) {
            extra = b.getString("scan");
            try {
                //parse qr to json object
                JsonParser parser = new JsonParser();
                JsonObject scanres = (JsonObject) parser.parse(extra);
                GECoClient client = new DefaultGECoClient(PreferenceManager.getDefaultSharedPreferences(this.getBaseContext()).getString("saved_api_key", "error"));
                Mono<LanUser> monoLanUser = client.getLanUserByID((long) scanres.get("id").getAsInt());
                monoLanUser.doOnError(Throwable::printStackTrace).subscribe(lanUser -> {
                    this.user = lanUser;
                    runOnUiThread(() -> {
                        search();
                        Loading.instance.done();
                    });
                });
                new Loading(this).show();
                search();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Lookup User items. Request depends on form used
     */
    private void search() {
        EditText userName = (EditText) findViewById(R.id.txt_username);
        EditText seatEditText = (EditText) findViewById(R.id.txt_seat);
        GECoClient client = new DefaultGECoClient(PreferenceManager.getDefaultSharedPreferences(this.getBaseContext()).getString("saved_api_key", "error"));
        if (userName.getText().toString().length() > 0) {
            Mono<LanUser> monoLanUser = client.getLanUserByName(userName.getText().toString());
            monoLanUser.doOnError(Throwable::printStackTrace).subscribe(lanUser -> {
                this.user = lanUser;
                runOnUiThread(() -> {
                    Loading.instance.done();
                    searchItems();
                });
            });
            new Loading(this).show();
        } else if (seatEditText.getText().toString().length() > 0) {
            Mono<Seat> monoSeat = client.getSeatByName(seatEditText.getText().toString());
            monoSeat.doOnError(Throwable::printStackTrace).subscribe(seat -> {
                if(seat.getUserName().isPresent())
                    client.getLanUserByName(seat.getUserName().get()).subscribe(lanUser -> {
                        this.user = lanUser;
                        runOnUiThread(() -> {
                            Loading.instance.done();
                            searchItems();
                        });
                    });

            });
            new Loading(this).show();
        }

        if (this.user != null) {
            searchItems();
        }
    }

    private void searchItems() {
        adapter.clear();
        Flux<BorrowedItem> fluxItems = this.user.getBorrowedItems();
        fluxItems.collectList().subscribe(borrowedItems -> {
            runOnUiThread(() -> {listItems(borrowedItems);});
        });
    }

    private void listItems(List<BorrowedItem> borrowedItems) {
        int i = 0;
        for(BorrowedItem item : borrowedItems) {
            adapter.add(item.getName());
          itemid.put(i, item.getID());
        i++;
        }
    }

    /**
     * Save a new Item to database
     */
    private void save() {
        if (this.user == null) {
            Toast.makeText(Rent.this, "Bitte gibt einen Usernamen oder Platz an!", Toast.LENGTH_LONG).show();
            return;
        }
        EditText name = (EditText) findViewById(R.id.txt_propName);
        if (name.getText().toString().length() > 0) {
            this.user.borrowItem(name.getText().toString());
            Mono<BorrowedItem> itemMono = this.user.borrowItem(name.getText().toString());
            itemMono.subscribe(item -> {
                runOnUiThread(() -> {search();});
            });
        } else {
            Toast.makeText(Rent.this, "Bitte gibt einen Namen ein!", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Delete Item from database
     * @param itemPos
     */
    private void delete(int itemPos) {
        final long itemId = itemid.get(itemPos);
        this.user.getBorrowedItemByID(itemId).subscribe(borrowedItem -> borrowedItem.remove().subscribe());
        search();
    }

}
