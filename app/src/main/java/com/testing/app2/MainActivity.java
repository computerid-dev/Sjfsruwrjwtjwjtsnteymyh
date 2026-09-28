package com.testing.app2;

import android.app.*;import android.os.*;import android.print.*;import android.webkit.*;import android.content.*;import android.net.Uri;import android.view.*;import java.io.*;

public class MainActivity extends Activity {
  WebView web;
  WebView printWeb; // simpan referensi supaya tidak di-garbage-collect sebelum dialog print muncul

  @Override public void onCreate(Bundle b){super.onCreate(b); getWindow().setStatusBarColor(android.graphics.Color.rgb(10,22,37));
    web=new WebView(this); web.setBackgroundColor(android.graphics.Color.rgb(7,17,31));
    WebSettings s=web.getSettings(); s.setJavaScriptEnabled(true); s.setDomStorageEnabled(true); s.setAllowFileAccess(true); s.setAllowContentAccess(true);
    web.setWebViewClient(new WebViewClient());
    web.setWebChromeClient(new WebChromeClient()); // wajib agar alert() dari JavaScript tampil
    web.addJavascriptInterface(new Bridge(),"Android");
    web.loadUrl("file:///android_asset/index.html"); setContentView(web);
  }

  // Catatan: method @JavascriptInterface dijalankan di thread background.
  // Semua yang menyentuh UI/WebView harus dipindah ke main thread lewat runOnUiThread.
  public class Bridge {
    @JavascriptInterface public void share(final String text){ runOnUiThread(() -> {
      Intent i=new Intent(Intent.ACTION_SEND); i.setType("text/plain"); i.putExtra(Intent.EXTRA_TEXT,text);
      MainActivity.this.startActivity(Intent.createChooser(i,"Bagikan laporan"));
    }); }

    @JavascriptInterface public void whatsapp(final String text){ runOnUiThread(() -> {
      try{ MainActivity.this.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/?text="+Uri.encode(text)))); }
      catch(Exception e){ share(text); }
    }); }

    @JavascriptInterface public void copy(final String text){ runOnUiThread(() -> {
      ClipboardManager cm=(ClipboardManager)MainActivity.this.getSystemService(Context.CLIPBOARD_SERVICE);
      if(cm!=null) cm.setPrimaryClip(ClipData.newPlainText("Laporan",text));
    }); }

    @JavascriptInterface public void print(final String html){ runOnUiThread(() -> {
      printWeb=new WebView(MainActivity.this);
      printWeb.setWebViewClient(new WebViewClient(){ @Override public void onPageFinished(WebView v,String u){
        PrintManager pm=(PrintManager)MainActivity.this.getSystemService(Context.PRINT_SERVICE);
        pm.print("Laporan Operasional",v.createPrintDocumentAdapter("Laporan Operasional"),new PrintAttributes.Builder().setMediaSize(PrintAttributes.MediaSize.ISO_A4).setColorMode(PrintAttributes.COLOR_MODE_COLOR).build());
      }});
      printWeb.loadDataWithBaseURL(null,html,"text/html","UTF-8",null);
    }); }
  }
}
