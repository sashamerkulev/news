package ru.merkulyevsasha.news;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;

import butterknife.Bind;
import butterknife.ButterKnife;

public class WebViewActivity extends AppCompatActivity {

    @Bind(R.id.webView)
    public WebView mWebView;

    @Bind(R.id.back_button)
    public ImageButton mBackButton;

    @Bind(R.id.fab)
    public FloatingActionButton mFab;

    @Bind(R.id.toolbar)
    public Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);

        Intent intent = getIntent();
        final String url = intent.getStringExtra("link");

        mWebView.setWebViewClient(new MyWebViewClient());
        mWebView.loadUrl(url);

        mBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, url);
                sendIntent.setType("text/plain");
                startActivity(sendIntent);
            }
        });
    }

    @Override
    public void onBackPressed() {
//        if(mWebView.canGoBack()) {
//            mWebView.goBack();
//        } else {
            super.onBackPressed();
//        }
    }

    private class MyWebViewClient extends WebViewClient
    {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url)
        {
            view.loadUrl(url);
            return true;
        }
    }
}
