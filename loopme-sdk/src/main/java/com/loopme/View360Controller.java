package com.loopme;

import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.loopme.adview.AdView;
import com.loopme.common.Logging;
import com.loopme.constants.StretchOption;
import com.loopme.video360.MDVRLibrary;

import java.util.List;

public class View360Controller implements IViewController {

    private static final String LOG_TAG = View360Controller.class.getSimpleName();

    private static final String ACCEL = "Accelerometer";
    private static final String GYRO = "Gyroscope";

    private GLSurfaceView mGLSurfaceView;
    private Callback mCallback;
    private MDVRLibrary mVRLibrary;

    public interface Callback {
        void onSurfaceReady(Surface surface);
        void onEvent(String event);
    }

    public View360Controller(Callback callback) {
        mCallback = callback;
    }

    @Override
    public void setViewSize(int w, int h) {
    }

    @Override
    public void setVideoSize(int w, int h) {
    }

    @Override
    public void buildVideoAdView(Context context, ViewGroup bannerView, AdView adView) {
        mGLSurfaceView = new GLSurfaceView(context);
        bannerView.addView(mGLSurfaceView, 0);

        if (adView != null) {
            adView.setBackgroundColor(Color.TRANSPARENT);
            adView.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null);
            if (adView.getParent() != null) {
                ((ViewGroup) adView.getParent()).removeView(adView);
            }
            bannerView.addView(adView, 1);
        }
    }

    @Override
    public void initVRLibrary(Context context) {
        if (mVRLibrary == null) {
            Logging.out(LOG_TAG, "initVRLibrary");
            mVRLibrary = MDVRLibrary.with(context)
                    .video(new MDVRLibrary.IOnSurfaceReadyCallback() {
                        @Override
                        public void onSurfaceReady(Surface surface) {
                            if (mCallback != null) {
                                mCallback.onSurfaceReady(surface);
                            }
                        }
                    })
                    .build(mGLSurfaceView);

            checkIsAccelGyroPresented(context);
            mVRLibrary.setEventCallback(mCallback);
        }
    }

    private void checkIsAccelGyroPresented(Context context) {
        SensorManager mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> msensorList = mSensorManager.getSensorList(Sensor.TYPE_ALL);
        boolean supportAccel;
        boolean supportGyro;
        for (Sensor s : msensorList) {
            if (s.getName().contains(ACCEL)) {
                supportAccel = true;
                mVRLibrary.setAccelSupported(supportAccel);
            }
            if (s.getName().contains(GYRO)) {
                supportGyro = true;
                mVRLibrary.setGyroSupported(supportGyro);
            }
        }
    }

    @Override
    public boolean handleTouchEvent(MotionEvent event) {
        if (mVRLibrary != null) {
            return mVRLibrary.handleTouchEvent(event);
        }
        return false;
    }

    @Override
    public void onResume() {
        if (mVRLibrary != null && mGLSurfaceView != null && mVRLibrary.isPaused()) {
            Logging.out(LOG_TAG, "VRLibrary onResume");
            mVRLibrary.onResume(mGLSurfaceView.getContext());
        }
    }

    @Override
    public void onPause() {
        if (mVRLibrary != null && mGLSurfaceView != null && !mVRLibrary.isPaused()) {
            Logging.out(LOG_TAG, "VRLibrary onPause");
            mVRLibrary.onPause(mGLSurfaceView.getContext());
        }
    }

    @Override
    public void onDestroy() {
        if (mVRLibrary != null) {
            Logging.out(LOG_TAG, "VRLibrary onDestroy");
            mVRLibrary.onDestroy();
            mVRLibrary = null;
        }
    }

    @Override
    public void rebuildView(ViewGroup bannerView, AdView adView) {
        Logging.out(LOG_TAG, "rebuildView");
        if (bannerView == null || adView == null || mGLSurfaceView == null) {
            return;
        }
        if (mGLSurfaceView.getParent() != null) {
            ((ViewGroup) mGLSurfaceView.getParent()).removeView(mGLSurfaceView);
        }
        if (adView.getParent() != null) {
            ((ViewGroup) adView.getParent()).removeView(adView);
        }
        bannerView.removeAllViews();
        bannerView.addView(mGLSurfaceView, 0);
        bannerView.addView(adView, 1);
    }

    @Override
    public void setStretchParam(StretchOption option) {
    }
}
