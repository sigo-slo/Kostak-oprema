package si.kostakdd;


import static android.app.PendingIntent.getActivity;
import static com.google.android.gms.location.LocationServices.getSettingsClient;
import static si.kostakdd.Constants.BITMAP_HIGH_COMPRESSION;
import static si.kostakdd.Constants.IMAGE_FORMAT;
import static si.kostakdd.Constants.LOGIN_URL;
import static si.kostakdd.Constants.MY_PERMISSIONS_REQUEST_LOCATION;
import static si.kostakdd.Constants.REQUEST_CODE_SCAN_QR;
import static si.kostakdd.Constants.REQUEST_CODE_TAKEPICTURE;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.nfc.tech.MifareUltralight;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.exifinterface.media.ExifInterface;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import si.kostakdd.connect.VolleyCallback;
import si.kostakdd.databinding.ActivityMainBinding;
import si.kostakdd.parser.NdefMessageParser;
import si.kostakdd.record.ParsedNdefRecord;
import si.kostakdd.tabela.LineItem;
import si.kostakdd.tabela.RowAdapter;
import si.kostakdd.ui.main.Admin_insert_equip;
import si.kostakdd.ui.main.ImageFragment;
import si.kostakdd.ui.main.MapFragment;

public class MainActivity extends AppCompatActivity {

    public String username;
    private String inv_st;
    private int isAdmin = 0;


    private SearchView searchView;

    private void initializeSearch() {
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = findViewById(R.id.search);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        //searchView.setMaxWidth(Integer.MAX_VALUE);

        // listening to search query text change
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // filter recycler view when query submitted
                rowAdapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                // filter recycler view when text is changed
                if(rowAdapter!=null)rowAdapter.getFilter().filter(query);
                return false;
            }
        });
        searchView.findViewById(R.id.search).setOnClickListener(view -> stopSearch());

    }
    boolean isNormalSearch,isGlobalSearch;

    private void startSearch(){
        if(isGlobalSearch)updateLocalDb();
        Log.d("GLOBAL SEARCH", isGlobalSearch +"");
        searchView.setVisibility(View.VISIBLE);
        searchView.setIconified(false);

    }

    private void stopSearch(){
        searchView.setVisibility(View.GONE);
        searchView.setIconified(true);
        if(isGlobalSearch){
            updateUserEquipmentTable(username) ;
            iv_search_global.setBackgroundTintList(getResources().getColorStateList(R.color.kostak_green));
        } else {
                iv_search.setBackgroundTintList(getResources().getColorStateList(R.color.kostak_green));
            }
        isNormalSearch=false;
        isGlobalSearch=false;

    }



    private ImageView iv_search, iv_search_global, iv_scan, iv_admin;
    private void getUsername() {
        Bundle bundle = getIntent().getExtras();
        TextView tv = findViewById(R.id.TV_username);
        if (username == null && bundle != null) {
            username = bundle.getString("username");
            isAdmin = bundle.getInt("isAdmin");
            //   username = readFromFile("USERID.TXT");
            tv.setText(username);
            //Log.d("GET_USERNAME;", username);
           // Log.d("IS ADMIN;", String.valueOf(isAdmin));
            // TODO
            updateUserEquipmentTable(username);//to je samo za iskanje vse opreme za trenutnega uporabnika v bazi
        }

    }
    private void updateLocalDb(){
       getString(result -> {
           String message;
           int success;
           JSONObject responce;
           try {
               Log.d("UPDATE_LOCAL_DB:",result);
               responce= new JSONObject(result);

               message = responce.getString("message");
               success = responce.getInt("success");
               if (success==1){
                   writeToFile(responce.toString(), "KOSTAKDB.TXT");
                  // Log.d("UPDATE_LOCAL_DB:",message);
                   updateTable(responce);
               }
           } catch (JSONException e) {

               e.printStackTrace();
           }

           //Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
       }, "getEquipDB",username,"");

    }

    private void setLinks() {


        iv_search = findViewById(R.id.iv_search);
        iv_search.setOnClickListener(v -> {
            if (searchView.getVisibility()==View.VISIBLE) {
                stopSearch();

            } else {
                iv_search.setBackgroundTintList(getResources().getColorStateList(R.color.red));
                isNormalSearch=true;
                startSearch();


            }
        } );

        iv_search_global = findViewById(R.id.iv_search_global);
        iv_search_global.setOnClickListener(v -> {
                    if (searchView.getVisibility()==View.VISIBLE) {
                        stopSearch();

                    } else {
                        iv_search_global.setBackgroundTintList(getResources().getColorStateList(R.color.red));
                        isGlobalSearch=true;
                        startSearch();

                    }

        }
        );


        iv_scan = findViewById(R.id.iv_scan);
        iv_scan.setOnClickListener(v ->
                startActivityForResult(new Intent(getApplicationContext(), ScanCodeActivity.class), REQUEST_CODE_SCAN_QR)
        );
        //searchView.setVisibility(View.GONE);

        if (isAdmin == 1) {
            iv_admin = findViewById(R.id.iv_admin);
            iv_admin.setVisibility(View.VISIBLE);
            iv_admin.setOnClickListener(v -> openFragment(Constants.FRAGMENT_ADMIN, username)
            );
        }
    }

   public void setEquipmentUser(String inv_st) {
        //action.php (Inv_št,  user, TimeOfEvent,  Tip_spremembe, GEOLokacija)
        String query = inv_st + "_" + username + "_" + getMyLocation();
        getString(result -> {
            String message;
            int success;
            JSONObject responce;
            try {
                Log.d("SET_USER_QUERY:",query);
                Log.d("SET_USER_QUERY_res",result);
                responce= new JSONObject(result);
                message = responce.getString("message");
                success = responce.getInt("success");
                if (success==1){
                    writeToFile(result, "KOSTAKDB.TXT");

                    Log.d("SET_USER:",inv_st +"-"+ message);
                }
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            } catch (JSONException e) {

                e.printStackTrace();
            }
        },"setUser",query,"");

       // return result;
    }

    public void setStatus(String inv_st, int jeAktivna, String opis) {
        //ACTION.php
        //Inv_št, user, TimeOfEvent,  Tip_spremembe, OpisSpremembe, GEOLokacija
        String query = inv_st + "_" + username + "_" + jeAktivna+"_"+ opis+"_" + getMyLocation();
        getString(result -> {
            String message;
            int success;
            JSONObject responce;
            try {
                Log.d("SET_EQ_STATE_QUERY:",query);
                Log.d("SET_EQ_STATE_QUERY_res",result);
                responce= new JSONObject(result);
                message = responce.getString("message");
                success = responce.getInt("success");
                if (success==1){
                    Log.d("SET_EQUIPMENT_STATE:",inv_st +"-"+ message+"-"+jeAktivna);

                }
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            } catch (JSONException e) {

                e.printStackTrace();
            }
        },"setStatus",query,"");

        // return result;
    }

    public String getMyLocation() {
        String addr = lokacijaAddress;
        String koord = lokacijaCoord;
        return koord+"-*-"+addr;
    }

    // Komunikacija s strežnikom
    public void getString(VolleyCallback callback, String command, String query, String imageData) {
        // response
        StringRequest postRequest = new StringRequest(Request.Method.POST,LOGIN_URL,
                callback::onSuccess, error -> {
                    // error
                    Log.d("Error.Response", error.toString());
                })
        {
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
    // ////////////////////////////////////////////////////////Komunikacija s strežnikom
    private boolean equipmenSearch=false;
    public void findEquipment(String barcode, String scanType, String action) {
        Log.d("FIND_EQUIPMENT;", scanType + " | " + barcode + " | " + action);
        if (barcode.length() > 2) {
            if (scanType.equals("barcode")) {
                barcode = barcode.substring(1, barcode.length() - 1);

            }
            inv_st = barcode;
        } else {
            Toast.makeText(this, getResources().getString(R.string.badBarcode) + "-" + barcode, Toast.LENGTH_SHORT).show();
        }
        if (username != null) {

            getString(result -> {
                String message;
                int success;
                JSONObject responce;
                try {
                    responce= new JSONObject(result);
                    message = responce.getString("message");
                    success = responce.getInt("success");
                    if (success==1){
                        Log.d("getEquipmentData:",inv_st +"-"+ message);
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                        equipmenSearch=true;
                        updateTable(responce);

                    }
                } catch (JSONException e) {

                    e.printStackTrace();
                }
            },"getEquipmentData",inv_st,"");

        }
    }

    private void ImageUploadToServerFunction(String imgName, Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            bitmap.compress(Bitmap.CompressFormat.WEBP_LOSSY, BITMAP_HIGH_COMPRESSION, byteArrayOutputStream);
        }
        String encodeToString = Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT);
        getString(result -> {
            String message;
            int success;
            JSONObject responce;
            try {
                responce= new JSONObject(result);
                message = responce.getString("message");
                success = responce.getInt("success");
                if (success==1){
                    Log.d("imgUpload:",imgName +"-"+ message);
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {

                e.printStackTrace();
            }
        },"imgUpload",imgName,encodeToString);

        currentPhotoFile.delete();
    }
    //\ Komunikacija s strežnikom

    // Delo s slikami
    private static Bitmap rotateImageIfRequired(Bitmap bitmap, String str) throws IOException {
        int rotationInt = new ExifInterface(str).getAttributeInt("Orientation", ExifInterface.ORIENTATION_NORMAL);
        Log.d("ORIENTACIJA:", rotationInt + "");
        if (rotationInt == 3) {
            return rotateImage(bitmap, 180);
        }
        if (rotationInt == 6) {
            return rotateImage(bitmap, 90);
        }
        if (rotationInt != 8) {
            return bitmap;
        }
        return rotateImage(bitmap, 270);
    }

    private static Bitmap rotateImage(Bitmap bitmap, int i) {
        Matrix matrix = new Matrix();
        matrix.postRotate((float) i);
        Bitmap createBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        bitmap.recycle();
        return createBitmap;
    }

    private File createImageFile() {
        String fileName = inv_st + IMAGE_FORMAT;
        File file = new File(getFilesDir(), "slike");
        if (!file.exists()) {
            file.mkdir();
        }
        File file2 = new File(file, fileName);
        if (file2.exists()) {
            file2.delete();
        }
        return new File(file, fileName);
    }
    //\ Delo s slikami

    // Pomožne funkcije
    public String status(String str) {
        return str.equals("1") ? getString(R.string.working) : getString(R.string.broken);
    }

    public Long datediff(Date date, String str) throws ParseException {
        long date2 = Objects.requireNonNull(new SimpleDateFormat("yyy-MM-dd HH:mm:ss").parse(str)).getTime();
        long diff = date.getTime() - date2;
        long seconds = diff / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        return hours / 24;
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

    private String dumpTagData(Tag tag) {
        StringBuilder sb = new StringBuilder();
        byte[] id = tag.getId();
        sb.append("ID (hex): ").append(toHex(id)).append('\n');
        sb.append("ID (reversed hex): ").append(toReversedHex(id)).append('\n');
        sb.append("ID (dec): ").append(toDec(id)).append('\n');
        sb.append("ID (reversed dec): ").append(toReversedDec(id)).append('\n');

        String prefix = "android.nfc.tech.";
        sb.append("Technologies: ");
        for (String tech : tag.getTechList()) {
            sb.append(tech.substring(prefix.length()));
            sb.append(", ");
        }
        sb.delete(sb.length() - 2, sb.length());
        for (String tech : tag.getTechList()) {
            if (tech.equals(MifareClassic.class.getName())) {
                sb.append('\n');
                String type = "Unknown";

                try {
                    MifareClassic mifareTag = MifareClassic.get(tag);

                    switch (mifareTag.getType()) {
                        case MifareClassic.TYPE_CLASSIC:
                            type = "Classic";
                            break;
                        case MifareClassic.TYPE_PLUS:
                            type = "Plus";
                            break;
                        case MifareClassic.TYPE_PRO:
                            type = "Pro";
                            break;
                    }
                    sb.append("Mifare Classic type: ");
                    sb.append(type);
                    sb.append('\n');

                    sb.append("Mifare size: ");
                    sb.append(mifareTag.getSize()).append(" bytes");
                    sb.append('\n');

                    sb.append("Mifare sectors: ");
                    sb.append(mifareTag.getSectorCount());
                    sb.append('\n');

                    sb.append("Mifare blocks: ");
                    sb.append(mifareTag.getBlockCount());
                } catch (Exception e) {
                    sb.append("Mifare classic error: ").append(e.getMessage());
                }
            }

            if (tech.equals(MifareUltralight.class.getName())) {
                sb.append('\n');
                MifareUltralight mifareUlTag = MifareUltralight.get(tag);
                String type = "Unknown";
                switch (mifareUlTag.getType()) {
                    case MifareUltralight.TYPE_ULTRALIGHT:
                        type = "Ultralight";
                        break;
                    case MifareUltralight.TYPE_ULTRALIGHT_C:
                        type = "Ultralight C";
                        break;
                }
                sb.append("Mifare Ultralight type: ");
                sb.append(type);
            }
        }

        return sb.toString();
    }

    private String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = bytes.length - 1; i >= 0; --i) {
            int b = bytes[i] & 0xff;
            if (b < 0x10)
                sb.append('0');
            sb.append(Integer.toHexString(b));
            if (i > 0) {
                sb.append(" ");
            }
        }
        return sb.toString();
    }

    private String toReversedHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; ++i) {
            if (i > 0) {
                sb.append(" ");
            }
            int b = bytes[i] & 0xff;
            if (b < 0x10)
                sb.append('0');
            sb.append(Integer.toHexString(b));
        }
        return sb.toString();
    }

    private long toDec(byte[] bytes) {
        long result = 0;
        long factor = 1;
        for (byte aByte : bytes) {
            long value = aByte & 0xffL;
            result += value * factor;
            factor *= 256L;
        }
        return result;
    }

    private long toReversedDec(byte[] bytes) {
        long result = 0;
        long factor = 1;
        for (int i = bytes.length - 1; i >= 0; --i) {
            long value = bytes[i] & 0xffL;
            result += value * factor;
            factor *= 256l;
        }
        return result;
    }

    private void displayMsgs(NdefMessage[] msgs, String action) {
        //Toast.makeText(this,"NFC"+msgs+"|"+action,Toast.LENGTH_SHORT).show();
        if (msgs == null || msgs.length == 0)
            return;

        StringBuilder builder = new StringBuilder();
        List<ParsedNdefRecord> records = NdefMessageParser.parse(msgs[0]);
        final int size = records.size();
        for (int i = 0; i < size; i++) {
            ParsedNdefRecord record = records.get(i);
            String str = record.str();
            if (i < size - 1) {
                builder.append(str).append("\n");
            } else {
                builder.append(str);
            }

        }

        inv_st = builder.toString();
        //TODO

        findEquipment(inv_st, "NFC", action);
    }

    private String readFromFile(String filename) {
        String emptyString = "";
        StringBuilder stringBuilder;
        try {
            FileInputStream openFileInput = openFileInput(filename);
            if (openFileInput == null) {
                return emptyString;
            }
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(openFileInput));
            stringBuilder = new StringBuilder();
            while (true) {
                String readLine = bufferedReader.readLine();
                if (readLine != null) {
                    stringBuilder.append(readLine);
                } else {
                    openFileInput.close();
                    return stringBuilder.toString();
                }
            }
        } catch (FileNotFoundException e) {
            Log.e("MainAct_READFROMFILE:", "File not found: ");
            return emptyString;
        } catch (IOException e2) {
            Log.e("MainAct_READFROMFILE:", "Can not read file: ");
            return emptyString;
        }
    }

    public void updateUserEquipmentTable(String query) {
       getString(result -> {
           String message="";
           int success = 0;
           try {
               JSONObject responce= new JSONObject(result);
               message = responce.getString("message");
               success = responce.getInt("success");
                  if (success==1) {
                      updateTable(responce);
                      Log.d("getUserEquip", message);
                 }

           } catch (JSONException e) {
               //message="Nekaj je šlo narobe. Preverite povezavo in poskusi znova!";
               e.printStackTrace();
           }
           //Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
       },"getUserEquip", query, "");
    }

    private RowAdapter rowAdapter;

    private void updateTable(JSONObject jSONObject) {
        int i = 0;
        List<LineItem> rowList = new ArrayList();
        rowAdapter = new RowAdapter(rowList);
        RecyclerView rvRow = findViewById(R.id.RV);
        rvRow.setLayoutManager(new LinearLayoutManager(this));
        rvRow.setAdapter(rowAdapter);
        // rvRow.addItemDecoration(new DividerItemDecoration(this, 1));
        try {
            JSONArray jSONArray = jSONObject.getJSONArray("podatki");
            while (i < jSONArray.length()) {
                JSONObject jSONObj = jSONArray.getJSONObject(i);

                    i++;

                String inv_st,opis,geoLokacija,geoCoord,row_num,assignedTo;
                int status = jSONObj.getInt("jeAktivna");
                inv_st = jSONObj.getString("Inv_št");
                row_num = i + ".";
                opis = jSONObj.getString("Naziv_osn_sred") ;
                //geoCoord = jSONObj.getString("GEOCoord");
                geoLokacija = jSONObj.getString("GEOLokacija");
                assignedTo = jSONObj.getString("AssignedTo");

                    rowList.add(new LineItem(row_num,inv_st,opis,geoLokacija,assignedTo,status)); //, status, datediff(new Date(), update_date), SERVER_IMG_FOLDER +inv_st + IMAGE_FORMAT));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void showWirelessSettings() {
        // Toast.makeText(this, "Za uporabo NFC čitalnika je potrebno v nastavitvah omogočiti NFC.", Toast.LENGTH_SHORT).show();
        if (android.os.Build.VERSION.SDK_INT >= 16) {
            startActivity(new Intent(android.provider.Settings.ACTION_NFC_SETTINGS));
        } else {
            startActivity(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS));
        }
    }
    //\ Pomožne funkcije


    /////// GEOLOKACIJA
    private Geocoder geocoder;

    private String lokacijaCoord = "0,0";
    private String lokacijaAddress = "";

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle(R.string.title_location_permission)
                        .setMessage(R.string.text_location_permission)
                        .setPositiveButton(R.string.allow, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();

            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
        } else {
        }
    }

    protected void startLocationUpdates() {

        // Create the location request to start receiving updates
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        /* 10 secs */
        long UPDATE_INTERVAL = 10 * 1000;
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        /* 2 sec */
        long FASTEST_INTERVAL = 5000;
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);

        // do work here
        LocationCallback mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                // do work here
                onLocationChanged(locationResult.getLastLocation());
            }
        };
        // Create LocationSettingsRequest object using location request
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();

        // Check whether location settings are satisfied
        // https://developers.google.com/android/reference/com/google/android/gms/location/SettingsClient
        SettingsClient settingsClient = getSettingsClient(this);
        settingsClient.checkLocationSettings(locationSettingsRequest);

        // new Google API SDK v11 uses getFusedLocationProviderClient(this)

        FusedLocationProviderClient mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
    }
    private void onLocationChanged(@NonNull Location location) {
        // New location has now been determined
        lokacijaCoord = location.getLatitude() + "," + location.getLongitude();
        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            String cityName = addresses.get(0).getAddressLine(0);
            String stateName = addresses.get(0).getAddressLine(1);
            String countryName = addresses.get(0).getAddressLine(2);
           lokacijaAddress = cityName ;
        } catch (IOException e) {
            e.printStackTrace();
        }
        //Toast.makeText(this, lokacija, Toast.LENGTH_SHORT).show();
        // You can now create a LatLng Object for use with maps

    }

    //\\\\\\ GEOLOKACIJA


    private void resolveIntent(Intent intent) {
        String action = intent.getAction();
        Log.d("ResolveIntentAction",action);
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);//EXTRA_ID);//EXTRA_NDEF_MESSAGES);
            NdefMessage[] msgs;
            equipmenSearch=true;
            if (rawMsgs != null) {
                msgs = new NdefMessage[rawMsgs.length];

                for (int i = 0; i < rawMsgs.length; i++) {
                    msgs[i] = (NdefMessage) rawMsgs[i];
                }

            } else {
                byte[] empty = new byte[0];
                byte[] id = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID);
                Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                byte[] payload = dumpTagData(tag).getBytes();
                NdefRecord record = new NdefRecord(NdefRecord.TNF_UNKNOWN, empty, id, payload);
                NdefMessage msg = new NdefMessage(new NdefRecord[] {record});
                msgs = new NdefMessage[] {msg};
            }

            displayMsgs(msgs,action);
        }
    }
    private File currentPhotoFile;
    public boolean dispatchTakePictureIntent() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        Log.d("TakePictureIntent",intent.getAction());
        if (intent.resolveActivity(getPackageManager()) != null) {
            currentPhotoFile = createImageFile();
            if (currentPhotoFile != null) {
                Uri uriForFile = FileProvider.getUriForFile(this, "kostak.si.android.fileprovider", currentPhotoFile);
                //      currentImageUri = uriForFile;
                intent.putExtra(MediaStore.EXTRA_OUTPUT, uriForFile);

                startActivityForResult(intent, REQUEST_CODE_TAKEPICTURE);
                return false;
            }
        }
        return false;
    }
    private ImageView iv_nfc;
    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private void initializeNFC(boolean onResume) {
        iv_nfc=findViewById(R.id.iv_nfc);
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null)
        { // Če NFC ni prisoten v napravi
            //Toast.makeText(this, "TA NAPRAVA NE PODPIRA NFC", Toast.LENGTH_LONG).show(); // Objavi sporočilo

            iv_nfc.setVisibility(View.GONE);
        } else {

                pendingIntent = getActivity(this, 0,
                        new Intent(this, this.getClass())
                                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), PendingIntent.FLAG_MUTABLE);

                iv_nfc.setVisibility(View.VISIBLE);
                if (!nfcAdapter.isEnabled()) { // in če ni omogočena
                    // napoti uporabnika do nastavitev brezžičnih povezav, kjer je tudi NFC
                    Toast.makeText(this, "NFC čitalnik je onemogočen. Vključite ga v nastavitvah!", Toast.LENGTH_SHORT).show();
                    iv_nfc.setImageResource(R.drawable.ic_no_nfc);
                    iv_nfc.setOnClickListener(new View.OnClickListener() {
                                                  @Override
                                                  public void onClick(View v) {
                                                      showWirelessSettings();
                                                  }
                                              }
                    );

                }
        }
    }

///TODO - tale search je potrebno preveriti ali dela vse ok

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

                //startActivityForResult(new Intent(this, ScanCodeActivity.class),REQUEST_CODE_SCAN_QR);


        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        checkLocationPermission();

        if (nfcAdapter != null) {// Če aplikacija zazna NFC opremo
            iv_nfc.setVisibility(View.VISIBLE);
            if (!nfcAdapter.isEnabled()) { // in če ni omogočena
                //showWirelessSettings(); // napoti uporabnika do nastavitev brezžičnih povezav, kjer je tudi NFC
                Toast.makeText(this, "NFC čitalnik je onemogočen. Vključite ga v nastavitvah!", Toast.LENGTH_SHORT).show();

                iv_nfc.setImageResource(R.drawable.ic_no_nfc);

            } else {
                nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
                iv_nfc.setImageResource(R.drawable.ic_nfc);


            }
        }
        //updateUserEquipmentTable(username);
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String action = intent.getAction();
       // Log.d("onNewIntent----",action);
        setIntent(intent);
        resolveIntent(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        String str = "ACTIVITY_RESULT:";
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CODE_SCAN_QR)
            {
                Log.d(str, " REQUEST_CODE_SCAN_QR");
                inv_st = data.getStringExtra("Inv_št");
                findEquipment(inv_st,"barcode", data.getAction());
                requestCode=333;
            } else  if (requestCode == REQUEST_CODE_TAKEPICTURE)
            {
                Bitmap bitmap;

                if (data != null) {
                   // Log.d(str, " REQUEST_CODE_TAKEPICTURE");
                    bitmap = (Bitmap) data.getExtras().get("data");
                } else {
                   // Log.d(str, "NULL INTENT");
                    bitmap = BitmapFactory.decodeFile(currentPhotoFile.getAbsolutePath());
                    try {
                        bitmap = rotateImageIfRequired(bitmap, currentPhotoFile.getAbsolutePath());
                       // Log.d("ROTATION:", "OK");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                ImageUploadToServerFunction(inv_st + IMAGE_FORMAT, bitmap);
            }
        }
        if (resultCode == Activity.RESULT_CANCELED) {

            //Write your code if there's no result
        }


    }

    @Override
    public void onBackPressed() {
        int fragsize = getSupportFragmentManager().getFragments().size();
        if (searchView != null && searchView.getVisibility()==View.VISIBLE) {
            stopSearch();
            // } else if (inv_st != null) {
            //     inv_st = null;

        } else if (equipmenSearch) {
            updateUserEquipmentTable(username);
            equipmenSearch=false;


        } else if (fragsize>1) {
            getSupportFragmentManager().popBackStack();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setIcon(android.R.drawable.ic_dialog_alert);
            builder.setTitle("IZHOD");
            builder.setMessage("Ali želite zapustiti aplikacijo?");
            builder.setPositiveButton("Zapusti", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }

            });
            builder.setNegativeButton("Prekliči", null);
            AlertDialog alert = builder.create();

            alert.getWindow().setBackgroundDrawableResource(R.drawable.my_dialog);
            alert.show();
        }
    }

    public RequestQueue queue;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        queue= Volley.newRequestQueue(this);
        FrameLayout frameContainer = findViewById(R.id.fragment_viewer);
        getUsername();
        setLinks();
        initializeSearch();
        checkLocationPermission();
        geocoder = new Geocoder(this, Locale.getDefault());
        startLocationUpdates();
        initializeNFC(false);



    }

    public void openFragment(int FRAGMENT_TYPE, String data){

        switch (FRAGMENT_TYPE){
            case Constants.FRAGMENT_IMAGE:
                ImageFragment IF = ImageFragment.newInstance(data);
                FragmentManager FM = getSupportFragmentManager();
                FragmentTransaction FT = FM.beginTransaction();
                FT.setCustomAnimations(R.anim.enter_from_right,R.anim.exit_to_right,R.anim.enter_from_right,R.anim.exit_to_right);
                FT.add(R.id.fragment_viewer,IF,"slika").addToBackStack("slika").commit();
                break;
            case Constants.FRAGMENT_MAP:
                MapFragment MF = MapFragment.newInstance(data);
                FragmentManager mFM = getSupportFragmentManager();
                FragmentTransaction mFT = mFM.beginTransaction();
                mFT.setCustomAnimations(R.anim.enter_from_right,R.anim.exit_to_right,R.anim.enter_from_right,R.anim.exit_to_right);
                mFT.add(R.id.fragment_viewer,MF,"map").addToBackStack("map").commit();
                break;
            case Constants.FRAGMENT_ADMIN:
                Admin_insert_equip MFa = Admin_insert_equip.newInstance(data);
                FragmentManager mFMa = getSupportFragmentManager();
                FragmentTransaction mFTa = mFMa.beginTransaction();
                mFTa.setCustomAnimations(R.anim.enter_from_right,R.anim.exit_to_right,R.anim.enter_from_right,R.anim.exit_to_right);
                mFTa.add(R.id.fragment_viewer,MFa,"admin").addToBackStack("admin").commit();
                break;
        }



    }
}