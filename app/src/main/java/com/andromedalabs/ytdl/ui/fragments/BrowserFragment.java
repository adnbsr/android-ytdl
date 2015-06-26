package com.andromedalabs.ytdl.ui.fragments;

import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;
import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import com.andromedalabs.ytdl.R;
import com.andromedalabs.ytdl.YTDL;
import com.andromedalabs.ytdl.model.Item;
import com.andromedalabs.ytdl.ui.animations.Animator;
import com.andromedalabs.ytdl.utils.Common;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class BrowserFragment extends Fragment {

	@InjectView(R.id.webView) WebView webView;
	@InjectView(R.id.download) FloatingActionButton mDownload;

	private static String startUrl = "https://www.youtube.com";
	private MyWebViewClient client;
	private MyChromeClient chromeClient;
	private String m_url = "";
	private String m_title;
	private String mVideoId;
	private final YTDL ytdl = new YTDL(getActivity());

	@Override public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_browser, container, false);
		ButterKnife.inject(this,view);
		return view;
	}

	@Override public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);


		WebSettings settings = webView.getSettings();
		settings.setJavaScriptEnabled(true);
		settings.setBlockNetworkLoads(false);
		client = new MyWebViewClient();
		chromeClient = new MyChromeClient();
		webView.setWebViewClient(client);
		webView.setWebChromeClient(chromeClient);
		webView.loadUrl(startUrl);

		webView.setOnKeyListener(new View.OnKeyListener()
		{
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event)
			{
				if(event.getAction() == KeyEvent.ACTION_DOWN)
				{
					WebView webView = (WebView) v;

					switch(keyCode)
					{
						case KeyEvent.KEYCODE_BACK:
							if(webView.canGoBack())
							{
								webView.goBack();
								return true;
							}
							break;
					}
				}

				return false;
			}
		});
	}

	private class  MyWebViewClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            webView.loadUrl(url);
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
        }
    }


    private String extract_id(String url){
        String pattern = "(?<=watch\\?v=|/videos/|embed\\/)[^#\\&\\?]*";

        Pattern compiledPattern = Pattern.compile(pattern);
        Matcher matcher = compiledPattern.matcher(url);

        if(matcher.find()){
            return matcher.group();
        }

        return null;
    }

		@SuppressWarnings("unused")
		@OnClick(R.id.download)
		public void onDownload(){
			if(mVideoId != null){
				getItems();
			}
		}


    private class MyChromeClient extends WebChromeClient{

        @Override
        public void onReceivedTitle(WebView view, String title) {
            super.onReceivedTitle(view, title);
            m_url = view.getUrl();
            m_title = view.getTitle();
	          checkId(m_url);

        }
    }


		public void checkId(String url){
			mVideoId = extract_id(url);

			if(mVideoId == null){
				mDownload.setVisibility(View.GONE);
			}else{
				mDownload.setVisibility(View.VISIBLE);
			}

		}

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void getItems() {


        if(mVideoId == null){
	        Timber.e("Video id cannot be null...");
            return;
        }


        View rate = LayoutInflater.from(getActivity()).inflate(R.layout.rate_dialog,null);
        final RelativeLayout rateArea = (RelativeLayout) rate.findViewById(R.id.rate_area);

        rateArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRate();
            }
        });

        final ObjectAnimator animator = Animator.tada(rateArea, 1f);
        animator.setRepeatMode(-1);
        animator.setRepeatCount(Animation.INFINITE);

        final AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        dialog.setTitle(m_title);
        dialog.setView(rate);

        final ArrayList<String> optionList = new ArrayList<String>();
	    final List<Item> items = new ArrayList<>();


	    ytdl.getItems(mVideoId)
			    .observeOn(AndroidSchedulers.mainThread())
			    .subscribeOn(Schedulers.newThread())
			    .subscribe(new Observer<Item>() {
				    @Override public void onCompleted() {
					    Timber.d("onDialog Completed!");
					    final String[] stringArray = Arrays.copyOf(optionList.toArray(),
							    optionList.toArray().length, String[].class);

					    dialog.setItems(stringArray, new DialogInterface.OnClickListener() {
						    @Override public void onClick(DialogInterface dialog, int which) {
							    save(items.get(which));
						    }
					    });

					    animator.start();
					    dialog.create().show();


				    }

				    @Override public void onError(Throwable e) {
					    Timber.e(e.getMessage());
				    }

				    @Override public void onNext(Item item) {
					    Timber.d("itag %s",item.itag);
					    optionList.add(item.getQuality(item.itag));
					    items.add(item);
				    }
			    });



    }

    public void save(final Item item){
        Common.downloadFile(getActivity(), item.url, item.title + item.getExtension(item.itag), 1);
    }

    private void onRate(){
        String myUrl ="market://details?id="+ getActivity().getPackageName();
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(myUrl)));
    }

		@TargetApi(11)
    @Override public void onPause() {
        super.onPause();
        webView.onPause();
    }

		@TargetApi(11)
    @Override public void onResume() {
        super.onResume();
        webView.onResume();
    }

    @Override public void onDestroy() {
        webView.destroy();
        super.onDestroy();
    }
}
