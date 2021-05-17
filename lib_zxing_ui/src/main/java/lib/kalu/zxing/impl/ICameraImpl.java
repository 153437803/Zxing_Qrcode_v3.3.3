package lib.kalu.zxing.impl;

import android.content.Context;
import android.view.View;
import com.google.zxing.qrcode.QRCodeReader;

import lib.kalu.zxing.analyze.AnalyzerImpl;
import lib.kalu.zxing.camerax.CameraConfig;
import lib.kalu.zxing.listener.OnCameraScanChangeListener;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.CameraSelector;

/**
 * @description: 相机生命周期Impl
 * @date: 2021-05-17 13:46
 */
public interface ICameraImpl extends ICameraBaseLifecycleImpl, ICameraBaseControlImpl {

    String SCAN_RESULT = "SCAN_RESULT";
    int LENS_FACING_FRONT = CameraSelector.LENS_FACING_FRONT;
    int LENS_FACING_BACK = CameraSelector.LENS_FACING_BACK;

    /**
     * 是否需要支持自动缩放
     */
    boolean isNeedAutoZoom = true;

    /**
     * 是否需要支持触摸缩放
     */
    boolean isNeedTouchZoom = true;

    /**
     * 是否需要支持触摸缩放
     *
     * @return
     */
    default boolean isNeedTouchZoom() {
        return isNeedTouchZoom;
    }


//    /**
//     * 设置是否需要支持触摸缩放
//     *
//     * @param needTouchZoom
//     * @return
//     */
//    default ICameraImpl setNeedTouchZoom(boolean needTouchZoom) {
//        isNeedTouchZoom = needTouchZoom;
//        return this;
//    }

    /**
     * 是否需要支持自动缩放
     *
     * @return
     */
    default boolean isNeedAutoZoom() {
        return isNeedAutoZoom;
    }

//    /**
//     * 设置是否需要支持自动缩放
//     *
//     * @param needAutoZoom
//     * @return
//     */
//    public ICameraImpl setNeedAutoZoom(boolean needAutoZoom) {
//        isNeedAutoZoom = needAutoZoom;
//        return this;
//    }

    /**
     * 设置相机配置，请在{@link #start(@NonNull Context context)}之前调用
     *
     * @param cameraConfig
     */
    ICameraImpl setCameraConfig(CameraConfig cameraConfig);

    /**
     * 设置是否分析图像
     *
     * @param analyze
     */
   ICameraImpl setAnalyzeImage(boolean analyze);

    /**
     * 设置分析器，内置了一些{@link AnalyzerImpl}的实现类如下
     *
     * @param analyzer
     * @see {@link QRCodeReader}
     */
   ICameraImpl setAnalyzer(AnalyzerImpl analyzer);

    /**
     * 设置是否震动
     *
     * @param vibrate
     */
    ICameraImpl setVibrate(boolean vibrate);

    /**
     * 设置是否播放提示音
     *
     * @param playBeep
     */
   ICameraImpl setPlayBeep(boolean playBeep);

    /**
     * 绑定手电筒，绑定后可根据光线传感器，动态显示或隐藏手电筒
     *
     * @param v
     */
    ICameraImpl bindFlashlightView(@Nullable View v);

    /**
     * 设置光线足够暗的阈值（单位：lux），需要通过{@link #bindFlashlightView(View)}绑定手电筒才有效
     *
     * @param lightLux
     */
    ICameraImpl setDarkLightLux(float lightLux);

    /**
     * 设置光线足够明亮的阈值（单位：lux），需要通过{@link #bindFlashlightView(View)}绑定手电筒才有效
     *
     * @param lightLux
     */
     ICameraImpl setBrightLightLux(float lightLux);

    /*********************/

    /**
     * 设置扫码结果回调
     *
     * @param callback
     */
    ICameraImpl setOnCameraScanChangeListener(@NonNull OnCameraScanChangeListener callback);
}
