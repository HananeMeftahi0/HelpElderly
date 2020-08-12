package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private static final int IMAGE_CAPTURE_CODE = 1001;
    private static final int PERMISSION_CODE =1000 ;
    private static final int PERMISSION_REQUEST =0 ;
    private static final int RESULT_LOAD_IMAGE = 0;

    ImageView imageview;
    Button button2,button3,button4;
    TextToSpeech textToSpeech;
    StringBuilder text;
    Uri image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        imageview=findViewById(R.id.imageView);
        button2=findViewById(R.id.button2);
        button3=findViewById(R.id.button3);
        button4=findViewById(R.id.button4);


        textToSpeech=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
              if(i!=TextToSpeech.ERROR){
                  textToSpeech.setLanguage(Locale.ENGLISH);
              }
            }
        });

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                text= detect();
                String speak= text.toString();
                speak=speak.replaceAll("[^a-zA-Z0-9]", " ");
                Toast.makeText(getApplicationContext(),speak,Toast.LENGTH_SHORT).show();

                textToSpeech.setSpeechRate((float) 0.09);
                textToSpeech.speak(speak,TextToSpeech.getMaxSpeechInputLength(),null);
            }
        });
        button4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)== PackageManager.PERMISSION_DENIED){
                    String[] permissions= { Manifest.permission.READ_EXTERNAL_STORAGE};
                    requestPermissions(permissions,PERMISSION_REQUEST);
                }
            Intent i=new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(i,RESULT_LOAD_IMAGE);
            }
        });

        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
                    if(checkSelfPermission(Manifest.permission.CAMERA)== PackageManager.PERMISSION_DENIED||checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_DENIED){
                        String[] permission= {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
                       requestPermissions(permission,PERMISSION_CODE);


                    }else{
                        openCamera();
                    }

                }else{
                    openCamera();
                }
            }
        });


    }
    public StringBuilder detect(){
        TextRecognizer textRecognizer=new TextRecognizer.Builder(MainActivity.this).build();
        Bitmap bitmap=((BitmapDrawable)imageview.getDrawable()).getBitmap();
        Frame frame=new Frame.Builder().setBitmap(bitmap).build();
        SparseArray<TextBlock> sparseArray= textRecognizer.detect(frame);
        StringBuilder stringBuilder=new StringBuilder();
        String string=null;
        for (int i=0;i<sparseArray.size();i++){
            TextBlock text=sparseArray.get(i);
            string=text.getValue();
            stringBuilder.append(string);
            stringBuilder.append("   ");

        }
        return stringBuilder;

}
public void openCamera(){
    ContentValues contentValues=new ContentValues();
    contentValues.put(MediaStore.Images.Media.TITLE,"New picture...");
    contentValues.put(MediaStore.Images.Media.DESCRIPTION,"From the camera...");
    image=getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,contentValues);
    Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    intent.putExtra(MediaStore.EXTRA_OUTPUT,image);
    startActivityForResult(intent,IMAGE_CAPTURE_CODE );

}




    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
        case PERMISSION_CODE:{
        if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
            openCamera();
        }else{
            Toast.makeText(this,"Permission denied...",Toast.LENGTH_SHORT).show();
    }
}
            case PERMISSION_REQUEST:{
                if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(this,"Permission granted...",Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(this,"Permission denied...",Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
    }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            imageview.setImageURI(image);
        }
        switch (requestCode)  {
            case RESULT_LOAD_IMAGE:{
                if (resultCode == RESULT_OK) {
                    Uri selectedImage=data.getData();
                    String[] filePatchCol= {MediaStore.Images.Media.DATA};
                    Cursor cursor=getContentResolver().query(selectedImage,filePatchCol,null,null,null);
                    cursor.moveToFirst();
                    int colIndex=cursor.getColumnIndex(filePatchCol[0]);
                    String picturePath=cursor.getString(colIndex);
                    cursor.close();
                    imageview.setImageBitmap(BitmapFactory.decodeFile(picturePath));
                }

            }
        }
    }
    public void onPause(){
        if(textToSpeech !=null){

            textToSpeech.stop();
        }
        super.onPause();
    }


}