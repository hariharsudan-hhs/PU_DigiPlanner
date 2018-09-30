package com.example.nadus.pu_planner.ListAdapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.nadus.pu_planner.R;

import java.util.List;

public class RecyclerViewAdapter_All_Calendar extends RecyclerView.Adapter<RecyclerViewAdapter_All_Calendar.ViewHolder> {

    public List<String> mData;
    public List<String> mData2;
    public List<String> mData3;
    public List<String> mData4;
    public LayoutInflater mInflater;
    public ItemClickListener mClickListener;

    // data is passed into the constructor
    public RecyclerViewAdapter_All_Calendar(Context context, List<String> data, List<String> data2, List<String> data3, List<String> data4) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        this.mData2 = data2;
        this.mData3 = data3;
        this.mData4 = data4;
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.calender_list_item, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String time = mData.get(position);
        holder.calendar_card_time.setText(time);

        String name = mData2.get(position);
        holder.calendar_card_name.setText(name);

        String description = mData3.get(position);
        holder.calendar_card_description.setText(description);

        String status = mData4.get(position);
        holder.calendar_card_status.setText(status);

    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView calendar_card_time, calendar_card_name, calendar_card_description, calendar_card_status;

        ViewHolder(View itemView) {
            super(itemView);
            calendar_card_time = itemView.findViewById(R.id.calendar_card_time);
            calendar_card_name = itemView.findViewById(R.id.calendar_card_name);
            calendar_card_description = itemView.findViewById(R.id.calendar_card_description);
            calendar_card_status = itemView.findViewById(R.id.calendar_card_status);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    // convenience method for getting data at click position
    public String getItem(int id) {
        return mData.get(id);
    }

    // allows clicks events to be caught
    public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}