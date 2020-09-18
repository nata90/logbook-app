package com.example.logbook;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.AppCompatActivity;

import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import static android.R.layout.simple_list_item_1;
import static com.example.logbook.R.color.colorRed;
import static com.example.logbook.R.color.colorGreen;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {
    private TextView mTextViewResult;
    private RequestQueue mQueue;
    private Spinner spintugas;
    private EditText editTextDeskripsi;
    private EditText editTextJumlah;
    private Context context;
    private ProgressDialog pDialog;
    private BottomNavigationView menu_bawah;

    Calendar myCalendar;
    DatePickerDialog.OnDateSetListener date;
    EditText datelogbook;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parse);

        Button buttonSubmit = findViewById(R.id.button_parse);

        datelogbook = findViewById(R.id.datelogbook);
        String date_now = new SimpleDateFormat("dd-MMMM-yyyy").format(new Date());
        datelogbook.setText(date_now);
        myCalendar = Calendar.getInstance();

        date = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                TextView tanggal = findViewById(R.id.datelogbook);
                String myFormat = "dd MMMM yyyy";
                SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
                tanggal.setText(sdf.format(myCalendar.getTime()));
            }
        };

        datelogbook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(MainActivity.this, date,
                        myCalendar.get(Calendar.YEAR),
                        myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setBackgroundColor(ContextCompat.getColor(this, colorRed));
        buttonSubmit.setBackgroundColor(ContextCompat.getColor(this, colorGreen));

        context = MainActivity.this;

        pDialog = new ProgressDialog(context);

        spintugas = (Spinner) findViewById(R.id.drop_tugas);
        editTextDeskripsi = (EditText) findViewById(R.id.deskripsi);
        editTextJumlah = (EditText) findViewById(R.id.jumlah);

        TextView toolbarText = (TextView) findViewById(R.id.toolbar_text);
        if(toolbarText!=null && toolbar!=null) {
            toolbarText.setText("E-LOGBOOK "+Preferences.getLoggedInUser(getBaseContext()));
            setSupportActionBar(toolbar);
        }

        mQueue = Volley.newRequestQueue(this);

        getTugas();
        buttonSubmit.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                addTugas();
            }
        });

        menu_bawah = findViewById(R.id.menu_bawah);
        menu_bawah.setSelectedItemId(R.id.logbook);
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


    private void getTugas(){
        String url = "https://hananfr.online/elogbook/web/index.php?r=webservice/datatugas&id="+Preferences.getLoggedUnitKerja(getBaseContext());

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try{
                            JSONArray jsonArray = response.getJSONArray("data");
                            ArrayList<String> list = new ArrayList<String>();

                            Spinner spin = (Spinner) findViewById(R.id.drop_tugas);
                            for(int i=0;i < jsonArray.length();i++){
                                JSONObject tugas = jsonArray.getJSONObject(i);
                                String id = tugas.getString("id");
                                String namatugas = tugas.getString("tugas");
                                String tugastext = id + " | "+ namatugas;

                                list.add(tugastext);
                            }

                            ArrayAdapter adapter = new ArrayAdapter(getApplicationContext(),
                                    simple_list_item_1 ,
                                    list);


                            spin.setAdapter(adapter);
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

    private void addTugas(){
        final String tugas = spintugas.getSelectedItem().toString().trim();
        final String deskripsi = editTextDeskripsi.getText().toString().trim();
        final String jumlah = editTextJumlah.getText().toString().trim();
        final String tanggal =  datelogbook.getText().toString().trim();
        final String pegawai = Preferences.getLoggedPegawaiId(getBaseContext());

        class AddTugas extends AsyncTask<Void,Void,String>{
            ProgressDialog loading;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                loading = ProgressDialog.show(MainActivity.this,"Menambahkan...","Tunggu...",false,false);
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                loading.dismiss();
                Toast.makeText(MainActivity.this,s,Toast.LENGTH_LONG).show();
            }

            @Override
            protected String doInBackground(Void... voids) {
                HashMap<String,String> params = new HashMap<>();
                params.put("tugas",tugas);
                params.put("deskripsi",deskripsi);
                params.put("jumlah",jumlah);
                params.put("idpeg",pegawai);
                params.put("tanggal",tanggal);

                RequestHandler rh = new RequestHandler();
                String res = rh.sendPostRequest("https://hananfr.online/elogbook/web/index.php?r=webservice/simpankinerja", params);
                return res;
            }
        }

        AddTugas ae = new AddTugas();
        ae.execute();
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