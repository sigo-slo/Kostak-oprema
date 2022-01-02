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
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.chaos.view.PinView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;

import si.kostakdd.connect.VolleyCallback;
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




    private void login(View view, String username, PinView password)
    {
        closeKeyboard(view);
        String query = username +","+password.getText().toString();
        getString(new VolleyCallback() {
            @Override
            public void onSuccess(String result) {
                    Log.d("login",result);
                String message;
                int success ;
                Intent myIntent;
                int isAdmin;
                try {
                    JSONObject responce= new JSONObject(result);
                    message = responce.getString("message");
                    success = responce.getInt("success");
                    isAdmin = responce.getInt("isAdmin");
                    if (success==1){
                        myIntent = new Intent(LoginActivity.this,MainActivity.class);
                        myIntent.putExtra("username", username); //Optional parameters
                        myIntent.putExtra("isAdmin", isAdmin); //Optional parameters

                        startActivity(myIntent);
                        writeToFile(username, "USERID.TXT");
                        closeKeyboard(view);
                        Toast.makeText(getApplicationContext(),message,Toast.LENGTH_SHORT).show();
                        finish();
                    }
                } catch (JSONException e) {
                    ///message="Nekaj je šlo narobe. Preverite povezavo in poskusi znova!";
                    e.printStackTrace();
                }
            }
        },"login",query,"");

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

    public boolean update_app = true;
    RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        if(isOnline(this) && update_app){
            UpdateChecker.checkForDialog(this);
        }
        queue = Volley.newRequestQueue(this);
        String username = readFromFile("USERID.TXT");
        EditText ET_username = findViewById(R.id.TV_username);
        PinView ET_password = findViewById(R.id.ET_password);
        ET_username.setText(username);
        //getSupportActionBar().hide();

        ET_password.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // If the event is a key-down event on the "enter" button
                if ((event.getAction() == KeyEvent.ACTION_DOWN  &&
                        keyCode == KeyEvent.KEYCODE_ENTER) || ET_password.getText().toString().length()==4) {
                    // Perform action on key press
                    login(v, ET_username.getText().toString(),ET_password);
                    ET_password.setText("");
                    closeKeyboard(v);
                    return true;
                }
                //Toast.makeText(v.getContext(), ET_username.getText().length(), Toast.LENGTH_SHORT).show();
                return false;
            }
        });




    }

    private void getString(VolleyCallback callback, String command, String query, String imageData) {
        StringRequest postRequest = new StringRequest(Request.Method.POST,"http://192.168.64.105/kostak/action.php",
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        // response
                        callback.onSuccess(response);

                    }
                }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error) {
                // error
                Log.d("Error.Response", error.toString());
            }
        }
        ) {
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<String, String>();
                params.put("command", command);
                params.put("query", query);
                params.put("imgData", imageData);

                return params;
            }
        };
        queue.add(postRequest);
    }
}




