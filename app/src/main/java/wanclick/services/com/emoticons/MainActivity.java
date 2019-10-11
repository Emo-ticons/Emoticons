package wanclick.services.com.emoticons;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionPoint;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceLandmark;

import org.w3c.dom.Text;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class MainActivity extends AppCompatActivity {

    FirebaseVisionFaceDetector faceDetector;
    ImageView analyseImage;
    TextView textView;
    Button analyseButton;
    Button captureButton;
    public static final int RequestPermissionCode = 1;
    Intent intent;
    FirebaseVisionImage firebaseVisionImage;
    //face features
    float smilingProbability = 0.0f;
    float rightEyeOpenProb = 0.0f;
    float leftEyeOpenProb = 0.0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseApp.initializeApp(this);
        analyseButton = findViewById(R.id.buttonAnalyse);
        captureButton = findViewById(R.id.buttonCapture);
        analyseImage = findViewById(R.id.analyseImage);
        textView = findViewById(R.id.smilingProbability);

        EnableRuntimePermission();

        captureButtonClickHandler();

        analyseButtonClickHandler();









    }

    private void captureButtonClickHandler(){
        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent,7);
            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == 7 && resultCode == RESULT_OK) {

            Bitmap bitmap = (Bitmap) data.getExtras().get("data");

            analyseImage.setImageBitmap(bitmap);
        }
    }

    public void EnableRuntimePermission(){

        if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                Manifest.permission.CAMERA))
        {

            Toast.makeText(MainActivity.this,"CAMERA permission allows us to Access CAMERA app", Toast.LENGTH_LONG).show();

        } else {

            ActivityCompat.requestPermissions(MainActivity.this,new String[]{
                    Manifest.permission.CAMERA}, RequestPermissionCode);

        }
    }

    @Override
    public void onRequestPermissionsResult(int RC, String per[], int[] PResult) {

        switch (RC) {

            case RequestPermissionCode:

                if (PResult.length > 0 && PResult[0] == PackageManager.PERMISSION_GRANTED) {

                    Toast.makeText(MainActivity.this,"Permission Granted, Now your application can access CAMERA.", Toast.LENGTH_LONG).show();

                } else {

                    Toast.makeText(MainActivity.this,"Permission Canceled, Now your application cannot access CAMERA.", Toast.LENGTH_LONG).show();

                }
                break;
        }
    }

    private void analyseButtonClickHandler(){
        analyseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap bitmap = ((BitmapDrawable)analyseImage.getDrawable()).getBitmap();
                firebaseVisionImage = FirebaseVisionImage.fromBitmap(bitmap);
                detectFaces();

                if(smilingProbability>=0.7&&(rightEyeOpenProb>=0.6&&leftEyeOpenProb>=0.6)){
                    textView.setText("The person is interested");
                }else if(smilingProbability>=0.7&&(rightEyeOpenProb<=0.6&&leftEyeOpenProb<=0.6)){
                    textView.setText("The person is fucking sleeping and dreaming");
                }else if(smilingProbability<0.7&&(rightEyeOpenProb<=0.6&&leftEyeOpenProb<=0.6)){
                    textView.setText("The person is fucking passed out, jaa ghar main so bc");
                }

            }
        });
    }

    private void detectFaces(){
        FirebaseVisionFaceDetectorOptions highOptions = new FirebaseVisionFaceDetectorOptions.Builder()
                .setPerformanceMode(FirebaseVisionFaceDetectorOptions.ACCURATE)
                .setLandmarkMode(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
                .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
                .enableTracking()
                .build();

        //real time contour detection of face
        FirebaseVisionFaceDetectorOptions realOptions = new FirebaseVisionFaceDetectorOptions.Builder()
                .setContourMode(FirebaseVisionFaceDetectorOptions.ALL_CONTOURS)
                .build();

        faceDetector = FirebaseVision.getInstance().getVisionFaceDetector(highOptions);

        //setting up the click listner to detect face characteristics
        Task<List<FirebaseVisionFace>> result = faceDetector.detectInImage(firebaseVisionImage)
                .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionFace>>() {
                    @Override
                    public void onSuccess(List<FirebaseVisionFace> firebaseVisionFaces) {
                        for(FirebaseVisionFace face:firebaseVisionFaces){
                            Rect bounds = face.getBoundingBox();
                            float rotY = face.getHeadEulerAngleY();  // Head is rotated to the right rotY degrees
                            float rotZ = face.getHeadEulerAngleZ();  // Head is tilted sideways rotZ degrees



                            // If landmark detection was enabled (mouth, ears, eyes, cheeks, and
                            // nose available):
                            FirebaseVisionFaceLandmark leftEar = face.getLandmark(FirebaseVisionFaceLandmark.LEFT_EAR);
                            if (leftEar != null) {
                                FirebaseVisionPoint leftEarPos = leftEar.getPosition();
                            }

                            // If classification was enabled:
                            if (face.getSmilingProbability() != FirebaseVisionFace.UNCOMPUTED_PROBABILITY) {
                                 smilingProbability = face.getSmilingProbability();
                            }
                            if (face.getRightEyeOpenProbability() != FirebaseVisionFace.UNCOMPUTED_PROBABILITY) {
                                 rightEyeOpenProb = face.getRightEyeOpenProbability();
                            }
                            if(face.getLeftEyeOpenProbability() != FirebaseVisionFace.UNCOMPUTED_PROBABILITY){
                                leftEyeOpenProb = face.getLeftEyeOpenProbability();
                            }

                            // If face tracking was enabled:
                            if (face.getTrackingId() != FirebaseVisionFace.INVALID_ID) {
                                int id = face.getTrackingId();
                            }
                        }
                        // [END get_face_info]
                        // [END_EXCLUDE]
                    }


                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("Problem in detection",e.getMessage());
                    }
                });








    }

    private void detectFaceCharacteristics(){



    }
}
