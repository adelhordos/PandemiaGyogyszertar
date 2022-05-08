package com.example.gyogyszertar;
import android.os.AsyncTask;
import android.widget.TextView;

import java.lang.ref.WeakReference;

public class RandomAsyncTask extends android.os.AsyncTask<Void,Void, String> {

    private WeakReference<TextView> mTextView;
    RandomAsyncTask(TextView textView) {
        mTextView=new WeakReference<>(textView);
    }

    //külön szálon fut a függvény
    @Override
    protected String doInBackground(Void... voids) {
        int ms=10000;
        try{
            Thread.sleep(ms);
        }catch (InterruptedException e){
            e.printStackTrace();
        }
        return "Bejelentkezés vendégként";
    }

    @Override
    protected void onPostExecute(String s) {
        mTextView.get().setText(s);
    }
}