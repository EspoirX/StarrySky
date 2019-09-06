package com.lzx.musiclib;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.lzx.musiclib.example.ListPlayExampleActivity;


public class MainActivity extends AppCompatActivity {

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.btn1).setOnClickListener(v -> startListPlayExampleActivity());
    }

    public void startListPlayExampleActivity() {
        startActivity(new Intent(this, ListPlayExampleActivity.class));
    }


}
