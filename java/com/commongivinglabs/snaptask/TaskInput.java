package com.commongivinglabs.snaptask;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.List;

public class TaskInput extends AppCompatActivity {

    Integer max_number_of_task = 15;
    List<String> task_detail = new ArrayList<>();
    List<String> countdown_timer_for_task = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_input);

        task_name_generator();

        final EditText task_input_box = (EditText) findViewById(R.id.task_input_box);
        Button add = (Button) findViewById(R.id.add);

        //putting focus on the edit text field and open keyboard
        task_input_box.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);


        //we will also handle add/remove of Task in SP in this class
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                List<String> task_storage_name = new ArrayList<>();

                //Common for all tasks - task related
                String task_text = task_input_box.getText().toString();
                SharedPreferences task_storage = getSharedPreferences("task_information_db", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = task_storage.edit();

                //Common for all tasks - time related
                long currentSystemTime = System.currentTimeMillis();
                String date_for_storage = String.valueOf(currentSystemTime);
                SharedPreferences date_storage = getSharedPreferences("date_time", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor1 = date_storage.edit();
                SharedPreferences task_storage_read = getSharedPreferences("task_information_db", MODE_PRIVATE);

                for (int i = 0; i < max_number_of_task; i++) {
                    task_storage_name.add(i, task_storage_read.getString(task_detail.get(i), null));
                    if (task_storage_name.get(i) == null) {
                        editor.putString(task_detail.get(i), task_text);
                        editor.commit();
                        editor1.putString(countdown_timer_for_task.get(i), date_for_storage);
                        System.out.println("Saving into Task 1");
                        editor1.commit();
                        break;
                    } else {
                        System.out.println("You already have 15 tasks");
                    }
                }

                //hide keyboard after adding the task
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(task_input_box.getWindowToken(), 0);
                //go to main activity
                Intent i = new Intent(TaskInput.this, MainActivity.class);
                startActivityForResult(i, 1);

            }
        });


        Button cancel = (Button) findViewById(R.id.cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(TaskInput.this, MainActivity.class);
                startActivity(i);
                //hide keyboard
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(task_input_box.getWindowToken(), 0);
            }
        });


    }

    public void task_name_generator() {
        for (int i = 0; i < max_number_of_task; i++) {
            String task_name = "task";
            String date_name = "date";
            task_detail.add(i, (task_name + String.valueOf(i)));
            countdown_timer_for_task.add(i, (date_name + String.valueOf(i)));
            System.out.println(task_detail.get(i) + "  " + countdown_timer_for_task.get(i));
        }
    }

}
