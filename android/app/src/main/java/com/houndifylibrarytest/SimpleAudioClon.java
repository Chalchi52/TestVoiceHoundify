package com.houndifylibrarytest;

import android.os.Environment;

import com.hound.android.sdk.audio.SimpleAudioByteStreamSource;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SimpleAudioClon extends SimpleAudioByteStreamSource {
    private ByteArrayOutputStream baos;
    private DataOutputStream bytesList;
    private byte[] audioData;
    private int offsetAudio;
    private int byteCount;

    public SimpleAudioClon() {
        this.offsetAudio = 0;
        this.byteCount = 0;
        this.audioData = new byte[]{};
        baos = new ByteArrayOutputStream();
        bytesList = new DataOutputStream(baos);
    }

    public SimpleAudioClon(int preferredAudioRecordSource) {
        super(preferredAudioRecordSource);
        baos = new ByteArrayOutputStream();
        bytesList = new DataOutputStream(baos);
        this.offsetAudio = 0;
        this.byteCount = 0;
        this.audioData = new byte[]{};
    }

    @Override
    public int read() throws IOException {
        return super.read();
    }

    @Override
    public synchronized int read(byte[] buffer, int byteOffset, int byteCount) {
//        byte[] c = new byte[audioData.length + buffer.length];
//        System.arraycopy(audioData, 0, c, 0, audioData.length);
//        System.arraycopy(buffer, 0, c, audioData.length, buffer.length);
//        this.audioData = c;
//        this.offsetAudio += byteOffset;
//        this.byteCount += byteCount;
        try {
            bytesList.write(Arrays.copyOf(buffer, byteCount));
            this.offsetAudio += byteOffset;
            this.byteCount += byteCount;
        } catch (Exception e) {

        }
        return super.read(buffer, byteOffset, byteCount);
    }

    @Override
    public synchronized void close() throws IOException {
        super.close();
        //bytesList.flush();
    }

    @Override
    public synchronized void setAudioSource(int audioSource) throws IOException {
        super.setAudioSource(audioSource);
    }

    public byte[] getAudioData() {
        try {
            bytesList.flush();
            if (baos.size() > 0) {
                audioData = baos.toByteArray();
            } else {
                audioData = new byte[]{};
            }
        } catch (Exception e) {
            audioData = new byte[]{};
        }
        return audioData;
    }

    public int getByteCount() {
        return byteCount;
    }

    public int getOffsetAudio() {
        return offsetAudio;
    }
}
