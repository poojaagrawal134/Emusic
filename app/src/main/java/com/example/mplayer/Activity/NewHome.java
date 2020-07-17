package com.example.mplayer.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mplayer.R;
import com.google.firebase.auth.FirebaseAuth;
import com.microsoft.projectoxford.face.FaceServiceClient;
import com.microsoft.projectoxford.face.FaceServiceRestClient;
import com.microsoft.projectoxford.face.contract.Face;
import com.microsoft.projectoxford.face.contract.FaceRectangle;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.content.ContentValues.TAG;

public class NewHome extends AppCompatActivity {
    private FaceServiceClient faceServiceClient = new FaceServiceRestClient("https://centralindia.api.cognitive.microsoft.com/face/v1.0/", "de8df0fb1aac4453997d306778c2b400");
    private static final int RESULT_LOAD_IMAGE  = 100;
    private static final int REQUEST_CAMERA_CODE =  300;
    private static final int REQUEST_PERMISSION_CODE = 200;
    TextView finalre;
    JSONObject jsonObject, jsonObject1;
    ImageView imageView;
    Button musicbtn;
    Bitmap mBitmap;
    TextView happy,sad,surprose,neutral,anger,contempt,disgust,fear;
    boolean takePicture = false;

    private ProgressDialog detectionProgressDialog;
    Face[] facesDetected;
    public void getImage() {
        // check if user has given us permission to access the gallery
        if(checkPermission()) {
            Intent choosePhotoIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(choosePhotoIntent, RESULT_LOAD_IMAGE);
        }
        else {
            requestPermission();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0 && resultCode == RESULT_OK) {
            Bitmap bitmap = (Bitmap) data.getExtras().get("data");
            imageView.setImageBitmap(bitmap);
            detectAndFrame(bitmap);
        }
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {

            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };
            Cursor cursor = getContentResolver().query(selectedImage,filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            // a string variable which will store the path to the image in the gallery
            String picturePath= cursor.getString(columnIndex);
            cursor.close();
            Bitmap bitmap = BitmapFactory.decodeFile(picturePath);
            imageView.setImageBitmap(bitmap);
            detectAndFrame(bitmap);
        }

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==R.id.nav_log)
        {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(getApplicationContext(), Login.class));
            finish();
        }
        else if(item.getItemId()==R.id.nav_fee)
        {
            startActivity(new Intent(NewHome.this,Feedback.class));
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_home);
        happy=findViewById(R.id.happ);
        sad=findViewById(R.id.sad);
        surprose=findViewById(R.id.surprise);
        neutral=findViewById(R.id.neutral);
        anger=findViewById(R.id.anger);
        disgust=findViewById(R.id.disgust);
        fear=findViewById(R.id.fear);
        musicbtn=findViewById(R.id.MusicBtn);
        finalre=findViewById(R.id.finresult);
        contempt=findViewById(R.id.contempt);
        detectionProgressDialog = new ProgressDialog(this);
        jsonObject = new JSONObject();
        jsonObject1 = new JSONObject();
//        mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.billgates);
        imageView = findViewById(R.id.imageView);
        Toast.makeText(getApplicationContext(), "Press the Detect Button to take a picture. Press Identify to identify the person.", Toast.LENGTH_LONG).show();
//        imageView.setImageBitmap(mBitmap);
        Button btnDetect = findViewById(R.id.btnDetectFace);
//        Button btnIdentify = findViewById(R.id.btnIdentify);

        Button se=findViewById(R.id.selectgallary);
        se.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getImage();
            }
        });

        btnDetect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(NewHome.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 110);
                } else {
                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(cameraIntent, 0);
                }

            }
        });
        musicbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(finalre.getText().toString().equals("Happy"))
                {
                    startActivity(new Intent(NewHome.this, HappyActivity.class));
                }
                else if(finalre.getText().toString().equals("Sad"))
                {
                    startActivity(new Intent(NewHome.this, SadActivity.class));
                }
                else if(finalre.getText().toString().equals("Angry"))
                {
                    startActivity(new Intent(NewHome.this, AngryActivity.class));
                }
            }
        });




    }

    private void detectAndFrame(final Bitmap imageBitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        ByteArrayInputStream inputStream =
                new ByteArrayInputStream(outputStream.toByteArray());

        @SuppressLint("StaticFieldLeak") AsyncTask<InputStream, String, Face[]> detectTask =

                new AsyncTask<InputStream, String, Face[]>() {
                    String exceptionMessage = "";

                    @Override
                    protected Face[] doInBackground(InputStream... params) {
                        try {
                            publishProgress("Detecting...");
                            Face[] result = faceServiceClient.detect(
                                    params[0],
                                    true,         // returnFaceId
                                    false,        // returnFaceLandmarks
                                    // returnFaceAttributes:
                                    new FaceServiceClient.FaceAttributeType[] {
                                            FaceServiceClient.FaceAttributeType.Emotion,
                                            FaceServiceClient.FaceAttributeType.Gender }
                            );

                            for (int i=0;i<result.length;i++) {
                                jsonObject.put("happiness" , result[i].faceAttributes.emotion.happiness);
                                jsonObject.put("sadness" , result[i].faceAttributes.emotion.sadness);
                                jsonObject.put("surprise" , result[i].faceAttributes.emotion.surprise);
                                jsonObject.put("neutral"  , result[i].faceAttributes.emotion.neutral);
                                jsonObject.put("anger" , result[i].faceAttributes.emotion.anger);
                                jsonObject.put("contempt" , result[i].faceAttributes.emotion.contempt);
                                jsonObject.put("disgust" , result[i].faceAttributes.emotion.disgust);
                                jsonObject.put("fear" , result[i].faceAttributes.emotion.fear);
                                Log.e(TAG, "doInBackground: "+jsonObject.toString()  );

                                jsonObject1.put(  (String.valueOf(i)),jsonObject);
                            }
//
                            runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    Toast.makeText(NewHome.this,"DATA"+jsonObject1.toString(),Toast.LENGTH_LONG).show();
                                    try {
                                        happy.setText(jsonObject.getString("happiness"));
                                        sad.setText(jsonObject.getString("sadness"));
                                        surprose.setText(jsonObject.getString("surprise"));
                                        neutral.setText(jsonObject.getString("neutral"));
                                        anger.setText(jsonObject.getString("anger"));
                                        contempt.setText(jsonObject.getString("contempt"));
                                        disgust.setText(jsonObject.getString("disgust"));
                                        fear.setText(jsonObject.getString("fear"));
                                        Float n1=Float.parseFloat(happy.getText().toString());
                                        Float n2=Float.parseFloat(sad.getText().toString());
                                        Float n3=Float.parseFloat(surprose.getText().toString());
                                        Float n4=Float.parseFloat(neutral.getText().toString());
                                        Float n5=Float.parseFloat(anger.getText().toString());
                                        Float n6=Float.parseFloat(contempt.getText().toString());
                                        Float n7=Float.parseFloat(disgust.getText().toString());
                                        Float n8=Float.parseFloat(fear.getText().toString());
                                        HashMap<String, Float> meMap=new HashMap<String, Float>();
                                        meMap.put("Happy",n1);
                                        meMap.put("Sad",n2);
                                        meMap.put("Surprise",n3);
                                        meMap.put("Neutral",n4);
                                        meMap.put("Angry",n5);
                                        meMap.put("Contempt",n6);
                                        meMap.put("Disgust",n7);
                                        meMap.put("Fear",n8);
                                       Float maxValueInMap=(Collections.max(meMap.values()));  // This will return max value in the Hashmap
                                        for (final Map.Entry<String, Float> entry : meMap.entrySet()) {  // Itrate through hashmap
                                            if (entry.getValue() == maxValueInMap) {
                                                finalre.setText(entry.getKey());     // Print the key with max value
                                                musicbtn.setText(entry.getKey()+ " Songs");
                                            }

                                        }


                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }});

                            if (result == null) {
                                publishProgress(
                                        "Detection Finished. Nothing detected");
                                return null;
                            }
                            Log.e("TAG", "doInBackground: "+"   "+result.length );
                            publishProgress(String.format(
                                    "Detection Finished. %d face(s) detected",
                                    result.length));

                            return result;
                        } catch (Exception e) {
                            exceptionMessage = String.format(
                                    "Detection failed: %s", e.getMessage());
                            return null;
                        }
                    }

                    @Override
                    protected void onPreExecute() {
                        //TODO: show progress dialog
                        detectionProgressDialog.show();
                    }

                    @Override
                    protected void onProgressUpdate(String... progress) {
                        //TODO: update progress
                        detectionProgressDialog.setMessage(progress[0]);
                    }

                    @Override
                    protected void onPostExecute(Face[] result) {
                        //TODO: update face frames
                        detectionProgressDialog.dismiss();

                        facesDetected = result;

                        if (!exceptionMessage.equals("")) {
                            if (facesDetected == null) {
//                                showError(exceptionMessage + "\nNo faces detected.");
                            } else {
//                                showError(exceptionMessage);
                            }
                        }
                        if (result == null) {
                            if (facesDetected == null) {
//                                showError("No faces detected");
                            }
                        }
                        Log.e("TAG", "onPostExecute: "+facesDetected );

                        ImageView imageView = findViewById(R.id.imageView);
                        imageView.setImageBitmap(
                                drawFaceRectanglesOnBitmap(imageBitmap, result));
                        imageBitmap.recycle();
//                        Toast.makeText(getApplicationContext(), "Now you can identify the person by pressing the \"Identify\" Button", Toast.LENGTH_LONG).show();
                        takePicture = true;
                    }
                };

        detectTask.execute(inputStream);
    }


    private static Bitmap drawFaceRectanglesOnBitmap(
            Bitmap originalBitmap, Face[] faces) {
        Bitmap bitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.RED);
        paint.setStrokeWidth(9);
        if (faces != null) {
            for (Face face : faces) {
                FaceRectangle faceRectangle = face.faceRectangle;
                canvas.drawRect(
                        faceRectangle.left,
                        faceRectangle.top,
                        faceRectangle.left + faceRectangle.width,
                        faceRectangle.top + faceRectangle.height,
                        paint);
            }
        }
        return bitmap;
    }

//
//    private void showError(String message) {
//        new AlertDialog.Builder(this)
//                .setTitle("Error")
//                .setMessage(message)
//                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int id) {
//                    }
//                })
//                .create().show();
//    }

//    private class IdentificationTask extends AsyncTask<UUID, String, IdentifyResult[]> {
//        String personGroupId;
//
//        private ProgressDialog mDialog = new ProgressDialog(MainActivity.this);
//
//        public IdentificationTask(String personGroupId) {
//            this.personGroupId = personGroupId;
//        }
//
//        @Override
//        protected IdentifyResult[] doInBackground(UUID... params) {
//
//            try {
//                publishProgress("Getting person group status...");
//                TrainingStatus trainingStatus = faceServiceClient.getPersonGroupTrainingStatus(this.personGroupId);
//                if (trainingStatus.status != TrainingStatus.Status.Succeeded) {
//                    publishProgress("Person group training status is " + trainingStatus.status);
//                    return null;
//                }
//                publishProgress("Identifying...");
//
//                IdentifyResult[] results = faceServiceClient.identity(personGroupId, // person group id
//                        params // face ids
//                        , 1); // max number of candidates returned
//
//                return results;
//
//            } catch (Exception e) {
//                return null;
//            }
//        }
//
//        @Override
//        protected void onPreExecute() {
//            mDialog.show();
//        }
//
//        @Override
//        protected void onPostExecute(IdentifyResult[] identifyResults) {
//            mDialog.dismiss();
//
//            for (IdentifyResult identifyResult : identifyResults) {
//                new PersonDetectionTask(personGroupId).execute(identifyResult.candidates.get(0).personId);
//            }
//        }
//
//        @Override
//        protected void onProgressUpdate(String... values) {
//            mDialog.setMessage(values[0]);
//        }
//    }

//    private class PersonDetectionTask extends AsyncTask<UUID, String, Person> {
//        private ProgressDialog mDialog = new ProgressDialog(MainActivity.this);
//        private String personGroupId;
//
//        public PersonDetectionTask(String personGroupId) {
//            this.personGroupId = personGroupId;
//        }
//
//        @Override
//        protected Person doInBackground(UUID... params) {
//            try {
//                publishProgress("Getting person group status...");
//
//                return faceServiceClient.getPerson(personGroupId, params[0]);
//            } catch (Exception e) {
//                return null;
//            }
//        }
//
//        @Override
//        protected void onPreExecute() {
//            mDialog.show();
//        }
//
//        @Override
//        protected void onPostExecute(Person person) {
//            mDialog.dismiss();
//            imageView.setImageBitmap(drawFaceRectangleOnBitmap(mBitmap, facesDetected, person.name));
//        }
//
//        @Override
//        protected void onProgressUpdate(String... values) {
//            mDialog.setMessage(values[0]);
//        }
//    }

//    private Bitmap drawFaceRectangleOnBitmap(Bitmap mBitmap, Face[] facesDetected, String name) {
//
//        Bitmap bitmap = mBitmap.copy(Bitmap.Config.ARGB_8888, true);
//        Canvas canvas = new Canvas(bitmap);
//        //Rectangle
//        Paint paint = new Paint();
//        paint.setAntiAlias(true);
//        paint.setStyle(Paint.Style.STROKE);
//        paint.setColor(Color.RED);
//        paint.setStrokeWidth(12);
//
//        if (facesDetected != null) {
//            for (Face face : facesDetected) {
//                FaceRectangle faceRectangle = face.faceRectangle;
//                canvas.drawRect(faceRectangle.left,
//                        faceRectangle.top,
//                        faceRectangle.left + faceRectangle.width,
//                        faceRectangle.top + faceRectangle.height,
//                        paint);
//                drawTextOnCanvas(canvas, 100, ((faceRectangle.left + faceRectangle.width) / 2) + 100, (faceRectangle.top + faceRectangle.height) + 50, Color.RED, name);
//
//            }
//        }
//        return bitmap;
//    }

//    private void drawTextOnCanvas(Canvas canvas, int textSize, int x, int y, int color, String name) {
//        Paint paint = new Paint();
//        paint.setAntiAlias(true);
//        paint.setStyle(Paint.Style.FILL);
//        paint.setColor(color);
//        paint.setTextSize(textSize);
//
//        float textWidth = paint.measureText(name);
//
//        canvas.drawText(name, x - (textWidth / 2), y - (textSize / 2), paint);
//    }
public boolean checkPermission() {
    int result = ContextCompat.checkSelfPermission(getApplicationContext(),READ_EXTERNAL_STORAGE);
    int result2 = ContextCompat.checkSelfPermission(getApplicationContext(),CAMERA);
    return result == PackageManager.PERMISSION_GRANTED && result2 == PackageManager.PERMISSION_GRANTED;
}

    private void requestPermission() {
        ActivityCompat.requestPermissions(NewHome.this,new String[]{READ_EXTERNAL_STORAGE,CAMERA}, REQUEST_PERMISSION_CODE);
    }

}
