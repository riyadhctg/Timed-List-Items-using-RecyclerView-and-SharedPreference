package com.commongivinglabs.snaptask;


import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    MyRecyclerViewAdapter adapter; //creating an object of the custom RecyclerView class
    Integer max_number_of_task = 15; //This is the max number of tasks a user can add
    java.util.List<String> task_storage_name = new java.util.ArrayList<>(); //this is where the tasks are saved. This will be passed to the recylcerView
    java.util.List<String> time_remaing = new java.util.ArrayList<>(); //this is where the 'Time Remainings for each task' are saved. This will be passed to the recylcerView
    java.util.List<String> task_detail = new java.util.ArrayList<>(); //list for task names used in SharedPreference as identifier: task1, task2, .... task15
    java.util.List<String> countdown_timer_for_task = new java.util.ArrayList<>(); //list for 'date' IDs used in SharedPreference as identifier: date1, ... date15

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getIntent().setAction("Already created");

        /* current date and time banner code starts */
        long date = System.currentTimeMillis(); //this variable will be used to view current time and date in the app banner
        TextView todaysDate = (TextView) findViewById(R.id.todaysDate);
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMM dd yyyy");
        String dateString = sdf.format(date);
        todaysDate.setText(dateString);
        /* current date and time banner code ends */



        task_name_generator();

        System.out.println("task_storage_name.size()" + task_storage_name.size());

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (task_storage_name.size() < max_number_of_task) {
                    Intent i = new Intent(MainActivity.this, TaskInput.class);
                    startActivity(i);
                }
                if (task_storage_name.size() == max_number_of_task){
                    Toast.makeText(MainActivity.this, "You already have maximum number of tasks",
                            Toast.LENGTH_SHORT).show();

            }

            }
        });

        /*settings button*/
        ImageButton settingButton = (ImageButton) findViewById(R.id.settingsButton);
        settingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(i);
            }
        });

        /* /settings button*/

        /*random backgrond */
        LinearLayout bannerLayout = (LinearLayout) findViewById(R.id.dateViewBakgroundLayout);
        int images[] = {R.drawable.glitter, R.drawable.landscape, R.drawable.mountains, R.drawable.planet, R.drawable.ship};
        Random rand = new Random();
        int i=rand.nextInt(images.length);
        bannerLayout.setBackground(getResources().getDrawable(images[i]));
        /* /random background */

        recyclerViewSetup();
        deleteTaskOnExpiry();

    }


    //set up the RecyclerView - this is where all the magic happens... it sets and gets tasks, notification etc.
    public void recyclerViewSetup() {

        final SharedPreferences task_storage_read = getSharedPreferences("task_information_db", MODE_PRIVATE);
        final SharedPreferences date_storage = getSharedPreferences("date_time", Context.MODE_PRIVATE);


        if (task_storage_name.size() <= max_number_of_task) {
            for (int i = 0; i < max_number_of_task; i++) {
                int j = task_storage_name.size();
                if (task_storage_read.getString(task_detail.get(i), null) != null) {
                    //getting the task from SharedPreference (saved in the TaskInput class) and saving them in the task_storage_name List
                    task_storage_name.add(j, task_storage_read.getString(task_detail.get(i), null));
                    //getting the associated time calculated using the 'calculateTimeDifference' function and saving them in the time_remaing List
                    time_remaing.add(j, String.valueOf(calculateTimeDifference(countdown_timer_for_task.get(i))[0]) + ":"
                            + String.valueOf(calculateTimeDifference(countdown_timer_for_task.get(i))[1]));

                    // NOTFCN: DON'T DELETE
                    /* setting notifcations */
                    /* NOTFCN: DON'T DELETE
                    String taskString = task_storage_name.get(j);
                        int taskStringLength = taskString.length();
                        String shortenedTaskString;

                        if (taskStringLength < 10) {
                            shortenedTaskString = taskString.length() < taskStringLength ? taskString : taskString.substring(0, taskStringLength);
                        } else {
                            shortenedTaskString = taskString.length() < 10 ? taskString : taskString.substring(0, 10);
                        }
                        System.out.println("Main writer: " + shortenedTaskString + "NOT-DEBUG " + notificationPreference() + " minutes " + "ID: " + j);
                        scheduleNotification(getNotification(shortenedTaskString + "... is about to expire in " + notificationPreference() + " minutes"), j);
                    */
                        /* setting notifcations */

                    //storage, list index, and notification synchronizer
                    if (task_storage_name.get(j) != task_storage_read.getString(task_detail.get(j), null)) {

                        /* task and time sychronizer starts */
                        task_storage_read.edit().putString(task_detail.get(i), null).commit();
                        task_storage_read.edit().putString(task_detail.get(j), task_storage_name.get(j)).commit();
                        date_storage.edit().putString(countdown_timer_for_task.get(j), date_storage.getString(countdown_timer_for_task.get(i), null)).commit();
                        date_storage.edit().putString(countdown_timer_for_task.get(i), null).commit();
                        /* task and time sychronizer ends */

                        // NOTFCN: DON'T DELETE
                        /* notification sychronizer */
                        /* cancel the last notification in order to synchronize the notifcations
                        this is because when a task is deleted, notifications are sychornized just as tasks, but one deleted
                         task means the last notification (unless the last notifcation was deleted) will go one position up
                         in terms of notification ID (and position in the list, which should be the same number). However, the last
                         notification will not move, rather will be copied to the upper position, leaving behind a duplicate
                         notification in the last ID (at this point, there is not real task in the position/ID). That's why
                         the following two lines of code is needed to delete that last notification */

                        /* NOTFCN: DON'T DELETE
                        int lastNotificationID = task_storage_name.size();
                        cancelNotification(lastNotificationID);
                            //re-write the notification
                            taskString = task_storage_name.get(j);
                            taskStringLength = taskString.length();
                            if (taskStringLength < 10) {
                                shortenedTaskString = taskString.length() < taskStringLength ? taskString : taskString.substring(0, taskStringLength);
                            } else {
                                shortenedTaskString = taskString.length() < 10 ? taskString : taskString.substring(0, 10);
                            }
                            scheduleNotification(getNotification(shortenedTaskString + "... is about to expire in " + notificationPreference() + " minutes"), j);
                        System.out.println("Sync Writer After Sync: " + shortenedTaskString + "NOT-DEBUG " + notificationPreference() + " minutes " + "ID: " + j);
                        // notification sychronizer
                        */

                    }

                }
            }
        }

        android.support.v7.widget.RecyclerView recyclerView = findViewById(R.id.recyclerViewTask);
        android.support.v7.widget.LinearLayoutManager layoutManager = new android.support.v7.widget.LinearLayoutManager(this);

        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);

        recyclerView.setLayoutManager(layoutManager);

        /*
        //this adds border around rows
        android.support.v7.widget.DividerItemDecoration dividerItemDecoration = new android.support.v7.widget.DividerItemDecoration(recyclerView.getContext(),
                layoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);*/


        adapter = new MyRecyclerViewAdapter(this, task_storage_name, time_remaing, new MyRecyclerViewAdapter.onDoneClick() {
            //onClick on any 'done' button in the list
            @Override
            public void onClick(View v, int position) {
                System.out.println("Cancelling this: NOT-DEBUG  " + task_storage_name.get(position) + "ID: " + position);
                //clear task and time SPs
                task_storage_read.edit().putString(task_detail.get(position), null).commit();
                date_storage.edit().putString(countdown_timer_for_task.get(position), null).commit();

                //clear task and time lists
                task_storage_name.remove(position);
                time_remaing.remove(position);

                /*NOTFCN: DON'T DELETE
                //clear notification
                cancelNotification(position);*/

                //refresh the list
                r.run();

            }
        });
        recyclerView.setAdapter(adapter);

    }

    //this calculates the time remaining in millies which is used in setting notifications
    public long calculateTimeDifferenceInMillies(String dateNumber) {

        long time_difference=0;
        SharedPreferences date1_storage_read = getSharedPreferences("date_time", MODE_PRIVATE);
        String date_saved_string = date1_storage_read.getString(dateNumber, null);


        //if the date1_saved_string was stored (not NULL) the following function will execute
        if (date_saved_string != null) {
            //get current time in millis (long)
            long currentSystemTime = System.currentTimeMillis();
            //convert date1_saved_string to long
            long savedDate1 = Long.valueOf(date_saved_string);
            //calculate difference between current and saved time (millis in long)
            time_difference = currentSystemTime - savedDate1;
        }
        System.out.println("TIME diff: " + time_difference);
        return time_difference;

        }

    //this calculates and returns the time remaining in HH:MM:SS format
    public int[] calculateTimeDifference(String dateNumber) {

        int[] arrayOfTime = {100, 100, 100};
        SharedPreferences date1_storage_read = getSharedPreferences("date_time", MODE_PRIVATE);
        String date_saved_string = date1_storage_read.getString(dateNumber, null);


        //if the date1_saved_string was stored (not NULL) the following function will execute
        if (date_saved_string != null) {
            //get current time in millis (long)
            long currentSystemTime = System.currentTimeMillis();
            //convert date1_saved_string to long
            long savedDate1 = Long.valueOf(date_saved_string);
            //calculate difference between current and saved time (millis in long)
            long time_difference = currentSystemTime - savedDate1;

            System.out.println("TIME diff main: " + time_difference);

            //if the difference is less than 24 hours (86400000 millis) then execute the following function
            if (time_difference < 86400000) {
                //get the time difference in hour
                long time_diff_hr = time_difference / 1000 / 60 / 60;
                //get the remaining hour by substracting from 23 (not 24 because we calculate min and sec as well.. e.g. 23hr:59min:60sec)
                long remaining_hr = 23 - time_diff_hr;
                int remaining_hr_int = (int) remaining_hr;
                arrayOfTime[0] = remaining_hr_int;
                //get the remainder in hr calculation to calculate remaining min
                long remainder_min = (time_difference - time_diff_hr * 1000 * 60 * 60) / 1000 / 60;
                long remaining_min = 59 - remainder_min;
                int remaining_min_int = (int) remaining_min;
                arrayOfTime[1] = remaining_min_int;
                //get the remainder in min calculation to calculate remaining sec
                long remainder_sec = (time_difference - remainder_min * 60 * 1000) / 1000;
                long remaining_sec = 60 - remainder_sec;
                int remaining_sec_int = (int) remaining_sec;
                arrayOfTime[2] = remaining_sec_int;
            } else {

            }

        }
        return arrayOfTime;

    }

    /* running the refresh in a separate thread for efficiency */
    final Runnable r = new Runnable() {
        public void run() {
            refresh();
        }
    };
    public void refresh() {
        Intent intent = getIntent();
        overridePendingTransition(0, 0);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        finish();
        overridePendingTransition(0, 0);
        startActivity(intent);
    }
    /* running the refresh in a separate thread for efficiency */

    //delete task, time, notification on expiry
    public void deleteTaskOnExpiry() {

        SharedPreferences task_storage_read = getSharedPreferences("task_information_db", MODE_PRIVATE);
        SharedPreferences date1_storage_read = getSharedPreferences("date_time", MODE_PRIVATE);

        System.out.println("LIST: " + task_storage_name);

        for (int i = 0; i < task_storage_name.size(); i++) {
            if ((task_storage_read.getString(task_detail.get(i), null) != null)
                    && (date1_storage_read.getString(countdown_timer_for_task.get(i), null) != null)) {
                long currentSystemTime = System.currentTimeMillis();
                //convert date1_saved_string to long
                long savedDate = Long.valueOf(date1_storage_read.getString(countdown_timer_for_task.get(i), null));
                //calculate difference between current and saved time (millis in long)
                long time_difference = currentSystemTime - savedDate;



                if (time_difference >= 86400000) {

                    /* NOTFCN: DON'T DELETE delete notification
                    cancelNotification(i);
                    delete notification*/

                    task_storage_read.edit().putString(task_detail.get(i), null).commit();
                    date1_storage_read.edit().putString(countdown_timer_for_task.get(i), null).commit();
                    task_storage_name.remove(i);
                    time_remaing.remove(i);
                    r.run();
                }

            }

        }
    }




    @Override
    protected void onResume() {

        String action = getIntent().getAction();
        // Prevent endless loop by adding a unique action, don't restart if action is present
        if(action == null || !action.equals("Already created")) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }
        // Remove the unique action so the next time onResume is called it will restart
        else
            getIntent().setAction(null);

        super.onResume();
        deleteTaskOnExpiry();
    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
                    Intent i = new Intent(MainActivity.this, SettingsActivity.class);
                    startActivity(i);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //generates IDs/Names for tasks (e.g. task1, .. task15) and dates/time remaining (e.g. date1,.... date15)
    public void task_name_generator() {
        for (int i = 0; i < max_number_of_task; i++) {
            String task_name = "task";
            String date_name = "date";
            task_detail.add(i, (task_name + String.valueOf(i)));
            countdown_timer_for_task.add(i, (date_name + String.valueOf(i)));
            System.out.println(task_detail.get(i) + "  " + countdown_timer_for_task.get(i));
        }
    }


    /* NOTFCN: DON'T DELETE - all notification related functions for Pro Version

    //sets notification
    private void scheduleNotification(Notification notification, int taskNumber) {

        String setActionString = String.valueOf(taskNumber);
        String notificationPrefString = notificationPreference();

        //get the preferred notification duration in millis
        long notificationPrefLong = Long.valueOf(notificationPrefString) * 60 * 1000;

        Intent intent = new Intent(this, NotificationReceiver.class);
        intent.setAction(setActionString); //using setAction in the intent helps to have specific intent for each task. the action is synced with the taskNumber
        intent.putExtra(NotificationReceiver.NOTIFICATION_ID, taskNumber); //this is where we are passing the notification ID
        intent.putExtra(NotificationReceiver.NOTIFICATION, notification); //this is where we are passing the notifications details (content, title etc.)
        PendingIntent pi = PendingIntent.getBroadcast(this, taskNumber, intent, PendingIntent.FLAG_ONE_SHOT); // Pending Intent creates alarm. Requires code (here, taskNumber) is the ID of each PendingIntent... this ID is needed in order to later access the same alarm

        System.out.println("NOT-DEBUG " + "SET FUNCTION for ID " + taskNumber);

        long INTERVAL = 86400000 - calculateTimeDifferenceInMillies(countdown_timer_for_task.get(taskNumber)) - notificationPrefLong;
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + INTERVAL, pi);

    }


    //cancels notification by re-creating the pendingIntent with same RequestCode
    private void cancelNotification(int taskNumber) {

        String setActionString = String.valueOf(taskNumber);

        Intent intent = new Intent(this, NotificationReceiver.class);
        intent.setAction(setActionString);
        PendingIntent pi = PendingIntent.getBroadcast(this, taskNumber, intent, PendingIntent.FLAG_ONE_SHOT);

        System.out.println("NOT-DEBUG " + "CANCEL FUNCTION for ID " + taskNumber);

        AlarmManager alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);

        alarmManager.cancel(pi);

    }

    //notification creator. The return of this function is passed to the scheduleNotification function which uses the return as an input and put them in the intent's extra which eventually used in pendingIntent
    private Notification getNotification(String content) {
        String NOTIFICATION_CHANNEL_ID = "my_channel_id_01";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "My Notifications", NotificationManager.IMPORTANCE_HIGH);

            // Configure the notification channel.
            notificationChannel.setDescription("Channel description");
            notificationChannel.enableLights(true);

            //notificationChannel.setLightColor(Color.RED);
            notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            notificationChannel.enableVibration(true);
        }

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getApplicationContext(), NOTIFICATION_CHANNEL_ID);

        notificationBuilder.setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setSmallIcon(R.drawable.ic_launcher)
                .setTicker("100")
                .setPriority(Notification.PRIORITY_MAX)
                .setContentTitle("Task Expiry")
                .setContentText(content) //this is content of the notification which is needed to be passed in this function
                .setContentInfo("100");
        return notificationBuilder.build();
    }



    //get user notification preference (120 mins etc.)
    public String notificationPreference () {
        SharedPreferences notificationPreference = PreferenceManager.getDefaultSharedPreferences(this);
        String notificationPreferenceTime = notificationPreference.getString("notification_time", null);
        System.out.println("Notification: " + notificationPreferenceTime);
        return notificationPreferenceTime;
    }

    */


}


/* For notification: NOTFCN: DON'T DELETE   this is Backup for NotificationReceiver Class... create a class called NotificationReceiver and then copy this content there,
include <receiver android:name=".NotificationReceiver" /> in the Manifest

package com.commongivinglabs.snaptask;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;


public class NotificationReceiver  extends BroadcastReceiver {


    public static String NOTIFICATION_ID = "notification-id";
    public static String NOTIFICATION = "notification";


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onReceive(Context context, Intent intent) {

        NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = intent.getParcelableExtra(NOTIFICATION);

        int id = intent.getIntExtra(NOTIFICATION_ID, 0);

        if(intent.getExtras() != null && notification != null) {
            notificationManager.notify(id, notification);
            System.out.println("notificationInString: " + "ID: " + id);

        }


    }



}


 */