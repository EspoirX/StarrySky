package com.lzx.musiclib;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.lzx.musiclib.example.ListPlayExampleActivity;
import com.lzx.musiclib.example.PlayDetailActivity;
import com.lzx.musiclib.example.RequestBeforePlayActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btn1).setOnClickListener(v -> startListPlayExampleActivity());
        findViewById(R.id.btn2).setOnClickListener(v -> startRequestBeforePlayActivity());
        findViewById(R.id.btn3).setOnClickListener(v -> startPlayDetailActivity());

        List<Info> list = new ArrayList<>();
        list.add(new Info("1"));
        list.add(new Info("2"));
        list.add(new Info("3"));

        Info[] backupArray = new Info[list.size()];
        list.toArray(backupArray);

        Collections.shuffle(list);

        for (int i = 0; i < list.size(); i++) {
            Log.i("xian", "排序后 = " + list.get(i).text);
        }

        List<Info> backup = Arrays.asList(backupArray);

        for (int i = 0; i < backup.size(); i++) {
            Log.i("xian", "结果 = " + backup.get(i).text);
        }
    }

    private class Info implements Cloneable {
        String text;

        Info(String text) {
            this.text = text;
        }

        @Override
        protected Object clone() {
            Object obj = null;
            //调用Object类的clone方法，返回一个Object实例
            try {
                obj = super.clone();
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
            return obj;
        }
    }

    public void startListPlayExampleActivity() {
        startActivity(new Intent(this, ListPlayExampleActivity.class));
    }

    public void startRequestBeforePlayActivity() {
        startActivity(new Intent(this, RequestBeforePlayActivity.class));
    }

    public void startPlayDetailActivity() {
        startActivity(new Intent(this, PlayDetailActivity.class));
    }
}
