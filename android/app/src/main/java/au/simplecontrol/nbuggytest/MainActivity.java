package au.simplecontrol.nbuggytest;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.net.*;
import android.provider.Settings;
import android.webkit.*;
import android.widget.*;
import android.view.View;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;

public class MainActivity extends Activity {
 private WebView web;
 private EditText address;
 private TextView info;
 private ConnectivityManager cm;
 private ConnectivityManager.NetworkCallback callback;
 private String host="192.168.4.1";
 @Override public void onCreate(Bundle saved){
  super.onCreate(saved);
  LinearLayout layout=new LinearLayout(this); layout.setOrientation(1);
  layout.setBackground(new CamoBackground());
  getWindow().setStatusBarColor(Color.rgb(50,65,55));
  getWindow().setNavigationBarColor(Color.rgb(50,65,55));
  layout.setPadding(12,36,12,16);
  info=new TextView(this);info.setText("N Buggy · Bench test\nConnect to N-Buggy-Test Wi-Fi. No motor outputs.");layout.addView(info);
  info.setTextColor(Color.rgb(32,44,36));info.setTextSize(17);info.setPadding(8,6,8,12);
  Button wifi=new Button(this);wifi.setText("Wi-Fi connection settings");wifi.setOnClickListener(v->startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS)));layout.addView(wifi);
  styleButton(wifi);
  address=new EditText(this);address.setSingleLine();address.setHint("ESP32 IPv4 address");address.setText(getPreferences(0).getString("address",host));layout.addView(address);
  address.setTextColor(Color.rgb(32,44,36));address.setBackgroundColor(0xffe7e7db);address.setPadding(18,12,18,12);
  Button connect=new Button(this);connect.setText("Connect / reload controller");connect.setOnClickListener(v->connect());layout.addView(connect);
  styleButton(connect);
  web=new WebView(this);web.getSettings().setJavaScriptEnabled(true);web.getSettings().setAllowFileAccess(false);web.getSettings().setAllowContentAccess(false);
  web.setBackgroundColor(Color.TRANSPARENT);
  web.setWebViewClient(new WebViewClient(){
   @Override public boolean shouldOverrideUrlLoading(WebView w,WebResourceRequest r){return !allowed(r.getUrl());}
   @Override public WebResourceResponse shouldInterceptRequest(WebView w,WebResourceRequest r){
    if(allowed(r.getUrl()))return null;
    return new WebResourceResponse("text/plain","UTF-8",new java.io.ByteArrayInputStream("Blocked".getBytes()));
   }
   @Override public void onReceivedHttpAuthRequest(WebView w,HttpAuthHandler handler,String h,String realm){
    if(!host.equals(h)){handler.cancel();return;}
    LinearLayout box=new LinearLayout(MainActivity.this);box.setOrientation(1);
    EditText user=new EditText(MainActivity.this);user.setText("admin");box.addView(user);
    EditText pass=new EditText(MainActivity.this);pass.setHint("Controller password");pass.setInputType(129);box.addView(pass);
    new android.app.AlertDialog.Builder(MainActivity.this).setTitle("Controller login").setView(box).setPositiveButton("Log in",(d,n)->handler.proceed(user.getText().toString(),pass.getText().toString())).setNegativeButton("Cancel",(d,n)->handler.cancel()).show();
   }
   @Override public void onReceivedError(WebView w,WebResourceRequest r,WebResourceError e){if(r.isForMainFrame())info.setText("Cannot reach controller. Check Wi-Fi and address, then reconnect.");}
  });
  layout.addView(web,new LinearLayout.LayoutParams(-1,0,1));setContentView(layout);
  cm=(ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
 }
 private void styleButton(Button button){
  GradientDrawable face=new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,new int[]{0xff718573,0xff4c6352});
  face.setCornerRadius(12);face.setStroke(2,0xff3f5144);
  button.setBackground(face);button.setTextColor(0xfffaf9ef);button.setAllCaps(false);button.setMinHeight(56);button.setElevation(3);
  LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);lp.setMargins(0,6,0,8);button.setLayoutParams(lp);
 }
 private boolean allowed(Uri u){return "http".equals(u.getScheme())&&host.equals(u.getHost())&&(u.getPort()==-1||u.getPort()==80);}
 private void connect(){
  String ip=address.getText().toString().trim();String[] parts=ip.split("\\.",-1);
  boolean valid=parts.length==4;
  try{for(String p:parts){if(!p.matches("[0-9]{1,3}")||Integer.parseInt(p)>255)valid=false;}}catch(Exception e){valid=false;}
  if(!valid){info.setText("Enter an IPv4 address, e.g. 192.168.4.1");return;}
  host=ip;getPreferences(0).edit().putString("address",host).apply();
  if(callback!=null){cm.unregisterNetworkCallback(callback);callback=null;}
  cm.bindProcessToNetwork(null);
  callback=new ConnectivityManager.NetworkCallback(){
   @Override public void onAvailable(Network n){cm.bindProcessToNetwork(n);runOnUiThread(()->{info.setText("Local Wi-Fi selected · "+host);web.loadUrl("http://"+host+"/");});}
   @Override public void onLost(Network n){cm.bindProcessToNetwork(null);runOnUiThread(()->{web.stopLoading();web.loadUrl("about:blank");info.setText("Wi-Fi disconnected — controller state unknown. Reconnect to refresh.");});}
  };
  info.setText("Waiting for Wi-Fi. Use connection settings above if needed.");
  cm.requestNetwork(new NetworkRequest.Builder().addTransportType(NetworkCapabilities.TRANSPORT_WIFI).removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET).build(),callback);
 }
 @Override protected void onDestroy(){if(callback!=null)cm.unregisterNetworkCallback(callback);cm.bindProcessToNetwork(null);web.destroy();super.onDestroy();}
}
