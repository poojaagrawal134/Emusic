package com.example.mplayer.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.mplayer.R;

public class Feedback extends AppCompatActivity {
    Button fed;
    EditText ed;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);
        ed=(EditText)findViewById(R.id.feed);
        fed=(Button)findViewById(R.id.feedb);
        fed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ed.getText().equals(""))
                {Toast.makeText(Feedback.this, "Can not be empty", Toast.LENGTH_LONG).show();

                }else {
                    Toast.makeText(Feedback.this, "Thank You !! Feedback has Recorded", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}