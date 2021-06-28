package lib.kalu.zbar;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageSwitcher;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;

import net.sourceforge.zbar.Format;
import net.sourceforge.zbar.Image;
import net.sourceforge.zbar.ImageScanner;
import net.sourceforge.zbar.Symbol;
import net.sourceforge.zbar.SymbolSet;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.ByteBuffer;

import lib.kalu.zbar.camerax.CameraManager;
import lib.kalu.zbar.listener.OnCameraStatusChangeListener;
import lib.kalu.zbar.util.BeepUtil;
import lib.kalu.zbar.util.BitmapUtil;
import lib.kalu.zbar.util.LogUtil;
import lib.kalu.zbar.util.VibratorUtil;
import lib.kalu.zbar.util.ZbarUtil;

/**
 * @description: 二维码扫描UI
 * @date: 2021-05-07 10:50
 */
@Keep
public final class QrcodeActivity extends AppCompatActivity implements OnCameraStatusChangeListener {

    @Keep
    public static final int RESULT_CODE_SUCC = 10890001;
    @Keep
    public static final int RESULT_CODE_FAIL = 10890002;
    @Keep
    public static final int RESULT_CODE_CANCLE = 10890003;
    @Keep
    public static final String INTENT_RESULT = "intent_result";
    @Keep
    public static final String INTENT_EXTRA = "intent_extra";
    @Keep
    public static final String INTENT_BEEP = "intent_beep";
    @Keep
    public static final String INTENT_VIBRATOR = "intent_vibrator";
    @Keep
    public static final String INTENT_TOAST = "intent_toast";

    @Override
    public void onBackPressed() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        Intent intent = new Intent();
        intent.putExtra(INTENT_EXTRA, getIntent().getStringExtra(INTENT_EXTRA));
        setResult(RESULT_CODE_CANCLE, intent);
        super.onBackPressed();
    }

    @Override
    public void finish() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.finish();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.moudle_zbar_ui_activity_qrcode);

        // 关闭
        findViewById(R.id.lib_zbar_ui_id_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // 相册
        findViewById(R.id.lib_zbar_ui_id_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent intent = new Intent();
                    /* 开启Pictures画面Type设定为image */
                    intent.setType("image/*");
                    /* 使用Intent.ACTION_GET_CONTENT这个Action */
                    // intent.setAction(Intent.ACTION_GET_CONTENT);
                    intent.setAction(Intent.ACTION_PICK);
                    /* 取得相片后返回本画面 */
                    startActivityForResult(intent, 0X87);
                } catch (Exception e) {
                }
            }
        });

        // 手电筒
        findViewById(R.id.lib_zbar_ui_id_flashlight).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {

                    CameraManager cameraManager = CameraManager.build();
                    boolean isTorch = cameraManager.isTorchEnabled();
                    cameraManager.enableTorch(!isTorch);

                    TextView textView = findViewById(R.id.lib_zbar_ui_id_flashlight);
                    textView.setCompoundDrawablesWithIntrinsicBounds(0, !isTorch ? R.drawable.moudle_zbar_ic_flashlight_on : R.drawable.moudle_zbar_ic_flashlight_off, 0, 0);
                    textView.setText(!isTorch ? R.string.moudle_zbar_string_light_off : R.string.moudle_zbar_string_light_on);
                } catch (Exception e) {
                }
            }
        });

        // 预览
        initCamera();

        // 相机
        startCamera();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        CameraManager.build().resume(this);
        TextView textView = findViewById(R.id.lib_zbar_ui_id_flashlight);
        textView.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.moudle_zbar_ic_flashlight_off, 0, 0);
        textView.setText(R.string.moudle_zbar_string_light_on);
    }

    @Override
    protected void onPause() {
        super.onPause();
        CameraManager.build().pause(this);
    }

    /**
     * 初始化相机
     */
    public void initCamera() {
        PreviewView previewView = findViewById(R.id.lib_zbar_ui_id_preview);
        CameraManager cameraManager = CameraManager.build();
        cameraManager.init(this, previewView, true, true);
        cameraManager.setOnCameraScanChangeListener(this);
    }

    /**
     * 启动相机预览
     */
    public void startCamera() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            PreviewView previewView = findViewById(R.id.lib_zbar_ui_id_preview);
            CameraManager.build().start(this, previewView);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 0X86);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != 0X86)
            return;
        requestCameraPermissionResult(permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        LogUtil.log("onActivityResult => requestCode = " + requestCode + ", resultCode = " + resultCode + ", data = " + data + ", uri = " + (null == data ? "null" : data.getData()));
        if (null == data || null == data.getData() || requestCode != 0X87)
            return;

        Uri uri = data.getData();
        if (null == uri || null == uri.toString() || uri.toString().length() == 0) {
            Toast.makeText(getApplicationContext(), R.string.moudle_zbar_string_null, Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {

                String decodeFromUrl = ZbarUtil.decodeFromUrl(getApplicationContext(), uri);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        boolean toast = getIntent().getBooleanExtra(INTENT_TOAST, false);
                        if (toast) {
                            Toast.makeText(getApplicationContext(), decodeFromUrl, Toast.LENGTH_SHORT).show();
                        }
                        Intent intent = new Intent();
                        intent.putExtra(INTENT_RESULT, decodeFromUrl);
                        intent.putExtra(INTENT_EXTRA, getIntent().getStringExtra(INTENT_EXTRA));
                        setResult(null != decodeFromUrl && decodeFromUrl.length() > 0 ? RESULT_CODE_SUCC : RESULT_CODE_FAIL, intent);
                        finish();
                    }
                });
            }
        }).start();
    }

    /**
     * 请求Camera权限回调结果
     *
     * @param permissions
     * @param grantResults
     */
    public void requestCameraPermissionResult(@NonNull String[] permissions, @NonNull int[] grantResults) {

        int length = permissions.length;
        for (int i = 0; i < length; i++) {
            if (Manifest.permission.CAMERA.equals(permissions[i])) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    startCamera();
                    return;
                }
            }
        }

        finish();
    }

    @Override
    protected void onDestroy() {
        CameraManager.build().release(this);
        super.onDestroy();

        try {
            android.os.Process.killProcess(android.os.Process.myPid());
        } catch (Exception e) {
            System.exit(0);
        }
    }

    @Override
    public void onResult(Symbol result) {

        // 声音
        boolean beep = getIntent().getBooleanExtra(INTENT_BEEP, true);
        if (beep) {
            BeepUtil.beep();
        }

        // 震动
        boolean vibrator = getIntent().getBooleanExtra(INTENT_VIBRATOR, true);
        if (vibrator) {
            VibratorUtil.vibrator(getApplicationContext());
        }

        // 释放
        if (beep) {
            BeepUtil.release();
        }

        Intent intent = new Intent();
        intent.putExtra(INTENT_RESULT, result.getData());
        intent.putExtra(INTENT_EXTRA, getIntent().getStringExtra(INTENT_EXTRA));
        setResult(null != result && null != result.getData() && result.getData().length() > 0 ? RESULT_CODE_SUCC : RESULT_CODE_FAIL, intent);
        finish();
    }

    @Override
    public void onSensor(boolean dark, float lightLux) {
    }
}