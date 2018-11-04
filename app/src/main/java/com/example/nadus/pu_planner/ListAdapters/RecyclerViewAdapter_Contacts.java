package com.example.nadus.pu_planner.ListAdapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.example.nadus.pu_planner.FirebaseAdapters.ContactsAdapter;
import com.example.nadus.pu_planner.R;

import java.util.ArrayList;
import java.util.List;

public class RecyclerViewAdapter_Contacts extends RecyclerView.Adapter<RecyclerViewAdapter_Contacts.ViewHolder> {

    private List<ArrayList<String>> mData;
    public LayoutInflater mInflater;
    public ItemClickListener mClickListener;

    // data is passed into the constructor
    public RecyclerViewAdapter_Contacts(Context context, List<ArrayList<String>> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.contact_layout_2, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.contact_name.setText(mData.get(position).get(2));
        holder.contact_designation.setText(mData.get(position).get(4));
        holder.contact_id.setText(mData.get(position).get(0));
        holder.contact_number.setText(mData.get(position).get(8));
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView contact_name, contact_designation, contact_id, contact_number;

        ViewHolder(View itemView) {
            super(itemView);
            contact_name = itemView.findViewById(R.id.contact_name);
            contact_designation = itemView.findViewById(R.id.contact_designation);
            contact_id = itemView.findViewById(R.id.contact_id);
            contact_number = itemView.findViewById(R.id.contact_number);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    // convenience method for getting data at click position
//    public String getItem(int id) {
//        return mData.get(id);
//    }

    // allows clicks events to be caught
    public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}