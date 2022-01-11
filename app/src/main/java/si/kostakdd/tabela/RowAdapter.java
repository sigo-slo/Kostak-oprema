package si.kostakdd.tabela;

import android.app.Dialog;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import si.kostakdd.Constants;
import si.kostakdd.MainActivity;
import si.kostakdd.R;
import si.kostakdd.ui.main.MySwitch;


public class RowAdapter extends RecyclerView.Adapter<RowAdapter.RowViewHolder> implements Filterable {
    private static final int TYPE_ROW = 0;
    private static final int TYPE_ROW_COLORFUL = 1;

    private List<LineItem> filteredRowList;///s
    private final List<LineItem> rowList;
    private int prev_expanded = -1;
    private RecyclerView recycler = null;
    private OnItemCheckedChangeListener onItemCheckedChangeListener;



    public class RowViewHolder extends ViewHolder {
    //    public final ImageView imageInfo;
        public final ImageView imageLoc;
        public  ImageView imagePic;
        public  ImageView imageEdit;
        public  ImageView imageArrow;
        public  TextView row_num;
       // public  JSONObject Json;
        public  TextView txtopis,inv_st, tv_edit;

        public  ConstraintLayout row;
        public  TableRow expandeditems;
        public  LinearLayout Edit,Pic,Loc, linLayInfo;
        public  ConstraintLayout edit_extra;
        public  Button btn_assign;
        public  MySwitch sw_state;
        private final String currentUser=((MainActivity)recycler.getContext()).username;


private void collapse_extraEdit() {
            //if(edit_extra.getVisibility()==View.VISIBLE){
            Edit.setBackgroundResource(0);
            tv_edit.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.card_arrow_down, 0);
            edit_extra.setVisibility(View.GONE);
}

private void expand_extraEdit(String assignTo) {

    Edit.setBackgroundResource(R.drawable.round_corners_10dp);

    boolean test = currentUser.equals(assignTo);
    //Log.d("user=user",test+"ok");
    if(test){

        btn_assign.setVisibility(View.GONE);
        sw_state.setVisibility(View.VISIBLE);

    } else {
        btn_assign.setVisibility(View.VISIBLE);
        sw_state.setVisibility(View.GONE);
    }
    tv_edit.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.card_arrow_up, 0);
    tv_edit.refreshDrawableState();
    edit_extra.setVisibility(View.VISIBLE);
}

private void expand_cardView(){
    //if (expandeditems.getVisibility()==View.GONE){
        expandeditems.setVisibility(View.VISIBLE);
        imageArrow.setImageResource(R.drawable.card_arrow_up);

  //  } else {

 //   }
}

private void collapse_cardView(){
    //if (expandeditems.getVisibility()==View.GONE){

        collapse_extraEdit();
        expandeditems.setVisibility(View.GONE);
        imageArrow.setImageResource(R.drawable.card_arrow_down);

 //   }
}

private void showUpdateStatusDialog(String inv,boolean isChecked) {

            final Dialog dialog = new Dialog(recycler.getContext());
            //We have added a title in the custom layout. So let's disable the default title.
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            //The user will be able to cancel the dialog bu clicking anywhere outside the dialog.
            dialog.setCancelable(true);
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.my_dialog);
            //Mention the name of the layout of your custom dialog.
            dialog.setContentView(R.layout.dialog_main);

            //Initializing the views of the dialog.
            final EditText opis_napake = dialog.findViewById(R.id.error_desc_et);
            final EditText opis_popravila = dialog.findViewById(R.id.repair_desc_et);
            if (isChecked) {
                opis_popravila.setVisibility(View.VISIBLE);
            } else {
                opis_napake.setVisibility(View.VISIBLE);
            }
            Button submitButton = dialog.findViewById(R.id.submit_button);
            ImageButton cancel = dialog.findViewById(R.id.cancel_button);
            cancel.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    sw_state.setChecked(!isChecked);
                    if (!isChecked) {

                        row.setBackground(recycler.getContext().getDrawable(R.drawable.card_background));
                    }

                    else{
                        row.setBackground(recycler.getContext().getDrawable(R.drawable.card_background_red));
                    }
                    dialog.dismiss();
                    opis_popravila.setVisibility(View.GONE);
                    opis_napake.setVisibility(View.GONE);

                }
            });

            submitButton.setOnClickListener(new View.OnClickListener() {
                String opis="";
                @Override
                public void onClick(View v) {
                    if (isChecked) {
                        opis = opis_popravila.getText().toString();
                    } else {
                        opis = opis_napake.getText().toString();
                    }
                    ((MainActivity) recycler.getContext()).setStatus(inv,isChecked?1:0,opis);

                    dialog.dismiss();
                    opis_popravila.setVisibility(View.GONE);
                    opis_napake.setVisibility(View.GONE);

                }
            });
            dialog.setOnKeyListener((arg0, keyCode, event) -> {
                // TODO Auto-generated method stub
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    sw_state.setChecked(!isChecked);
                    if (!isChecked) {

                        row.setBackground(recycler.getContext().getDrawable(R.drawable.card_background));
                    }

                    else{
                        row.setBackground(recycler.getContext().getDrawable(R.drawable.card_background_red));
                    }

                    opis_popravila.setVisibility(View.GONE);
                    opis_napake.setVisibility(View.GONE);
                    dialog.dismiss();
                }
                return true;
            });
            dialog.show();

        }
        private void drawSwitch(RowViewHolder  holder, boolean checked) {
            if (checked) {

                holder.itemView.findViewById(R.id.background_layout).setBackground(recycler.getContext().getDrawable(R.drawable.card_background));
            }

            else{
                holder.itemView.findViewById(R.id.background_layout).setBackground(recycler.getContext().getDrawable(R.drawable.card_background_red));
            }
        }

public RowViewHolder(View view) {
            super(view);
            row = view.findViewById(R.id.background_layout);
            row_num = view.findViewById(R.id.txtrow);
            txtopis = view.findViewById(R.id.txtOpis);
            inv_st = view.findViewById(R.id.inv_st);
            imagePic = view.findViewById(R.id.imagePic);
     //       imageInfo = view.findViewById(R.id.Info);
            imageEdit = view.findViewById(R.id.imageEdit);
            tv_edit = view.findViewById(R.id.tv_edit);
            imageLoc = view.findViewById(R.id.imageLoc);
            imageArrow = view.findViewById(R.id.kartica_expand);
            expandeditems=view.findViewById(R.id.expandedItems);
            Edit=view.findViewById(R.id.Edit);
            Loc=view.findViewById(R.id.Loc);
            linLayInfo=view.findViewById(R.id.linLayInfo);
            Pic=view.findViewById(R.id.Pic);
            edit_extra=view.findViewById(R.id.edit_extra);
            btn_assign=view.findViewById(R.id.btn_assign);
            sw_state=view.findViewById(R.id.sw_state);



        }


    }

    public RowAdapter(List<LineItem> list) { ///itemsAdapter
        this.rowList = list;
        this.filteredRowList = list;
    }

    @Override
    public int getItemViewType(int position)
    {
        return position;
    }


    @NonNull
    public RowViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {


            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_layout, viewGroup, false);
            return new RowViewHolder(view);

    }


    @Override
    public void onBindViewHolder(RowViewHolder  holder/*ViewHolder*/, int position) { //ViewHolder

        LineItem lineItem = filteredRowList.get(position);
        holder.row_num.setText(lineItem.row_num);
        holder.txtopis.setText(lineItem.opis);
        holder.inv_st.setText(lineItem.inv_st);



       // holder.imageEdit.setContentDescription(lineItem.assignedTo);
        if(!lineItem.getExpanded()){
            holder.collapse_cardView();

            //Log.d("CARDVIEW"+holder.row_num.getText().toString(), "setExpanded: ");
        }
        //holder.imageLoc.setContentDescription(lineItem.Json.toString());
        holder.sw_state.setChecked(lineItem.status == 1);
        holder.drawSwitch(holder,lineItem.status == 1 );


        holder.sw_state.setOnCheckedChangeListener((compoundButton, b) -> {
            if (!compoundButton.isPressed()) {
                return;
            }
            int adapterPosition= holder.getAdapterPosition();
            LineItem tapped = filteredRowList.get(adapterPosition);
            holder.drawSwitch(holder, compoundButton.isChecked());
            filteredRowList.set(adapterPosition, new LineItem(
                    tapped.row_num,
                    tapped.inv_st,
                    tapped.opis,
                    //tapped.geoCoord,
                    tapped.geoLokacija,
                    tapped.assignedTo,
                    compoundButton.isChecked()?1:0));
                    if (onItemCheckedChangeListener != null) {
                        onItemCheckedChangeListener.onItemCheckedChanged(adapterPosition, compoundButton.isChecked());
                    }

            holder.showUpdateStatusDialog(tapped.inv_st, compoundButton.isChecked());
            //holder.collapse_extraEdit();
        }
        );
        holder.row.setOnClickListener(v -> {
            final boolean visibility = holder.expandeditems.getVisibility() == View.VISIBLE;
            final int adapterPosition = holder.getAdapterPosition();
            if (!visibility) {///če je kartica zaprta

                holder.expand_cardView(); ////odpre kartico
                filteredRowList.get(adapterPosition).setExpanded(true);///označi kartico kot odprto
                if (prev_expanded != -1 && prev_expanded != position)
                    {///če ni prvi klik na kartico ali če ni klik na isto kartico
                    if (recycler.findViewHolderForLayoutPosition(prev_expanded) != null)
                        {///Če je element še viden na zaslonu
                            recycler.findViewHolderForLayoutPosition(prev_expanded).itemView.setActivated(false); //zapre prej odprto kartico
                            recycler.findViewHolderForLayoutPosition(prev_expanded).itemView.findViewById(R.id.expandedItems).setVisibility(View.GONE);
                        //označi pozicijo odprte kartice


                    }
                        filteredRowList.get(prev_expanded).setExpanded(false);
                }

                prev_expanded=adapterPosition;
            } else { /////Če je kartica odprta
                holder.collapse_cardView(); //zapre karrtico
                filteredRowList.get(adapterPosition).setExpanded(false);


            }
            //TransitionManager.beginDelayedTransition(recycler);

            // notifyItemChanged(position);

        });

        holder.linLayInfo.setOnClickListener(v ->
        {///IKONA INFO
            //Toast.makeText(recycler.getContext(), "klik INFO", Toast.LENGTH_SHORT).show();
           // ((MainActivity) recycler.getContext()).findEquipment(lineItem.inv_st, "NFC", "klik na tabelo");
        });

/////////////////ZA klike na ikone oz layout ikon na expnded kartici
        holder.Pic.setOnClickListener(v ->
                ////IKONA SLIKA
                ((MainActivity) recycler.getContext()).openFragment(Constants.FRAGMENT_IMAGE, lineItem.inv_st)
        );
/////////////////ZA klike na ikone oz layout ikon na expnded kartici
        holder.Loc.setOnClickListener(v ->
                ////IKONA LOKACIJA
                ((MainActivity) recycler.getContext()).openFragment(Constants.FRAGMENT_MAP, lineItem.geoLokacija)
        );

/////////////////ZA klike na ikone oz layout ikon na expnded kartici
        holder.Edit.setOnClickListener(v ->
                        ////IKONA EDIT
                {
                    if (holder.edit_extra.getVisibility() == View.VISIBLE) {
                        holder.collapse_extraEdit();
                    } else {
                        holder.expand_extraEdit(lineItem.assignedTo);
                    }
                }
        );
        holder.btn_assign.setOnClickListener(v ->
                        ////IKONA EDIT
                {
                    ((MainActivity) recycler.getContext()).setEquipmentUser(holder.inv_st.getText().toString());
                }
        );




















    }


    public void setOnItemCheckedChangeListener(@Nullable OnItemCheckedChangeListener onItemCheckedChangeListener) {
        this.onItemCheckedChangeListener = onItemCheckedChangeListener;
    }

    interface OnItemCheckedChangeListener {
        /**
         * Fired when the item check state is changed
         */
        void onItemCheckedChanged(int position, boolean isChecked);
    }

    @Override
    public void onViewRecycled(@NonNull RowViewHolder holder) {
        super.onViewRecycled(holder);
        holder.sw_state.setOnCheckedChangeListener(null);
    }

    @Override
    public int getItemCount() {
        return filteredRowList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.recycler= recyclerView;
    }

    @Override
    public Filter getFilter(){
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence)
            {
                String charString = charSequence.toString();
                if (charString.isEmpty() || charString.length()<3)
                {
                   filteredRowList = rowList;
                } else {

                    List<LineItem> filteredList = new ArrayList<>();
                    for (LineItem lineItem : rowList) {
                        for (Field classVariable : lineItem.getClass().getDeclaredFields()) {

                           //System.out.println("Fields: " + Modifier.toString(classVariable.getModifiers())); // modyfiers
                            String test = classVariable.getName();
                            //Log.d("Fields" , test);        //real var name
                            if(test=="opis" || test=="inv_st" ) {
                                classVariable.setAccessible(true);                                //var readable


                                String strValue = "";
                                try {
                                    strValue = (String) classVariable.get(lineItem);
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
