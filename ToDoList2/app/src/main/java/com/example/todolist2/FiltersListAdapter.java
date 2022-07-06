package com.example.todolist2;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class FiltersListAdapter extends ArrayAdapter<Filter> {

    private Context mContext;
    public ArrayList<Filter> filterList;
    private FiltersListAdapter filtersListAdapter;
    private boolean isFromView = false;

    private OnCheckBoxClickListener listener;

    public FiltersListAdapter(Context context, int resource, List<Filter> objects){
        super(context, resource, objects);
        this.mContext = context;
        this.filterList = (ArrayList<Filter>) objects;
        this.filtersListAdapter = this;
    }

    @Override
    public View getDropDownView(int position, View convertView,
                                ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    public View getCustomView(final int position, View convertView, ViewGroup parent){
        final ViewHolder holder;
        if(convertView == null){
            LayoutInflater layoutInflater = LayoutInflater.from(mContext);
            convertView = layoutInflater.inflate(R.layout.filters_item, null);
            holder = new ViewHolder();
            holder.mTextView = convertView.findViewById(R.id.filterTextId);
            holder.mCheckBox = convertView.findViewById(R.id.filterCheckBoxId);
            convertView.setTag(holder);
        }
        else{
            holder = (ViewHolder) convertView.getTag();
        }

        holder.mTextView.setText(filterList.get(position).getTitle());
        isFromView = true;
        holder.mCheckBox.setChecked(filterList.get(position).isSelected());
        isFromView = false;
        if(position == 0){
            holder.mCheckBox.setVisibility(View.INVISIBLE);
        }
        else{
            holder.mCheckBox.setVisibility(View.VISIBLE);
        }

        holder.mCheckBox.setTag(position);

        holder.mCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(!isFromView){
                    filterList.get(position).setSelected(b);
                    if(listener != null){
                        listener.onCheckBoxClick(filterList.get(position));
                    }
                }
            }
        });

        return  convertView;
    }

    private class ViewHolder{
        private TextView mTextView;
        public CheckBox mCheckBox;
    }

    public interface OnCheckBoxClickListener {
        void onCheckBoxClick(Filter filter);
    }

    public void setOnCheckBoxClickListener(OnCheckBoxClickListener listener) {
        this.listener = listener;
    }
}
