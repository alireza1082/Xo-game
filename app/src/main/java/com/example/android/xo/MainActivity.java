package com.example.android.xo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.LayoutInflater;

import java.util.zip.Inflater;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
//        inflater.inflate();
        LayoutInflater.from(getApplicationContext()).inflate(R.layout.board , null);
    }
}