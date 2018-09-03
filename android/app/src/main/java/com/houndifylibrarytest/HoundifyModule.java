package com.houndifylibrarytest;

import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.hound.android.fd.DefaultRequestInfoFactory;
import com.hound.android.fd.Houndify;
import com.hound.android.sdk.AsyncTextSearch;
import com.hound.android.sdk.TextSearchListener;
import com.hound.android.sdk.VoiceSearch;
import com.hound.android.sdk.VoiceSearchInfo;
import com.hound.android.sdk.audio.SimpleAudioByteStreamSource;
import com.hound.android.sdk.util.HoundRequestInfoFactory;
import com.hound.core.model.sdk.HoundRequestInfo;
import com.hound.core.model.sdk.HoundResponse;
import com.hound.core.model.sdk.PartialTranscript;

import android.app.Activity;
import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;

public class HoundifyModule extends ReactContextBaseJavaModule {

    private static final String DURATION_SHORT_KEY = "SHORT";
    private static final String DURATION_LONG_KEY = "LONG";
    private Houndify houndify;
    private VoiceSearch voiceSearch;
    private AsyncTextSearch asyncTextSearch;
    private ReactApplicationContext _context;
    private SimpleAudioClon _simpleAudio;
    MainActivity mActivity;

    public HoundifyModule(ReactApplicationContext reactContext) {
        super(reactContext);
        InitilizeHoundify(reactContext);
        _context = reactContext;
    }

    public HoundifyModule(ReactApplicationContext reactContext, Activity activity) {
        super(reactContext);
        mActivity = (MainActivity) activity;
        _context = reactContext;
        InitilizeHoundify(reactContext);
    }

    private void InitilizeHoundify(ReactApplicationContext reactContext) {
        //StartRecording(new SimpleAudioByteStreamSource());
    }

    @Override
    public Map<String, Object> getConstants() {
        final Map<String, Object> constants = new HashMap<>();
        constants.put(DURATION_SHORT_KEY, "Short");
        constants.put(DURATION_LONG_KEY, "Long");
        return constants;
    }

    @Override
    public String getName() {
        return "ReactHoundifyTest";
    }

    @ReactMethod
    public void Show(String message) {
        Toast.makeText(getReactApplicationContext(), message, Toast.LENGTH_LONG).show();
    }

    @ReactMethod
    public void StartRecording() {
        if (voiceSearch != null) {
            return;
        }
        String clientId = _context.getResources().getString(R.string.houndify_cliente_id_0);
        String clientKey = _context.getResources().getString(R.string.houndify_cliente_key_0);
        this._simpleAudio = new SimpleAudioClon();
        voiceSearch = new VoiceSearch.Builder().setRequestInfo(GetHoundRequestInfo())
                //.setClientId("_-J90GYyvMo67EmDZtTdwA==")
                //.setClientKey("8wg8ubcuB7LsVkQhOoDkjW-pVfXUELgZPHnrkZwg_AclvXUP-ukkz5pRCGYJYMVonQQgz0jSRGes1-2Ese3HMw==")
                .setClientId(clientId)
                .setClientKey(clientKey)
                .setListener(voiceListener)
                //.setAudioSource(new SimpleAudioByteStreamSource())
                .setAudioSource(this._simpleAudio)
                .setInputLanguageEnglishName("English")
                .build();

        voiceSearch.start();

        SendEventMessage("onStartRecording", null);
    }

    @ReactMethod
    public void StopRecording() {
        if (voiceSearch != null) {
            voiceSearch.stopRecording();
            voiceSearch = null;
            if (_simpleAudio != null) {
                String path = "testAudio" + _simpleAudio.getByteCount() + ".aac";
                Log.d("StopRecording", "archivo: " + path);
                Log.d("StopRecording", "getAudioData: " + _simpleAudio.getAudioData().length);
                Log.d("StopRecording", "getByteCount: " + _simpleAudio.getByteCount());
                Log.d("StopRecording", "getOffsetAudio: " + _simpleAudio.getOffsetAudio());
                Log.d("StopRecording", "Directorio: " + _context.getFilesDir());
                Log.d("StopRecording", "Directorio2: " + Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS));
                File mp3 = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), path);
                FileOutputStream outputStream;

                try {
                    FileOutputStream fos = new FileOutputStream(mp3);
                    fos.write(_simpleAudio.getAudioData());
                    fos.close();
                    Log.d("StopRecording", "Debe existir el archivo AAC");
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.d("StopRecording", "No guardo ni mergas: " + e.getMessage());
                }
            } else {
                Log.d("StopRecording", "_simpleAudio es Nulo ijaaaaa");
            }
            SendEventMessage("onStopRecording", null);
        }
    }

    @ReactMethod
    public void SearchText(String text) {
        if (asyncTextSearch != null) {
            return;
        }
        String clientId = _context.getResources().getString(R.string.houndify_cliente_id_0);
        String clientKey = _context.getResources().getString(R.string.houndify_cliente_key_0);
        AsyncTextSearch.Builder builder = new AsyncTextSearch.Builder()
                .setRequestInfo(GetHoundRequestInfo())
                //.setClientId("_-J90GYyvMo67EmDZtTdwA==")
                //.setClientKey("8wg8ubcuB7LsVkQhOoDkjW-pVfXUELgZPHnrkZwg_AclvXUP-ukkz5pRCGYJYMVonQQgz0jSRGes1-2Ese3HMw==")
                .setClientId(clientId)
                .setClientKey(clientKey)
                .setListener(textSerachListener)
                .setQuery(text);

        asyncTextSearch = builder.build();
        asyncTextSearch.start();
    }

    private void StopText() {
        if (asyncTextSearch != null) {
            asyncTextSearch = null;
        }
    }

    private final Listener voiceListener = new Listener();

    private class Listener implements VoiceSearch.RawResponseListener {

        @Override
        public void onTranscriptionUpdate(final PartialTranscript transcript) {
        }

        @Override
        public void onResponse(String rawResponse, VoiceSearchInfo voiceSearchInfo) {
            StopRecording();
            String jsonString;

            Show("Response");
            try {
                jsonString = new JSONObject(rawResponse).toString(4);
                SendEventMessage("onHoundifyResponse", jsonString);

            } catch (final JSONException ex) {
                jsonString = "Failed to parse content:\n" + rawResponse;
                SendEventMessage("onHoundifyResponseError", jsonString);
            }
        }

        @Override
        public void onError(final Exception ex, final VoiceSearchInfo info) {
            StopRecording();
            SendEventMessage("onListeningError", ex.getMessage());
        }

        @Override
        public void onRecordingStopped() {
        }

        @Override
        public void onAbort(final VoiceSearchInfo info) {
        }
    }

    private final TextListener textSerachListener = new TextListener();

    private class TextListener implements AsyncTextSearch.RawResponseListener {

        @Override
        public void onError(Exception e, VoiceSearchInfo info) {
            StopText();
            SendEventMessage("onHoundifyTextError", e.getMessage());
        }

        @Override
        public void onAbort(VoiceSearchInfo info) {

        }

        @Override
        public void onResponse(String rawResponse, VoiceSearchInfo voiceSearchInfo) {
            StopText();
            String jsonString;

            try {
                jsonString = new JSONObject(rawResponse).toString(4);
                SendEventMessage("onHoundifyTextResponse", jsonString);
            } catch (final JSONException ex) {
                SendEventMessage("onHoundifyTextError", ex.getMessage());
            }

        }
    }

    private void SendEventMessage(String eventName, Object message) {
        _context.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(eventName, message);
    }

    private HoundRequestInfo GetHoundRequestInfo() {
        final HoundRequestInfo requestInfo = HoundRequestInfoFactory.getDefault(_context);
        requestInfo.setUserId("Mac testing");
        requestInfo.setRequestId(UUID.randomUUID().toString());

        requestInfo.setWakeUpPattern("[(\"hi\" | \"hey\" | \"hello\")] .\"Rosy\"");

        return requestInfo;
    }
}