/**
 * MIT License
 * <p>
 * Copyright (c) 2020 ZOLOZ-PTE-LTD
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.zoloz.saas.example;


import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.alibaba.fastjson.JSON;
import com.ap.zoloz.hummer.api.IZLZCallback;
import com.ap.zoloz.hummer.api.ZLZConstants;
import com.ap.zoloz.hummer.api.ZLZFacade;
import com.ap.zoloz.hummer.api.ZLZRequest;
import com.ap.zoloz.hummer.api.ZLZResponse;

import java.util.Base64;


public class MainActivity extends AppCompatActivity {
    public static final String TAG = MainActivity.class.getSimpleName();


    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mHandler = new Handler();

        // 獲取 Intent
        Intent intent = getIntent();
        Uri data = intent.getData();

        if (data != null) {
            // 提取 Scheme URL 的資訊
            String scheme = data.getScheme(); // 例如 "myapp"
            String host = data.getHost();     // 例如 "open"
            String path = data.getPath();     // 例如 "/details"
            String query = data.getQuery();   // 例如 "id=123"
            Log.d(TAG, "收到 intent! Host: " + host + ", Path: " + path + ", Query: " + query);
            // 根據資料執行相應操作

            String decode = decodeBase64(query);


            if (decode == null) {
                Log.d(TAG, "Base64 解碼失敗");
            } else {
                Log.d(TAG, "解碼: " + decode);
                startZoloz(decode);
            }
        }
    }


    private void runOnIoThread(Runnable runnable) {
        Thread thread = new Thread(runnable);
        thread.start();
    }

    public String decodeBase64(String encodedString) {
        try {
            byte[] decodedBytes = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                decodedBytes = Base64.getDecoder().decode(encodedString);
            }
            return new String(decodedBytes);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return null;
        }
    }


    public void startZoloz(String rawConfig) {
        runOnIoThread(new Runnable() {
            @Override
            public void run() {
                final InitResponse initResponse = JSON.parseObject(rawConfig, InitResponse.class);
                final ZLZFacade zlzFacade = ZLZFacade.getInstance();
                final ZLZRequest request = new ZLZRequest();
                request.zlzConfig = initResponse.clientCfg;
                request.bizConfig.put(ZLZConstants.CONTEXT, MainActivity.this);
                request.bizConfig.put(ZLZConstants.PUBLIC_KEY, initResponse.rsaPubKey);
                request.bizConfig.put(ZLZConstants.CHAMELEON_CONFIG_PATH, "config_realId.zip");
                request.bizConfig.put(ZLZConstants.LOCALE, "zh_tw_#hant");
                Log.d(TAG, "request success:");
                mHandler.postAtFrontOfQueue(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "start zoloz");
                        zlzFacade.start(request, new IZLZCallback() {
                            @Override
                            public void onCompleted(ZLZResponse response) {
                                Log.d(TAG, "ZOLOZ Face on completed. " + JSON.toJSONString(response));
                            }

                            @Override
                            public void onInterrupted(ZLZResponse response) {
                                showResponse(initResponse.transactionId, JSON.toJSONString(response));
                                Log.d(TAG, "ZOLOZ Face on interrupted. " + JSON.toJSONString(response));
                                Toast.makeText(MainActivity.this, "interrupted", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
            }
        });
    }


    private void showResponse(final String flowId, String response) {
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("Check Result")
                .setMessage(response)
                .setNegativeButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ClipboardManager myClipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                        ClipData myClip;
                        String text = flowId;
                        myClip = ClipData.newPlainText("text", text);
                        myClipboard.setPrimaryClip(myClip);
                        dialog.dismiss();
                    }
                })
                .create();
        alertDialog.setCancelable(false);
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }
}
