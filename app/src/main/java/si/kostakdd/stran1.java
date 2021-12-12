package si.kostakdd;

import static si.kostakdd.Constants.IMAGE_FORMAT;
import static si.kostakdd.Constants.SERVER_IMG_FOLDER;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link stran1#newInstance} factory method to
 * create an instance of this fragment.
 */
public class stran1 extends Fragment {
    public MainActivity M_A;
    public ImageView imageView;
    public Button assignButton, izpravno,pokvarjeno;
    private void setButtons(View v) {

        assignButton= v.findViewById(R.id.prevzButton);
        assignButton.setEnabled(false);
        assignButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (!M_A.inv_st.equals("")) {
                    String query = M_A.username + "_"+ M_A.lokacija + "_" + M_A.inv_st;
                    M_A.PostToServer("setUser",query,null);
                }
            }
        });
        izpravno =v.findViewById(R.id.pokvButtonfalse);
        izpravno.setEnabled(false);
        izpravno.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (!tagIDTextview.getText().toString().equals("")) {
                    M_A.jeAktivna = "0";
                    String query =  M_A.lokacija + "_" + M_A.inv_st + "_"+ M_A.jeAktivna;
                    M_A.PostToServer("setStatus",query,null);
                }
            }
        });
        pokvarjeno = v.findViewById(R.id.pokvButtonTrue);
        pokvarjeno.setEnabled(false);
        pokvarjeno.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (!tagIDTextview.getText().toString().equals("")) {
                    M_A.jeAktivna = "1";
                    String query =  M_A.lokacija + "_" + M_A.inv_st + "_"+ M_A.jeAktivna;
                    M_A.PostToServer("setStatus",query,null);
                }
            }
        });
    }
    public String datediff(Date date, String str) throws ParseException {
        long time = (date.getTime() - new SimpleDateFormat("yyy-MM-dd HH:mm:ss").parse(str).getTime()) / 1000;
        int floor = (int) Math.floor((double) (time / 86400));
        time = (time - ((long) ((floor * 24) * 3600))) / 3600;
        String stringBuilder = "" +
                floor;
        return stringBuilder;
    }
    public void setOnScreen(int i) {
        String userdatediff="", statusdatediff="";
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
        GeolokacijaTV.setText(M_A.Geolokacija[0]);
        if (i == 2) {
            M_A.refreshTable(M_A.username);
        }
        if (M_A.searchView != null && !M_A.searchView.isIconified()) {
            M_A.searchView.setIconified(true);
        }
    }


    public TextView tagIDTextview, opisTextView, statusTextView, assignedToTextView, tipTextView, GeolokacijaTV;
    public TableLayout tableLayout;
    public LinearLayout ButtonLayout;
    public void initializeVars(View v) {
        tagIDTextview = v.findViewById(R.id.tagIDTextview);
       /* tagIDTextview.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
                } else if (M_A.dispatchTakePictureIntent()) {
                    setOnScreen(2);
                }
            }
        });*/
        tableLayout=v.findViewById(R.id.tableLayout);
        ButtonLayout=v.findViewById(R.id.button_layout);
        opisTextView = v.findViewById(R.id.opisTextView);
        statusTextView = v.findViewById(R.id.statusTextView);
        assignedToTextView = v.findViewById(R.id.assignedToTextView);
        imageView = v.findViewById(R.id.imageView);
        tableLayout.setVisibility(View.VISIBLE);
        imageView.setVisibility(View.VISIBLE);
        ButtonLayout.setVisibility(View.VISIBLE);
        GeolokacijaTV = v.findViewById(R.id.Geolokacija);
        tipTextView = v.findViewById(R.id.tipTextview);
    }


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    public stran1() {
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
    public static stran1 newInstance(String param1, String param2) {
        stran1 fragment = new stran1();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
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
        View v = inflater.inflate(R.layout.fragment_stran1, container, false);
        initializeVars(v);
        setButtons(v);
      return v;
    }
}