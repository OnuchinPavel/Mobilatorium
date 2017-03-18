package com.example.onuchinx.mobilatorium;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.widget.ImageView;

import com.google.android.things.contrib.driver.button.Button;
import com.google.android.things.pio.PeripheralManagerService;

import java.io.IOException;
import java.nio.ByteBuffer;

import timber.log.Timber;

/**
 * Created by onuchinx on 18/03/2017.
 */

public class DoorbellActivity extends Activity {
    private Handler backgroundHandler;
    private HandlerThread backgroundThread;
    private DoorbellCamera camera;
    private static final String GPIO_PIN_BUTTON_NAME = "BCM21";
    ImageView imageView;

    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        imageView = (ImageView)findViewById(R.id.imageView);

        startBackgroundThread();
        camera = DoorbellCamera.getInstance();
        camera.initializeCamera(this, backgroundHandler, mOnImageAvailableListener);


        Timber.plant(new Timber.DebugTree());

        PeripheralManagerService service = new PeripheralManagerService();
        Timber.d("Available GPIO: " + service.getGpioList());

        try {
            button = new Button(
                    GPIO_PIN_BUTTON_NAME,
                    Button.LogicState.PRESSED_WHEN_LOW);
            button.setOnButtonEventListener(new Button.OnButtonEventListener() {
                @Override
                public void onButtonEvent(Button button, boolean pressed) {
                    String pressedString = pressed ? "pressed" : "unpressed";
                    Timber.d(GPIO_PIN_BUTTON_NAME + ": " + pressedString);

                }
            });
        } catch (IOException e) {
            Timber.e(e);
        }

    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            button.close();
        } catch (IOException e) {
            Timber.e(e, "Error while close " + GPIO_PIN_BUTTON_NAME + "button");
        }

        backgroundThread.quitSafely();
        camera.shutDown();

    }
    private Button.OnButtonEventListener mButtonCallback =
            new Button.OnButtonEventListener() {
                @Override
                public void onButtonEvent(Button button, boolean pressed) {
                    if (pressed) {
                        // Doorbell rang!
                        camera.takePicture();
                    }
                }
            };

    private ImageReader.OnImageAvailableListener mOnImageAvailableListener =
            new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader reader) {
                    // Get the raw image bytes
                    Image image = reader.acquireLatestImage();
                    ByteBuffer imageBuf = image.getPlanes()[0].getBuffer();
                    final byte[] imageBytes = new byte[imageBuf.remaining()];
                    imageBuf.get(imageBytes);
                    image.close();

                    onPictureTaken(imageBytes);
                }
            };

    private void onPictureTaken(byte[] imageBytes) {
        if (imageBytes != null) {
            // ...process the captured image...
            Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            imageView.setImageBitmap(bitmap);
        }
    }

    private void startBackgroundThread() {
        backgroundThread = new HandlerThread("InputThread");
        backgroundThread.start();
        backgroundHandler = new Handler(backgroundThread.getLooper());
    }
}
