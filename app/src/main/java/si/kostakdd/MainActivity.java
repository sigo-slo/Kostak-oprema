package si.kostakdd;

import static android.app.PendingIntent.getActivity;
import static com.google.android.gms.location.LocationServices.getSettingsClient;
import static si.kostakdd.Constants.BITMAP_HIGH_COMPRESSION;
import static si.kostakdd.Constants.HOST_URL;
import static si.kostakdd.Constants.IMAGE_FORMAT;
import static si.kostakdd.Constants.MY_PERMISSIONS_REQUEST_LOCATION;
import static si.kostakdd.Constants.REQUEST_CODE_SCAN_QR;
import static si.kostakdd.Constants.REQUEST_CODE_TAKEPICTURE;
import static si.kostakdd.Constants.SERVER_IMG_FOLDER;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
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
import android.text.Html;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.exifinterface.media.ExifInterface;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.MapView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

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
import java.util.List;
import java.util.Locale;

import si.kostakdd.connect.PostAsync;
import si.kostakdd.databinding.ActivityMainBinding;
import si.kostakdd.parser.NdefMessageParser;
import si.kostakdd.record.ParsedNdefRecord;
import si.kostakdd.tabela.LineItem;
import si.kostakdd.tabela.RowAdapter;
import si.kostakdd.ui.SectionsStateFragmentAdapter;
import si.kostakdd.ui.main.CustomViewPager;
import si.kostakdd.ui.main.SectionsPagerAdapter;

public class MainActivity extends AppCompatActivity {

    //private stran1 s1 = new stran1();
    public String inv_st;
    public String username,imgURL;
    SectionsPagerAdapter sectionsPagerAdapter;

    private CustomViewPager viewPager;

    private void getUsername() {
        Bundle bundle = getIntent().getExtras();
        if (username == null && bundle != null) {
            username = bundle.getString("username");
            //   username = readFromFile("USERID.TXT");
            Log.d("GET_USERNAME;", username);
            // TODO
            refreshTable(username);//to je samo za iskanje vse opreme za trenutnega uporabnika v bazi
        }

    }

    private void setActionBar() {

        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setDisplayShowTitleEnabled(true);
            supportActionBar.setTitle(Html.fromHtml("<small><font color=\"#680cee\">" + username + "</small>"));
            supportActionBar.setHomeAsUpIndicator(R.drawable.actionbar_icon);
            supportActionBar.setDisplayHomeAsUpEnabled(true);

        }
    }

    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallback;


    // Komunikacija s strežnikom
    private String lokacija;
    private String opis;
    private String nahajal;
    private String jeAktivna;
    private String userAssignedTo;
    private String userUpdate_date;
    private String statusUpdate_date;



    void PostToServer(String command, String query, String data) {
        new PostAsync(this, "Povezovanje...", output -> {
            String message = "";
            int success = 0;
            if (output != null) {
                try {
                    message = output.getString("message");
                    success = output.getInt("success");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            if (success == 1) {
                //TODO// Pripravi vse pogoje
                Log.d("Success!", message);
                if (command.equals("getEquipmentData")) {
                    try {
                        jeAktivna = output.getString("jeAktivna");
                        opis = output.getString("Naziv_osn_sred");
                        nahajal = output.getString("Nahajal") + "-(" + output.getString("Opis_lokacije") + ")";
                        userAssignedTo = output.getString("AssignedTo");
                        userUpdate_date = output.getString("userUpdate_date");
                        statusUpdate_date = output.getString("statusUpdate_date");
                        Geolokacija = output.getString("GEOLokacija").split(" - ");
                        imgURL = SERVER_IMG_FOLDER + inv_st + IMAGE_FORMAT;
                        setOnScreen(1);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        opis = "";
                        nahajal = "";
                        userAssignedTo = "";

                    }
                }
                if (command.equals("setStatus")) {
                    setOnScreen(2);

                }
                if (command.equals("setUser")) {
                    setOnScreen(2);
                }
                if (command.equals("getUserEquip")) {
                    drawTable(output);///povezano s funkcijami v paketu tabela za sremembo je potrebno več stvari
                }
                if (command.equals("getEquipDB")) {
                    writeToFile(output.toString(), "KOSTAKDB.TXT");
                    //drawTable(output);///povezano s funkcijami v paketu tabela za sremembo je potrebno več stvari
                }
            } else {
                Log.d("Failure", message);

            }
            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
        }).execute(Constants.LOGIN_URL, command, query, data);

    }

    public void findEquipment(String barcode, String scanType, String action) {
        Log.d("FIND_EQUIPMENT;", scanType + " | "+ barcode + " | "+ action);
        if (barcode.length() > 2) {
            if (scanType.equals("barcode")) {
                barcode = barcode.substring(1, barcode.length() - 1);

            }
            inv_st = barcode;
        } else {
            Toast.makeText(this, getResources().getString(R.string.badBarcode) + "-" + barcode, Toast.LENGTH_SHORT).show();
        }
        if (username != null) {
            PostToServer("getEquipmentData", inv_st, null);
        }
    }

    private void ImageUploadToServerFunction(String imgName, Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            bitmap.compress(Bitmap.CompressFormat.WEBP_LOSSY, BITMAP_HIGH_COMPRESSION, byteArrayOutputStream);
        }
        String encodeToString = Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT);
        PostToServer("imgUpload", imgName, encodeToString);
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
    public String datediff(Date date, String str) throws ParseException {
        long time = (date.getTime() - new SimpleDateFormat("yyy-MM-dd HH:mm:ss").parse(str).getTime()) / 1000;
        int floor = (int) Math.floor((double) (time / 86400));
        time = (time - ((long) ((floor * 24) * 3600))) / 3600;
        String stringBuilder = "" +
                floor;
        return stringBuilder;
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

    @NonNull
    public void refreshTable(String query) {
        PostToServer("getUserEquip", query, null);
    }

    public void setOnScreen(int i) {
      /*  String userdatediff="", statusdatediff="";
        try {
            userdatediff = datediff(new Date(), M_A.userUpdate_date);
            statusdatediff= datediff(new Date(), M_A.statusUpdate_date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (M_A.username.equals(M_A.userAssignedTo)) {
            assignButton.setEnabled(false);
            if (M_A.jeAktivna.equals("0")) {
                pokvarjeno.setEnabled(true);
                izpravno.setEnabled(false);

            } else  if (M_A.jeAktivna.equals("1")) {
                pokvarjeno.setEnabled(false);
                izpravno.setEnabled(true);

            }
        } else {
            assignButton.setEnabled(true);
        }

        Glide
                .with(this).load(SERVER_IMG_FOLDER+M_A.inv_st + IMAGE_FORMAT)
                .error(R.drawable.background_logo)
                .fallback(R.drawable.background_logo)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, com.bumptech.glide.request.target.Target<Drawable> target, boolean isFirstResource) {
                        // imageView.setVisibility(View.INVISIBLE);
                        imageView.setClickable(true);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, com.bumptech.glide.request.target.Target<Drawable> target, com.bumptech.glide.load.DataSource dataSource, boolean isFirstResource) {
                        // imageView.setVisibility(View.VISIBLE);
                        return false;
                    }
                })
                .into(imageView);
//

        tagIDTextview.setText(M_A.inv_st);
        opisTextView.setText(M_A.opis);
        imageView.setVisibility(View.VISIBLE);
        statusTextView.setText(M_A.status(M_A.jeAktivna)+"-"+statusdatediff+"dni");
        assignedToTextView.setText(M_A.userAssignedTo + "-"+userdatediff+"dni");
        tipTextView.setText(M_A.nahajal);
        GeolokacijaTV.setText(M_A.Geolokacija[0]);*/


  //      mViewPager.setCurrentItem(2);
 //       txtOpis2
        viewPager.setCurrentItem(2);
        Stran2 stran2 = (Stran2) sectionsPagerAdapter.getCurrentFragment();
        stran2.SetOnStran2(opis,username + " - " + userUpdate_date,inv_st, status(jeAktivna) + " - " + statusUpdate_date,nahajal,Geolokacija,imgURL);


        if (i == 2) {
            refreshTable(username);
        }
        if (searchView != null && !searchView.isIconified()) {
            searchView.setIconified(true);
        }
    }



    private HorizontalScrollView headerScroll;
    private int scrollX;
    private RowAdapter rowAdapter;

    private void drawTable(JSONObject jSONObject) {
        int i = 0;
        scrollX = 0;
        List<LineItem> rowList = new ArrayList();
        RecyclerView rvRow = findViewById(R.id.RV);
       // headerScroll = findViewById(R.id.horizontalScrollView);
        rowAdapter = new RowAdapter(this, rowList);
       // FixedGridLayoutManager fixedGridLayoutManager = new FixedGridLayoutManager();
       // fixedGridLayoutManager.setTotalColumnCount(1);
        rvRow.setLayoutManager(new LinearLayoutManager(this));
        rvRow.setAdapter(rowAdapter);
       // rvRow.addItemDecoration(new DividerItemDecoration(this, 1));
        try {
            JSONArray jSONArray = jSONObject.getJSONArray("podatki");
            while (i < jSONArray.length()) {
                JSONObject jSONObj = jSONArray.getJSONObject(i);

                String status = status(jSONObj.getString("jeAktivna"));
                String opis = jSONObj.getString("Naziv_osn_sred");
                String update_date = jSONObj.getString("userUpdate_date");
                String inv_st = jSONObj.getString("Inv_št");

                Log.i("PODATKI-Tabela:", SERVER_IMG_FOLDER +inv_st + IMAGE_FORMAT);
/*              Log.i("PODATKI-Tabela:", jSONObj.toString());
                if (!isWholeData) {
                    StringBuilder stringBuilder2 = new StringBuilder();
                    stringBuilder2.append(SERVER_IMG_FOLDER);
                    stringBuilder2.append(string3);
                    stringBuilder2.append(IMAGE_FORMAT);
                    str = stringBuilder2.toString();
                }
*/

                i++;
                rowList.add(new LineItem(i, inv_st, opis, status, datediff(new Date(), update_date), SERVER_IMG_FOLDER +inv_st + IMAGE_FORMAT));
            }
        } catch (ParseException | JSONException e) {
            e.printStackTrace();
        }
 /*       rvRow.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                scrollX += dx;

                headerScroll.scrollTo(scrollX, 0);
            }


            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }
        });*/
    }


    public SearchView searchView;


    public void showWirelessSettings() {
        // Toast.makeText(this, "Za uporabo NFC čitalnika je potrebno v nastavitvah omogočiti NFC.", Toast.LENGTH_SHORT).show();
        startActivity(new Intent("android.settings.WIRELESS_SETTINGS"));
    }
    //\ Pomožne funkcije


    /////// GEOLOKACIJA
    private Geocoder geocoder;
    public String[] Geolokacija = {"", ""};


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

    @SuppressLint("MissingPermission")
    protected void startLocationUpdates() {

        // Create the location request to start receiving updates
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        /* 10 secs */
        long UPDATE_INTERVAL = 10 * 1000;
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        /* 2 sec */
        long FASTEST_INTERVAL = 2000;
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);

        mLocationCallback = new LocationCallback() {
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

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
    }

    private void onLocationChanged(@NonNull Location location) {
        // New location has now been determined
        // String msg = "Updated Location: " +
        //          Double.toString(location.getLatitude()) + "," +
        //        Double.toString(location.getLongitude());
        lokacija = location.getLatitude() + "," + location.getLongitude();
        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            String cityName = addresses.get(0).getAddressLine(0);
            String stateName = addresses.get(0).getAddressLine(1);
            String countryName = addresses.get(0).getAddressLine(2);
            lokacija = cityName + " - (" + lokacija + ")";
        } catch (IOException e) {
            e.printStackTrace();
        }
        //Toast.makeText(this, lokacija, Toast.LENGTH_SHORT).show();
        // You can now create a LatLng Object for use with maps
        //LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        //lokacija=latLng.toString();

    }

    //\\\\\\ GEOLOKACIJA


    private void resolveIntent(Intent intent) {
        String action = intent.getAction();
        Log.d("NFC action",action);
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);//EXTRA_ID);//EXTRA_NDEF_MESSAGES);
            NdefMessage[] msgs;

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

    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private void initializeNFC() {
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null) { // Če NFC ni prisoten v napravi
            Toast.makeText(this, "TA NAPRAVA NE PODPIRA NFC", Toast.LENGTH_LONG).show(); // Objavi sporočilo

        } else {

            pendingIntent = getActivity(this, 0,
                    new Intent(this, this.getClass())
                            .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        }
    }

///TODO - tale search je potrebno preveriti ali dela vse ok
    private Boolean isWholeData = false;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu, menu);
        menu.findItem(R.id.searchInDatabase).setChecked(isWholeData);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        //searchView.setMaxWidth(Integer.MAX_VALUE);

        // listening to search query text change
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener()
        {
            @Override
            public boolean onQueryTextSubmit(String query)
            {
                // filter recycler view when query submitted
                rowAdapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query)
            {
                // filter recycler view when text is changed
                rowAdapter.getFilter().filter(query);
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case android.R.id.home:
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse(HOST_URL)));
                return true;
            case R.id.searchInDatabase:
                item.setChecked(!isWholeData);
                isWholeData = item.isChecked();
                if (isWholeData) {
                    try {
                        JSONObject json = new JSONObject(readFromFile("KOSTAKDB.TXT"));
                        drawTable(json);
                    } catch (JSONException e) {

                        e.printStackTrace();
                    }
                }else{
                    refreshTable(username);
                }
                return true;
            case R.id.getDatabase:
                PostToServer("getEquipDB", "null", "null");
                return true;
            case R.id.action_search:
                return true;

            case R.id.enableNFC:
                showWirelessSettings();
                return true;
            case R.id.qr_scanner:
                startActivityForResult(new Intent(this, ScanCodeActivity.class),REQUEST_CODE_SCAN_QR);
                return true;
            case R.id.exit:
                finish();
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        checkLocationPermission();
        // nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        getSupportActionBar().setSubtitle(Html.fromHtml("<font color=\"#FF0000\">QR čitalnik</font>"));

        if (nfcAdapter != null) {// Če aplikacija zazna NFC opremo
            if (!nfcAdapter.isEnabled()) { // in če ni omogočena
                //showWirelessSettings(); // napoti uporabnika do nastavitev brezžičnih povezav, kjer je tudi NFC
                Toast.makeText(this, "NFC čitalnik je onemogočen. Vključite ga v nastavitvah!", Toast.LENGTH_SHORT).show();
                //nfcitem.setVisible(true);

            } else {
                nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
                getSupportActionBar().setSubtitle(Html.fromHtml("<font color=\"#FF0000\">NFC ali QR čitalnik</font>"));


            }

        }else{

        }
//        if (nfcAdapter!= null ) {
//            //Log.d("NFC Permission:",PackageManager.PERMISSION_GRANTED+"-"+ContextCompat.checkSelfPermission(this, Manifest.permission.NFC));
//                if (nfcAdapter.isEnabled()) {
//
//                    getSupportActionBar().setSubtitle(Html.fromHtml("<font color=\"#FF0000\">NFC ali QR čitalnik</font>"));
//                    //nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
//
//                } else {
//                    showWirelessSettings();
//                    nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
//                    Toast.makeText(this, "NFC čitalnik je onemogočen. Vključite ga v nastavitvah!", Toast.LENGTH_SHORT).show();
//                    getSupportActionBar().setSubtitle(Html.fromHtml("<font color=\"#FF0000\">QR čitalnik</font>"));
//                }
//
//        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String action = intent.getAction();
        Log.d("onNewIntent----",action);
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
                findEquipment(inv_st,"barcode", getIntent().getAction());
                requestCode=333;
            } else  if (requestCode == REQUEST_CODE_TAKEPICTURE)
            {
                Bitmap bitmap;

                if (data != null) {
                    Log.d(str, " REQUEST_CODE_TAKEPICTURE");
                    bitmap = (Bitmap) data.getExtras().get("data");
                } else {
                    Log.d(str, "NULL INTENT");
                    bitmap = BitmapFactory.decodeFile(currentPhotoFile.getAbsolutePath());
                    try {
                        bitmap = rotateImageIfRequired(bitmap, currentPhotoFile.getAbsolutePath());
                        Log.d("ROTATION:", "OK");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                /*Glide
                        .with(this)
                        .load(bitmap)
                        .into(slika2);*/
                ImageUploadToServerFunction(inv_st + IMAGE_FORMAT, bitmap);
            }
        }
        if (resultCode == Activity.RESULT_CANCELED) {

            //Write your code if there's no result
        }


    }

    @Override
    public void onBackPressed() {

        if (searchView != null && !searchView.isIconified()) {
            searchView.setIconified(true);
        } else if (inv_st != null) {
            inv_st = null;
        } else if (isWholeData) {
            refreshTable(username);
            isWholeData = false;
        } else {
            new AlertDialog.Builder(this)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle("Zapiranje aplikacije...")
                    .setMessage("Ali želite zapustiti aplikacijo?")
                    .setPositiveButton("Zapusti", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }

                    })
                    .setNegativeButton("Prekliči", null)
                    .show();
        }
    }

/*    private  void SetViewPager(ViewPager viewPager) {
        SectionsStateFragmentAdapter adapter = new SectionsStateFragmentAdapter(getSupportFragmentManager());
        adapter.addFragment(new stran1(), "Prva Stran");
        adapter.addFragment(new stran2(), "Druga Stran");
        adapter.addFragment(new stran3(), "Tretja Stran");
        viewPager.setAdapter(adapter);
    }

    public void SetViewPager(int fragmentNumber){
        mViewPager.setCurrentItem(fragmentNumber);
    }*/


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        viewPager = binding.viewPager;
        viewPager.setAdapter(sectionsPagerAdapter);
        viewPager.setPagingEnabled(false);

    //    TabLayout tabs = binding.tabs;
     //   tabs.setupWithViewPager(viewPager);
        FloatingActionButton fab = binding.fab;

     //   mSectionFragmentAdapter = new SectionsStateFragmentAdapter(getSupportFragmentManager());
    //    mViewPager = (ViewPager) findViewById(R.id.view_pager);

        getUsername();
        checkLocationPermission();
        geocoder = new Geocoder(this, Locale.getDefault());
        startLocationUpdates();
        //initializeVars(); // inicializacija spremenljivk, NFC
        initializeNFC();
        setActionBar(); // inicializacija menuja s ikono
        //setButtons();
 /*       fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/
    }
}