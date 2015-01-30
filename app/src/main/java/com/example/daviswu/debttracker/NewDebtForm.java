package com.example.daviswu.debttracker;

import android.app.Activity;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Environment;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.transition.Explode;
import android.transition.Fade;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;


public class NewDebtForm extends Activity {

    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;

    private  EditText itemInput;
    private EditText nameInput;
    private EditText emailInput;
    private EditText phoneInput;
    private TextView instructions;
    private Spinner statusInput;
    private Button selectContactButton;
    private Item myItem;
    private Camera mCamera;
    private Preview mPreview;
    private FrameLayout captureButton;
    private String imageFilePath;
    private boolean captured;
    private ScrollView cameraContainer;
    private FrameLayout preview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_debt_form);
        setTitle("New Debt");
        imageFilePath = "";
        captured = false;

        instructions = (TextView) findViewById(R.id.capture_instructions);
        itemInput = (EditText) findViewById(R.id.item_input);
        nameInput = (EditText) findViewById(R.id.name_input);
        emailInput = (EditText) findViewById(R.id.email_input);
        phoneInput = (EditText) findViewById(R.id.phone_input);
        statusInput = (Spinner) findViewById(R.id.status_spinner);
        selectContactButton = (Button) findViewById(R.id.select_contact_button);
        preview = (FrameLayout) findViewById(R.id.camera_preview);
        cameraContainer = (ScrollView) findViewById(R.id.camera_container);

        cameraContainer.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                cameraContainer.post(new Runnable() {
                    public void run() {
                        cameraContainer.scrollTo(0, cameraContainer.getHeight()*3/2);
                    }
                });
            }
        });

        // Sets up camera capture button
        captureButton = (FrameLayout) findViewById(R.id.camera_preview);
        captureButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // get an image from the camera
                        if (!captured) {
                            captured = true;
                            instructions.setText("Tap to reset");
                            mCamera.takePicture(null, null, mPicture);
                        }
                        else {
                            File file = new File(imageFilePath);
                            boolean fileDeleted = file.delete();
                            releaseCamera();
                            instructions.setText("Tap for picture");
                            preview.removeAllViews();
                            startCamera();
                            captured = false;
                        }
                    }
                }
        );

        // Set up camera preview
        startCamera();

        // Opens contact selector
        selectContactButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent contactPickerIntent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                startActivityForResult(contactPickerIntent, 1001);
            }});

        Bundle b = getIntent().getExtras();

        if (b != null) {
            myItem = (Item) getIntent().getExtras().getSerializable("myitem");
        }
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
    public void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);

        switch (reqCode) {
            case (1001) :
                if (resultCode == Activity.RESULT_OK) {
                    getContactInfo(data);
                }
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        itemInput.requestFocus();

        itemInput.postDelayed(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                //InputMethodManager keyboard = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                //keyboard.showSoftInput(itemInput, 0);
            }
        },200);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_new_debt_form, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.done_button) {
            myItem.name = nameInput.getText().toString();
            myItem.itemOwed = itemInput.getText().toString();
            myItem.status = statusInput.getSelectedItem().toString();
            myItem.email = emailInput.getText().toString();
            myItem.phone = phoneInput.getText().toString();

            // Sets default image if no picture was taken
            if (imageFilePath == "") {
                myItem.imgPath = setDefaultImage();
            }
            else {
                myItem.imgPath = new File(imageFilePath);
            }

            Intent intent = new Intent();
            intent.putExtra("Obj", myItem);
            setResult(Activity.RESULT_OK, intent);
            finish();
            super.onBackPressed();
            return true;
        }
        else if (id == R.id.cancel_button) {
            super.onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void getContactInfo(Intent intent) {
        Uri contactData = intent.getData();
        Cursor c =  getContentResolver().query(contactData, null, null, null, null);
        if (c.moveToFirst()) {
            String contactId = c.getString(c.getColumnIndex(ContactsContract.Contacts._ID));

            // Get name
            String name = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
            nameInput.setText(name);

            // Get phone number
            String phone = "";
            String hasPhone = c.getString(c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
            if (hasPhone.equalsIgnoreCase("1"))
                hasPhone = "true";
            else
                hasPhone = "false" ;
            if (Boolean.parseBoolean(hasPhone)) {
                Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = "+ contactId,null, null);
                while (phones.moveToNext()) {
                    phone = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                }
                phones.close();
            }
            phoneInput.setText(phone);

            // Get Email Address
            String email = "";
            Cursor emails = getContentResolver().query(ContactsContract.CommonDataKinds.Email.CONTENT_URI,null,ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = " + contactId,null, null);
            while (emails.moveToNext())
            {
                email = emails.getString(emails.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
            }
            emails.close();
            emailInput.setText(email);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseCamera();              // release the camera immediately on pause event
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

    private File setDefaultImage() {
        Bitmap bmTemp = BitmapFactory.decodeResource(getResources(), R.drawable.money);
        Bitmap bm = Bitmap.createScaledBitmap(bmTemp, 1280, 720, false);
        File file = getOutputMediaFile(MEDIA_TYPE_IMAGE);
        try {
            FileOutputStream outStream = new FileOutputStream(file);
            bm.compress(Bitmap.CompressFormat.JPEG, 30, outStream);
            outStream.flush();
            outStream.close();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        return file;
    }
}