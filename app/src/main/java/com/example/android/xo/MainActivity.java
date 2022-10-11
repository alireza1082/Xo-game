package com.example.android.xo;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    MenuItem menuItem;
    private Long backPressedTime = 0L;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button = findViewById(R.id.start);
        button.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, GameBoard.class);
            startActivity(intent);
        });

        Button share = findViewById(R.id.share);
        share.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND);
            intent.putExtra(Intent.EXTRA_TEXT,
                    "https://myket.ir/app/" + BuildConfig.APPLICATION_ID);
            intent.setType("text/plain");
            Intent shareIntent = Intent.createChooser(intent, null);
            startActivity(shareIntent);
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menuItem = menu.add("about us").setOnMenuItemClickListener(item -> {
            startActivity(new Intent(MainActivity.this, AboutUs.class));
            return false;
        });
        menuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        if (backPressedTime + 3000 > System.currentTimeMillis()) {
            super.onBackPressed();
            finish();
        } else {
            Toast.makeText(this, "Press back again to leave the app.", Toast.LENGTH_LONG).show();
        }
        backPressedTime = System.currentTimeMillis();
    }
}