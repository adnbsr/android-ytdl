package com.andromedalabs.ytdl;

import android.content.Context;
import android.os.Environment;
import com.andromedalabs.ytdl.model.Item;
import com.andromedalabs.ytdl.model.VideoInfo;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.okhttp.Cache;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.mozilla.javascript.Scriptable;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Created by adnan on 3/4/15.
 */
public class YTDL {

    private final Context mContext;
    private String title;
    private String player_url;
    OkHttpClient client = new OkHttpClient();

    public YTDL(Context context){

        this.mContext = context;

        try {
            client.setCache(new Cache(new File(
                Environment.getExternalStorageDirectory().toString() + "/test.tmp"),10*1024*1024));
        } catch (IOException e) {
            Timber.e(e.getMessage());
            e.printStackTrace();
        }


    }

    private Observable<Response> getBody(final String url){

        return  Observable.create(new Observable.OnSubscribe<Response>() {
          @Override public void call(final Subscriber<? super Response> subscriber) {

            String mUrl;
            if (url.startsWith("//")) {
              mUrl = "https:" + url;
            } else {
              mUrl = url;
            }

            Request request = new Request.Builder().addHeader("User-Agent",
                "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2227.1 Safari/537.36")
                .url(mUrl)
                .build();

            Call call = client.newCall(request);

            call.enqueue(new Callback() {
              @Override public void onFailure(Request request, IOException e) {
                subscriber.onError(e);
              }

              @Override public void onResponse(Response response) throws IOException {
                subscriber.onNext(response);
                subscriber.onCompleted();
              }
            });
          }
        });
    }

    private Observable<Response> getJSBody(String mainBody){
        Document doc = Jsoup.parse(mainBody);

        title = doc.title();
        player_url = doc.select("script[name=html5player/html5player]").attr("src");

        return getBody(player_url);
    }

    public Observable<Item> getItems(String videoId){


        return getBody("https://www.youtube.com/watch?v="+videoId)
            .flatMap(new Func1<Response, Observable<Map.Entry<Observable<Response>, String>>>() {
                @Override public Observable<Map.Entry<Observable<Response>, String>> call(
                    final Response response) {

                    try {
                        final String mainBody = response.body().string();
                        return Observable.create(
                            new Observable.OnSubscribe<Map.Entry<Observable<Response>, String>>() {
                                @Override
                                public void call(Subscriber<? super Map.Entry<Observable<Response>, String>> subscriber) {
                                    subscriber.onNext(new AbstractMap.SimpleEntry<>(getJSBody(mainBody),mainBody));
                                    subscriber.onCompleted();
                                }
                            });
                    } catch (IOException e) {
                      Timber.e(e.getMessage());
                        e.printStackTrace();
                    }
                    return null;
                }
            }).flatMap(new Func1<Map.Entry<Observable<Response>, String>, Observable<Item>>() {
                @Override public Observable<Item> call(
                    Map.Entry<Observable<Response>, String> observableStringEntry) {
                    return extractAllItems(observableStringEntry.getValue(), observableStringEntry.getKey());
                }
            });
    }

  private Observable<Item> extractAllItems(final String mainBody,Observable<Response> jsBodyResponse)  {

    return jsBodyResponse.flatMap(new Func1<Response, Observable<Item>>() {

      @Override public Observable<Item> call(Response response) {
        try {
          return Observable.from(parseAllItems(response.body().string(), mainBody));
        } catch (IOException e) {
          Timber.e(e.getMessage());
          e.printStackTrace();
        }
        return Observable.from(new Item[0]);
      }
    });
  }

    private ArrayList<Item> parseAllItems(String jsBody,String mainBody){

        Pattern pt = Pattern.compile(";ytplayer\\.config\\s*=\\s*(\\{.*?\\});");
        Matcher matcher = pt.matcher(mainBody);

        GsonBuilder builder = new GsonBuilder();
        builder.disableHtmlEscaping();
        Gson gson = builder.create();

        ArrayList<Item> itemsResponse = new ArrayList<>();

        ArrayList<String> codeList = new ArrayList<>();
        codeList.add("18");
        codeList.add("140");
        codeList.add("37");
        codeList.add("17");
        codeList.add("22");
        codeList.add("36");


        while (matcher.find()) {

            VideoInfo video_info = gson.fromJson(matcher.group(1), VideoInfo.class);

            String encoded_url_map = video_info.args.adaptive_fmts + "," + video_info.args.url_encoded_fmt_stream_map;

            String[] itemsArray = encoded_url_map.split(",");

            for (int i = 0; i < itemsArray.length; i++) {
                final Map<String, String> item = _parse_qsl(itemsArray[i]);

                final String encrypted_signature = item.get("s");

                if (encrypted_signature != null && jsBody != null) {

                    String decrypted = decrypt_signature(encrypted_signature, jsBody);
                    item.put("url", item.get("url") + "&signature=" + decrypted);
                    itemsResponse.add(new Item(title, item.get("url"), item.get("itag")));
                } else {
                    itemsResponse.add(new Item(title, item.get("url"), item.get("itag")));
                }
            }

            Iterator<Item> iterator = itemsResponse.iterator();

            while (iterator.hasNext()){
                String itag = iterator.next().itag;
              Timber.d("itag : %s",itag);
                if(!codeList.contains(itag)){
                    iterator.remove();
                }

            }

        }

        return itemsResponse;
    }

    private String  extract_function(String code,String funcname){
        Pattern pt;

        if(funcname.startsWith("$")){
            pt = Pattern.compile("(?:function \\"+funcname+").+\\)\\};");
        }else{
            pt = Pattern.compile("(?:function "+funcname+").+\\)\\};");
        }


        Matcher matcher = pt.matcher(code);

        while (matcher.find()){
            return matcher.group();
        }

        return null;
    }


    private String extract_object(String code,String obj_name){

        Pattern pt;

        if(obj_name.startsWith("$")){
            pt = Pattern.compile("var \\"+obj_name+"(.*?).+\\}\\};");
        }else{
            pt = Pattern.compile("var "+obj_name+"(.*?).+\\}\\};");
        }

        Matcher matcher = pt.matcher(code);

        while (matcher.find()){
            return matcher.group();
        }

        return null;
    }

    private String extract_funcname(String code){

        Pattern pt = Pattern.compile("\\.sig(.*?)\\(");
        Matcher matcher = pt.matcher(code);

        while(matcher.find()){
            return matcher.group(1).split(".sig\\|\\|")[1];
        }

        return null;
    }


    private String extract_objname(String code){

        Pattern pt = Pattern.compile(";(.*?)\\.");
        Matcher matcher = pt.matcher(code);

        while(matcher.find()){
            return matcher.group(1);
        }

        return null;
    }

    private String decode(String s) {
        try {
            return URLDecoder.decode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return s;
    }

    private Map<String,String> _parse_qsl(String qs){

        Map<String,String> dict = new HashMap<String,String>();

        for(String s1:qs.split("&")){

            for(String s2:s1.split(";")){

                String[] nv = s2.split("=",2);
                String name = nv[0].replace("+"," ");
                String value = nv[1].replace("+"," " );

                name = decode(name);
                value = decode(value);
                dict.put(name, value);
            }
        }

        return dict;
    }

    private String decrypt_signature(String encrypted_sig,String jsBody){


        String funcname = extract_funcname(jsBody);


        String func = null;
        String command = null;
        String obj_name= null;
        String obj = null;

        if(funcname != null){
            func = extract_function(jsBody, funcname);
        }


        if(func != null){
            obj_name = extract_objname(func);
        }


        if(obj_name != null){
            obj = extract_object(jsBody, obj_name);
        }

        if(obj != null){
            command = obj + " "+ func + " "+funcname+"('"+encrypted_sig+"');";
        }


        org.mozilla.javascript.Context cx = org.mozilla.javascript.Context.enter();
        cx.setOptimizationLevel(-1);


        Scriptable scope = cx.initStandardObjects();

        Object result;


        result = cx.evaluateString(scope,command,"<cmd>", 0, null);

        return org.mozilla.javascript.Context.toString(result);


    }

}