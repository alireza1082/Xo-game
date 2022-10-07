package com.example.android.xo;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import java.util.concurrent.TimeUnit;

import io.reactivex.Completable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import ir.tapsell.plus.TapsellPlus;
import ir.tapsell.plus.TapsellPlusInitListener;
import ir.tapsell.plus.model.AdNetworkError;
import ir.tapsell.plus.model.AdNetworks;

public class SplashActivity extends AppCompatActivity {

    private CompositeDisposable disposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        disposable.add(Completable
                .timer(3, TimeUnit.SECONDS, AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::runMainActivity));

        initTapsell();
    }

    private void runMainActivity() {
        Intent mainIntent = new Intent(this, MainActivity.class);
        startActivity(mainIntent);
        finish();
    }

    private void initTapsell(){
        TapsellPlus.initialize(this, BuildConfig.TAPSELL_KEY,
                new TapsellPlusInitListener() {
                    @Override
                    public void onInitializeSuccess(AdNetworks adNetworks) {
                        Log.d("onInitializeSuccess", adNetworks.name());
                    }

                    @Override
                    public void onInitializeFailed(AdNetworks adNetworks,
                                                   AdNetworkError adNetworkError) {
                        Log.e("onInitializeFailed", "ad network: " + adNetworks.name() + ", error: " +	adNetworkError.getErrorMessage());
                    }
                });
    }
}