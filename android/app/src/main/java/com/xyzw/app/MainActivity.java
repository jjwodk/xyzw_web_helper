package com.xyzw.app;

import android.webkit.JavascriptInterface;
import android.webkit.WebView;

import com.getcapacitor.BridgeActivity;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends BridgeActivity {

    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private boolean jsInterfaceRegistered = false;

    @Override
    public void onResume() {
        super.onResume();
        registerNativeHttpInterface();
    }
    
    private void registerNativeHttpInterface() {
        if (jsInterfaceRegistered) return;
        
        try {
            WebView webView = this.getBridge().getWebView();
            
            if (webView == null) {
                android.util.Log.e("NativeHttp", "WebView 为空，延迟500ms后重试");
                // 延迟重试
                this.getWindow().getDecorView().postDelayed(this::registerNativeHttpInterface, 500);
                return;
            }
            
            android.util.Log.d("NativeHttp", "开始注册 NativeHttp 接口");
            android.util.Log.d("NativeHttp", "WebView: " + webView);
            android.util.Log.d("NativeHttp", "Bridge: " + this.getBridge());
            
            webView.addJavascriptInterface(new NativeHttpInterface(this), "NativeHttp");
            jsInterfaceRegistered = true;
            
            // 注入一个检测脚本，用于验证接口是否可用
            webView.evaluateJavascript(
                "console.log('[NativeHttp] JavaScript接口已注册'); window.NativeHttpAvailable = true;",
                null
            );
            
            android.util.Log.d("NativeHttp", "NativeHttp 接口注册成功");
            
        } catch (Exception e) {
            android.util.Log.e("NativeHttp", "注册接口失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 原生 HTTP 请求接口
     */
    public class NativeHttpInterface {

        private MainActivity activity;

        public NativeHttpInterface(MainActivity activity) {
            this.activity = activity;
        }

        /**
         * 发送 HTTP 请求
         * @param config JSON 格式的请求配置
         * @param callbackId 回调ID，用于返回结果
         */
        @JavascriptInterface
        public void request(String config, String callbackId) {
            executor.execute(() -> {
                try {
                    // 解析配置
                    org.json.JSONObject jsonConfig = new org.json.JSONObject(config);
                    String url = jsonConfig.getString("url");
                    String method = jsonConfig.optString("method", "GET");
                    org.json.JSONObject headers = jsonConfig.optJSONObject("headers");
                    String data = jsonConfig.optString("data", "");
                    int timeout = jsonConfig.optInt("timeout", 15000);

                    // 创建连接
                    URL requestUrl = new URL(url);
                    HttpURLConnection conn = (HttpURLConnection) requestUrl.openConnection();
                    conn.setRequestMethod(method);
                    conn.setConnectTimeout(timeout);
                    conn.setReadTimeout(timeout);
                    conn.setDoOutput(true);
                    conn.setDoInput(true);
                    conn.setUseCaches(false);

                    // 设置请求头
                    if (headers != null) {
                        java.util.Iterator<String> keys = headers.keys();
                        while (keys.hasNext()) {
                            String key = keys.next();
                            conn.setRequestProperty(key, headers.getString(key));
                        }
                    }

                    // 发送数据
                    if (!data.isEmpty() && "POST".equals(method)) {
                        OutputStream os = conn.getOutputStream();
                        os.write(data.getBytes("UTF-8"));
                        os.close();
                    }

                    // 获取响应
                    int responseCode = conn.getResponseCode();
                    BufferedReader reader;
                    if (responseCode >= 200 && responseCode < 300) {
                        reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
                    } else {
                        reader = new BufferedReader(new InputStreamReader(conn.getErrorStream(), "UTF-8"));
                    }
                    
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();
                    conn.disconnect();

                    // 返回结果给 JavaScript
                    String result = String.format(
                        "window.__nativeHttpCallback('%s', %d, %s)",
                        callbackId, responseCode, 
                        org.json.JSONObject.quote(response.toString())
                    );
                    
                    activity.runOnUiThread(() -> {
                        WebView webView = activity.getBridge().getWebView();
                        webView.evaluateJavascript(result, null);
                    });

                } catch (Exception e) {
                    // 返回错误
                    String error = org.json.JSONObject.quote("Error: " + e.getMessage());
                    String result = String.format(
                        "window.__nativeHttpCallback('%s', -1, %s)",
                        callbackId, error
                    );
                    activity.runOnUiThread(() -> {
                        WebView webView = activity.getBridge().getWebView();
                        webView.evaluateJavascript(result, null);
                    });
                }
            });
        }
    }
}
