package com.andromedalabs.ytdl.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import java.io.File;

/**
 * Created by adnan on 5/30/15.
 */
public class VideoUtils {

  public static void playFile(Context context,File file){
    context.startActivity(
        new Intent(Intent.ACTION_VIEW).setDataAndType(Uri.fromFile(file), "video/*"));
  }

  public static void shareFile(Context context,File file){
    Intent shareIntent = new Intent(Intent.ACTION_SEND);
    shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
    shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Video Downloader for Android");
    shareIntent.putExtra(Intent.EXTRA_TEXT, "");
    shareIntent.setType("video/*");
    context.startActivity(Intent.createChooser(shareIntent, "Send File"));
  }

  public static boolean deleteFile(File file){
    try {
      file.delete();
      return true;
    }catch (Exception e){

    }
    return false;
  }

}
