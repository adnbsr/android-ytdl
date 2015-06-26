package com.andromedalabs.ytdl.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import com.andromedalabs.ytdl.R;
import java.io.File;

/**
 * Created by adnan on 1/5/15.
 */
public class NotificationHelper {

    private final Context c;
    private NotificationCompat.Builder mBuilder;
    private NotificationManager mNotifyMgr;
    private static final int NOTIFICATION_ID = 0;

    public NotificationHelper(Context c){
        this.c = c;
        init();
    }

    public void init(){
        mNotifyMgr = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);

        mBuilder = new NotificationCompat.Builder(c)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(c.getString(R.string.app_name));

        mNotifyMgr.notify(NOTIFICATION_ID, mBuilder.build());
    }

    public void onProgress(int progress){

        mBuilder.setProgress(100, progress, false);
        mBuilder.setContentText(progress + "% is completed!");
        mNotifyMgr.notify(NOTIFICATION_ID, mBuilder.build());
    }

    public void onError(){
        mBuilder.setContentText("File is not found :( ");
        mNotifyMgr.notify(NOTIFICATION_ID,mBuilder.build());
    }

    @SuppressWarnings("unused")
    public void onFinish(){
        mNotifyMgr.cancel(0);
    }

    public void onComplete(File f){
        mBuilder.setContentText(f.getName())
                .setProgress(0,0,false);
        Intent play = new Intent(Intent.ACTION_VIEW);
        play.setDataAndType(Uri.fromFile(f),"audio/mp3");
        PendingIntent playPendingIntent = PendingIntent.getActivity(c,0,play,PendingIntent.FLAG_UPDATE_CURRENT);

        Notification mNotification = mBuilder.build();
        //mNotification.contentIntent = playPendingIntent;

        mNotifyMgr.notify(NOTIFICATION_ID, mNotification);
    }
}