package com.example.todolist2;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class CategoriesListAdapter extends BaseAdapter {

    private ArrayList<String> list = new ArrayList<String>();
    private Context context;
    private String usedCategories;

    public CategoriesListAdapter(ArrayList<String> list, Context context, String usedCategories) {
        this.list = list;
        this.context = context;
        this.usedCategories = usedCategories;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int pos) {
        return list.get(pos);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.categories_item, null);
        }

        TextView categoryName= (TextView)view.findViewById(R.id.categoryNameID);
        categoryName.setText(list.get(position));

        Button deleteButton= (Button)view.findViewById(R.id.deleteCategoryButtonID);
        deleteButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                boolean cannotDelete = false;
                String[] usedCategoriesSplitted = usedCategories.split("\t");
                for(String category: usedCategoriesSplitted){
                    if(list.get(position).equals(category)){
                        cannotDelete = true;
                    }
                }
                if(cannotDelete){
                    Toast.makeText(context, "Cannot delete category assigned to task.", Toast.LENGTH_LONG).show();
                }
                else{
                    new AlertDialog.Builder(context)
                            .setTitle("Delete category?")
                            .setMessage("Do you really want to delete this category?")
                            .setIcon(R.drawable.ic_baseline_delete_24)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    SharedPreferences sharedPref = context.getSharedPreferences("GLOBAL_PREFERENCES", Context.MODE_PRIVATE);
                                    sharedPref.edit().remove("category\t" + list.get(position)).apply();
                                    list.remove(position);
                                    updateAdapter(list);
                                }
                            })
                            .setNegativeButton("No", null)
                            .show();
                }
            }
        });
        return view;
    }

    public void updateAdapter(ArrayList<String> newList){
        this.list = newList;
        notifyDataSetChanged();
    }
}
