package com.example.vinzee.cmsc628hw2;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Timestamp;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private EditText edtUser, edtLat, edtLon, edtTimestmp;
    private Button btnSave, btnMaps;
    private WebserviceAsyncTask mytask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        edtUser = (EditText) findViewById(R.id.username);
        edtLat = (EditText) findViewById(R.id.latitude);
        edtLon = (EditText) findViewById(R.id.longitude);
        edtTimestmp = (EditText) findViewById(R.id.timestamp);
        btnSave = (Button) findViewById(R.id.save);
        btnSave.setOnClickListener(this);
        btnMaps = (Button) findViewById(R.id.maps);
        btnMaps.setOnClickListener(this);

        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        System.out.println(timestamp);
        edtTimestmp.setText(timestamp.toString());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.save:
                String[] params = new String[9];
                params[0] = "http://130.85.243.244:3000/nearbyfriends";
                params[1] = "Latitude";
                params[3] = "Longitude";
                params[5] = "Userid";
                params[7] = "Timestamp";
                params[2] = edtLat.getText().toString();
                params[4] = edtLon.getText().toString();
                params[6] = edtUser.getText().toString();
                params[8] = edtTimestmp.getText().toString();

                Log.w("time.", " " + Arrays.toString(params));

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

    private class WebserviceAsyncTask extends AsyncTask<String, Integer, String>{

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.w("msg ofter ex: ","Executed");
        }

        private String getParamsString(String[] params){
            int numkeyvalue = (params.length - 1);
            String paramsString = "";

            for(int i = 1;i < numkeyvalue; i+=2){
                if (i == numkeyvalue-3) paramsString = params[i]+"="+params[i+1];
                else paramsString = params[i]+"="+params[i+1]+"&";
            }

            return paramsString;
        }


        @Override
        protected String doInBackground(String... params) {
            System.out.println("doInBackground started");
            HttpURLConnection client = null;

            try {
                client = (HttpURLConnection) new URL(params[0]).openConnection();
                client.setRequestMethod("GET");
                client.setDoOutput(true);

                String urlparameters = getParamsString(params);
                OutputStream outputpost = new BufferedOutputStream(client.getOutputStream());

                outputpost.write(urlparameters.getBytes());
                outputpost.flush();
                outputpost.close();

                if(client.getResponseCode() == HttpURLConnection.HTTP_OK)   {
                    String line = "";
                    BufferedReader bread = new BufferedReader(new  InputStreamReader(client.getInputStream()));

                    while ((line = bread.readLine()) != null)   {
                        line += bread.readLine();
                    }

                    bread.close();

                    JSONObject object = new JSONObject(line);

                    if(new Integer(object.getString("Result")) == 1)  {
                        Log.w("response: ","executed successfully!");
                    }else   {
                        Log.w("response: ","execution failed!");
                    }
                }

                Toast.makeText(MainActivity.this,"Message sent",Toast.LENGTH_SHORT);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }
    }
}