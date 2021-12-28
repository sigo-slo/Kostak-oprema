package si.kostakdd.ui.main;

import static si.kostakdd.Constants.IMAGE_FORMAT;
import static si.kostakdd.Constants.SERVER_IMG_FOLDER;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.android.gms.maps.model.LatLng;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import si.kostakdd.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ImageFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ImageFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String argData = "data";

    public ImageFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     *
     * .
     * @return A new instance of fragment ImageFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ImageFragment newInstance(String data) {
        ImageFragment fragment = new ImageFragment();
        Bundle args = new Bundle();
        args.putString(argData,data);

        fragment.setArguments(args);
        return fragment;
    }
 private String picURL;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            String Jstring = getArguments().getString(argData);
            try {
                // TODO: Rename and change types of parameters
                JSONObject jarray = new JSONObject(Jstring);

                picURL = SERVER_IMG_FOLDER + jarray.getString("Inv_št") + IMAGE_FORMAT;


            } catch (JSONException e) {
                    e.printStackTrace();
            }


        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v =  inflater.inflate(R.layout.fragment_image, container, false);
        ImageView imageView = v.findViewById(R.id.imageView);
        ImageView fab = v.findViewById(R.id.iv_close_fragement);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getFragmentManager().popBackStack();
            }
        });

        Picasso.get()
                .load(picURL)
                .error(R.drawable.not_found)
                .into(imageView);

        return v;
    }
}