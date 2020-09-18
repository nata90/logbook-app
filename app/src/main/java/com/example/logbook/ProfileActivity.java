package com.example.logbook;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static android.R.layout.simple_list_item_1;
import static com.example.logbook.R.color.colorRed;
import static com.example.logbook.R.layout.activity_profile;

public class ProfileActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener{
    ListView listView;
    ArrayAdapter<CharSequence> adapter;
    private RequestQueue mQueue;
    private BottomNavigationView menu_bawah;
    private ProgressDialog pDialog;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(activity_profile);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Button buttonLogout = findViewById(R.id.logout);
        TextView textNama = findViewById(R.id.text_nama);
        context = ProfileActivity.this;

        pDialog = new ProgressDialog(context);

        toolbar.setBackgroundColor(ContextCompat.getColor(this, colorRed));
       // buttonLogout.setBackgroundColor(ContextCompat.getColor(this, colorRed));
        textNama.setText(Preferences.getLoggedInUser(getBaseContext()));

        TextView toolbarText = (TextView) findViewById(R.id.toolbar_text);
        if(toolbarText!=null && toolbar!=null) {
            toolbarText.setText("E-LOGBOOK "+Preferences.getLoggedInUser(getBaseContext()));
            setSupportActionBar(toolbar);
        }

        listView = (ListView)findViewById(R.id.listView);

        mQueue = Volley.newRequestQueue(this);
        getProfile();

        menu_bawah = findViewById(R.id.menu_bawah);
        menu_bawah.setSelectedItemId(R.id.home);
        menu_bawah.setOnNavigationItemSelectedListener(this);

        /*buttonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pDialog.setMessage("logout Process...");
                showDialog();
                Preferences.clearLoggedInUser(getBaseContext());
                Intent intent = new Intent(context, LoginActivity.class);
                startActivity(intent);
                finish();
                hideDialog();
            }
        });*/
    }

    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        switch (menuItem.getItemId()){
            case R.id.home:
                Intent intent = new Intent(getBaseContext(), ProfileActivity.class);
                startActivity(intent);
                finish();
                break;
            case R.id.logbook:
                intent = new Intent(getBaseContext(), MainActivity.class);
                startActivity(intent);
                finish();
                break;
            case R.id.rekap:
                intent = new Intent(getBaseContext(), RekapActivity.class);
                startActivity(intent);
                finish();
                break;
            case R.id.logout:
                pDialog.setMessage("logout Process...");
                showDialog();
                Preferences.clearLoggedInUser(getBaseContext());
                intent = new Intent(context, LoginActivity.class);
                startActivity(intent);
                finish();
                break;
        }

        return true;
    }

    private void getProfile(){
        String url = "https://hananfr.online/elogbook/web/index.php?r=webservice/profile&id="+Preferences.getLoggedPegawaiId(getBaseContext());
        //String url = "https://hananfr.online/elogbook/web/index.php?r=webservice/profile&id=183";

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try{
                            JSONArray jsonArray = response.getJSONArray("data");
                            ArrayList<String> list = new ArrayList<String>();

                            for(int i=0;i < jsonArray.length();i++){
                                JSONObject prof = jsonArray.getJSONObject(i);
                                String value = prof.getString("value");

                                list.add(value);
                            }

                            ArrayAdapter adapter = new ArrayAdapter(getApplicationContext(),
                                    simple_list_item_1 ,
                                    list);


                            listView.setAdapter(adapter);
                        }catch (JSONException e){
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });

        mQueue.add(request);
    }

    private void showDialog(){
        if(!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog(){
        if(pDialog.isShowing())
            pDialog.dismiss();
    }

}
