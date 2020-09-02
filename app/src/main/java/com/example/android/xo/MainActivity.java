package com.example.android.xo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import java.util.zip.Inflater;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.board);
        LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
        inflater.inflate(R.layout.board , null);
//        LayoutInflater.from(getApplicationContext()).inflate(R.layout.board , null , true);
    }
    public void click(){

    }
}