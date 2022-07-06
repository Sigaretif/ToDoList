package com.example.todolist2;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.StrictMode;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    public static final int ADD_TASK_REQUEST = 1;
    public static final int EDIT_TASK_REQUEST = 2;

    private TaskViewModel taskViewModel;

    private String usedCategories;

    private android.widget.SearchView searchView;

    private TaskAdapter adapter;
    private FiltersListAdapter filtersListAdapter;

    List<Task> tasksFromViewModel;

    ArrayList<String> listOfCategories = new ArrayList<>();

    Spinner filterSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FloatingActionButton addTaskButton = findViewById(R.id.addTaskButton);
        addTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent  = new Intent(MainActivity.this, NewTaskActivity.class);
                startActivityForResult(intent, ADD_TASK_REQUEST);
            }
        });

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);

//        TaskAdapter adapter = new TaskAdapter();
//        recyclerView.setAdapter(adapter);

        taskViewModel = ViewModelProviders.of(this).get(TaskViewModel.class);

        adapter = new TaskAdapter();
        recyclerView.setAdapter(adapter);

        taskViewModel.getAll().observe(this, new Observer<List<Task>>() {
            @Override
            public void onChanged(@Nullable List<Task> tasks) {
                tasksFromViewModel = tasks;
                adapter.setTasks(tasks);
                usedCategories = "";
                for(Task task: tasks){
                    usedCategories += task.getCategory() + "\t";
                }
                if(getIntent().hasExtra("NotificationStartup")){
                    searchView.setQuery(getIntent().getStringExtra("NotificationStartup"), true);
                    searchView.clearFocus();
                }
            }
        });

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder viewHolder1) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Delete task?")
                        .setMessage("Do you really want to delete this task?")
                        .setIcon(R.drawable.ic_baseline_delete_24)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                taskViewModel.delete(adapter.getTask(viewHolder.getAdapterPosition()));
                                Toast.makeText(MainActivity.this, "Task deleted", Toast.LENGTH_LONG).show();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                finish();
                                overridePendingTransition(0, 0);
                                startActivity(getIntent());
                                overridePendingTransition(0, 0);
                            }
                        })
                        .show();
            }
        }).attachToRecyclerView(recyclerView);

        adapter.setOnTaskClickListener(new TaskAdapter.OnTaskClickListener() {
            @Override
            public void onTaskClick(Task task) {
                Intent intent = new Intent(MainActivity.this, NewTaskActivity.class);
                intent.putExtra("ID", task.getId());
                intent.putExtra("TITLE", task.getTitle());
                intent.putExtra("DESCRIPTION", task.getDescription());
                intent.putExtra("CATEGORY", task.getCategory());
                intent.putExtra("START", task.getStart());
                intent.putExtra("END", task.getEnd());
                intent.putExtra("FILEPATH", task.getFilePath());
                startActivityForResult(intent, EDIT_TASK_REQUEST);
            }
        });

        adapter.setOnDoneButtonClickListener(new TaskAdapter.OnDoneButtonClickListener() {
            @Override
            public void onDoneButtonClick(Task task) {
                String title = task.getTitle();
                String description = task.getDescription();
                String category = task.getCategory();
                String start = task.getStart();
                String end = task.getEnd();
                int id = task.getId();
                boolean isFinished = task.getFinished();
                String filePath = task.getFilePath();

                Task updatedTask = new Task(title, category, description, start, end, !isFinished, filePath);
                updatedTask.setId(id);
                taskViewModel.update(updatedTask);
            }
        });


        adapter.setOnAttachmentClickListener(new TaskAdapter.OnAttachmentClickListener() {
            @Override
            public void onAttachmentClick(Task task) {
                String fileName = task.getFilePath();
                File file = new File(getFilesDir().getAbsolutePath()+"/"+fileName);
                Uri uri = FileProvider.getUriForFile(MainActivity.this, BuildConfig.APPLICATION_ID + ".provider", file);
                String extension = getContentResolver().getType(uri);
                Intent intent = new Intent();
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.setAction(Intent.ACTION_VIEW);
                intent.setDataAndType(uri, extension);
                startActivity(intent);
            }
        });

        searchView = findViewById(R.id.searchView);
        searchView.clearFocus();
        searchView.setOnQueryTextListener(new android.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                filterTasks(s);
                return true;
            }
        });
        searchView.clearFocus();

        retrieveCategoriesFromPreferences();

        initializeFilterSpinner();

    }

    private void initializeFilterSpinner(){
        filterSpinner = findViewById(R.id.filterSpinnerID);
        ArrayList<Filter> filterList = new ArrayList<>();

        Filter filterInit = new Filter();
        filterInit.setTitle("Pick category");
        filterInit.setSelected(false);
        filterList.add(filterInit);

        for(String category: listOfCategories){
            Filter filter = new Filter();
            filter.setTitle(category);
            filter.setSelected(false);
            filterList.add(filter);
        }

        filtersListAdapter = new FiltersListAdapter(this, 0, filterList);
        filterSpinner.setAdapter(filtersListAdapter);

        filtersListAdapter.setOnCheckBoxClickListener(new FiltersListAdapter.OnCheckBoxClickListener() {
            @Override
            public void onCheckBoxClick(Filter filter) {
                filterTasksByCategory();
            }
        });
    }

    private Map<String, ?> getAllPreferences(){
        SharedPreferences sharedPref = getSharedPreferences("GLOBAL_PREFERENCES", Context.MODE_PRIVATE);
        return sharedPref.getAll();
    }

    private void retrieveCategoriesFromPreferences(){
        Map<String, ?> cities = getAllPreferences();
        for(Map.Entry<String, ?> entry : cities.entrySet()){
            if(entry.getKey().startsWith("category\t")){
                String[] categoryName = entry.getKey().split("\t");
                listOfCategories.add(categoryName[1]);
            }
        }
    }

    private void filterTasksByCategory(){
        List<Task> filteredList = new ArrayList<>();
        for(Task task: tasksFromViewModel){
            for(Filter filter: filtersListAdapter.filterList){
                if(filter.isSelected()){
                    if(task.getCategory().equals(filter.getTitle())){
                        filteredList.add(task);
                    }
                }
            }
        }
        adapter.setFilteredList(filteredList);

        int filterCounter = 0;
        for(Filter filter: filtersListAdapter.filterList){
            if(!filter.isSelected()){
                filterCounter++;
            }
        }
        if(filterCounter == filtersListAdapter.filterList.size()){
            adapter.setFilteredList(tasksFromViewModel);
        }
    }

    private void filterTasks(String text){
        List<Task> filteredList = new ArrayList<>();
        for(Task task: tasksFromViewModel){
            if(task.getTitle().toLowerCase().contains(text.toLowerCase())){
                filteredList.add(task);
            }
        }
        if(filteredList.isEmpty()){
            Toast.makeText(this, "Tasks not found", Toast.LENGTH_SHORT).show();
        }
        else{
            adapter.setFilteredList(filteredList);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == ADD_TASK_REQUEST && resultCode == RESULT_OK){
            String title = data.getStringExtra("TITLE");
            String description = data.getStringExtra("DESCRIPTION");
            String start = data.getStringExtra("START");
            String end = data.getStringExtra("END");
            String category = data.getStringExtra("CATEGORY");
            String filePath = data.getStringExtra("FILEPATH");

            Task task = new Task(title, category, description, start, end, false, filePath);
            taskViewModel.insert(task);

            long timeOfNotification = data.getLongExtra("NOTIFICATION_TIME", System.currentTimeMillis());
            createNotificationChannel();
            setAlarm(timeOfNotification, task.getId(), task.getTitle());

            Toast.makeText(this, "Task saved", Toast.LENGTH_SHORT).show();
        }
        else if(requestCode == EDIT_TASK_REQUEST && resultCode == RESULT_OK) {
            int id = data.getIntExtra("ID", -1);
            if(id == -1){
                Toast.makeText(this, "Task cannot be updated", Toast.LENGTH_LONG).show();
                return;
            }
            String title = data.getStringExtra("TITLE");
            String description = data.getStringExtra("DESCRIPTION");
            String start = data.getStringExtra("START");
            String end = data.getStringExtra("END");
            String category = data.getStringExtra("CATEGORY");
            String filePath = data.getStringExtra("FILEPATH");

            Task task = new Task(title, category, description, start, end, false, filePath);
            task.setId(id);
            taskViewModel.update(task);

            long timeOfNotification = data.getLongExtra("NOTIFICATION_TIME", System.currentTimeMillis());
            createNotificationChannel();
            setAlarm(timeOfNotification, task.getId(), task.getTitle());

            Toast.makeText(this, "Task updated", Toast.LENGTH_LONG).show();
        }
        else{
            Toast.makeText(this, "Task not saved", Toast.LENGTH_SHORT).show();
        }
        initializeFilterSpinner();
    }

    private void setAlarm(long time, int reqCode, String taskTitle){
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        intent.putExtra("notificationTaskTitle", taskTitle);
        intent.putExtra("notificationTaskId", reqCode);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, reqCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        alarmManager.set(AlarmManager.RTC_WAKEUP, time, pendingIntent);
    }

    private void createNotificationChannel(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            CharSequence name = "Pink Panther reminder channel";
            String description = "Channel for alarm manager";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("pinkpantherchannel", name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.categories_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case R.id.go_to_categories:
                goToCategories();
                return true;
            default:
                return  super.onOptionsItemSelected(item);
        }
    }

    private void goToCategories(){
        Intent intent = new Intent(MainActivity.this, CategoriesActivity.class);
        intent.putExtra("usedCategories", usedCategories);
        startActivity(intent);
    }
}