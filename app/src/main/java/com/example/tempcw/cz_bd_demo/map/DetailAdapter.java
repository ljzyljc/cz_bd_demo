package com.example.tempcw.cz_bd_demo.map;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


import com.example.tempcw.cz_bd_demo.R;

import java.util.ArrayList;


public class DetailAdapter extends BaseAdapter {
    private ArrayList<String> data;
    private Context context;
    private ViewHolder holder = null;

    public DetailAdapter(Context context, ArrayList<String> data) {
        this.data = data;
        this.context = context;
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.route_detail_item, null);
            holder = new ViewHolder();
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.tv = (TextView) convertView.findViewById(R.id.tv_info);
        holder.tv.setText(data.get(position));
        return convertView;
    }

    class ViewHolder {
        TextView tv;
    }
}
