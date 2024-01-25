package com.example.qrbarcodescanner;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.Instrumentation;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.lang.reflect.Type;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private MaterialButton cameraBtn;
    private MaterialButton galleryBtn;
    private ImageView imageIv;

    private MaterialButton scanBtn;

    private TextView resultTv;

    private static final int CAMERA_REQUEST_CODE = 100;

    private static final int STORAGE_REQUEST_CODE = 101;

    private String[] cameraPermission;
    private String[] storagePermission;

    private Uri imageUri = null;

    private BarcodeScannerOptions barcodeScannerOptions;
    private BarcodeScanner barcodeScanner;

    private static final String TAG = "MAIN_TAG";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cameraBtn = findViewById(R.id.cameraBtn);
        galleryBtn = findViewById(R.id.galleryBtn);
        imageIv = findViewById(R.id.imageIv);
        scanBtn = findViewById(R.id.scanBtn);
        resultTv = findViewById(R.id.resultTv);

        cameraPermission = new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePermission = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};

        barcodeScannerOptions = new BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
                .build();

        barcodeScanner = BarcodeScanning.getClient(barcodeScannerOptions);



        cameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkCameraPermission()){
                    pickImageCamera();
                }
                else{
                    requestCameraPermission();
                }

            }
        });

        galleryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(checkStoragePermission()){
                    pickImageGallery();
                }
                else{
                    requestStoragePermission();
                }

            }
        });

        scanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(imageUri == null){
                    Toast.makeText(MainActivity.this,"Pick image first...",Toast.LENGTH_SHORT).show();
                }
                else{
                    detectResultFromImage();
                }

            }
        });






    }

    private void detectResultFromImage() {
        try{
            InputImage inputImage = InputImage.fromFilePath(this,imageUri);

            Task<List<Barcode>> barcodeResult = barcodeScanner.process(inputImage)
                    .addOnSuccessListener(new OnSuccessListener<List<Barcode>>() {
                        @Override
                        public void onSuccess(List<Barcode> barcodes) {
                            extractBarCodeQRCodeInfo(barcodes);

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(MainActivity.this,"Failed Scanning due to "+e.getMessage(),Toast.LENGTH_SHORT).show();

                        }
                    });

        }
        catch (Exception e){
            Toast.makeText(this,"Failed due to"+e.getMessage(),Toast.LENGTH_SHORT).show();

        }
    }

    private void extractBarCodeQRCodeInfo(List<Barcode> barcodes) {
        for(Barcode barcode : barcodes) {
            Rect bounds = barcode.getBoundingBox();
            Point[] corners = barcode.getCornerPoints();


            String rawValue = barcode.getRawValue();
            Log.d(TAG,"extractBarcodeQrCodeInfo: rawValue: "+ rawValue);


            int valueType = barcode.getValueType();
            switch (valueType) {
                case Barcode.TYPE_WIFI: {
                    Barcode.WiFi typeWifi = barcode.getWifi();

                    String ssid = "" + typeWifi.getSsid();
                    String password = "" + typeWifi.getPassword();
                    String encryptionType = "" + typeWifi.getEncryptionType();
                    Log.d(TAG, "extractBarCodeQRCodeInfo: TYPE_WIFI");
                    Log.d(TAG, "extractBarCodeQRCodeInfo: ssid" + ssid);
                    Log.d(TAG, "extractBarCodeQRCodeInfo: password" + password);
                    Log.d(TAG, "extractBarcodeQRCodeInfo : encryption" + encryptionType);

                    resultTv.setText("TYPE: TYPE_WIFI\nssid:" + ssid + "\npassword:  " + password + "\nencryptionType: " + encryptionType + "\nraw value:" + rawValue);
                }
                break;
                case Barcode.TYPE_URL: {
                    Barcode.UrlBookmark typeURL = barcode.getUrl();
                    String title = "" + typeURL.getTitle();
                    String url = "" + typeURL.getUrl();

                    Log.d(TAG, "extractBarCodeQRCodeInfo: TYPE_URL");
                    Log.d(TAG, "extractBarCodeQRCodeInfo: title:" + title);
                    Log.d(TAG, "extractBarCodeQRCodeInfo: url:" + url);

                    resultTv.setText("Type:TYPE_URL\ntitle:" + title + "\nurl:" + url + "\nraw value:" + rawValue);
                }
                break;
                case Barcode.TYPE_EMAIL: {
                    Barcode.Email typeEmail = barcode.getEmail();
                    String address = "" + typeEmail.getAddress();
                    String body = "" + typeEmail.getBody();
                    String subject = "" + typeEmail.getSubject();
                    Log.d(TAG, "extractBarCodeInfo: TYPE_EMAIL");
                    Log.d(TAG, "extractBarCodeInfo: address" + address);
                    Log.d(TAG, "extractBarCodeInfo: body" + body);
                    Log.d(TAG, "extractBarCodeInfo: subject" + subject);

                    resultTv.setText("TYPE: TYPE_EMAIL \naddress:" + address + "\nbody:  " + body + "\nsubject: " + subject + "\nraw value:" + rawValue);
                }
                break;
                case Barcode.TYPE_CONTACT_INFO: {
                    Barcode.ContactInfo typeContact = barcode.getContactInfo();

                    String title = "" + typeContact.getTitle();
                    String organizer = "" + typeContact.getOrganization();
                    String name = "" + typeContact.getName().getFirst() + " " + typeContact.getName().getLast();
                    String phone = "" + typeContact.getPhones().get(0).getNumber();

                    Log.d(TAG, "extractBarCodeInfo: TYPE_CONTACT_INFO");
                    Log.d(TAG, "extractBarCodeInfo: title" + title);
                    Log.d(TAG, "extractBarCodeInfo: organizer" + organizer);
                    Log.d(TAG, "extractBarCodeInfo: name" + name);
                    Log.d(TAG, "extractBarcodeInfo: phone" + phone);

                    resultTv.setText("TYPE: TYPE_CONTACT_INFO \ntitle:" + title + "\norganizer:  " + organizer + "\nname: " + name + "\nphone:" + phone + "\nraw value: +rawValue");


                }
                break;
                default: {
                    resultTv.setText("raw value" +rawValue);
                }
            }
        }
    }

    private void pickImageGallery(){
        Intent intent = new Intent(Intent.ACTION_PICK);

        intent.setType("image/*");
        galleryActivityResultLauncher.launch(intent);
    }

    private final ActivityResultLauncher<Intent> galleryActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK){
                        Intent data = result.getData();
                        imageUri = data.getData();
                        Log.d(TAG,"onActivityResult: imageUri : "+imageUri);
                        imageIv.setImageURI(imageUri);
                    }
                    else{
                        Toast.makeText(MainActivity.this,"Cancelled",Toast.LENGTH_SHORT).show();
                    }

                }
            }
    );
    private void pickImageCamera(){

        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.TITLE,"Sample Title");
        contentValues.put(MediaStore.Images.Media.DESCRIPTION,"Sample Image Description");

        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,contentValues);

        Intent intent = new Intent(MediaStore.ACTION_PICK_IMAGES);
        intent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);

    }

    private final ActivityResultLauncher<Intent> cameraActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {

                    if (result.getResultCode()==Activity.RESULT_OK){
                        Intent data = result.getData();
                        Log.d(TAG,"onActivityResult : imageUri"+ imageUri);
                        imageIv.setImageURI(imageUri);
                    }
                    else{
                        Toast.makeText(MainActivity.this,"Cancelled...",Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    private boolean checkStoragePermission(){
        boolean result = ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;

        return result;
    }
    private void requestStoragePermission(){
        ActivityCompat.requestPermissions(this,storagePermission,STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermission(){

        boolean resultCamera = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)

                == PackageManager.PERMISSION_GRANTED;
        boolean resultStorage = ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE)
                ==PackageManager.PERMISSION_GRANTED;

        return resultCamera && resultStorage;
    }
    private void requestCameraPermission(){
        ActivityCompat.requestPermissions(this,cameraPermission,CAMERA_REQUEST_CODE);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode){
            case CAMERA_REQUEST_CODE:{
                if (grantResults.length>0){
                    boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if(cameraAccepted&& storageAccepted){
                        pickImageCamera();
                    }
                    else{
                        Toast.makeText(this,"Camera & Storage Permission are required...",Toast.LENGTH_SHORT).show();
                    }
                }

            }
            break;
            case STORAGE_REQUEST_CODE:{
                if (grantResults.length>0){
                    boolean storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;

                    if(storageAccepted){
                        pickImageGallery();
                    }
                    else {
                        Toast.makeText(this,"Storage Permisson is required...",Toast.LENGTH_SHORT).show();
                    }
                }

            }
            break;
        }
    }

}