package com.example.todolist2;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

public class NewTaskActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

    private ArrayList<String> categories;

    private EditText editTextTitle;
    private EditText editTextDescription;
    private EditText editTextCategory;
    private Spinner spinnerCategory;
    private DatePicker datePickerStart;
    private DatePicker datePickerEnd;
    private TextView attachmentPath;
    private Button attachmentAdd;
    private TimePicker timePickerEnd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_task);

        categories = new ArrayList<>();
        categories.add("");
        retrieveCategoriesFromPreferences();

        editTextTitle = findViewById(R.id.titleID);
        editTextDescription = findViewById(R.id.descriptionID);
        editTextCategory = findViewById(R.id.categoryID);
        spinnerCategory = findViewById(R.id.categorySpinnerID);
        datePickerEnd = findViewById(R.id.endDatePickerID);
        datePickerStart = findViewById(R.id.startDatePickerID);
        spinnerCategory.setOnItemSelectedListener(this);
        attachmentPath = findViewById(R.id.attachmentPath);
        attachmentAdd = findViewById(R.id.attachmentAddButton);
        timePickerEnd = findViewById(R.id.endTimePickerID);
        timePickerEnd.setIs24HourView(true);

        attachmentAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.setType("*/*");
                startActivityForResult(intent,3);
            }
        });


        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(arrayAdapter);

        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_close_24);

        Intent intent = getIntent();
        if(intent.hasExtra("ID")){
            setTitle("Edit Task");
            editTextTitle.setText(intent.getStringExtra("TITLE"));
            //editTextCategory.setText(intent.getStringExtra("CATEGORY"));
            editTextDescription.setText(intent.getStringExtra("DESCRIPTION"));
            String[] startDateSplitted = intent.getStringExtra("START").split("-");
            datePickerStart.updateDate(Integer.parseInt(startDateSplitted[2]), Integer.parseInt(startDateSplitted[1])-1, Integer.parseInt(startDateSplitted[0]));
            String[] endDateSplitted = intent.getStringExtra("END").split("-");
            datePickerEnd.updateDate(Integer.parseInt(endDateSplitted[2]), Integer.parseInt(endDateSplitted[1])-1, Integer.parseInt(endDateSplitted[0]));
            attachmentPath.setText(intent.getStringExtra("FILEPATH"));
            String categoryFromIntent = intent.getStringExtra("CATEGORY");
            for(int i=0; i<categories.size(); i++){
                if(categories.get(i).equals(categoryFromIntent)){
                    spinnerCategory.setSelection(i);
                    break;
                }
            }
        }else{
            setTitle("Add Task");
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.new_task_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case R.id.save_task:
                saveTask();
                return true;
            default:
                return  super.onOptionsItemSelected(item);
        }
    }

    private void setPreference(String key, String value){
        SharedPreferences sharedPref = getSharedPreferences("GLOBAL_PREFERENCES", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(key, value);
        editor.apply();
    }

    private Map<String, ?> getAllPreferences(){
        SharedPreferences sharedPref = getSharedPreferences("GLOBAL_PREFERENCES", Context.MODE_PRIVATE);
        return sharedPref.getAll();
    }

    private void retrieveCategoriesFromPreferences(){
        Map<String, ?> cities = getAllPreferences();
        for(Map.Entry<String, ?> entry : cities.entrySet()){
            if(entry.getKey().startsWith("category\t")){
                String[] category = entry.getKey().split("\t");
                categories.add(category[1]);
            }
        }
    }

    private void saveTask(){
        String title = editTextTitle.getText().toString();
        String description = editTextDescription.getText().toString();
        int dayStart = datePickerStart.getDayOfMonth();
        int monthStart = datePickerStart.getMonth() + 1;
        int yearStart = datePickerStart.getYear();
        String start = dayStart + "-" + monthStart + "-" + yearStart;
        int dayEnd = datePickerEnd.getDayOfMonth();
        int monthEnd = datePickerEnd.getMonth() + 1;
        int yearEnd = datePickerEnd.getYear();
        String end = dayEnd + "-" + monthEnd + "-" + yearEnd;
        String category = editTextCategory.getText().toString();
        String filePath = attachmentPath.getText().toString();

        if(title.trim().isEmpty() || description.trim().isEmpty() || category.trim().isEmpty()){
            if(title.trim().isEmpty()) {
                editTextTitle.setError("This field cannot be blank");
            }
            if(description.trim().isEmpty()) {
                editTextDescription.setError("This field cannot be blank");
            }
            if(category.trim().isEmpty()) {
                editTextCategory.setError("This field cannot be blank");
            }
            return;
        }

        SharedPreferences sharedPref = getSharedPreferences("GLOBAL_PREFERENCES", Context.MODE_PRIVATE);
        if(!sharedPref.contains("category\t" + category)){
            setPreference("category\t" + category, category);
        }

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, yearEnd);
        calendar.set(Calendar.MONTH, monthEnd - 1);
        calendar.set(Calendar.DAY_OF_MONTH, dayEnd);
        calendar.set(Calendar.HOUR_OF_DAY, timePickerEnd.getHour());
        calendar.set(Calendar.MINUTE, timePickerEnd.getMinute());
        calendar.set(Calendar.SECOND, 0);


        Intent intent = new Intent();
        intent.putExtra("TITLE", title);
        intent.putExtra("DESCRIPTION", description);
        intent.putExtra("START", start);
        intent.putExtra("END", end);
        intent.putExtra("CATEGORY", category);
        intent.putExtra("FILEPATH", filePath);

        int id = getIntent().getIntExtra("ID", -1);
        if(id != -1){
            intent.putExtra("ID", id);
        }

        intent.putExtra("NOTIFICATION_TIME", calendar.getTimeInMillis());

        setResult(RESULT_OK, intent);
        finish();

    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        Object category = adapterView.getItemAtPosition(i);
        editTextCategory.setText(category.toString());
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        editTextCategory.setText("");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == 3 && resultCode == RESULT_OK && data != null){
            Uri uri = data.getData();
            getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            attachmentPath.setText(getFileName(uri));

            File dst = new File(getFilesDir().getAbsolutePath()+"/"+attachmentPath.getText().toString());
            try {
                if(dst.createNewFile()){
                    copyFile(uri, dst);
                }
            } catch (IOException e) {
                Toast.makeText(this, "Error occurred. Cannot add attachment", Toast.LENGTH_SHORT).show();
            }

            Toast.makeText(this, "Attachment added successfully", Toast.LENGTH_SHORT).show();
        }
    }

    public void copyFile(Uri src, File dst) throws IOException {
        InputStream in = getContentResolver().openInputStream(src);
        try {
            OutputStream out = new FileOutputStream(dst);
            try {
                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            } finally {
                out.close();
            }
        } finally {
            in.close();
        }
    }

    @SuppressLint("Range")
    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }
}