package com.example.daviswu.debttracker;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.transition.ChangeImageTransform;
import android.transition.Explode;
import android.transition.Fade;
import android.transition.Slide;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;


public class EditViewActivity extends Activity {

    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    private ImageView image;
    public Item card;
    private EditText nameLabel;
    private TextView statusLabel;
    private EditText itemLabel;
    private LinearLayout frame;
    private EditText phoneLabel;
    private EditText emailLabel;
    private FrameLayout captureButton;
    private boolean captured;
    private String imageFilePath;
    private String imageFilePathOld;
    private Preview mPreview;
    private FrameLayout preview;
    private TextView instructions;
    private Camera mCamera;
    private ScrollView cameraContainer;
    private boolean cameraStarted = false;
    private ImageView emailButton;
    private ImageView phoneButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_view);
        setTitle("Edit");

        Bundle b = getIntent().getExtras();
        if (b != null) {
            card = (Item) getIntent().getExtras().getSerializable("awesomeitem");
        }

        // transition settings
        getWindow().setExitTransition(new Explode());
        getWindow().setEnterTransition(new Explode());
        getWindow().setAllowEnterTransitionOverlap(false);
        getWindow().setAllowReturnTransitionOverlap(false);

        captured = false;
        imageFilePath = card.imgPath.getAbsolutePath();
        imageFilePathOld = imageFilePath;

        image = (ImageView) findViewById(R.id.image_view);
        itemLabel = (EditText) findViewById(R.id.item_label);
        nameLabel = (EditText) findViewById(R.id.name_label);
        phoneLabel = (EditText) findViewById(R.id.phone_label);
        emailLabel = (EditText) findViewById(R.id.email_label);
        statusLabel = (TextView) findViewById(R.id.status_label);
        frame = (LinearLayout) findViewById(R.id.frame);
        preview = (FrameLayout) findViewById(R.id.camera_preview_edit);
        cameraContainer = (ScrollView) findViewById(R.id.camera_container_edit);
        instructions = (TextView) findViewById(R.id.capture_instructions_edit);
        emailButton = (ImageView) findViewById(R.id.email_button);
        phoneButton = (ImageView) findViewById(R.id.call_button);

        image.setImageBitmap(rotateBitmap(BitmapFactory.decodeFile(card.imgPath.getAbsolutePath()),90));
        itemLabel.setText(card.itemOwed);
        nameLabel.setText(card.name);
        statusLabel.setText(card.status);
        emailLabel.setText(card.email);
        phoneLabel.setText(card.phone);

        frame.requestFocus();

        // Sets up camera capture button
        captureButton = (FrameLayout) findViewById(R.id.camera_preview_edit);
        captureButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 startCapture();
             }
        });

        cameraContainer.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                cameraContainer.post(new Runnable() {
                    public void run() {
                        cameraContainer.scrollTo(0, cameraContainer.getHeight());
                    }
                });
            }
        });

        emailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                        "mailto",card.email, null));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "I want my "+card.itemOwed+" back.");
                startActivity(Intent.createChooser(emailIntent, "Send email..."));
            }
        });

        phoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneNumber = card.phone;
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse("tel:"+phoneNumber));
                startActivity(callIntent);
            }
        });

        captureButton.bringToFront();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_edit_view, menu);
        return true;
    }

    private void noAction() {
        if (!captured || cameraStarted) {
            releaseCamera();
            preview.removeAllViews();
        }

        image.bringToFront();

        if (imageFilePath != imageFilePathOld) {
            File file = new File(imageFilePath);
            file.delete();
        }
    }

    @Override
    public void onBackPressed() {
        noAction();
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.cancel) {
            noAction();
            super.onBackPressed();
            return true;
        }
        else if (id == R.id.done) {
            card.name = nameLabel.getText().toString();
            card.itemOwed = itemLabel.getText().toString();
            card.email = emailLabel.getText().toString();
            card.phone = phoneLabel.getText().toString();
            card.imgPath = new File(imageFilePath);

            if (!captured || cameraStarted) {
                releaseCamera();
                preview.removeAllViews();
            }

            if (imageFilePath != imageFilePathOld) {
                File file = new File(imageFilePathOld);
                file.delete();
                image.setImageBitmap(rotateBitmap(BitmapFactory.decodeFile(imageFilePath),90));
            }

            image.bringToFront();

            Intent intent = new Intent();
            intent.putExtra("Obj1", card);
            setResult(Activity.RESULT_OK, intent);

            super.onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public static Bitmap rotateBitmap(Bitmap source, float angle){
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    private void releaseCamera(){
        if (mCamera != null){
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
    }

    public void startCamera() {
        mCamera = getCameraInstance();
        mPreview = new Preview(this, mCamera);
        preview.addView(mPreview);
    }

    /** Create a File for saving an image or video */
    private File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        //File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "Debt");
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory().toString()+File.separator+"DebtTracker");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".jpg");
            imageFilePath = mediaStorageDir.getPath() + File.separator + "IMG_"+ timeStamp + ".jpg";
        } else if(type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_"+ timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }

    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
            if (pictureFile == null){
                return;
            }

            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
            } catch (FileNotFoundException e) {
                Log.e("", "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.e("", "Error accessing file: " + e.getMessage());
            }

            Log.w("Tag", imageFilePath);
        }
    };

    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
            Method rotateMethod;
            rotateMethod = android.hardware.Camera.class.getMethod("setDisplayOrientation", int.class);
            rotateMethod.invoke(c, 90);
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseCamera();              // release the camera immediately on pause event
    }

    private void startCapture() {
        // get an image from the camera
        if (!cameraStarted) {
            Log.w("tag", "yay");
            instructions.setText("Tap for picture");
            startCamera();
            cameraStarted = true;
        }
        else {
            if (!captured) {
                captured = true;
                instructions.setText("Tap to reset");
                mCamera.takePicture(null, null, mPicture);
            }
            else {
                File file = new File(imageFilePath);
                file.delete();
                releaseCamera();
                instructions.setText("Tap for picture");
                preview.removeAllViews();
                startCamera();
                captured = false;
            }
        }
    }
}
