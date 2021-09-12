package com.example.reminders;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {

    private ArrayList<ReminderItem> myReminderList;
    private RecyclerViewClickListener listener;

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView whiteDotImageView;
        TextView reminderTitleView;
        TextView reminderDescriptionView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            whiteDotImageView = itemView.findViewById(R.id.whiteDot);
            reminderTitleView = itemView.findViewById(R.id.reminderTitle);
            reminderDescriptionView = itemView.findViewById(R.id.reminderDescription);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            listener.onClick(v, getAdapterPosition());
        }
    }

    public RecyclerAdapter(ArrayList<ReminderItem> reminderList, RecyclerViewClickListener listener){
        this.myReminderList=reminderList;
        this.listener=listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view =LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_row,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        try {
            ReminderItem currentReminderItem = myReminderList.get(position);
            String reminderDescription = currentReminderItem.getReminderDescriptionTime()
                    +"    "
                    +currentReminderItem.getReminderDescriptionDate()
                    +"    "
                    +currentReminderItem.getReminderDescriptionRepeat();
            holder.whiteDotImageView.setImageResource(currentReminderItem.getWhiteDotImage());
            holder.reminderTitleView.setText(currentReminderItem.getReminderTitle());
            holder.reminderDescriptionView.setText(reminderDescription);
        }catch (NullPointerException e){
            Log.e("Recycler Adapter", "onBindViewHolder: Null Pointer: " + e.getMessage() );
        }
    }

    @Override
    public int getItemCount() {
        return myReminderList.size();
    }

    public interface RecyclerViewClickListener{
        void onClick(View v, int position);
    }

}
