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
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

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
    MainActivity mActivity;

    public HoundifyModule(ReactApplicationContext reactContext) {
        super(reactContext);
        InitilizeHoundify(reactContext);
        _context = reactContext;
    }

    public HoundifyModule(ReactApplicationContext reactContext, Activity activity) {
        super(reactContext);
        mActivity = (MainActivity)activity;
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
        if(voiceSearch != null) {
            return;
        }

        voiceSearch = new VoiceSearch.Builder().setRequestInfo(GetHoundRequestInfo())
            .setClientId("_-J90GYyvMo67EmDZtTdwA==")
            .setClientKey("8wg8ubcuB7LsVkQhOoDkjW-pVfXUELgZPHnrkZwg_AclvXUP-ukkz5pRCGYJYMVonQQgz0jSRGes1-2Ese3HMw==")
            .setListener(voiceListener)
            .setAudioSource(new SimpleAudioByteStreamSource())
            .setInputLanguageEnglishName("English")
            .build();

        voiceSearch.start();

        SendEventMessage("onStartRecording", null);
    }

    @ReactMethod
    public void StopRecording() {
        if(voiceSearch != null) {
            voiceSearch.stopRecording();
            voiceSearch = null;
            SendEventMessage("onStopRecording", null);
        }
    }

    @ReactMethod
    public void SearchText(String text) {
        if(asyncTextSearch != null) {
            return;
        }

        AsyncTextSearch.Builder builder = new AsyncTextSearch.Builder()
                .setRequestInfo(GetHoundRequestInfo())
                .setClientId("_-J90GYyvMo67EmDZtTdwA==")
                .setClientKey("8wg8ubcuB7LsVkQhOoDkjW-pVfXUELgZPHnrkZwg_AclvXUP-ukkz5pRCGYJYMVonQQgz0jSRGes1-2Ese3HMw==")
                .setListener(textSerachListener)
                .setQuery(text);

        asyncTextSearch = builder.build();
        asyncTextSearch.start();
    }

    private void StopText() {
        if(asyncTextSearch != null) {
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
        public void onRecordingStopped() { }

        @Override
        public void onAbort(final VoiceSearchInfo info) {}
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
            } catch(final JSONException ex) {
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

        requestInfo.setWakeUpPattern("[(\"hi\" | \"hey\" | \"hello\")] . \"Rosy\"");

        return requestInfo;
    }
}