package si.kostakdd.tabela;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.Group;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import si.kostakdd.R;


public class RowAdapter extends RecyclerView.Adapter<RowAdapter.RowViewHolder> implements Filterable {
    private static final int TYPE_ROW = 0;
    private static final int TYPE_ROW_COLORFUL = 1;

    private final Context context;
    private List<LineItem> filteredRowList;
    private final List<LineItem> rowList;

    public class RowViewHolder extends ViewHolder {
        public ImageView imageInfo;
        public ImageView imageLoc;
        public ImageView imagePic;
        public ImageView imageEdit;
        public ImageView imageArrow;
        public TextView row_num;
        public TextView txtinv_st;
        public TextView txtopis;
        public TextView txtstatus;
        public CardView row;
        public Group expandeditems;

        public RowViewHolder(View view) {
            super(view);
            row = view.findViewById(R.id.kartica);
            row_num = view.findViewById(R.id.txtrow);
            txtopis = view.findViewById(R.id.txtOpis);
          //  txtstatus = view.findViewById(R.id.txtStatus);
           // txtv_uporabi = view.findViewById(R.id.txtdays);
            imagePic = view.findViewById(R.id.imagePic);
            imageInfo = view.findViewById(R.id.imageInfo);
            imageEdit = view.findViewById(R.id.imageEdit);
            imageLoc = view.findViewById(R.id.imageLoc);
            imageArrow = view.findViewById(R.id.kartica_expand);
            expandeditems=view.findViewById(R.id.expandedItems);

            imageArrow.setOnClickListener(new View.OnClickListener() {
                                             @Override
                                             public void onClick(View v) {
                                                    if (expandeditems.getVisibility()==View.GONE){
                                                        expandeditems.setVisibility(View.VISIBLE);
                                                        imageArrow.setImageResource(R.drawable.ic_baseline_arrow_drop_up_24);
                                                    } else {
                                                        expandeditems.setVisibility(View.GONE);
                                                        imageArrow.setImageResource(R.drawable.ic_arrow_drop_down);
                                                    }

                                               //  Toast.makeText(context,"klik INFO",Toast.LENGTH_SHORT).show();
                                                 //((MainActivity)context).findEquipment(txtinv_st.getText().toString(), "NFC","klik na tabelo");
                                             }
                                         }

            );

            imageInfo.setOnClickListener(new View.OnClickListener() {
                                   @Override
                                   public void onClick(View v) {
                                       Toast.makeText(context,"klik INFO",Toast.LENGTH_SHORT).show();
                                    //((MainActivity)context).findEquipment(txtinv_st.getText().toString(), "NFC","klik na tabelo");
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
            return TYPE_ROW;//TYPE_ROW_COLORFUL;
        }

        return TYPE_ROW;
    }


    public RowViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        if (viewType == TYPE_ROW)
        {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_layout, viewGroup, false);
            return new RowViewHolder(view);
        } else
        {
          View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_layout, viewGroup, false);
            return new RowViewHolder(view);
        }
    }

    public void onBindViewHolder(RowViewHolder holder, int position) {
        LineItem lineItem = filteredRowList.get(position);

        holder.row_num.setText(lineItem.row_num);

       holder.txtopis.setText(lineItem.opis + " (" +lineItem.inv_st+ ")");
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
