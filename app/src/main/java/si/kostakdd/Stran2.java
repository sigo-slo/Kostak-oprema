package si.kostakdd;

import static si.kostakdd.Constants.MAPVIEW_BUNDLE_KEY;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.ObjectKey;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.snackbar.Snackbar;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Stran2#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Stran2 extends Fragment implements OnMapReadyCallback {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    public TextView txtOpis2,txtUser2, txtInv_st2, txtStatus2, txtDays2, txtLokacija2;
    private MapView mapView;
    private ImageView slika2;
    private double lat,lang;
    private String inv_st;
    GoogleMap mMap;
    Marker currentMarker = null;


    public Stran2() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment stran1.
     */
    // TODO: Rename and change types and number of parameters
    public static Stran2 newInstance(String param1, String param2) {
        Stran2 fragment = new Stran2();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(  args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            // TODO: Rename and change types of parameters
            String mParam1 = getArguments().getString(ARG_PARAM1);
            String mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View inflatedView = inflater.inflate(R.layout.fragment_stran2, container, false);
        txtOpis2 = (TextView) inflatedView.findViewById(R.id.txtOpis2);
        txtInv_st2 = (TextView) inflatedView.findViewById(R.id.txtInv_st2);
        txtUser2 = (TextView) inflatedView.findViewById(R.id.txtUser2);
        txtStatus2 = (TextView) inflatedView.findViewById(R.id.txtStatus2);
        txtDays2 = (TextView) inflatedView.findViewById(R.id.txtNahajalisce2);
        txtLokacija2 = (TextView) inflatedView.findViewById(R.id.txtLokacija2);
        slika2 = (ImageView) inflatedView.findViewById(R.id.slika2);
        mapView = (MapView) inflatedView.findViewById(R.id.mapView);
        mapView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Klik Na mapo", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }});

        slika2 = (ImageView) inflatedView.findViewById(R.id.slika2);
        initGoogleMap(savedInstanceState);

        return inflatedView;

    }
    private void initGoogleMap(Bundle savedInstanceState){
        // *** IMPORTANT ***
        // MapView requires that the Bundle you pass contain _ONLY_ MapView SDK
        // objects or sub-Bundles.
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }

        mapView.onCreate(mapViewBundle);

        mapView.getMapAsync(this);
    }


    public void SetOnStran2(String Opis2,String User2,String  Inv_st2,String  Status2,String  Days2,String[] Lokacija2, String imgURL){
        txtOpis2.setText(Opis2);
        inv_st = Inv_st2;
        txtUser2.setText(User2);
        txtInv_st2.setText(Inv_st2);
        txtStatus2.setText(Status2);
        txtDays2.setText(Days2);
        txtLokacija2.setText(Lokacija2[0]);
        String lok[] = Lokacija2[1].substring(1,Lokacija2[1].length()-1 ).split(",");
        Log.i("Stran2 geolakocaija",lok[0]+"-"+lok[1]);
        lat = Double.parseDouble(lok[0]);
        lang = Double.parseDouble(lok[1]);
        placeMarker(inv_st,lat,lang);
        Glide.with(this)
                .load(imgURL)
                .error(R.drawable.background_logo)
                .fallback(R.drawable.background_logo)
                .into(slika2);


    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Bundle mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle);
        }

        mapView.onSaveInstanceState(mapViewBundle);
    }
    public void placeMarker(String title, double lat, double lon) {
        if (mMap != null) {
            if (currentMarker!=null) {
                currentMarker.remove();
                currentMarker=null;
            }

            if (currentMarker==null) {
                LatLng marker = new LatLng(lat, lon);
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(marker, 15));
                currentMarker = mMap.addMarker(new MarkerOptions().title(title).position(marker).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

            }

        }
    }
    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap=map;
    }

    @Override
    public void onPause() {
        mapView.onPause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        mapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}