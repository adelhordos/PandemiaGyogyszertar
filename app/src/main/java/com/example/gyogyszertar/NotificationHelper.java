package com.example.gyogyszertar;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class NotificationHelper {
    private static final String CHANNEL_ID="notification_channel";
    private final int NOTIFICATION_ID=0;
    private NotificationManager manager;
    private Context mContext;

    public NotificationHelper(Context context){
        this.mContext=context;
        this.manager=(NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        createChannel();
    }
    private void createChannel(){
        //minden értesítés ami erre a csatornára megy ki, annak ugyanazok lesznek a tulajdonságai
        if(Build.VERSION.SDK_INT<Build.VERSION_CODES.O){
            return; //nem hozok létre csatornát
        }
        NotificationChannel channel=new NotificationChannel(CHANNEL_ID,"notification",NotificationManager.IMPORTANCE_DEFAULT);
        channel.enableVibration(true);
        channel.setDescription("Pandemia gyógyszertár értesítése");
        manager.createNotificationChannel(channel);
    }
    public void send(String message){
        Intent intent=new Intent(mContext,ProductsActivity.class);
        PendingIntent pendingIntent=PendingIntent.getActivity(mContext,NOTIFICATION_ID,intent,PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder=new NotificationCompat.Builder(mContext,CHANNEL_ID).setContentTitle("Pandemia gyógyszertár")
                .setContentText(message).setSmallIcon(R.drawable.icon_cart).setContentIntent(pendingIntent);
       manager.notify(NOTIFICATION_ID,builder.build());
    }
    public void cancel(){
        manager.cancel(NOTIFICATION_ID);
    }
}
