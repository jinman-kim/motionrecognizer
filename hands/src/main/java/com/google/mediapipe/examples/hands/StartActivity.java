package com.google.mediapipe.examples.hands;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

public class StartActivity extends AppCompatActivity {
    Animation fadeOutAnim;
    LinearLayout mLayout;
    ImageView img, img2;
    Button Btn1, Btn2;
    TextView TextView;
    FrameLayout Frame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        img = (ImageView) findViewById( R.id.ImgView );
        img2 = (ImageView) findViewById(R.id.ImgView2);
        Frame = (FrameLayout) findViewById(R.id.Frame);
        MyAnimationTask task = new MyAnimationTask();
        fadeOutAnim = AnimationUtils.loadAnimation(StartActivity.this, R.anim.fade_out);
        mLayout = (LinearLayout) findViewById(R.id.mLayout);
        mLayout.startAnimation(fadeOutAnim);

        Timer t = new Timer(false);

        t.schedule(task,2800);

        Btn1 = (Button) findViewById(R.id.Btn1);
        Btn2 = (Button) findViewById(R.id.Btn2);

        Btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Setup.class);
                startActivity(intent);
            }
        });

        Btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), SearchYoutube.class);
                startActivity(intent);
            }
        });

    }
    class MyAnimationTask extends TimerTask
    {
        public void run() {
            StartActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    img.setVisibility(View.GONE);
                    Frame.setVisibility(View.GONE);
                    img2.setVisibility(View.VISIBLE);
                    Btn1.setVisibility(View.VISIBLE);
                    Btn2.setVisibility(View.VISIBLE);
                }
            });

        }
    };

}