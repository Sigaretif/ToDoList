package com.example.todolist2;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Map;

public class CategoriesActivity extends AppCompatActivity {

    ListView listView;
    ArrayList<String> listOfCategories = new ArrayList<>();
    CategoriesListAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categories);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        String usedCategories = getIntent().getStringExtra("usedCategories");

        retrieveCategoriesFromPreferences();

        listView = findViewById(R.id.categoriesListID);
        adapter = new CategoriesListAdapter(listOfCategories, CategoriesActivity.this, usedCategories);
        listView.setAdapter(adapter);

        setTitle("Edit Categories");

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

    private void setPreference(String key, String value){
        SharedPreferences sharedPref = getSharedPreferences("GLOBAL_PREFERENCES", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public void addCategory(View view){
        SharedPreferences sharedPref = getSharedPreferences("GLOBAL_PREFERENCES", Context.MODE_PRIVATE);
        EditText newCategory = findViewById(R.id.newCategoryNameID);
        String category = newCategory.getText().toString();
        if(!category.equals("")){
            if(!sharedPref.contains("category\t" + category)){
                setPreference("category\t" + category, category);
                listOfCategories.add(category);
                adapter.updateAdapter(listOfCategories);
            }
            else{
                Toast.makeText(getApplicationContext(), "Category already exists.", Toast.LENGTH_LONG).show();
            }
        }
    }


}