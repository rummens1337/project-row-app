package row.david.davidapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class WebViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);

        // Declaration of webview
        WebView myWebView = (WebView) findViewById(R.id.webView);
        WebSettings webSettings = myWebView.getSettings();

        // Required settings
        webSettings.setJavaScriptEnabled(true);
        // TODO: Redirect the user to the correct website. Preferably something like raspberrypi.local.
        myWebView.loadUrl("http://192.168.4.1");

        // Just-to-be-sure settings
        myWebView.setWebViewClient(new WebViewClient()); // Prevents the app from opening a browser, uses built-in browser instead.

    }
}
