package com.example.onuchinx.mobilatorium;




import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.media.ImageReader;
import android.os.Handler;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.Collections;

import static android.content.ContentValues.TAG;
import static android.content.Context.CAMERA_SERVICE;

/**
 * Created by onuchinx on 18/03/2017.
 */

public class DoorbellCamera {
    // Camera image parameters (device-specific)
    private static final int IMAGE_WIDTH  =640;
    private static final int IMAGE_HEIGHT = 480;
    private static final int MAX_IMAGES   = 10;
    private static DoorbellCamera camera;

    // Image result processor
    private ImageReader imageReader;
    // Active camera device connection
    private CameraDevice cameraDevice;
    // Active camera capture session
    private CameraCaptureSession captureSession;

    private void triggerImageCapture() {
        try {
            final CaptureRequest.Builder captureBuilder =
                    cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(imageReader.getSurface());
            captureBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON);

            captureSession.capture(captureBuilder.build(), mCaptureCallback, null);
        } catch (CameraAccessException cae) {
            Log.d(TAG, "camera capture exception");
        }
    }

    // Callback handling capture progress events
    private final CameraCaptureSession.CaptureCallback mCaptureCallback =
            new CameraCaptureSession.CaptureCallback() {


                @Override
                public void onCaptureCompleted(CameraCaptureSession session,
                                               CaptureRequest request,
                                               TotalCaptureResult result) {
                    if (session != null) {
                        session.close();
                        captureSession = null;
                        Log.d(TAG, "CaptureSession closed");
                    }
                }
            };

    // Initialize a new camera device connection
    public void initializeCamera(Context context,
                                 Handler backgroundHandler,
                                 ImageReader.OnImageAvailableListener imageListener) {

        // Discover the camera instance
        CameraManager manager = (CameraManager) context.getSystemService(CAMERA_SERVICE);
        String[] camIds = {};
        try {
            camIds = manager.getCameraIdList();
        } catch (CameraAccessException e) {
            Log.d(TAG, "Cam access exception getting IDs", e);
        }
        if (camIds.length < 1) {
            Log.d(TAG, "No cameras found");
            return;
        }
        String id = camIds[0];

        // Initialize image processor
        imageReader = ImageReader.newInstance(IMAGE_WIDTH, IMAGE_HEIGHT,
                ImageFormat.JPEG, MAX_IMAGES);
        imageReader.setOnImageAvailableListener(imageListener, backgroundHandler);

        // Open the camera resource
        try {
            manager.openCamera(id, mStateCallback, backgroundHandler);
        } catch (SecurityException cae) {
            Log.d(TAG, "Camera access exception", cae);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    // Callback handling devices state changes
    private final CameraDevice.StateCallback mStateCallback =
            new CameraDevice.StateCallback() {

                @Override
                public void onOpened(CameraDevice cameraDevice) {
                    cameraDevice = cameraDevice;
                }

                @Override
                public void onDisconnected(@NonNull CameraDevice camera) {

                }

                @Override
                public void onError(@NonNull CameraDevice camera, int error) {

                }


            };

    // Close the camera resources
    public void shutDown() {
        if (cameraDevice != null) {
            cameraDevice.close();
        }
    }


    public void takePicture() {
        if (cameraDevice == null) {
            Log.w(TAG, "Cannot capture image. Camera not initialized.");
            return;
        }

        // Here, we create a CameraCaptureSession for capturing still images.
        try {
            cameraDevice.createCaptureSession(
                    Collections.singletonList(imageReader.getSurface()),
                    mSessionCallback,
                    null);
        } catch (CameraAccessException cae) {
            Log.d(TAG, "access exception while preparing pic", cae);
        }
    }

    // Callback handling session state changes
    private final CameraCaptureSession.StateCallback mSessionCallback =
            new CameraCaptureSession.StateCallback() {

                @Override
                public void onConfigured(CameraCaptureSession cameraCaptureSession) {
                    // When the session is ready, we start capture.
                    captureSession = cameraCaptureSession;
                    triggerImageCapture();
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {
                    Log.w(TAG, "Failed to configure camera");
                }
            };
   public  static  DoorbellCamera getInstance(){
       if (camera==null){
           camera = new DoorbellCamera();
       }
       return  camera;
   }


}
