package com.wudaokou.nrf.tool;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.lzf.easyfloat.EasyFloat;
import com.lzf.easyfloat.enums.ShowPattern;
import com.lzf.easyfloat.interfaces.OnInvokeView;
import com.lzf.easyfloat.interfaces.OnPermissionResult;
import com.lzf.easyfloat.permission.PermissionUtils;

public class MainActivity extends AppCompatActivity {
    public static MainActivity instance;
    Activity activity;
    Context context;
    ImageView scan;
    ImageButton flashlight;
    Drawable drawable;
    Drawable drawable1;
    Drawable drawable2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        activity = this;
        context = this;
        instance = this;

        EdgeToEdge.enable(this);

        Button button = findViewById(R.id.button);
        Button button1 = findViewById(R.id.button1);
        Button button2 = findViewById(R.id.button2);
        ImageButton imageButton = findViewById(R.id.github);
        flashlight = findViewById(R.id.flashlight);
        drawable = AppCompatResources.getDrawable(context, R.drawable.scan);
        drawable1 = AppCompatResources.getDrawable(context, R.drawable.scanning);
        drawable2 = AppCompatResources.getDrawable(context, R.drawable.flashlight);

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse("https://www.github.com/EX3124/RF-Tool")));
            }
        });
        flashlight.setImageDrawable(drawable2);
        flashlight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (flashlight.getDrawable() == drawable2)
                    flashlight.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.flashlight_on));
                else
                    flashlight.setImageDrawable(drawable2);
            }
        });

        if (ContextCompat.checkSelfPermission(context, "android.permission.CAMERA") != PackageManager.PERMISSION_GRANTED)
            button.setEnabled(true);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityCompat.requestPermissions(activity, new String[]{"android.permission.CAMERA"}, 0);
            }
        });

        if (ContextCompat.checkSelfPermission(context, "android.permission.CAMERA") == PackageManager.PERMISSION_GRANTED && !PermissionUtils.checkPermission(context))
            button1.setEnabled(true);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PermissionUtils.requestPermission(activity, new OnPermissionResult() {
                    @Override
                    public void permissionResult(boolean b) {
                        findViewById(R.id.button1).setEnabled(false);
                        findViewById(R.id.button2).setEnabled(true);
                        showFloatWindow();
                    }
                });
            }
        });

        if (ContextCompat.checkSelfPermission(context, "android.permission.CAMERA") == PackageManager.PERMISSION_GRANTED && PermissionUtils.checkPermission(context)) {
            button2.setEnabled(true);
            showFloatWindow();
        }
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveTaskToBack(true);
            }
        });
    }

    public void showFloatWindow() {
        EasyFloat.with(activity)
            .setLayout(R.layout.float_window, new OnInvokeView() {
                    @Override
                    public void invoke(View view) {
                        scan = view.findViewById(R.id.float_show);
                        scan.setBackground(drawable);
                        scan.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (scan.getBackground() == drawable)
                                    startService(new Intent(context, CameraService.class));
                                else
                                    stopService(new Intent(context, CameraService.class));
                            }
                        });
                    }
                })
            .setShowPattern(ShowPattern.ALL_TIME)
            .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 0) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                findViewById(R.id.button).setEnabled(false);
                if (PermissionUtils.checkPermission(context)) {
                    findViewById(R.id.button2).setEnabled(true);
                    showFloatWindow();
                } else
                    findViewById(R.id.button1).setEnabled(true);
            }
        }
    }
}