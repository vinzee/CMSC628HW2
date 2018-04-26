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
public class Register extends AppCompatActivity implements View.OnClickListener {

    private EditText edtUser, edtPassword, edtPasswordConf;
    private Button btnLogin, btnRegister;
    private String username, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_register);

        edtUser = findViewById(R.id.username);
        edtPassword = findViewById(R.id.password);
        edtPasswordConf = findViewById(R.id.password_confirmation);

        btnLogin = findViewById(R.id.login);
        btnLogin.setOnClickListener(this);
        btnRegister = findViewById(R.id.register);
        btnRegister.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.register:
                username = edtUser.getText().toString().trim();
                password = edtPassword.getText().toString().trim();
                String passwordConf = edtPasswordConf.getText().toString().trim();

                if (username.equals("")) {
                    edtUser.setError( "User Name is required." );
                    return;
                }

                if (password.equals("")) {
                    edtPassword.setError( "Password is required." );
                    return;
                }

                if (!password.equals(passwordConf)) {
                    edtPasswordConf.setError( "Password confirmation does not match password." );
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
            case R.id.login:
                Intent intent = new Intent(Register.this, MainActivity.class);
                Bundle b = new Bundle();
                b.putString("username", username);
                intent.putExtras(b);
                Register.this.startActivity(intent);
                break;
        }
    }

    private class WebserviceAsyncTask extends AsyncTask<JSONObject, Integer, String[]> {

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
            Toast.makeText(Register.this, s[1].toString(), Toast.LENGTH_SHORT).show();

            if(s[0] == "true"){
                Intent intent = new Intent(Register.this, MainActivity.class);
                Bundle b = new Bundle();
                b.putString("username", username);
                b.putString("password", password);
                intent.putExtras(b);
                Register.this.startActivity(intent);
            }
        }

        @Override
        protected String[] doInBackground(JSONObject... jsonObjects) {
            Log.w("WebserviceAsyncTask","doInBackground");

            try {

                URL url = new URL(Constants.BASE_URL + "/register");

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

                    String temp = "";
                    while ((temp = bread.readLine()) != null) {
                        line.append(temp);
                    }
                    bread.close();

//                    JSONObject object = new JSONObject(line.toString());

                    Log.d("WebserviceAsyncTask","executed successfully!" + line);

                    return new String[]{"true", "User Registered Successfully!"};
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