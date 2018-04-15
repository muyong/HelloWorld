package com.example.yongmu.helloworld;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import java.net.URI;
import java.net.URISyntaxException;

import org.json.JSONException;
import org.json.JSONObject;

import android.widget.Button;
import android.widget.TextView;
import android.util.Log;
import android.os.Build;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class MainActivity extends AppCompatActivity {
    public static final String EXTRA_MESSAGE = "com.example.myfirstapp.MESSAGE";

    private OkHttpClient client;
    private WebSocket ws;

    private final class EchoWebSocketListener extends WebSocketListener {
        private static final int NORMAL_CLOSURE_STATUS = 1000;
        @Override
        public void onOpen(WebSocket webSocket, Response response) {
            try {
                webSocket.send(formatMessage("Hello", "Android app"));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            //webSocket.close(NORMAL_CLOSURE_STATUS, "Goodbye !");
        }
        @Override
        public void onMessage(WebSocket webSocket, String text) {
            Log.i("Websocket", "Receiving text : " + text);
            JSONObject message = null;
            try {
                message = new JSONObject(text);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            if(message != null) {
                String content = null;
                try {
                    content = (String)message.get("content");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                final String finalContent = content;
                runOnUiThread(()-> {
                        TextView textView = (TextView)findViewById(R.id.textView);
                        textView.setText(textView.getText() + "\n" + finalContent);
                });
            }
        }
        @Override
        public void onClosing(WebSocket webSocket, int code, String reason) {
            webSocket.close(NORMAL_CLOSURE_STATUS, null);
            Log.i("Websocket","Closing : " + code + " / " + reason);
        }
        @Override
        public void onFailure(WebSocket webSocket, Throwable t, Response response) {
            Log.i("Websocket","Error : " + t.getMessage());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        client = new OkHttpClient();
        start();
    }

    /** Called when the user taps the Send button */
    public void sendMessage(View view) {
        //Intent intent = new Intent(this, DisplayMessageActivity.class);
        EditText editText = (EditText) findViewById(R.id.editText);
        String message = editText.getText().toString();
        //intent.putExtra(EXTRA_MESSAGE, message);
        //startActivity(intent);

        try {
            ws.send(formatMessage(message, Build.MANUFACTURER + " " + Build.MODEL));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        System.exit(0);
    }

    private static String formatMessage(String message, String user) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("content", message);
        json.put("sender", user);
        json.put("received", "");
        return json.toString();
    }

    private void start() {
        Request request = new Request.Builder().url("ws://10.0.14.100:8025/ws/chat").build();
        EchoWebSocketListener listener = new EchoWebSocketListener();
        ws = client.newWebSocket(request, listener);
        //client.dispatcher().executorService().shutdown();
    }
}
