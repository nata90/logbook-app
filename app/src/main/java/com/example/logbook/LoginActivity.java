package com.example.logbook;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static android.R.layout.simple_list_item_1;
import static com.android.volley.Request.*;
import static com.android.volley.toolbox.Volley.*;
import static com.example.logbook.R.layout.activity_login;

public class LoginActivity extends AppCompatActivity {

    private EditText editTextUsername;
    private EditText editTextPassword;
    private Context context;
    private AppCompatButton buttonLogin;
    private ProgressDialog pDialog;


    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(activity_login);
        context = LoginActivity.this;

        if(Preferences.getLoggedInStatus(getBaseContext())){
            Intent intent = new Intent(getBaseContext(), ProfileActivity.class);
            startActivity(intent);
            finish();
        }

        pDialog = new ProgressDialog(context);
        editTextUsername = (EditText) findViewById(R.id.username);
        editTextPassword = (EditText) findViewById(R.id.password);

        buttonLogin = (AppCompatButton) findViewById(R.id.login);

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login();
            }
        });

    }

    private void login(){
        final String username = editTextUsername.getText().toString().trim();
        final String password = editTextPassword.getText().toString().trim();

        pDialog.setMessage("login Process...");
        showDialog();

        String url = "https://hananfr.online/elogbook/web/index.php?r=webservice/login";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if(response.contains("gagal")){
                            hideDialog();
                            Toast.makeText(context, "Invalid username or password", Toast.LENGTH_LONG).show();
                        }else{
                            hideDialog();
                            String[] arr_split = response.split("\\#",-1);
                            Preferences.setLoggedInUser(getBaseContext(),arr_split[0]);
                            Preferences.setLoggedPegawaiId(getBaseContext(),arr_split[1]);
                            Preferences.setLoggedUnitKerja(getBaseContext(),arr_split[2]);
                            Preferences.setLoggedInStatus(getBaseContext(),true);
                            gotoMainActivity();
                        }

                    }
                },
                new Response.ErrorListener(){
                    public void onErrorResponse(VolleyError error){
                        hideDialog();
                        Toast.makeText(context, "The Server unreachable", Toast.LENGTH_SHORT).show();
                    }
                }){

                protected Map<String, String> getParams() throws AuthFailureError{
                    Map<String, String> params = new HashMap<>();
                    params.put("username", username);
                    params.put("password",password);

                    return params;
                }
        };

        Volley.newRequestQueue(this).add(stringRequest);
    }

    private void gotoMainActivity(){
        Intent intent = new Intent(getBaseContext(), ProfileActivity.class);
        startActivity(intent);
        finish();
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
