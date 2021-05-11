package com.example.task;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    ImageView captureOrPickImage;
    public static final int REQUEST_CODE_FOR_CAMERA = 100, IMAGE_REQUEST_CODE_FOR_INTENT = 200,REQUEST_CODE_FOR_STORAGE = 300;
    int imageSourceIndicator = 0; //1 for capturing image through camera, 2 for picking image from gallery.
    String imageFileName,timeStamp;
    File file_image;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        captureOrPickImage = findViewById(R.id.iv_capturedImage);

        captureOrPickImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                PopupMenu popupMenu = new PopupMenu(MainActivity.this, captureOrPickImage);
                popupMenu.getMenuInflater().inflate(R.menu.image_upload_options, popupMenu.getMenu());

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
                            case R.id.camera:
                                imageSourceIndicator = 1;
                                checkDynamicPermissionsForCamera();
                                break;
                            case R.id.gallery:
                                imageSourceIndicator = 2;
                                checkDynamicInternalStoragePermissions();
                                break;
                        }
                        return true;
                    }
                });
                popupMenu.show();

            }
        });
    }

    public void checkDynamicPermissionsForCamera() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CODE_FOR_CAMERA);
                }
            } else {
                checkDynamicInternalStoragePermissions();
            }
        } else {
            checkDynamicInternalStoragePermissions();
        }
    }

    public void checkDynamicInternalStoragePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        || !ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_FOR_STORAGE);
                }
            } else {
                LaunchIntentForPic();
            }
        } else {
            LaunchIntentForPic();
        }
    }

    public void LaunchIntentForPic() {
        if (imageSourceIndicator == 1) {

          Intent  intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(intent, IMAGE_REQUEST_CODE_FOR_INTENT);

        } else if (imageSourceIndicator == 2) {
            Intent intent = new Intent(Intent.ACTION_PICK,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, IMAGE_REQUEST_CODE_FOR_INTENT);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode) {

            case IMAGE_REQUEST_CODE_FOR_INTENT:
                if (resultCode == Activity.RESULT_OK) {
                    try {
                        if (imageSourceIndicator == 1) {

                            Bitmap bitmap = (Bitmap) data.getExtras().get("data");

                            Uri selectedImage =  getImageUri(MainActivity.this,bitmap);

                            InputStream imageStream = null;
                            try {
                                imageStream = getContentResolver().openInputStream(
                                        selectedImage);
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            }

                            Bitmap bmp = BitmapFactory.decodeStream(imageStream);

                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                            bmp.compress(Bitmap.CompressFormat.PNG, 60, stream);
                            byte[] byteArray = stream.toByteArray();
                            try {
                                stream.close();
                                stream = null;
                            } catch (IOException e) {

                                e.printStackTrace();
                            }

                            saveImageInExternalStorage(bmp);
                            putImageInViewAndAskForUpload(bmp);

                        } else if (imageSourceIndicator == 2) {
                            Uri pictureUri = data.getData();
                            Bitmap bmp= BitmapFactory.decodeStream(getContentResolver().openInputStream(pictureUri));
                            int noOfBytesBeforeCompress = bmp.getByteCount();
                            int nh = (int) ( bmp.getHeight() * (512.0 / bmp.getWidth()) );
                            Bitmap scaled = Bitmap.createScaledBitmap(bmp, 512, nh, true);
                            int noOfBytesAfterCompress = scaled.getByteCount();

                            putImageInViewAndAskForUpload(scaled);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        Snackbar.make(findViewById(android.R.id.content)
                                , "" + e
                                , Snackbar.LENGTH_SHORT).show();
                    }
                } else {
                    if (file_image != null && !imageFileName.isEmpty()) {
                        if (file_image.isFile()) {
                            file_image.delete();
                        }
                        imageSourceIndicator = 0;
                        file_image = null;
                        imageFileName = "";
                    }
                    Toast.makeText(this, "Sorry! Failed to get your picture", Toast.LENGTH_SHORT).show();
                }
                break;

            default:
                throw new IllegalStateException("Unexpected value: " + requestCode);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void putImageInViewAndAskForUpload(Bitmap bitmap) {
        if (bitmap != null) {
            if (imageSourceIndicator == 1 || imageSourceIndicator == 2) {

                captureOrPickImage.setImageBitmap(bitmap);

            } else {
                Snackbar.make(findViewById(android.R.id.content)
                        , "Sorry! unable to convert your picture!!"
                        , Snackbar.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {

            case REQUEST_CODE_FOR_CAMERA:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Snackbar.make(findViewById(android.R.id.content)
                            , "Thankyou! We got a chance to provide better service."
                            , Snackbar.LENGTH_SHORT).show();
                    checkDynamicInternalStoragePermissions();
                } else {
                    Snackbar.make(findViewById(android.R.id.content)
                            , "Need camera permission to take pictures!!"
                            , Snackbar.LENGTH_SHORT).show();
                }
                break;

            case REQUEST_CODE_FOR_STORAGE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Snackbar.make(findViewById(android.R.id.content)
                            , "Thankyou! We got a chance to provide better service."
                            , Snackbar.LENGTH_SHORT).show();
                    LaunchIntentForPic();
                } else {
                    Snackbar.make(findViewById(android.R.id.content)
                            , "Need storage permission to store pictures!!"
                            , Snackbar.LENGTH_SHORT).show();
                }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    private void saveImageInExternalStorage(Bitmap finalBitmap) {

        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/CapturedImages");
        myDir.mkdirs();
        String fname = "Image-"+ timeStamp +".jpg";
        File file = new File (myDir, fname);
        if (file.exists ()) file.delete ();
        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
            Toast.makeText(MainActivity.this, "Image Saved Successfully", Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }


}