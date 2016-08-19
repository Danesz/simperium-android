package com.simperium.android;

import com.koushikdutta.async.callback.CompletedCallback;
import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.AsyncHttpRequest;
import com.koushikdutta.async.http.AsyncHttpGet;
import com.koushikdutta.async.http.AsyncHttpClient.WebSocketConnectCallback;
import com.koushikdutta.async.http.WebSocket;

import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import java.io.IOException;

class AsyncWebSocketProvider implements WebSocketManager.ConnectionProvider {

    public static final String TAG = "Simperium.AsyncWebSocketProvider";

    protected final String mAppId;
    protected final String mSessionId;
    protected final AsyncHttpClient mAsyncClient;
    protected final Handler mHandler;

    AsyncWebSocketProvider(String appId, String sessionId, AsyncHttpClient asyncClient) {
        mAppId = appId;
        mAsyncClient = asyncClient;
        mSessionId = sessionId;
        mHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void connect(final WebSocketManager.ConnectionListener listener) {

        Uri uri = Uri.parse(String.format(AndroidClientHelper.WEBSOCKET_URL, mAppId));

        AsyncHttpRequest request = new AsyncHttpGet(uri);
        request.setHeader(AndroidClientHelper.USER_AGENT_HEADER, mSessionId);

        // Protocol is null
        mAsyncClient.websocket(request, null, new WebSocketConnectCallback() {

            @Override
            public void onCompleted(Exception ex, final WebSocket webSocket) {
                if (ex != null) {
                    listener.onError(ex);
                    return;
                }

                if (webSocket == null) {
                    listener.onError(new IOException("WebSocket could not be opened"));
                    return;
                }

                final WebSocketManager.Connection connection = new WebSocketManager.Connection() {

                    @Override
                    public void close() {
                        mHandler.post(new Runnable() {

                            @Override
                            public void run() {
                                webSocket.close();
                            }

                        });
                    }

                    @Override
                    public void send(final String message) {
                        mHandler.post(new Runnable() {

                            @Override
                            public void run() {
                                webSocket.send(message);
                            }

                        });
                    }

                };

                webSocket.setStringCallback(new WebSocket.StringCallback() {

                   @Override
                   public void onStringAvailable(String s) {
                       listener.onMessage(s);
                   }

                });

                webSocket.setEndCallback(new CompletedCallback() {

                    @Override
                    public void onCompleted(Exception ex) {
                        listener.onDisconnect(ex);
                    }

                });

                webSocket.setClosedCallback(new CompletedCallback() {
                    @Override
                    public void onCompleted(Exception ex) {
                        listener.onDisconnect(ex);
                    }
                });

                listener.onConnect(connection);

            }

        });
    }

}
