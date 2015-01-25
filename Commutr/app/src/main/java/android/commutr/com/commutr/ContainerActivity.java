package android.commutr.com.commutr;

import android.commutr.com.commutr.Utils.DisplayMessenger;
import android.commutr.com.commutr.base.BaseActivity;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class ContainerActivity extends BaseActivity {

    private WebView webView;
    private SwipeRefreshLayout swipeView;
    private boolean webViewLoadTimeout = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_container);
        constructWebView();
        handlePullRefresh();
    }

    private void constructWebView(){

        webView = (WebView) findViewById(R.id.CMTRContainerWebView);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        buildWebViewEvents();
        webView.loadUrl(getResources().getString(R.string.CMTRWebViewURL));

    }

    private void buildWebViewEvents(){

        webView.setWebViewClient
        (
                new WebViewClient() {

                    @Override
                    public void onPageStarted(WebView view, String url, Bitmap favicon) {
                        swipeView.setRefreshing(true);
                        //hide web view at start
                        view.setVisibility(View.GONE);
                        view.startAnimation(AnimationUtils.loadAnimation(view.getContext(), R.anim.fade_out));
                        handleTimeout(view);

                    }

                    private void handleTimeout(final WebView view) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Thread.sleep(getResources().getInteger(R.integer.web_view_load_timeout));
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                if (webViewLoadTimeout) {

                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            swipeView.setRefreshing(false);
                                            view.stopLoading();
                                            DisplayMessenger.showBasicToast
                                                    (getApplicationContext(),
                                                            getResources().getString(R.string.web_view_timeout_message));
                                        }
                                    });
                                }
                            }
                        }).start();
                    }

                    @Override
                    public void onPageFinished(WebView view, String url) {
                        swipeView.setRefreshing(false);
                        webViewLoadTimeout = false;
                        //show when done
                        view.setVisibility(View.VISIBLE);
                        view.startAnimation(AnimationUtils.loadAnimation(view.getContext(), R.anim.fade_in));
                    }

                    @Override
                    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                        super.onReceivedError(view, errorCode, description, failingUrl);
                        webViewLoadTimeout = false;
                        swipeView.setRefreshing(false);
                        view.setVisibility(View.GONE);
                        view.startAnimation(AnimationUtils.loadAnimation(view.getContext(), R.anim.fade_out));
                    }

                    @Override
                    public boolean shouldOverrideUrlLoading(WebView view, String url) {
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse(url));
                        startActivity(i);
                        return true;

                    }

                }
        );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_container, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void handlePullRefresh(){

        swipeView = (SwipeRefreshLayout) findViewById(R.id.swipe);
        final TypedArray styledAttributes = getTheme().obtainStyledAttributes(
                new int[] { android.R.attr.actionBarSize });
        int actionBarSize = (int) styledAttributes.getDimension(0, 0);
        styledAttributes.recycle();

        swipeView.setProgressViewOffset(true, actionBarSize, actionBarSize + getResources().getInteger(R.integer.pull_refresh_offset));

        swipeView.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                webViewLoadTimeout = true;
                webView.reload();
            }
        });
    }


}
