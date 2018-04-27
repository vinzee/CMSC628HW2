package com.example.vinzee.cmsc628hw2;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private EditText edtUser, edtPassword;
    private Button btnLogin, btnRegister;
    private String username, password;
    private SharedPreferences sharedpreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedpreferences = getSharedPreferences("MyPREFERENCES", Context.MODE_PRIVATE);

        if (sharedpreferences.contains("username")) {
            username = sharedpreferences.getString("username", "");
            password = sharedpreferences.getString("password", "");

            if (!username.equals("")) {
                JSONObject params = new JSONObject();

                try {
                    params.put("username", username);
                    params.put("password", password);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                Log.d("Params", params.toString());

                new WebserviceAsyncTask().execute(params);

            }
        }

        setContentView(R.layout.activity_main);

        edtUser = findViewById(R.id.username);
        edtPassword = findViewById(R.id.password);

        btnLogin = findViewById(R.id.login);
        btnLogin.setOnClickListener(this);
        btnRegister = findViewById(R.id.register);
        btnRegister.setOnClickListener(this);

        ConnectivityManager cm = (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        Log.d("isConnected: ", isConnected + "");

        if (!isConnected) {
            Toast.makeText(MainActivity.this, "No internet connectivity", Toast.LENGTH_SHORT).show();
        }

        Bundle b = getIntent().getExtras();

        if (b != null) {
            if (b.containsKey("username")){
                edtUser.setText(b.getString("username"));
            }

            if (b.containsKey("password")){
                edtPassword.setText(b.getString("password"));
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.login:
                username = edtUser.getText().toString().trim();
                password = edtPassword.getText().toString().trim();

                if (username.equals("")) {
                    edtUser.setError( "User Name is required." );
                    return;
                }

                if (password.equals("")) {
                    edtPassword.setError( "Password is required." );
                    return;
                }

                JSONObject params = new JSONObject();

                try {
                    params.put("username", username);
                    params.put("password", password);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                Log.d("Params", params.toString());

                new WebserviceAsyncTask().execute(params);

                break;
            case R.id.register:
                Intent intent = new Intent(MainActivity.this, Register.class);
                Bundle b = new Bundle();
                b.putString("username", username);
                intent.putExtras(b);
                MainActivity.this.startActivity(intent);

                break;

        }
    }

    private class WebserviceAsyncTask extends AsyncTask<JSONObject, Integer, String[]>{

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String[] s) {
            super.onPostExecute(s);
            Log.w("WebserviceAsyncTask","onPostExecute");
            Toast.makeText(MainActivity.this, s[1].toString(), Toast.LENGTH_SHORT).show();

            if(s[0] == "true"){
                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putString("username", username);
                editor.putString("password", password);
                editor.commit();

                Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                Bundle b = new Bundle();
                b.putString("username", username);
                intent.putExtras(b);
                MainActivity.this.startActivity(intent);
            }
        }

        @Override
        protected String[] doInBackground(JSONObject... jsonObjects) {
            Log.w("WebserviceAsyncTask","doInBackground");

            try {

                URL url = new URL(Constants.BASE_URL + "/login");

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setReadTimeout(5000);
                connection.setConnectTimeout(3000);
                connection.setRequestMethod("POST");
                connection.setDoInput(true);
                connection.setChunkedStreamingMode(0);
                connection.setRequestProperty("Content-Type", "application/json;charset=utf-8");
                connection.setRequestProperty("X-Requested-With", "XMLHttpRequest");
                connection.connect();

                Writer writer = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
                writer.write(jsonObjects[0].toString());
                writer.close();

                if(connection.getResponseCode() == HttpURLConnection.HTTP_OK)   {
                    StringBuilder line = new StringBuilder();
                    BufferedReader bread = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                    String temp;
                    while ((temp = bread.readLine()) != null) {
                        line.append(temp);
                    }
                    bread.close();

//                    JSONObject object = new JSONObject(line.toString());

                    Log.d("WebserviceAsyncTask","executed successfully!" + line);

                    return new String[]{"true", "User logged In !"};
                } else {
                    Log.d("WebserviceAsyncTask", "Invalid Request: " + connection.getResponseCode() + " , " + connection.getResponseMessage());

                    return new String[] {"false", "Error: " + connection.getResponseMessage()};
                }
            } catch (IOException e) {
                Log.d("WebserviceAsyncTask: ", "IOException");
                e.printStackTrace();
                return new String[] {"false", "IOException: " + e.getMessage()};
            } catch (Exception e) {
                Log.d("WebserviceAsyncTask: ", "Exception");
                e.printStackTrace();
                return new String[]{"false", "Exception: " + e.getMessage()};
            }
        }
    }
}