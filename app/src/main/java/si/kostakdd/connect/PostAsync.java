package si.kostakdd.connect;


import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import java.util.HashMap;

import si.kostakdd.parser.JSONParser;

public class PostAsync extends AsyncTask<String, String, JSONObject> {
    public Context context;
    String connectMessage;
    String message;
    int success;
    JSONParser jsonParser = new JSONParser();
    JSONObject jsonbj;
    public PostAsyncResponse delegate = null;
    public PostAsync(Context context1, String connectMessage1, PostAsyncResponse response){
        context = context1;
        delegate = response;
        connectMessage=connectMessage1;
    }
    private ProgressDialog pDialog;
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";

    @Override
    protected void onPreExecute() {
       /*pDialog = new ProgressDialog(context);
        pDialog.setMessage(connectMessage);
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(false);
        //pDialog.show();*/
    }
    @Override
    protected JSONObject doInBackground(String... args) {
        try {
            HashMap<String, String> params = new HashMap<>();
            String COMMAND = args[1];
            String QUERY = args[2];
            String LOGIN_URL=args[0];
            params.put("command", COMMAND);
            params.put("query", QUERY);
            if(COMMAND.equals("imgUpload")){
                params.put("imgData", args[3]);
            }
            Log.d("Request starting...", params.toString());
            /*"http://192.168.1.16/kostak/index.php"; */
            JSONObject json = jsonParser.makeHttpRequest(
                    LOGIN_URL, "POST", params);

            if (json != null) {

                Log.d("JSON result", json.toString());
                jsonbj=json;
                message=json.getString(TAG_MESSAGE);
                success=json.getInt(TAG_SUCCESS);
                return json;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    protected void onPostExecute(JSONObject json) {

       /* if (pDialog != null && pDialog.isShowing()) {
            pDialog.dismiss();

        }*/
        delegate.processFinish(json);
    }

}