package si.kostakdd.ui.main;

import static si.kostakdd.Constants.LOGIN_URL;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import si.kostakdd.MainActivity;
import si.kostakdd.R;
import si.kostakdd.connect.VolleyCallback;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AdminFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AdminFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String argData = "data";

    public AdminFragment() {
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
    public static AdminFragment newInstance(String data) {
        AdminFragment fragment = new AdminFragment();
        Bundle args = new Bundle();
        args.putString(argData,data);

        fragment.setArguments(args);
        return fragment;
    }

    public void getString(VolleyCallback callback, String command, String query, String imageData) {
        // response
        StringRequest postRequest = new StringRequest(Request.Method.POST,LOGIN_URL,
                callback::onSuccess, error -> {
            // error
            Log.d("ADMIN_FRAG_Response.er", error.toString());
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
        ((MainActivity)getContext()).queue.add(postRequest);
    }
 private String username;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
                username =getArguments().getString(argData);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v =  inflater.inflate(R.layout.fragment_admin, container, false);
        ImageView fab = v.findViewById(R.id.iv_close_fragement);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                        getActivity().getSupportFragmentManager().popBackStack();
            }
        });
        inv_st = v.findViewById(R.id.inv_st_TIL);
        opis = v.findViewById(R.id.opis_TIL);
        Button btn =v.findViewById(R.id.vnos_opreme);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!confirmInput(view)){
                    String inv = inv_st.getEditText().getText().toString().trim();
                    String op = opis.getEditText().getText().toString().trim();
                    getString(result -> {
                        String message="napaka";
                        //int success;
                        JSONObject responce;
                        try {
                            Log.d("ADMIN_INSERT_EQ",result);
                            responce = new JSONObject(result);

                            message = responce.getString("message");
                           //9//success = responce.getInt("success");
                        } catch (JSONException e) {

                            e.printStackTrace();
                        }

                        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                    }, "insertNewEq",inv+"-**-"+op+"-**-"+((MainActivity) getContext()).getMyLocation(),"");
                }
            }
        });
        return v;
    }
    TextInputLayout inv_st,opis;

    private boolean validateInvSt() {
        String inv = inv_st.getEditText().getText().toString().trim();
        inv_st.setError(null);

        getString(result -> {
            String message="napaka";
            int success;
            JSONObject responce;
            try {
                Log.d("ADMIN_SELECT_INV_res",result);
                Log.d("ADMIN_SELECT_INV_req",inv);
                responce = new JSONObject(result);

                message = responce.getString("message");
                success = responce.getInt("success");
                if (success==1){
                    inv_st.setError(message);
                }
            } catch (JSONException e) {

                e.printStackTrace();
            }

            //Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }, "checkInvExists",inv,"");
        if (inv.isEmpty()) {
            inv_st.setError("Polje ne sme biti prazno");
        }
        Log.d("ADMIN_SELECT_RETURN",inv+"-"+(inv_st.getError()==null));
        return inv_st.getError()==null;
    }


    private boolean validateOpis() {
        String op = opis.getEditText().getText().toString().trim();
        opis.setError(null);
         if (op.isEmpty()) {
            inv_st.setError("Polje ne sme biti prazno");
            return false;
        }

        return opis.getError()==null;
    }

    public boolean confirmInput(View v) {
        if (!validateInvSt() | !validateOpis()) {
            return true;
        }
        return false;
    }
}