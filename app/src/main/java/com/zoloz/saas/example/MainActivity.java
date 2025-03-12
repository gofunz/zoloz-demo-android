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
import com.ap.zoloz.hummer.api.BaseFacade;
import com.ap.zoloz.hummer.api.IZLZCallback;
import com.ap.zoloz.hummer.api.ZLZConstants;
import com.ap.zoloz.hummer.api.ZLZFacade;
import com.ap.zoloz.hummer.api.ZLZRequest;
import com.ap.zoloz.hummer.api.ZLZResponse;
import com.ap.zoloz.hummer.biz.HummerLogger;
import com.ap.zoloz.hummer.common.ClientConfig;

import java.util.Base64;


public class MainActivity extends AppCompatActivity {
    public static final String TAG = MainActivity.class.getSimpleName();


    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mHandler = new Handler();
        startZoloz("");

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


    public void startZoloz(String clientCfgBase64encode) {
        runOnIoThread(new Runnable() {
            @Override
            public void run() {

                final ZLZFacade zlzFacade = ZLZFacade.getInstance();
                final ZLZRequest request = new ZLZRequest();
                request.bizConfig.put(ZLZConstants.CONTEXT, MainActivity.this);
                request.bizConfig.put(ZLZConstants.PUBLIC_KEY, "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAkzAc5OiwL+UjjAij8XgODE/rauj8cQET6LGVcatt9tWXQMS6WWu2EiDsC2n1SIVVKTMNlWWkgRE3L3670Ls5qXbLVEg7xdA6H2vY6dU0RaLNgj0GtGnJN3IbX/0fS34WuqUPHXw5f8bAofQ4/EiPMYba6RmYMVd5ZiFUo/vt58Elbwc5gU2nlpDswFg8FajiFmN5gsX9bguI23xGdNfuRxRFo+DE64l7P3fe1WWCYRCVDgYoC9TRlSq1AJ7IXv4LjQr6JGrTWqOj59QknO5Vpp4OIexCP/qXREYUXOTkTi4JFCevV5jWHshAfQXDRGHCHdWcBl//149R7/ld1+gUawIDAQAB");
                request.bizConfig.put(ZLZConstants.LOCALE, "zh_tw_#hant");
                request.bizConfig.put(ZLZConstants.CHAMELEON_CONFIG_PATH, "CONFIG_UI_ZOLOZ_v1.2.zip");
                request.zlzConfig = decodeBase64("eyJjbGllbnRTdGFydEluZGV4IjoxLCJmbG93SWQiOiJHMDAwMDAwMDA2YTQ5NzUxZGE0OGYzYjcxNmFkMGJkYmZlMWQxYjVlMWMiLCJmYWN0b3JDb250ZXh0Ijp7IkdBVEVXQVlfVVJMIjoiaHR0cHM6Ly9zZy1wcm9kdWN0aW9uLXptZ3Muem9sb3ouY29tL3ptZ3MvdjIvc2VjIiwiV09SS1NQQUNFX0lEIjoiZGVmYXVsdCIsIkFQUF9JRCI6ImRlZmF1bHQiLCJmYWNlWmltSWQiOiJHMDAwMDAwMDA2YTQ5NzUxZGE0OGYzYjcxNmFkMGJkYmZlMWQxYjVlMWMiLCJrZXlIYXNoIjoiN0YwOTk2IiwibWVyY2hhbnRJZCI6IjIyOSIsImtleU1ldGEiOiJ7XCJyZXNvdXJjZUNvZGVcIjpcIjAwMDAwMDAwXCIsXCJtaWRcIjpcIjIyOVwiLFwia2V5VmVyXCI6XCJ2MVwifSIsInppbUluaXRSZXNwIjoiQ0dRYUtrY3dNREF3TURBd01EWmhORGszTlRGa1lUUTRaak5pTnpFMllXUXdZbVJpWm1VeFpERmlOV1V4WXlLRERVTjBkMGhEYVhSSVRVUkJkMDFFUVhkTlJFRXlXVmRSTkUweVJUUlBWRWw1V2tSR2JVMVhXbWxaVkZGM1RVZEZNVmt5U1RGWlZGRXhXVEpHYTAxcVFtdEZSMUZ4Y1dka04wbHRUblppUjNkcFQyNXphVmxZVmpCaFJ6bDVZVmh3YUdSSGJIWmlhVWsyV20xR2MyTXlWWE5KYmxaM1lrYzVhRnBGVW14alNGSnZVa2RHTUZsVFNUWmFiVVp6WXpKVmMwbHVWbmRpUnpsb1drVXhkbUp0YkRCaU0wcFJZVmROYVU5cVJYTkpiWGh3V2pKb01FbHFiM2xOUkVGelNXMWFhR0pIVG5aaWEwNTJZa2Q0YkZrelVsVmhWekZzU1dwdk5VMUVRWE5KYmxKd1lsZFZhVTlxU1hkTVEwcG9XVEZTTldOSFZXbFBhVXBxVFZOSmMwbHVTbXhrU0VvMVNXcHZlV1pUZDJsa1YydHBUMmxKTlU5VVNXbE1RMG94WTBkNGRsbFhVV2xQYm5OcFpGaENjMkl5Um10WU1rNTJZbGhDZVZwWVRucFlNMHBvWkVkVmFVOXFRWFZQUTNkcFdrZFdlbUZZU214YVJtUndXa2hTYjBscWJ6QlBSRUZ6U1cxT2RtSkhlR3haTTFKd1lqSTBhVTlzYzJsVlIwWjFZbmxLWkdaVGQybGliVVl5WVZOSk5tVjVTbXhpYlVacFlrZFZhVTl1VW5sa1YxWTVURU5LTWxwWVNuQmFibXhPWWpKU2JFbHFiMmxpYlRsNVlsZEdjMGxwZDJsak1rNXNZbTFXUm1KdVdXbFBibk5wWXpKT2JHSnRWa1JpTWxKc1NXcHZhV1Z0T1hOaU0yOXlXVEk1ZFdKdFZtcGtRM1F5V2xoS2NGcHVhM0prYlZaNVlWZGFOVWxwZDJsak1rNXNZbTFXVldWWVFteEphbTlwWW0wNWVXSlhSbk5KYmpCelNXMVdkV1JwU1RaTlEzZHBXbTFHYWxwV1VuQmpTRTFwVDI1emFWbHVTbWhpYlZKVllWaEJhVTlwU1dkSmJqQnpTVzFHYzFveU9YbGhXRkp2WWxOSk5tVjVTbmRpTTA1c1dESjRjRm95YURCSmFtOTNUR3BOYzBsdFNuTmhWelZ5V0RKU2NGcHRXbVprUjJoNVdsaE9iMkl5ZUd0WU1rWXhaVU5KTmsxVE5IZE1RMHAzWWpOT2JGZ3piR2hrTURGd1ltbEpOa3hVUVhWTmFYZHBZek5TYUZreWRHWmtSMngwV2xOSk5rMVROREZNUTBwellqSmtabUpIVmpKYVYzZHBUMnBCYzBsdVVtOWpiVlo2WVVjNWMxcERTVFpsZVVvMldtMUdhbHBWU25OaFZ6VnlWRWRzTWxwWE5XeGpNMDFwVDJ4emQweHFTWE5OUXpReFdGZ3djMGx0VmpWYVZqbDJZMGRXZFdKdFZucGplVWsyVFVNMGVVeERTbXhsVjFabVlqSk9hbUpJVm5waFZ6bDFXRE5PYW1JelNteFlNMUp2WTIxV2VtRkhPWE5hUTBrMlRVTTBNRXhEU21saVIyeDFZVEU1ZG1OSFZuVmliVlo2WXpFNU1HRklTbXhqTW1oMllrZFNabGxZVmpSSmFtOTNUR3BCYzBsdGVIQmtiVloxV2xoT2VsZ3lUblppVjBwd1ltMUdNR0ZYT1hWSmFuQmlTVzV3YlZsWFRteFJiWGh3WW0xMFRXRllXbXhpYlZaNlkzbEtaRXhEU25kaU0wNXNXRE5DY0dSSFRtOUphbTkzVEdwSmMwbHVRblpqTWxabVpWZEdNMGxxYjNkTWFrbHpTVzVDZG1NeVZtWmFNa1l4WXpOT2NGbFhOR2xQYWtGMVRWUlZjMGx0TVdobFJqbHdZakpSYVU5cVFYVk9SRlZ6U1c1T2JGa3hRbmxpTTFKMldUSTVjMGxxYjJsSmFYZHBZMGM1ZWxwV09YUmlNMUp3WWpJMGFVOXFRWFZOUTNkcFkxaFdhR0pIYkRCbFZqbDBZVmMxWm1OWVZtaGlSMnd3WlZOSk5rMXFRWE5KYlVwellWYzFjbGd5T1hkYVZ6VjFXbGhPZWtscWIzZE1ha1Z6U1c1Q2RtTXlWbVpqUjJ3d1dUSm9UbUZYTkdsUGFUQjNUR3BKYzBsdVFuWmpNbFptWTIxV2FtUklaSEJhU0ZKdlNXcHZkMHhxU1RGTVEwcHNaVmRXUkdKSE9YcGFWa1l4V1ZkNGNHUklhMmxQYWtGMVRVUkZjMGx1UW5aak1sWm1ZVmMxTUZwWFpIbGhXRkkxU1dwdmQweHFhM05KYlRGd1ltdzVjR0l5VVdsUGFrRjFUVlJvT1daU1MwRkJiamhHT1hsaFdrSkRWbnBaVDJwbVNuaDNUR0ZCUW1wMFMxOWpSbnBoY25samNVUjJaa1JhTFdWYVdFWXdja05GY0hkelpuUlZVVzkxYmt4eE9FcGtjWGRXVUZCblpuWmxXR3hSUnpJd1VYbFVWME5sWldWelkyVllSelpJUm04d1FsQkNPRk5mWjFNeU1USm5hMjR4ZUVWbVlqQjRlRVpCV1dKUGJ6Rm1hSEZrZVVJd2FtVjZWVWRtWTNScmFYTkdhemt3V0RkaVpsbFJTME5OWlV4bGJETnZaamw0U0c5U1lVOVFNbGg0WTBKUWVqWkNMVlZ5UTNKcFowdENObFpHTjJSQ2FqWkdPV3RIZEZaUlZWVkdRWFppYVRsRVgzQktkMGQxT0RKeFpEQmxUVU4wUWxWdllXWTFObmhNUkcwMk1EVnhZV0ZKUkhvNE9WRjBibTVLWDB0VmJUTnRXRlZGZFZwbWJuaEhZMnBEVnpRdFNHeHVWMEozYkdrNGRIQnFlbmxpUlVjMWRFbGxjMHMwY1VNd1JtOWFXbGQ1YmtseE56WXdXRFZuVFdKMVYyTklOSEpvUzA1bGRXbHRTblU0UVdzNVJrOVJLZ0F5QlZvMU1URXdPZzdtaUpEbGlwOGdLRm8xTVRFd0tRPT0iLCJhdXRoVHlwZSI6IkNPTk5FQ1QiLCJFTlYiOiIwIiwiQzJTX1BVQl9LRVkiOiJNSUlCSWpBTkJna3Foa2lHOXcwQkFRRUZBQU9DQVE4QU1JSUJDZ0tDQVFFQTREemloQlp5NmkvRDcyL2hSRzJzUjVkOWlxeE14QW5XU2dmVGtuZDZQV1ZRZTcwU1lqOGJQMDNMUFVCczFCcE5MTklZRGJURCttR25wUFdYRU1iTkI5M2E1OGJ2NXZKT01aZVJiaGdBeVFQYU9ndTJrOHhzVjJnYXVLc1oyZjRXL0MyemhPV0lMQlV6bEYwcXRTUktic2pGZTdWajY1ckYyWmpXM0s3eWVEQ2R2RVgzcDh3dzJWSi95TE01dmpkYTU0K2JuZEpRTnJaOThuTlZZWkNlT1dMcXBUVXJPR29XWjlHL0JDQnN5WmFudXRQbmhFdzV1WEhsaFpZWU1sdjdSUDY3NEdidDFWRXVOQTBoVjdtMldGQWs2UEJ1SGZkai9UaHNzdjRnbnExU3ltRDBvbmJseVlzU3RGRERsUWVsM0twR0tvSGZ4bkc5WmNkSW9MWS9Vd0lEQVFBQiIsIlJFTU9URUxPR19VUkwiOiJodHRwczovL3NnLXByb2R1Y3Rpb24tem1ncy56b2xvei5jb20vbG9nZ3cvbG9nVXBsb2FkLmRvIn0sInRhc2tzIjpbeyJpbnB1dFBhcmFtcyI6eyJ6aW1JZCI6eyJ2YWx1ZSI6ImZhY2VaaW1JZCIsInBpcGVUeXBlIjoiY29udGV4dCJ9LCJ6aW1Jbml0UmVzcCI6eyJ2YWx1ZSI6InppbUluaXRSZXNwIiwicGlwZVR5cGUiOiJjb250ZXh0In0sInZhbGlkYXRlRGVsZWdhdGUiOnsidmFsdWUiOmZhbHNlLCJwaXBlVHlwZSI6ImNvbnN0YW50In0sInVzZURlZmF1bHRFeGl0Ijp7InZhbHVlIjp0cnVlLCJwaXBlVHlwZSI6ImNvbnN0YW50In19LCJuYW1lIjoiWkZhY2UiLCJpbmRleCI6MSwidHlwZSI6Im5hdGl2ZVRhc2sifV19");
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
                                showResponse("xxxxx", JSON.toJSONString(response));
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
