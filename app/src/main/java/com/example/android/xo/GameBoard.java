package com.example.android.xo;

import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.android.xo.Constants.Constants;

import java.util.concurrent.TimeUnit;

import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import ir.tapsell.plus.AdRequestCallback;
import ir.tapsell.plus.AdShowListener;
import ir.tapsell.plus.TapsellPlus;
import ir.tapsell.plus.TapsellPlusBannerType;
import ir.tapsell.plus.model.TapsellPlusAdModel;
import ir.tapsell.plus.model.TapsellPlusErrorModel;

public class GameBoard extends AppCompatActivity {

    public static final int x_player = 12;
    public static final int o_player = 15;
    public static final int empty = 20;
    public static final int No_Winner = -1;
    int winner = No_Winner;
    int[] status = {empty, empty, empty, empty, empty, empty, empty, empty, empty};
    int active_player;
    int[][] winner_state = {{0, 1, 2}, {3, 4, 5}, {6, 7, 8}, {0, 3, 6}, {1, 4, 7}, {2, 5, 8}, {0, 4, 8}, {2, 4, 6}};

    int x_points = 0;
    int o_points = 0;

    ImageView[] columns;

    private Long backPressedTime = 0L;
    TextView current;
    TextView x_point;
    TextView o_point;
    Resources resources;
    FrameLayout adContainer;

    private String standardBannerResponseId = "";
    private final CompositeDisposable disposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.board);
        resources = getResources();
        adContainer = findViewById(R.id.standard_ad_container);

        columns = new ImageView[]{findViewById(R.id.c0), findViewById(R.id.c1), findViewById(R.id.c2),
                findViewById(R.id.c3), findViewById(R.id.c4), findViewById(R.id.c5),
                findViewById(R.id.c6), findViewById(R.id.c7), findViewById(R.id.c8)};

        current = findViewById(R.id.current_player);
        x_point = findViewById(R.id.x_point);
        o_point = findViewById(R.id.o_point);

        startGame();

        x_point.setText(String.format(resources.getString(R.string.x_point), 0));
        o_point.setText(String.format(resources.getString(R.string.o_point), 0));
        requestTapsellStandard();
    }

    public void click(View view) {
        ImageView image = (ImageView) view;
        int num = Integer.parseInt((String) view.getTag());
        if (status[num] != empty || winner != No_Winner)
            return;
        image.setAlpha(0f);
        if (active_player == o_player) {
            image.setImageResource(R.drawable.o);
            active_player = x_player;
            status[num] = o_player;
        } else if (active_player == x_player) {
            image.setImageResource(R.drawable.x);
            active_player = o_player;
            status[num] = x_player;
        }
        image.animate().alpha(1f).setDuration(1000);
        winner = checkWinner();
        if (winner != No_Winner) {
            Toast.makeText(this, "winner : " + ((winner == x_player) ? "x player" : "o player"), Toast.LENGTH_SHORT).show();
        }
        if (filled() || winner != No_Winner) {
            plus_point(winner);
            disposable.add(Completable
                    .timer(1, TimeUnit.SECONDS, AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe(this::showDialogGame));
        }
        changeCurrentText();
    }

    private void showDialogGame() {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
        builder1.setMessage("Another Game?");
        builder1.setCancelable(false);

        builder1.setPositiveButton(
                "Continue",
                (dialog, id) -> finishGame());

        builder1.setNegativeButton(
                "Exit",
                (dialog, id) -> {
                    finish();
                    dialog.cancel();
                });

        AlertDialog alert11 = builder1.create();
        alert11.show();
    }

    private void clearMap() {
        for (int j = 0; j < 9; j++) {
            status[j] = empty;
            columns[j].setAlpha(0f);
        }
        winner = No_Winner;
    }

    private void finishGame() {
        clearMap();
        startGame();
    }

    @Override
    protected void onDestroy() {
        destroyAd();
        super.onDestroy();
    }

    private void startGame() {
        if (Math.random() < 0.5)
            active_player = x_player;
        else
            active_player = o_player;
        Toast.makeText(this, "first player is : " + (active_player == x_player ? "X" : "O"), Toast.LENGTH_LONG).show();

        changeCurrentText();
    }

    private void changeCurrentText() {
        if (active_player == x_player)
            current.setText(R.string.current_x);
        else
            current.setText(R.string.current_o);
    }

    private int checkWinner() {
        for (int[] post : winner_state) {
            if (status[post[0]] == status[post[1]] && status[post[1]] == status[post[2]] && status[post[0]] != empty) {
                return status[post[0]];
            }
        }
        return No_Winner;
    }

    private boolean filled() {
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

    private void plus_point(int player) {
        if (player == x_player) {
            x_points += 1;
            x_point.setText(String.format(resources.getString(R.string.x_point), x_points));
        } else if (player == o_player) {
            o_points += 1;
            o_point.setText(String.format(resources.getString(R.string.o_point), o_points));
        }
    }

    private void requestTapsellStandard() {
        TapsellPlus.requestStandardBannerAd(
                this, Constants.STANDARD_ZONE_ID,
                TapsellPlusBannerType.BANNER_320x50,
                new AdRequestCallback() {
                    @Override
                    public void response(TapsellPlusAdModel tapsellPlusAdModel) {
                        super.response(tapsellPlusAdModel);

                        standardBannerResponseId = tapsellPlusAdModel.getResponseId();
                        Log.i("Tapsell", "standard banner request created");
                    }

                    @Override
                    public void error(@NonNull String message) {
                        adContainer.setVisibility(View.GONE);
                        Log.e("Tapsell", "standard banner request got error");
                    }
                });

        disposable.add(Completable
                .timer(6, TimeUnit.SECONDS, AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::showStandardAd));
    }

    private void showStandardAd() {
        TapsellPlus.showStandardBannerAd(this, standardBannerResponseId,
                adContainer,
                new AdShowListener() {
                    @Override
                    public void onOpened(TapsellPlusAdModel tapsellPlusAdModel) {
                        Log.i("Tapsell", "standard banner opened");
                        adContainer.setVisibility(View.VISIBLE);
                        super.onOpened(tapsellPlusAdModel);
                    }

                    @Override
                    public void onError(TapsellPlusErrorModel tapsellPlusErrorModel) {
                        Log.i("Tapsell", "standard banner error on show");
                        adContainer.setVisibility(View.GONE);
                        super.onError(tapsellPlusErrorModel);
                    }
                });
    }

    private void destroyAd() {
        TapsellPlus.destroyStandardBanner(this, standardBannerResponseId, findViewById(R.id.standard_ad_container));
    }
}
