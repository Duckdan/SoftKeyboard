package com.study.softkeyboard;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;

public class Main3Activity extends AppCompatActivity {

    private ListView lv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);
        lv = (ListView) findViewById(R.id.lv);

        ArrayList lists = new ArrayList();
        for (int i = 0; i < 100; i++) {
            lists.add(new Object());
        }

        lv.setAdapter(new ArrayAdapter<Object>(this, R.layout.item_edit_layout, R.id.tv, lists) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                if (convertView != null) {
                    EditText et = convertView.findViewById(R.id.et);
                    et.setHint("请输入内容" + "===" + position);
                }
                return super.getView(position, convertView, parent);
            }
        });
    }
}
