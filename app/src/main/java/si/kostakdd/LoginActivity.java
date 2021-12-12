package si.kostakdd;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import si.kostakdd.connect.PostAsync;
import si.kostakdd.update.UpdateChecker;


public class LoginActivity extends AppCompatActivity {
    private boolean isOnline(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
    }

    private void closeKeyboard(View view)    {
        // this will give us the view
        // which is currently focus
        // in this layout

        // if nothing is currently
        // focus then this will protect
        // the app from crash
        if (view != null) {
            // now assign the system
            // service to InputMethodManager
            InputMethodManager manager
                    = (InputMethodManager)
                    this.getSystemService(Context.INPUT_METHOD_SERVICE);
            manager
                    .hideSoftInputFromWindow(
                            view.getWindowToken(), 0);
        }
    }

    private String readFromFile(String filename) {
        String ret = "";
        try {
            InputStream inputStream =  this.openFileInput(filename);

            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ((receiveString = bufferedReader.readLine()) != null) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();

                ret = stringBuilder.toString();
            }
        } catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }

        return ret;
    }

    String message="Nekaj je šlo narobe...Preverite povezavo in ponovno poskusite";
    int success = 0;
    Intent myIntent;
    private void login(View view, String username, String password)
    {
        closeKeyboard(view);
        String query = username +","+ password;
        new PostAsync(this, "Povezovanje...", output -> {
            if (output!=null){
                try {
                    message = output.getString("message");
                    success = output.getInt("success");
                } catch (JSONException e) {
                    message="Nekaj je šlo narobe. Preverite povezavo in poskusi znova!";
                    e.printStackTrace();
                }
            }
            Toast.makeText(getApplicationContext(),message,Toast.LENGTH_SHORT).show();
            if (success==1){
                myIntent = new Intent(LoginActivity.this,MainActivity.class);
                myIntent.putExtra("username", username); //Optional parameters
                startActivity(myIntent);
                writeToFile(username, "USERID.TXT");
                finish();
            }
        }).execute(Constants.LOGIN_URL, "login", query);

    }


    private void writeToFile(String data, String filename) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(openFileOutput(filename, Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
            //Toast.makeText(this, "Datoteka "+ filename +" shranjena", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Log.d("Exception", "Napaka pri shranjevanju " + e.toString());
        }
    }

    private String username, password;
    // private boolean userLoggedIn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        username = readFromFile("USERID.TXT");
        EditText ET_username = findViewById(R.id.ET_username);
        EditText ET_password = findViewById(R.id.ET_password);
        ET_username.setText(username);
        //getSupportActionBar().hide();
        Button loginButton = findViewById(R.id.loginButton);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                username = ET_username.getText().toString();
                password = ET_password.getText().toString();
                if(isOnline(LoginActivity.this)){
                    if (username!= "" && password!= "") {


                        login(view, ET_username.getText().toString(),ET_password.getText().toString());

                    }

                }

            }

        });

       ET_password.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    // Perform action on key press
                    login(v, ET_username.getText().toString(),ET_password.getText().toString());
                    return true;
                }
                return false;
            }
        });

        if(isOnline(this)){
            UpdateChecker.checkForDialog(this);
        }


    }




}

