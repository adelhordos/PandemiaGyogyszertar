package com.example.gyogyszertar;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.loader.content.AsyncTaskLoader;

public class RandomAsyncLoader extends AsyncTaskLoader<String> {
    public RandomAsyncLoader(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        //biztosan megindul a loadInBackGround
        forceLoad();
    }

    @Nullable
    @Override
    public String loadInBackground() {
        int ms=10000;
        try{
            Thread.sleep(ms);
        }catch (InterruptedException e){
            e.printStackTrace();
        }
        return "Bejelentkezés vendégként";
    }
}
