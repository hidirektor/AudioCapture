package me.t3sl4.audiohandler.Callback;

import android.content.Intent;

public interface ActivityResultCallback {
    void startActivityForResult(Intent intent, int requestCode);
}

