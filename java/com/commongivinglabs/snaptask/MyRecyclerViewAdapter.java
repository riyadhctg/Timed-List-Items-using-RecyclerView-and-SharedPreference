package com.commongivinglabs.snaptask;


import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class MyRecyclerViewAdapter extends RecyclerView.Adapter<MyRecyclerViewAdapter.ViewHolder> {

    private List<String> taskData;
    private List<String> timeData;
    private LayoutInflater mInflater;
    private onDoneClick mListener;
    Integer row_position;
    SharedPreferences task_db;
    SharedPreferences date_db;

    //Constructor
    MyRecyclerViewAdapter(Context context, List<String> task, List<String> date, onDoneClick listener) {

        this.mInflater = LayoutInflater.from(context);
        this.mListener = listener;
        this.taskData = task;
        this.timeData = date;
        this.task_db = context.getSharedPreferences("task_information_db", MODE_PRIVATE);
        this.date_db = context.getSharedPreferences("date_time", MODE_PRIVATE);

    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.recyclerview_row, parent, false);

        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        row_position = position;
        String task = taskData.get(position);
        String date = timeData.get(position);

        String[] dateParts = date.split(":");
        String dateHour = dateParts[0];
        String dateMin = dateParts[1];
        String dateFormated;

        int dateHourInt = Integer.valueOf(dateHour);

        if (dateHourInt == 0) {
            dateFormated = "Expires in " + dateMin + " m";
            holder.dateView.setTextColor(Color.parseColor("#FF0000"));
        }else {
            dateFormated = "Expires in " + dateHour + " h " + dateMin + " m";
        }

        holder.taskView.setText(task);
        holder.dateView.setText(dateFormated);
        holder.clockIconView.setText("\u23F2");
        //this listener will be called from MainActivity
        holder.doneButtonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onClick(v, position);//where position is the position in the adapter
            }
        });
    }


    // total number of rows
    @Override
    public int getItemCount() {
        return taskData.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView taskView;
        TextView dateView;
        TextView clockIconView;
        //Button doneButtonView;
        CheckBox doneButtonView;

        ViewHolder(View itemView) {
            super(itemView);

            taskView = itemView.findViewById(R.id.taskViewBox);
            dateView = itemView.findViewById(R.id.dateViewBox);
            clockIconView = itemView.findViewById(R.id.clockIcon);
            doneButtonView = itemView.findViewById(R.id.doneButton);
        }
    }


    // parent activity will implement this method to respond to click events
    public interface onDoneClick {
        void onClick(View v, int position);
    }


}