package com.phcpransli.labreport;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.ClipData;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.provider.MediaStore;
import android.util.Base64;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.webkit.WebViewAssetLoader;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class MainActivity extends Activity {

    private static final String HOST = "appassets.androidplatform.net";
    private WebView web;
    private long lastBack = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        web = new WebView(this);
        web.setBackgroundColor(0xFFF1F5F9);
        setContentView(web);

        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setUseWideViewPort(true);
        s.setLoadWithOverviewMode(true);
        s.setSupportZoom(true);
        s.setBuiltInZoomControls(true);
        s.setDisplayZoomControls(false);
        s.setAllowFileAccess(false);

        final WebViewAssetLoader loader = new WebViewAssetLoader.Builder()
                .addPathHandler("/assets/", new WebViewAssetLoader.AssetsPathHandler(this))
                .build();

        web.setWebViewClient(new WebViewClient() {
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                return loader.shouldInterceptRequest(request.getUrl());
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri u = request.getUrl();
                if (HOST.equals(u.getHost())) return false;
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, u));   // WhatsApp / external links
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "Cannot open link", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        });
        web.setWebChromeClient(new WebChromeClient());
        web.addJavascriptInterface(new Bridge(), "AndroidBridge");

        web.loadUrl("https://" + HOST + "/assets/splash.html");
    }

    /** Called from the web page (see index.html: window.AndroidBridge) */
    private class Bridge {
        @JavascriptInterface
        public void print() {
            runOnUiThread(() -> {
                PrintManager pm = (PrintManager) getSystemService(PRINT_SERVICE);
                PrintDocumentAdapter adapter = web.createPrintDocumentAdapter("Lab_Report");
                PrintAttributes attrs = new PrintAttributes.Builder()
                        .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                        .build();
                pm.print("Lab_Report", adapter, attrs);
            });
        }

        /** PDF sidhi WhatsApp ma attach (koi text nahi). phone hoy to e j chat khule. */
        @JavascriptInterface
        public void shareWhatsApp(String base64, String fileName, String phone) {
            try {
                byte[] data = Base64.decode(base64, Base64.DEFAULT);
                File dir = new File(getCacheDir(), "shared");
                if (!dir.exists()) dir.mkdirs();
                String safe = fileName.replaceAll("[\\\\/:*?\"<>|]", "_");
                File f = new File(dir, safe);
                try (FileOutputStream fos = new FileOutputStream(f)) {
                    fos.write(data);
                    fos.flush();
                    fos.getFD().sync();
                }
                if (f.length() == 0) throw new Exception("PDF is empty");
                Uri uri = FileProvider.getUriForFile(MainActivity.this,
                        "com.phcpransli.labreport.fileprovider", f);

                String[] pkgs = {"com.whatsapp", "com.whatsapp.w4b"};
                boolean started = false;
                for (String pkg : pkgs) {
                    try {
                        // WhatsApp ne file vachvani permission pehla thi j apvi (cold start ma pan attach thase)
                        grantUriPermission(pkg, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        Intent i = new Intent(Intent.ACTION_SEND);
                        i.setType("application/pdf");
                        i.putExtra(Intent.EXTRA_STREAM, uri);
                        i.setClipData(ClipData.newRawUri("Lab Report", uri));
                        i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        i.setPackage(pkg);
                        // Contact number hoy to e j number ni chat ma PDF attach thai ne khule
                        if (phone != null && !phone.isEmpty()) {
                            i.putExtra("jid", phone + "@s.whatsapp.net");
                        }
                        startActivity(i);
                        started = true;
                        break;
                    } catch (ActivityNotFoundException ignored) { }
                }
                if (!started) toast("WhatsApp is not installed");
            } catch (Exception e) {
                toast("Could not share PDF: " + e.getMessage());
            }
        }

        @JavascriptInterface
        public void savePdf(String base64, String fileName) {
            try {
                byte[] data = Base64.decode(base64, Base64.DEFAULT);
                ContentResolver resolver = getContentResolver();
                ContentValues v = new ContentValues();
                v.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
                v.put(MediaStore.Downloads.MIME_TYPE, "application/pdf");
                v.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
                Uri uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, v);
                if (uri == null) throw new Exception("insert failed");
                try (OutputStream out = resolver.openOutputStream(uri)) {
                    out.write(data);
                }
                toast("PDF saved to Downloads: " + fileName);
            } catch (Exception e) {
                toast("Could not save PDF: " + e.getMessage());
            }
        }
    }

    private void toast(final String msg) {
        runOnUiThread(() -> Toast.makeText(MainActivity.this, msg, Toast.LENGTH_LONG).show());
    }

    @Override
    public void onBackPressed() {
        long now = System.currentTimeMillis();
        if (now - lastBack < 2000) {
            super.onBackPressed();
        } else {
            lastBack = now;
            Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show();
        }
    }
}
