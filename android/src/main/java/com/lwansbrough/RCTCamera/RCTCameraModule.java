/**
 * Created by Fabrice Armisen (farmisen@gmail.com) on 1/4/16.
 */

package com.lwansbrough.RCTCamera;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.util.Pair;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

public class RCTCameraModule extends ReactContextBaseJavaModule {
    private static final String TAG = "RCTCameraModule";

    public static final int RCT_CAMERA_ASPECT_FILL = 0;
    public static final int RCT_CAMERA_ASPECT_FIT = 1;
    public static final int RCT_CAMERA_ASPECT_STRETCH = 2;
    public static final int RCT_CAMERA_CAPTURE_MODE_STILL = 0;
    public static final int RCT_CAMERA_CAPTURE_MODE_VIDEO = 1;
    public static final int RCT_CAMERA_CAPTURE_TARGET_MEMORY = 0;
    public static final int RCT_CAMERA_CAPTURE_TARGET_DISK = 1;
    public static final int RCT_CAMERA_CAPTURE_TARGET_CAMERA_ROLL = 2;
    public static final int RCT_CAMERA_CAPTURE_TARGET_TEMP = 3;
    public static final int RCT_CAMERA_ORIENTATION_AUTO = 0;
    public static final int RCT_CAMERA_ORIENTATION_LANDSCAPE_LEFT = 1;
    public static final int RCT_CAMERA_ORIENTATION_LANDSCAPE_RIGHT = 2;
    public static final int RCT_CAMERA_ORIENTATION_PORTRAIT = 3;
    public static final int RCT_CAMERA_ORIENTATION_PORTRAIT_UPSIDE_DOWN = 4;
    public static final int RCT_CAMERA_TYPE_FRONT = 1;
    public static final int RCT_CAMERA_TYPE_BACK = 2;
    public static final int RCT_CAMERA_FLASH_MODE_OFF = 0;
    public static final int RCT_CAMERA_FLASH_MODE_ON = 1;
    public static final int RCT_CAMERA_FLASH_MODE_AUTO = 2;
    public static final int RCT_CAMERA_TORCH_MODE_OFF = 0;
    public static final int RCT_CAMERA_TORCH_MODE_ON = 1;
    public static final int RCT_CAMERA_TORCH_MODE_AUTO = 2;
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    public static final int WHAT_STOP_CAMERA = 0;
    public static final int WHAT_START_CAMERA = 1;

    private final ReactApplicationContext _reactContext;

    private Handler mainHandler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case WHAT_STOP_CAMERA: {
                    Camera camera = (Camera) msg.obj;
                    camera.stopPreview();
                    return true;
                }
                case WHAT_START_CAMERA: {
                    try {
                        Camera camera = (Camera) msg.obj;
                        camera.startPreview();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return true;
                }
            }
            return false;
        }
    });

    public RCTCameraModule(ReactApplicationContext reactContext) {
        super(reactContext);
        _reactContext = reactContext;
    }

    @Override
    public String getName() {
        return "RCTCameraModule";
    }

    @Nullable
    @Override
    public Map<String, Object> getConstants() {
        return Collections.unmodifiableMap(new HashMap<String, Object>() {
            {
                put("Aspect", getAspectConstants());
                put("Type", getTypeConstants());
                put("CaptureQuality", getCaptureQualityConstants());
                put("CaptureMode", getCaptureModeConstants());
                put("CaptureTarget", getCaptureTargetConstants());
                put("Orientation", getOrientationConstants());
                put("FlashMode", getFlashModeConstants());
                put("TorchMode", getTorchModeConstants());
            }

            private Map<String, Object> getAspectConstants() {
                return Collections.unmodifiableMap(new HashMap<String, Object>() {
                    {
                        put("stretch", RCT_CAMERA_ASPECT_STRETCH);
                        put("fit", RCT_CAMERA_ASPECT_FIT);
                        put("fill", RCT_CAMERA_ASPECT_FILL);
                    }
                });
            }

            private Map<String, Object> getTypeConstants() {
                return Collections.unmodifiableMap(new HashMap<String, Object>() {
                    {
                        put("front", RCT_CAMERA_TYPE_FRONT);
                        put("back", RCT_CAMERA_TYPE_BACK);
                    }
                });
            }

            private Map<String, Object> getCaptureQualityConstants() {
                return Collections.unmodifiableMap(new HashMap<String, Object>() {
                    {
                        put("low", "low");
                        put("medium", "medium");
                        put("high", "high");
                    }
                });
            }

            private Map<String, Object> getCaptureModeConstants() {
                return Collections.unmodifiableMap(new HashMap<String, Object>() {
                    {
                        put("still", RCT_CAMERA_CAPTURE_MODE_STILL);
                        put("video", RCT_CAMERA_CAPTURE_MODE_VIDEO);
                    }
                });
            }

            private Map<String, Object> getCaptureTargetConstants() {
                return Collections.unmodifiableMap(new HashMap<String, Object>() {
                    {
                        put("memory", RCT_CAMERA_CAPTURE_TARGET_MEMORY);
                        put("disk", RCT_CAMERA_CAPTURE_TARGET_DISK);
                        put("cameraRoll", RCT_CAMERA_CAPTURE_TARGET_CAMERA_ROLL);
                        put("temp", RCT_CAMERA_CAPTURE_TARGET_TEMP);
                    }
                });
            }

            private Map<String, Object> getOrientationConstants() {
                return Collections.unmodifiableMap(new HashMap<String, Object>() {
                    {
                        put("auto", RCT_CAMERA_ORIENTATION_AUTO);
                        put("landscapeLeft", RCT_CAMERA_ORIENTATION_LANDSCAPE_LEFT);
                        put("landscapeRight", RCT_CAMERA_ORIENTATION_LANDSCAPE_RIGHT);
                        put("portrait", RCT_CAMERA_ORIENTATION_PORTRAIT);
                        put("portraitUpsideDown", RCT_CAMERA_ORIENTATION_PORTRAIT_UPSIDE_DOWN);
                    }
                });
            }

            private Map<String, Object> getFlashModeConstants() {
                return Collections.unmodifiableMap(new HashMap<String, Object>() {
                    {
                        put("off", RCT_CAMERA_FLASH_MODE_OFF);
                        put("on", RCT_CAMERA_FLASH_MODE_ON);
                        put("auto", RCT_CAMERA_FLASH_MODE_AUTO);
                    }
                });
            }

            private Map<String, Object> getTorchModeConstants() {
                return Collections.unmodifiableMap(new HashMap<String, Object>() {
                    {
                        put("off", RCT_CAMERA_TORCH_MODE_OFF);
                        put("on", RCT_CAMERA_TORCH_MODE_ON);
                        put("auto", RCT_CAMERA_TORCH_MODE_AUTO);
                    }
                });
            }
        });
    }

    @ReactMethod
    public void capture(final ReadableMap options, final Promise promise) {
        try {
            captureImage(options, promise);
        } catch (Exception e) {
            promise.reject(e);
        }
    }

    private void captureImage(final ReadableMap options, final Promise promise) {
        Camera camera = RCTCamera.getInstance().acquireCameraInstance(options.getInt("type"));
        if (null == camera) {
            promise.reject("No camera found.");
            return;
        }
        RCTCamera.getInstance().setCaptureQuality(options.getInt("type"), options.getString("quality"));
        camera.takePicture(null, null, new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(final byte[] data, final Camera camera) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        mainHandler.sendMessage(buildCameraMessage(WHAT_STOP_CAMERA, camera));

                        try {
                            handlePictureTakenResult(data, options, camera, promise);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        final int screenTransitionDelayMillis = 300;
                        mainHandler.sendMessageDelayed(buildCameraMessage(WHAT_START_CAMERA, camera), screenTransitionDelayMillis);
                    }
                }).start();
            }
        });
    }

    private Message buildCameraMessage(int what, Camera camera) {
        Message message = new Message();
        message.what = what;
        message.obj = camera;
        return message;
    }

    private void handlePictureTakenResult(byte[] data, ReadableMap options, Camera camera, Promise promise) {
        final Camera.Size pictureSize = camera.getParameters().getPictureSize();

        WritableMap response = Arguments.createMap();
        final int width = pictureSize.width;
        final int height = pictureSize.height;

        response.putInt("width", width);
        response.putInt("height", height);

        switch (options.getInt("target")) {
            case RCT_CAMERA_CAPTURE_TARGET_MEMORY:
                String encoded = Base64.encodeToString(data, Base64.DEFAULT);
                response.putString("data", encoded);
                promise.resolve(response);
                break;
            case RCT_CAMERA_CAPTURE_TARGET_CAMERA_ROLL:
                BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, bitmapOptions);
                String url = MediaStore.Images.Media.insertImage(
                        _reactContext.getContentResolver(),
                        bitmap, options.getString("title"),
                        options.getString("description"));

                response.putString("uri", url);
                promise.resolve(response);
                break;
            case RCT_CAMERA_CAPTURE_TARGET_DISK:
                File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
                if (pictureFile == null) {
                    promise.reject("Error creating media file.");
                    return;
                }

                try {
                    FileOutputStream fos = new FileOutputStream(pictureFile);
                    fos.write(data);
                    fos.close();
                } catch (FileNotFoundException e) {
                    promise.reject("File not found: " + e.getMessage());
                } catch (IOException e) {
                    promise.reject("Error accessing file: " + e.getMessage());
                }
                response.putString("uri", Uri.fromFile(pictureFile).toString());
                promise.resolve(response);
                break;
            case RCT_CAMERA_CAPTURE_TARGET_TEMP:
                File tempFile = getTempMediaFile(MEDIA_TYPE_IMAGE);

                if (tempFile == null) {
                    promise.reject("Error creating media file.");
                    return;
                }

                try {
                    FileOutputStream fos = new FileOutputStream(tempFile);
                    fos.write(data);
                    fos.close();
                } catch (FileNotFoundException e) {
                    promise.reject("File not found: " + e.getMessage());
                } catch (IOException e) {
                    promise.reject("Error accessing file: " + e.getMessage());
                }
                RCTCamera reactCameraInstance = RCTCamera.getInstance();

                final int maxWidth = reactCameraInstance.getMaxWidth();
                final int maxHeight = reactCameraInstance.getMaxHeight();

                Pair<File, PictureSize> resizedResult = getResizedImage(tempFile.getAbsolutePath(),
                        width, height, maxWidth, maxHeight);

                response.putInt("width", resizedResult.second.getWidth());
                response.putInt("height", resizedResult.second.getHeight());
                response.putString("uri", Uri.fromFile(resizedResult.first).toString());
                promise.resolve(response);
                break;
        }
    }

    @ReactMethod
    public void stopCapture(final ReadableMap options, final Promise promise) {
        // TODO: implement video capture
    }

    private File getOutputMediaFile(int type) {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "RCTCameraModule");

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.e(TAG, "failed to create directory:" + mediaStorageDir.getAbsolutePath());
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_" + timeStamp + ".jpg");
        } else if (type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_" + timeStamp + ".mp4");
        } else {
            Log.e(TAG, "Unsupported media type:" + type);
            return null;
        }
        return mediaFile;
    }


    private File getTempMediaFile(int type) {
        try {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            File outputDir = _reactContext.getCacheDir();
            File outputFile;

            if (type == MEDIA_TYPE_IMAGE) {
                outputFile = File.createTempFile("IMG_" + timeStamp, ".jpg", outputDir);
            } else if (type == MEDIA_TYPE_VIDEO) {
                outputFile = File.createTempFile("VID_" + timeStamp, ".mp4", outputDir);
            } else {
                Log.e(TAG, "Unsupported media type:" + type);
                return null;
            }
            return outputFile;
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            return null;
        }
    }

    private Pair<File, PictureSize> getResizedImage(final String realPath,
                                                    final int initialWidth, final int initialHeight,
                                                    int maxWidth, int maxHeight) {
        Bitmap photo = BitmapFactory.decodeFile(realPath);

        if (photo == null) {
            return null;
        }

        Bitmap scaledPhoto;
        if (maxWidth == 0) {
            maxWidth = initialWidth;
        }
        if (maxHeight == 0) {
            maxHeight = initialHeight;
        }
        float widthRatio = (float) maxWidth / initialWidth;
        float heightRatio = (float) maxHeight / initialHeight;

        float ratio = (widthRatio < heightRatio) ? widthRatio : heightRatio;

        Matrix matrix = new Matrix();
        matrix.postScale(ratio, ratio);
        matrix.postRotate(getOrientationRotateFromExif(realPath));

        scaledPhoto = Bitmap.createBitmap(photo, 0, 0, photo.getWidth(), photo.getHeight(), matrix, true);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        scaledPhoto.compress(Bitmap.CompressFormat.JPEG, 100, bytes);

        File file = getTempMediaFile(MEDIA_TYPE_IMAGE);
        FileOutputStream fileOutputStream;
        try {
            fileOutputStream = new FileOutputStream(file);
            try {
                fileOutputStream.write(bytes.toByteArray());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        scaledPhoto.recycle();
        photo.recycle();

        return new Pair<>(file, new PictureSize(scaledPhoto.getWidth(), scaledPhoto.getHeight()));
    }

    private float getOrientationRotateFromExif(String realPath) {
        ExifInterface exif;
        try {
            exif = new ExifInterface(realPath);

            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0);

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    return 90f;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    return 180f;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    return 270f;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return 0f;
        }
        return 0f;
    }

    public static class PictureSize {
        private int width;
        private int height;

        public PictureSize(int width, int height) {
            this.width = width;
            this.height = height;
        }

        public int getWidth() {
            return width;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public int getHeight() {
            return height;
        }

        public void setHeight(int height) {
            this.height = height;
        }
    }
}