package com.example.tempcw.cz_bd_demo.map;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;


import com.example.tempcw.cz_bd_demo.R;

import java.util.ArrayList;

public class DetailActivity extends Activity {

    private int type = -1;
    private ArrayList<String> data = null;

    private ListView lv = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.route_detail);
        findViews();
        setListener();
        initData();
        initView();
    }

    private void initData() {
        Intent localIntent = getIntent();
        Bundle localBundle = localIntent.getExtras();
        type = localBundle.getInt("type");
        data = localBundle.getStringArrayList("data");
    }

    private void setListener() {
    }

    private void findViews() {
        lv = (ListView) findViewById(R.id.lv_detail);
    }

    private void initView() {
        switch (type) {
            case 0:
                setTitle(getResources().getString(R.string.drive_plan));
                break;
            case 1:
                setTitle(getResources().getString(R.string.bus_plan));
                break;
            case 2:
                setTitle(getResources().getString(R.string.walk_plan));
                break;
        }
        DetailAdapter adapter = new DetailAdapter(this, data);
        lv.setAdapter(adapter);
    }
}
