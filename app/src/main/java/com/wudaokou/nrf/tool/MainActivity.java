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
    Activity activity;
    Context context;
    public static MainActivity instance;
    ImageView imageView;
    Drawable drawable;
    Drawable drawable1;
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
        drawable = AppCompatResources.getDrawable(context, R.drawable.scan);
        drawable1 = AppCompatResources.getDrawable(context, R.drawable.scanning);

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse("https://www.github.com/EX3124/RF-Tool")));
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
                    }
                });
            }
        });

        if (ContextCompat.checkSelfPermission(context, "android.permission.CAMERA") == PackageManager.PERMISSION_GRANTED && PermissionUtils.checkPermission(context))
            button2.setEnabled(true);
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EasyFloat.with(activity)
                        .setLayout(R.layout.float_window, new OnInvokeView() {
                            @Override
                            public void invoke(View view) {
                                imageView = view.findViewById(R.id.float_show);
                                imageView.setBackground(drawable);
                                imageView.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        if (imageView.getBackground() == drawable)
                                            startService(new Intent(context, CameraService.class));
                                        else
                                            stopService(new Intent(context, CameraService.class));
                                    }
                                });
                            }
                        })
                        .setShowPattern(ShowPattern.ALL_TIME)
                        .show();
                moveTaskToBack(true);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 0) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                findViewById(R.id.button).setEnabled(false);
                if (PermissionUtils.checkPermission(context))
                    findViewById(R.id.button2).setEnabled(true);
                else
                    findViewById(R.id.button1).setEnabled(true);
            }
        }
    }
}