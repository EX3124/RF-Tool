package com.wudaokou.nrf.tool;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleRegistry;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.util.Objects;


public class CameraService extends Service implements LifecycleOwner {
    private LifecycleRegistry lifecycleRegistry;
    @Override
    public void onCreate() {
        super.onCreate();
        lifecycleRegistry = new LifecycleRegistry(this);
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE);

        NotificationChannel serviceChannel = new NotificationChannel("0", getString(R.string.title), NotificationManager.IMPORTANCE_DEFAULT);
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(serviceChannel);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        MainActivity.instance.scan.setBackground(MainActivity.instance.drawable1);
        Notification notification = new NotificationCompat.Builder(this, "0")
                .setContentTitle(getString(R.string.title))
                .setContentText(getString(R.string.text))
                .setSmallIcon(R.mipmap.ic_launcher)
                .build();
        startForeground(1, notification);

        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START);
        startCamera();

        return START_STICKY;
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

                imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this), new ImageAnalysis.Analyzer() {
                    @OptIn(markerClass = ExperimentalGetImage.class)
                    @Override
                    public void analyze(@NonNull ImageProxy image) {
                        InputImage inputImage = InputImage.fromMediaImage(Objects.requireNonNull(image.getImage()), image.getImageInfo().getRotationDegrees());

                        BarcodeScanner scanner = BarcodeScanning.getClient();
                        scanner.process(inputImage)
                                .addOnSuccessListener(barcodes -> {
                                    for (Barcode barcode : barcodes) {
                                        String CodeResult = barcode.getRawValue();
                                        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY);
                                        sendBroadcast(new Intent().setAction("android.intent.action.SCANRESULT").putExtra("value", CodeResult));
                                        stopSelf();
                                    }
                                    image.close();
                                });
                    }
                });
                cameraProvider.unbindAll();
                Camera camera = cameraProvider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, imageAnalysis);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while (MainActivity.instance.scan.getBackground() == MainActivity.instance.drawable1) {
                            camera.getCameraControl().enableTorch(MainActivity.instance.flashlight);
                            try {
                                Thread.sleep(50);
                            } catch (Exception ignore) {
                            }
                        }
                    }
                }).start();
            } catch (Exception ignore) {
                stopSelf();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    @NonNull
    @Override
    public Lifecycle getLifecycle() {
        return lifecycleRegistry;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY);
        MainActivity.instance.scan.setBackground(MainActivity.instance.drawable);

    }
}
