package com.example.logbook;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static android.R.layout.simple_list_item_1;
import static com.example.logbook.R.color.colorGreen;
import static com.example.logbook.R.color.colorRed;
import static com.example.logbook.R.layout.activity_rekap;

public class RekapActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener{
    ListView listView;
    private BottomNavigationView menu_bawah;
    private RequestQueue mQueue;
    private Context context;
    private ProgressDialog pDialog;

    Calendar myCalendar;
    DatePickerDialog.OnDateSetListener date;

    TextView datedeparture;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(activity_rekap);

        datedeparture = findViewById(R.id.datedeparture);
        String date_now = new SimpleDateFormat("dd-MMMM-yyyy").format(new Date());
        datedeparture.setText(date_now);
        myCalendar = Calendar.getInstance();

        listView = (ListView)findViewById(R.id.listView);

        context = RekapActivity.this;

        pDialog = new ProgressDialog(context);

        mQueue = Volley.newRequestQueue(this);
        pDialog.setMessage("load data");
        showDialog();
        getRekap(date_now);

        hideDialog();
        date = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                TextView tanggal = findViewById(R.id.datedeparture);
                String myFormat = "dd-MMMM-yyyy";
                SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
                tanggal.setText(sdf.format(myCalendar.getTime()));
                getRekap(sdf.format(myCalendar.getTime()));
            }
        };

        datedeparture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(RekapActivity.this, date,
                        myCalendar.get(Calendar.YEAR),
                        myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        toolbar.setBackgroundColor(ContextCompat.getColor(this, colorRed));

        TextView toolbarText = (TextView) findViewById(R.id.toolbar_text);
        if(toolbarText!=null && toolbar!=null) {
            toolbarText.setText("E-LOGBOOK "+Preferences.getLoggedInUser(getBaseContext()));
            setSupportActionBar(toolbar);
        }



        menu_bawah = findViewById(R.id.menu_bawah);
        menu_bawah.setSelectedItemId(R.id.rekap);
        menu_bawah.setOnNavigationItemSelectedListener(this);


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
                //hideDialog();
                break;
        }

        return true;
    }

    private void getRekap(String param_date){
        String url = "http://ws.rsupsoeradji.id/elogbook/web/index.php?r=webservice/getrekap&id="+Preferences.getLoggedPegawaiId(getBaseContext())+"&date="+param_date;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try{
                            JSONArray jsonArray = response.getJSONArray("data");
                            ArrayList<String> list = new ArrayList<String>();

                            for(int i=0;i < jsonArray.length();i++){
                                JSONObject prof = jsonArray.getJSONObject(i);
                                String tugas = prof.getString("tugas");
                                String jumlah = prof.getString("jumlah");
                                String status = prof.getString("status");
                                String tugastext = tugas + " ( "+ jumlah + " ) " + " ( "+status+" )";


                                list.add(tugastext);
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
        //hideDialog();
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
