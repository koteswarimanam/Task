package com.example.task;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class SignUpActivity extends AppCompatActivity {

    EditText et_firstName,et_lastName,et_password,et_phoneNumber;
    Button btn_signUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        et_firstName = findViewById(R.id.et_firstName);
        et_lastName = findViewById(R.id.et_lastName);
        et_password = findViewById(R.id.et_password);
        et_phoneNumber = findViewById(R.id.et_phoneNumber);
        btn_signUp = findViewById(R.id.btn_signUp);

        btn_signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(et_firstName.getText().toString().isEmpty()){
                    et_firstName.requestFocus();
                    Toast.makeText(SignUpActivity.this,"Enter first name",Toast.LENGTH_SHORT).show();
                }else if(et_lastName.getText().toString().isEmpty()){
                    et_lastName.requestFocus();
                    Toast.makeText(SignUpActivity.this,"Enter last name",Toast.LENGTH_SHORT).show();
                }else if(et_password.getText().toString().isEmpty()){
                    et_password.requestFocus();
                    Toast.makeText(SignUpActivity.this,"Enter password",Toast.LENGTH_SHORT).show();
                }else if(et_phoneNumber.getText().toString().isEmpty()){
                    et_phoneNumber.requestFocus();
                    Toast.makeText(SignUpActivity.this,"Enter phone number",Toast.LENGTH_SHORT).show();
                }else{
                    signingUp signing = new signingUp();
                    signing.execute();
                }
            }
        });

    }

    private class signingUp extends AsyncTask<String, String, String> {

        private String resp;
        ProgressDialog progressDialog;

        @Override
        protected String doInBackground(String... params) {
            publishProgress("Sleeping..."); // Calls onProgressUpdate()
            HttpURLConnection urlConnections;
            StringBuilder result = new StringBuilder();
            BufferedReader reader;
            OutputStream out = null;


            try {
                URL checkUrl = new URL("https://www.mgnapp.net/usrRegister?"+"first_name="+et_firstName.getText().toString()+"&last_name="+et_lastName.getText().toString()+"&password="+et_password.getText().toString()+"&mobile="+et_phoneNumber.getText().toString());

                urlConnections = (HttpURLConnection) checkUrl.openConnection();
                urlConnections.setConnectTimeout(20000);
                urlConnections.setReadTimeout(20000);
                urlConnections.setRequestMethod("POST");
               // urlConnections.setRequestProperty("Accept", "application/json");
                urlConnections.setRequestProperty("Content-type", "application/json");
                out = new BufferedOutputStream(urlConnections.getOutputStream());





                int resCode = urlConnections.getResponseCode();
                if (resCode == HttpsURLConnection.HTTP_OK) {

                    InputStreamReader inp = new InputStreamReader(urlConnections.getInputStream());
                    reader = new BufferedReader(new InputStreamReader(urlConnections.getInputStream()));
                    Log.w("URL : ", checkUrl.toString());
                    Log.w("URL Connection : ", urlConnections.toString());
                    Log.w("Reader : ", reader.toString());

                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }
                } else {
                    reader = new BufferedReader(new InputStreamReader(urlConnections.getErrorStream()));
                    Log.w("GetPatient URL : ", checkUrl.toString());
                    Log.w("URL Connection : ", urlConnections.toString());
                    Log.w("Reader : ", reader.toString());

                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                        if (result.toString().contains("Fails")) {
                            return "Invalid Username and Password";
                        }
                    }
                }
            } catch (MalformedURLException e) {
                // handle invalid URL
                Log.e("Error ", e.toString());
            } catch (SocketTimeoutException e) {
                // hadle timeout
                Log.e("Error ", e.toString());
            } catch (IOException e) {
                // handle I/0
                Log.e("Error ", e.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return params.toString();
            //return   result.toString();
        }

        @Override
        protected void onPostExecute(String result) {
            // execution of result of Long time consuming operation
            progressDialog.dismiss();
           /* if (result!= null) {
                matchedPercentage = Double.parseDouble(result);
                if (matchedPercentage > 0){
                    ll_comparisionResultView.setVisibility(View.VISIBLE);
                    int percentage = (int) matchedPercentage;
                    tv_matching_percentage.setText("Cattle face matching percentage is: "+percentage);
                    loadBarGraph(percentage);
                }else {
                    ll_comparisionResultView.setVisibility(View.GONE);
                    Toast.makeText(FAceRecognitionActivity.this,"Cattle face is not matched",Toast.LENGTH_SHORT).show();
                    tv_compare_btn.setText("Retry cattle face");
                    imageUploaded = false;
                    matchingRate = 0;

                }
            }else {
                Toast.makeText(FAceRecognitionActivity.this,"Cattle face is not matched",Toast.LENGTH_SHORT).show();
            }*/
             Toast.makeText(SignUpActivity.this,result,Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(SignUpActivity.this,
                    "",
                    "Signing..");
        }


        @Override
        protected void onProgressUpdate(String... text) {
            //  finalResult.setText(text[0]);

        }
    }

    public void signingUp(){





    }
}