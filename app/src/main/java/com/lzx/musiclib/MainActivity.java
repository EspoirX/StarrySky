package com.lzx.musiclib;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.lzx.musiclib.example.ListPlayExampleActivity;
import com.lzx.musiclib.example.PlayDetailActivity;
import com.lzx.musiclib.example.RequestBeforePlayActivity;


public class MainActivity extends AppCompatActivity {

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btn1).setOnClickListener(v -> startListPlayExampleActivity());
        findViewById(R.id.btn2).setOnClickListener(v -> startRequestBeforePlayActivity());
        findViewById(R.id.btn3).setOnClickListener(v -> startPlayDetailActivity());

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
