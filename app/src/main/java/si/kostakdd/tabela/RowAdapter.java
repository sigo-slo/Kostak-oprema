package si.kostakdd.tabela;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import si.kostakdd.MainActivity;
import si.kostakdd.R;


public class RowAdapter extends RecyclerView.Adapter<RowAdapter.RowViewHolder> implements Filterable {
    private static final int TYPE_ROW = 0;
    private static final int TYPE_ROW_COLORFUL = 1;

    private final Context context;
    private List<LineItem> filteredRowList;
    private final List<LineItem> rowList;

    public class RowViewHolder extends ViewHolder {
        public ImageView imgPic;
        public TextView row_num;
        public TextView txtinv_st;
        public TextView txtopis;
        public TextView txtstatus;
        public TextView txtv_uporabi;
        public LinearLayout row;

        public RowViewHolder(View view) {
            super(view);
            row = view.findViewById(R.id.ll);
            row_num = view.findViewById(R.id.txtrow);
            txtinv_st = view.findViewById(R.id.txtInv_st);
            txtopis = view.findViewById(R.id.txtOpis);
            txtstatus = view.findViewById(R.id.txtStatus);
            txtv_uporabi = view.findViewById(R.id.txtdays);
            //           imgPic = view.findViewById(R.id.imgPic);

            row.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if(hasFocus) {
                       // Toast.makeText(context,"klik",Toast.LENGTH_SHORT).show();
                       ((MainActivity) context).inv_st = txtinv_st.getText().toString();
                       ((MainActivity) context).findEquipment(txtinv_st.getText().toString(), "NFC","klik na tabelo");
                        row.clearFocus();
                    }

                }
            });
            row.setOnClickListener(new View.OnClickListener() {
                                   @Override
                                   public void onClick(View v) {
                                       if (row.hasFocus()){
                                           row.clearFocus();
                                       }
                                   }
                               }

        );

        }


    }

    public RowAdapter(Context context, List<LineItem> list) {
        this.context = context;
        this.rowList = list;
        this.filteredRowList = list;
    }

    @Override
    public int getItemViewType(int position)
    {
        if (position % 2 == 0)
        {
            return TYPE_ROW_COLORFUL;
        }

        return TYPE_ROW;
    }


    public RowViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        if (viewType == TYPE_ROW)
        {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row, viewGroup, false);
            return new RowViewHolder(view);
        } else
        {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_color, viewGroup, false);
            return new RowViewHolder(view);
        }
    }

    public void onBindViewHolder(RowViewHolder holder, int position) {
        LineItem lineItem = filteredRowList.get(position);
        holder.row_num.setText(lineItem.row_num);
        holder.txtinv_st.setText(lineItem.inv_st);
        holder.txtopis.setText(lineItem.opis);
        holder.txtstatus.setText(lineItem.status);
        holder.txtv_uporabi.setText(lineItem.v_uporabi);
//      Glide.with(context)
//                .load(lineItem.picUrl)
//                .signature(new ObjectKey(String.valueOf(System.currentTimeMillis())))
//                .into(holder.imgPic);
    }
    @Override
    public int getItemCount() {
        return filteredRowList.size();
    }

    @Override
    public Filter getFilter(){
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence)
            {
                String charString = charSequence.toString();
                if (charString.isEmpty())
                {
                   filteredRowList = rowList;
                } else {

                    List<LineItem> filteredList = new ArrayList<>();
                    for (LineItem lineItem : rowList) {
                        for (Field classVariable : lineItem.getClass().getDeclaredFields()) {

                          //  System.out.println("Fields: " + Modifier.toString(classVariable.getModifiers())); // modyfiers
                           String test = classVariable.getName();
                            Log.d("Fields: " , test);        //real var name
                            classVariable.setAccessible(true);                                //var readable
                            String strValue = "";
                            try {
                                strValue= (String) classVariable.get(lineItem);
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            }

                            assert strValue != null;
                            if (strValue.toLowerCase().contains(charString.toLowerCase())) {
                                filteredList.add(lineItem);
                                break;

                            }
                        }
                    }
                    filteredRowList = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = filteredRowList;
                return filterResults;
            }

            @Override
            public void publishResults(CharSequence charSequence, FilterResults filterResults) {
                filteredRowList = Collections.unmodifiableList((ArrayList<LineItem>) filterResults.values);
                notifyDataSetChanged();
            }
        };
    }
}
