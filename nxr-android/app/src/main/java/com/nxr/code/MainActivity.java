package com.nxr.code;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.*;
import java.io.*;
import java.net.*;
import org.json.JSONObject;

public class MainActivity extends Activity {
    WebView w;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().setStatusBarColor(0xFF0B0B14);
        getWindow().setNavigationBarColor(0xFF0B0B14);
        w = new WebView(this);
        setContentView(w);
        WebSettings s = w.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        w.setWebViewClient(new WebViewClient());
        w.setWebChromeClient(new WebChromeClient());
        w.addJavascriptInterface(new Object() {
            @JavascriptInterface public void post(final int id, final String u, final String k, final String body) {
                new Thread(() -> {
                    String r;
                    try {
                        HttpURLConnection c = (HttpURLConnection) new URL(u).openConnection();
                        c.setRequestMethod("POST");
                        c.setConnectTimeout(15000);
                        c.setReadTimeout(60000);
                        c.setDoOutput(true);
                        c.setRequestProperty("Content-Type", "application/json");
                        if (!k.isEmpty()) c.setRequestProperty("Authorization", k);
                        c.getOutputStream().write(body.getBytes("UTF-8"));
                        InputStream in = c.getResponseCode() < 400 ? c.getInputStream() : c.getErrorStream();
                        ByteArrayOutputStream o = new ByteArrayOutputStream();
                        byte[] t = new byte[4096];
                        int n;
                        while ((n = in.read(t)) > 0) o.write(t, 0, n);
                        r = o.toString("UTF-8");
                    } catch (Exception e) {
                        r = "{\"message\":" + JSONObject.quote(String.valueOf(e.getMessage())) + "}";
                    }
                    final String f = r;
                    runOnUiThread(() -> w.evaluateJavascript("nxrCb(" + id + "," + JSONObject.quote(f) + ")", null));
                }).start();
            }
        }, "Android");
        w.loadUrl("file:///android_asset/index.html");
    }

    @Override public void onBackPressed() { moveTaskToBack(true); }
}
