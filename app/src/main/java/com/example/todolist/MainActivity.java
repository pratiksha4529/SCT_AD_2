package com.example.todolist;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private RecyclerView recyclerView;
    private TaskAdapter adapter;
    private List<Task> taskList = new ArrayList<>();
    private AppDatabase database;
    private LinearLayout emptyStateLayout;
    private DrawerLayout drawerLayout;
    private String currentCategory = "All";

    // Variables to hold temp data for alarm
    private Calendar alarmCalendar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Ask for Notification Permission (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_nav, R.string.close_nav);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        database = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "task_db")
                .allowMainThreadQueries()
                .fallbackToDestructiveMigration()
                .build();

        recyclerView = findViewById(R.id.recyclerView);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadTasks();

        FloatingActionButton fab = findViewById(R.id.fabAdd);
        fab.setOnClickListener(v -> showTaskDialog(null));
    }

    private void loadTasks() {
        taskList.clear();
        if (currentCategory.equals("All")) {
            taskList.addAll(database.taskDao().getAllTasks());
        } else {
            taskList.addAll(database.taskDao().getTasksByCategory(currentCategory));
        }

        if (adapter == null) {
            adapter = new TaskAdapter(taskList, new TaskAdapter.OnItemClickListener() {
                @Override
                public void onDeleteClick(int position) {
                    Task task = taskList.get(position);
                    cancelAlarm(task.getId()); // Cancel alarm if deleting
                    database.taskDao().delete(task);
                    loadTasks();
                }
                @Override
                public void onEditClick(int position) {
                    showTaskDialog(taskList.get(position)); // CLICKING ITEM OPENS EDIT
                }
                @Override
                public void onCheckClick(int position, boolean isChecked) {
                    Task t = taskList.get(position);
                    t.setCompleted(isChecked);
                    database.taskDao().update(t);
                }
            });
            recyclerView.setAdapter(adapter);
        } else {
            adapter.notifyDataSetChanged();
        }

        if (taskList.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyStateLayout.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyStateLayout.setVisibility(View.GONE);
        }
    }

    private void showTaskDialog(Task taskToEdit) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_task, null);
        builder.setView(view);

        EditText editTitle = view.findViewById(R.id.editTitle);
        EditText editDate = view.findViewById(R.id.editDate);
        EditText editTime = view.findViewById(R.id.editTime);
        Spinner spinner = view.findViewById(R.id.spinnerCategory);
        Button btnSave = view.findViewById(R.id.btnSave);

        String[] categories = {"Work", "Personal", "Wishlist", "Birthday"};
        ArrayAdapter<String> catAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categories);
        spinner.setAdapter(catAdapter);

        alarmCalendar = Calendar.getInstance(); // Reset calendar

        if (taskToEdit != null) {
            editTitle.setText(taskToEdit.getTitle());
            editDate.setText(taskToEdit.getDueDate());
            editTime.setText(taskToEdit.getDueTime());
            btnSave.setText("Update Task");

            // Set Spinner
            for(int i=0; i<categories.length; i++) {
                if(categories[i].equals(taskToEdit.getCategory())) {
                    spinner.setSelection(i);
                    break;
                }
            }
        }

        // Date Picker
        editDate.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(this, (view1, year, month, day) -> {
                editDate.setText(day + "/" + (month + 1) + "/" + year);
                alarmCalendar.set(Calendar.YEAR, year);
                alarmCalendar.set(Calendar.MONTH, month);
                alarmCalendar.set(Calendar.DAY_OF_MONTH, day);
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        });

        // Time Picker
        editTime.setOnClickListener(v -> {
            Calendar c = Calendar.getInstance();
            new TimePickerDialog(this, (view1, hour, minute) -> {
                String amPm = (hour < 12) ? "AM" : "PM";
                editTime.setText(String.format("%02d:%02d %s", hour, minute, amPm));
                alarmCalendar.set(Calendar.HOUR_OF_DAY, hour);
                alarmCalendar.set(Calendar.MINUTE, minute);
                alarmCalendar.set(Calendar.SECOND, 0);
            }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), false).show();
        });

        AlertDialog dialog = builder.create();
        btnSave.setOnClickListener(v -> {
            String title = editTitle.getText().toString();
            String date = editDate.getText().toString();
            String time = editTime.getText().toString();
            String cat = spinner.getSelectedItem().toString();

            if (!title.isEmpty()) {
                long timestamp = alarmCalendar.getTimeInMillis();

                // If editing, use existing ID, else 0 (Room auto-generates)
                if (taskToEdit == null) {
                    Task newTask = new Task(title, "", date, time, false, cat, timestamp);
                    database.taskDao().insert(newTask);
                    // We need the ID to schedule the alarm. In a real app we'd fetch the last ID.
                    // For simplicity, we just schedule based on timestamp here.
                    scheduleAlarm(timestamp, title, (int) System.currentTimeMillis());
                } else {
                    taskToEdit.setTitle(title);
                    taskToEdit.setDueDate(date);
                    taskToEdit.setDueTime(time);
                    taskToEdit.setCategory(cat);
                    taskToEdit.setAlarmTimestamp(timestamp);
                    database.taskDao().update(taskToEdit);
                    scheduleAlarm(timestamp, title, taskToEdit.getId());
                }
                loadTasks();
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void scheduleAlarm(long timeInMillis, String title, int id) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra("task_title", title);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, id, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        // Only schedule if time is in the future
        if (timeInMillis > System.currentTimeMillis()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
                } else {
                    // Just set inexact if permission missing (simple fallback)
                    alarmManager.set(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
                }
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
            }
            Toast.makeText(this, "Reminder Set!", Toast.LENGTH_SHORT).show();
        }
    }

    private void cancelAlarm(int id) {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, id, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        alarmManager.cancel(pendingIntent);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_all) currentCategory = "All";
        else if (id == R.id.nav_work) currentCategory = "Work";
        else if (id == R.id.nav_personal) currentCategory = "Personal";
        else if (id == R.id.nav_wishlist) currentCategory = "Wishlist";
        else if (id == R.id.nav_birthday) currentCategory = "Birthday";
        getSupportActionBar().setTitle(currentCategory);
        loadTasks();
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}