package com.example.android.xo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class GameBoard extends AppCompatActivity {

    public static final int x_player = 12;
    public static final int o_player = 15;
    public static final int empty = 20;
    public static final int No_Winner = -1;
    int winner = No_Winner;
    int [] status = {empty , empty , empty , empty , empty , empty , empty , empty , empty};
    int active_player;
    int [][] winner_state = {{0,1,2} , {3,4,5} , {6,7,8} , {0,3,6} , {1,4,7} , {2,5,8} , {0,4,8} , {2,4,6}};

    private Long backPressedTime = 0L;
    TextView current;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.board);
//        LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
//        inflater.inflate(R.layout.board , null);
//        LayoutInflater.from(getApplicationContext()).inflate(R.layout.board , null , true);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (Math.random() < 0.5)
            active_player = x_player;
        else
            active_player = o_player;
        Toast.makeText(this , "first player is : " + (active_player == x_player ? "X" : "O") , Toast.LENGTH_LONG).show();
        current = findViewById(R.id.current_player);
        if (active_player == x_player)
            current.setText(R.string.current_x);
        else
            current.setText(R.string.current_o);
    }
    public void click(View view){
        ImageView image = (ImageView) view;
        int num = Integer.parseInt((String) view.getTag());
        if (status[num] != empty || winner != No_Winner)
            return;
        image.setAlpha(0f);
        if (active_player == o_player) {
            image.setImageResource(R.drawable.o);
            active_player = x_player;
            status[num] = o_player;
        }
        else if (active_player == x_player) {
            image.setImageResource(R.drawable.x);
            active_player = o_player;
            status[num] = x_player;
        }
        image.animate().alpha(1f).setDuration(1000);
        winner = checkWinner();
        if (winner != No_Winner){
            Toast.makeText(this , "winner : " + ((winner == x_player) ? "x player" : "o player") , Toast.LENGTH_SHORT ).show();
        }
        if (filled() || winner != No_Winner)
            finish();
    }

    public int checkWinner(){
        for (int [] post : winner_state){
            if (status[post[0]] == status[post[1]] && status[post[1]] == status[post[2]] && status[post[0]] != empty) {
                return status[post[0]];
            }
        }
        return No_Winner;
    }
    public boolean filled(){
        for (int j : status) {
            if (j == empty)
                return false;
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
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
