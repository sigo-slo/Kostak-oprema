package si.kostakdd;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Stran1#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Stran1 extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String USERNAME = "username";
    private static final String ARG_PARAM2 = "param2";

   private String username;


    public Stran1() {
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
    public static Stran1 newInstance(String param1, String param2) {
        Stran1 fragment = new Stran1();
        Bundle args = new Bundle();
        args.putString(USERNAME, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            // TODO: Rename and change types of parameters
            username = getArguments().getString(USERNAME);
            Log.d("Stran 1 created",username);

            String mParam2 = getArguments().getString(ARG_PARAM2);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.activity_main, container, false);
        TextView TV_username = (TextView) v.findViewById(R.id.TV_username);
        TV_username.setText(username);
      return v;
    }
}