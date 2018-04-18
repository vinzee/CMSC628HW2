package com.example.vinzee.cmsc628hw2;

import android.content.Context;
import android.content.Intent;
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
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private EditText edtUser;
    private Button btnLogin, btnMaps;
    private WebserviceAsyncTask mytask;

    private static final String TAG ="MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        edtUser = (EditText) findViewById(R.id.username);

        btnLogin = (Button) findViewById(R.id.login);
        btnLogin.setOnClickListener(this);
        btnMaps = (Button) findViewById(R.id.maps);
        btnMaps.setOnClickListener(this);

        ConnectivityManager cm =
                (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        Log.d("isConnected: ", isConnected + "");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.login:
                JSONObject params = new JSONObject();

                try {
                    params.put("username", edtUser.getText().toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                Log.d("Params", params.toString());

                mytask = new WebserviceAsyncTask();
                mytask.execute(params);
                break;
            case R.id.maps:
                Log.w("onClick", "opening map");
                Intent myIntent = new Intent(MainActivity.this, MapsActivity.class);
                MainActivity.this.startActivity(myIntent);
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
            Log.w("WebserviceAsyncTask","onPostExecute called");
            Toast.makeText(MainActivity.this, s[1].toString(), Toast.LENGTH_SHORT).show();

            if(s[0] == "true"){
                Intent myIntent = new Intent(MainActivity.this, MapsActivity.class);
                MainActivity.this.startActivity(myIntent);
            }
        }

        @Override
        protected String[] doInBackground(JSONObject... jsonObjects) {
            Log.d("doInBackground", "started");
            try {
                Log.d("doInBackground", Constants.BASE_URL + "/login");

                URL url = new URL(Constants.BASE_URL + "/login");

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setReadTimeout(5000);
                connection.setConnectTimeout(5000);
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

                    String temp = "";
                    while ((temp = bread.readLine()) != null) {
                        line.append(temp);
                    }
                    bread.close();

//                    JSONObject object = new JSONObject(line.toString());

                    Log.d("response: ","executed successfully!" + line);

                    return new String[]{"true", "User logged in!"};
                } else {
                    return new String[] {"false", "Invalid Request: " + connection.getResponseCode()};
                }
            } catch (Exception e) {
                Log.d("WebserviceAsyncTask: ", "Exception");
                e.printStackTrace();
            }

            return null;
        }
    }
}