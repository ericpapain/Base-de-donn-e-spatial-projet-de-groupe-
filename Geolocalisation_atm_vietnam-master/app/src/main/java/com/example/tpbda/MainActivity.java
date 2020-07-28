package com.example.tpbda;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.tpbda.com.example.tpbda.view.MapsActivity;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    private DatabaseManager databaseManager;
    private RelativeLayout relativeLayout;
    private ListView listView;
    private ArrayList<String> bank;
    ArrayAdapter<String> bankItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        databaseManager = new DatabaseManager(this);
        bank = databaseManager.getBank();

        LayoutInflater inflater = getLayoutInflater();
        relativeLayout = (RelativeLayout) inflater.inflate(R.layout.activity_main, null);
        setTitle("Choose a Bank");

        setContentView(relativeLayout);

        bankItems = new ArrayAdapter<String>(this, R.layout.items_layout,R.id.item, bank);

        listView = (ListView) findViewById(R.id.listItem);
        listView.setAdapter(bankItems);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                intent.putExtra("_bank", bankItems.getItem(i));
                startActivity(intent);
            }
        });

        databaseManager.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.atm, menu);

        MenuItem menuItem = menu.findItem(R.id.search);
        SearchView searchView =  (SearchView) menuItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                bankItems.getFilter().filter(newText);
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()){
            case R.id.search:
                /*DO SEARCH*/

                return true;
            case R.id.about:
                /*PRINT ABOUT*/

                return true;
            case R.id.help:
                /*PRINT HELP*/

                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
