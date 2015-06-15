package com.applilandia.takepicture;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

//http://hmkcode.com/android-display-selected-image-and-its-real-path/
//Draw bitmap rounded by circle:
//          http://stackoverflow.com/questions/17040475/adding-a-round-frame-circle-on-rounded-bitmap

// https://teamtreehouse.com/forum/how-to-rotate-images-to-the-correct-orientation-portrait-by-editing-the-exif-data-once-photo-has-been-taken
public class MainActivity extends AppCompatActivity implements View.OnTouchListener {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_LOAD_IMAGE = 2;

    private static final String PHOTO_FILE_NAME = "photo_sample";

    private AppCompatButton mButtonTakePicture;
    private AppCompatButton mButtonSelectPicture;
    private AppCompatButton mButtonSelectAppPicture;
    private ImageView mImageViewFullPicture;
    private ImageView mImageViewPicture;
    private Uri mFileUri;
    private String mCurrentPhotoPath;
    /**
     * Variables for zooming
     */
    private static final int NONE = 0;
    private static final int DRAG = 1;
    private static final int ZOOM = 2;

    private static final float MINIMAL_DISTANCE = 10f;

    private float[] mLastMotionEvent = null;
    private Matrix mSavedMatrix = new Matrix();
    private Matrix mMatrixZoom = new Matrix();
    private float mLastDistance = 1f;
    private PointF mStartPoint = new PointF();
    private PointF mMidPoint = new PointF();
    private int mMode = NONE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        inflateViews();
        createButtonHandlers();
        createImageViewHandlers();
    }

    /**
     * Inflate activity views
     */
    private void inflateViews() {
        mButtonTakePicture = (AppCompatButton) findViewById(R.id.buttonTakePicture);
        mButtonSelectPicture = (AppCompatButton) findViewById(R.id.buttonSelectPicture);
        mButtonSelectAppPicture = (AppCompatButton) findViewById(R.id.buttonSelectLocalPicture);
        mImageViewPicture = (ImageView) findViewById(R.id.imagePicture);
        mImageViewFullPicture = (ImageView) findViewById(R.id.imageFullPicture);
    }

    private void createImageViewHandlers() {
        mImageViewFullPicture.setOnTouchListener(this);
    }

    /**
     * Create handlers for buttons on the activity
     */
    private void createButtonHandlers() {
        mButtonTakePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });

        mButtonSelectPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });

        mButtonSelectAppPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showImage(PHOTO_FILE_NAME + ".jpg", 1f);
            }
        });
    }

    /**
     * Choose an image to import from gallery of app that expose the action ACTION_GET_CONTENT
     * selected by user
     */
    private void chooseImage() {
        // Create intent to Open Image applications like Gallery, Google Photos
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        if (getIntent().resolveActivity(getPackageManager()) != null) {
            startActivityForResult(galleryIntent, REQUEST_LOAD_IMAGE);
        }
    }

    /**
     * Raise intent to take a photo
     */
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createTempImageFile(PHOTO_FILE_NAME);
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                mFileUri = Uri.fromFile(photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        mFileUri);
            }
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    /**
     * Create a temporary file to store the photo taken from the camera app
     *
     * @param fileName temporary file name
     * @return File object
     * @throws IOException
     */
    private File createTempImageFile(String fileName) throws IOException {
        //Gallery directory:
        //          Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        File storageDir = this.getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        File tempFile = File.createTempFile(
                fileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        //Hold the path of the file for the future processing
        mCurrentPhotoPath = tempFile.getAbsolutePath();
        return tempFile;
    }

    /**
     * Get the current orientation of a image file
     *
     * @param filePathName file name including the absolute path
     * @return Orientation value
     */
    private String getImageOrientation(String filePathName) {
        File file = new File(filePathName);
        return getImageOrientation(file);
    }

    /**
     * Get the current orientation of a image file
     *
     * @param file file object with the image
     * @return Orientation value
     */
    private String getImageOrientation(File file) {
        try {
            ExifInterface exifInterface = new ExifInterface(file.getAbsolutePath());
            return exifInterface.getAttribute(ExifInterface.TAG_ORIENTATION);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Save one orientation value into the metadata image file
     *
     * @param file  File object with the image
     * @param value Orientation value
     */
    private void setImageOrientation(File file, String value) {
        try {
            ExifInterface exifInterface = new ExifInterface(file.getAbsolutePath());
            exifInterface.setAttribute(ExifInterface.TAG_ORIENTATION, value);
            exifInterface.saveAttributes();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Save a bitmap scaled to the half size into a file in the pictures external directory
     *
     * @param bitmap   Source bitmap
     * @param fileName file name where to save bitmap
     */
    private void importPicture(Bitmap bitmap, String fileName) {
        Bitmap resizedBitmap = getScaledBitmap(bitmap, 0.5f, ExifInterface.ORIENTATION_UNDEFINED);
        File storageDirectory = this.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File file = new File(storageDirectory, fileName);
        try {
            FileOutputStream outputStream = new FileOutputStream(file);
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.flush();
            outputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Create a bitmap from a bitmap source with a scale factor and a rotation angle
     *
     * @param source        bitmap source
     * @param scale         scale factor
     * @param rotationAngle angle: 0, 90, 180, 270
     * @return Bitmap resized
     */
    private Bitmap getScaledBitmap(Bitmap source, float scale, int rotationAngle) {
        //Calculate and set the new scale
        int width = source.getWidth();
        int height = source.getHeight();

        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);
        if (rotationAngle != ExifInterface.ORIENTATION_UNDEFINED) {
            matrix.postRotate(rotationAngle);
        }
        Bitmap scaledBitmap = Bitmap.createBitmap(source, 0, 0, width, height, matrix, true);
        return scaledBitmap;
    }

    private void savePicture(String fileName) {
        //Open the temp file created when the photo has been taken
        File tempFile = new File(mCurrentPhotoPath);
        String orientationValue = getImageOrientation(tempFile);
        Bitmap bitmap = BitmapFactory.decodeFile(tempFile.getPath());

        //Create a new bitmap resized to the new dimensions
        Bitmap resizedBitmap = getScaledBitmap(bitmap, 0.5f, ExifInterface.ORIENTATION_UNDEFINED);

        FileOutputStream outputStream = null;
        try {
            File file = new File(this.getExternalFilesDir(Environment.DIRECTORY_PICTURES), fileName);
            outputStream = new FileOutputStream(file);
            resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.flush();
            outputStream.close();
            //Save the same orientation of the taken photo in the compressed file
            setImageOrientation(file, orientationValue);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //Remove the temporary file created when the photo was taken
        tempFile.delete();
    }

    /**
     * Show an image in the ImageView scaled as stated by parameter
     *
     * @param fileName file name containing the image
     * @param scale    scale factor
     */
    private void showImage(String fileName, float scale) {
        File directory = this.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        String pathFileName = directory.getPath() + "/" + fileName;
        Bitmap bitmap = BitmapFactory.decodeFile(pathFileName);

        String orientation = getImageOrientation(pathFileName);
        int orientationValue = orientation != null ? Integer.parseInt(orientation) : ExifInterface.ORIENTATION_NORMAL;
        int rotationAngle = 0;
        if (orientationValue == ExifInterface.ORIENTATION_ROTATE_90) rotationAngle = 90;
        if (orientationValue == ExifInterface.ORIENTATION_ROTATE_180) rotationAngle = 180;
        if (orientationValue == ExifInterface.ORIENTATION_ROTATE_270) rotationAngle = 270;

        Bitmap resizedBitmap = getScaledBitmap(bitmap, scale, rotationAngle);

        mImageViewPicture.setScaleType(ImageView.ScaleType.FIT_CENTER);
        mImageViewPicture.setImageBitmap(getRoundedBitmap(resizedBitmap));

        mImageViewFullPicture.setScaleType(ImageView.ScaleType.MATRIX);
        mImageViewFullPicture.setImageBitmap(resizedBitmap);
    }

    /**
     * Round a bitmap
     *
     * @param source bitmap to be rounded
     * @return bitmap rounded
     */
    private Bitmap getRoundedBitmap(Bitmap source) {
        int w = source.getWidth();
        int h = source.getHeight();

        int radius = Math.min(h / 2, w / 2);
        Bitmap output = Bitmap.createBitmap(w + 8, h + 8, Bitmap.Config.ARGB_8888);

        Paint p = new Paint();
        p.setAntiAlias(true);

        Canvas c = new Canvas(output);
        c.drawARGB(0, 0, 0, 0);
        p.setStyle(Paint.Style.FILL);

        c.drawCircle((w / 2) + 4, (h / 2) + 4, radius, p);

        p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

        c.drawBitmap(source, 4, 4, p);
        p.setXfermode(null);
        p.setStyle(Paint.Style.STROKE);
        p.setColor(Color.WHITE);
        p.setStrokeWidth(3);
        c.drawCircle((w / 2) + 4, (h / 2) + 4, radius, p);

        return output;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            savePicture(PHOTO_FILE_NAME + ".jpg");
            showImage(PHOTO_FILE_NAME + ".jpg", 1f);
        }
        if (requestCode == REQUEST_LOAD_IMAGE && resultCode == RESULT_OK) {
            if (data != null) {
                Uri imageUri = data.getData();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                    importPicture(bitmap, PHOTO_FILE_NAME + ".jpg");
                    showImage(PHOTO_FILE_NAME + ".jpg", 1f);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                Log.v(LOG_TAG, "Not chosen");
            }
        }
    }

    /**
     * Determine the space between the two fingers
     *
     * @param event
     * @return space in float format
     */
    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    /**
     * Calculate the mid point of the first two fingers
     *
     * @param point Output param.  Coordinates for a point.
     * @param event
     */
    private void getMidPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                mSavedMatrix.set(mMatrixZoom);
                mStartPoint.set(event.getX(), event.getY());
                mMode = DRAG;
                mLastMotionEvent = null;
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                mLastDistance = spacing(event);
                if (mLastDistance > MINIMAL_DISTANCE) {
                    mSavedMatrix.set(mMatrixZoom);
                    getMidPoint(mMidPoint, event);
                    mMode = ZOOM;
                }
                mLastMotionEvent = new float[4];
                mLastMotionEvent[0] = event.getX(0);
                mLastMotionEvent[1] = event.getX(1);
                mLastMotionEvent[2] = event.getY(0);
                mLastMotionEvent[3] = event.getY(1);
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                mMode = NONE;
                mLastMotionEvent = null;
                break;

            case MotionEvent.ACTION_MOVE:
                if (mMode == DRAG) {
                    mMatrixZoom.set(mSavedMatrix);
                    float dx = event.getX() - mStartPoint.x;
                    float dy = event.getY() - mStartPoint.y;
                    mMatrixZoom.postTranslate(dx, dy);
                } else if (mMode == ZOOM) {
                    float newDist = spacing(event);
                    if (newDist > MINIMAL_DISTANCE) {
                        mMatrixZoom.set(mSavedMatrix);
                        float scale = newDist / mLastDistance;
                        mMatrixZoom.postScale(scale, scale, mMidPoint.x, mMidPoint.y);
                    }
                }
                break;
        }
        mImageViewFullPicture.setImageMatrix(mMatrixZoom);
        return true;
    }
}
