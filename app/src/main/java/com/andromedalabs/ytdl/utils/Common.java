package com.andromedalabs.ytdl.utils;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.webkit.MimeTypeMap;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.ProgressCallback;
import java.io.File;
import timber.log.Timber;

/**
 * Created by adnan on 5/29/15.
 */
public class Common {

  public static void downloadFile(final Context context,String url,String title,int type){

	  File f;

	  if(type == 0){
		  f = new File(getPath(), title+"."+ MimeTypeMap.getFileExtensionFromUrl(url));
	  }else{
		  f = new File(getPath(), title);
	  }

    final NotificationHelper mNotificationHelper = new NotificationHelper(context);

    Ion mIon = Ion.getDefault(context);

    mIon.getHttpClient()
        .getSSLSocketMiddleware()
        .setSpdyEnabled(false);

    mIon.with(context)
        .load(url)
        .progress(new ProgressCallback() {
          @Override public void onProgress(long downloaded, long total) {
            mNotificationHelper.onProgress((int) (100 * downloaded / total));
          }
        })
        .write(f)
        .setCallback(new FutureCallback<File>() {
          @Override public void onCompleted(Exception e, File file) {
            if (e != null) {
              Timber.e(e.getMessage());
              mNotificationHelper.onError();
              return;
            } else {
              mNotificationHelper.onComplete(file);
              writeFiletoAndroidMediaDB(context, file);
            }
          }
        });
  }

  public static void writeFiletoAndroidMediaDB(Context context,File file){
    MediaScannerConnection.scanFile(context, new String[] { file.toString() }, null,
        new MediaScannerConnection.OnScanCompletedListener() {
          public void onScanCompleted(String path, Uri uri) {
          }
        });
  }

  /**
   * @return Directory for file storage while downloading
   */
  public static File getPath(){

    File dir = new File(Environment.getExternalStorageDirectory()+"/Videos");

    if(!dir.exists()){
      dir.mkdir();
    }

    return dir;
  }

  public static void rate(Context context){
    try {
      String myUrl ="market://details?id="+ context.getPackageName();
      context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(myUrl)));
    }catch (ActivityNotFoundException e){
      Timber.e(e.getMessage());
    }
  }

}
