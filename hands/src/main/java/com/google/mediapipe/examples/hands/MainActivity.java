package com.google.mediapipe.examples.hands;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.os.Handler;
import android.os.Message;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

// ContentResolver dependency

import com.google.android.material.navigation.NavigationView;
import com.google.android.youtube.player.YouTubeBaseActivity;
import com.google.mediapipe.formats.proto.LandmarkProto;
import com.google.mediapipe.formats.proto.LandmarkProto.Landmark;
import com.google.mediapipe.formats.proto.LandmarkProto.NormalizedLandmark;
import com.google.mediapipe.solutioncore.CameraInput;
import com.google.mediapipe.solutioncore.SolutionGlSurfaceView;
import com.google.mediapipe.solutioncore.VideoInput;
import com.google.mediapipe.solutions.hands.HandLandmark;
import com.google.mediapipe.solutions.hands.Hands;
import com.google.mediapipe.solutions.hands.HandsOptions;
import com.google.mediapipe.solutions.hands.HandsResult;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.utils.YouTubePlayerTracker;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.ui.utils.TimeUtilities;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.ui.views.YouTubePlayerSeekBar;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.ui.views.YouTubePlayerSeekBarListener;

import org.tensorflow.lite.Interpreter;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/** Main activity of MediaPipe Hands hands. */
public class MainActivity extends AppCompatActivity {
  YouTubePlayerView youTubePlayerView;
  YouTubePlayerSeekBar youTubePlayerSeekBar;
  String videoId;
  private static final String TAG = "MainActivity";
  Button btn_yStart;
  private Hands hands;
  Intent intent;
  SpeechRecognizer mRecognizer;
  final int PERMISSION = 1;
  String Voice_Result;
  // Run the pipeline and the model inference on GPU or CPU.
  private static final boolean RUN_ON_GPU = true;
  private String[] actions = {"play", "pause", "rewind","advance", "ok","voice_recognition","timestamp_record","go_to_tstmp_rcd"};
  private TextView Date;
  String time;
  private Interpreter tflite;
  private ProgressHandler handler;
  ByteBuffer inputs;
  float[][] outputs = new float[1][8];
  private List<String> seq = new ArrayList<>(30);
  private String Outputresult="";

  Toolbar toolbar_menu;
  private DrawerLayout drawerLayout;
  private NavigationView navigationView;
  View v_d;

  Float time_stamp; // 타임스탬프 저장된 시각



  private enum InputSource {
    UNKNOWN,
    CAMERA,
  }
  private InputSource inputSource = InputSource.UNKNOWN;

  // Image demo UI and image loader components.


  // Video demo UI and video loader components.
  private VideoInput videoInput;

  // Live camera demo UI and camera components.
  private CameraInput cameraInput;

  private SolutionGlSurfaceView<HandsResult> glSurfaceView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN);
    setContentView(R.layout.activity_main);

    // 만들 툴바를 가져와 셋팅한다

    toolbar_menu = (Toolbar) findViewById(R.id.up_toolbar);
    setSupportActionBar(toolbar_menu);
    ActionBar actionBar = getSupportActionBar();
    actionBar.setDisplayShowCustomEnabled(true); //커스터마이징 하기 위해 필요
    actionBar.setDisplayShowTitleEnabled(false); // 타이틀 이름 안보이게
    actionBar.setDisplayHomeAsUpEnabled(true); // 뒤로가기 버튼 생성
    actionBar.setHomeAsUpIndicator(R.drawable.ic_baseline_menu_open_24); //해당 리스너는 android.R.id.home 이다.
    //상태바 투명주기 (버전별로 다르게)
    if (Build.VERSION.SDK_INT >= 19 && Build.VERSION.SDK_INT < 21) {
      setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, true);
    }
    if (Build.VERSION.SDK_INT >= 19) {
      getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }
    //make fully Android Transparent Status bar
    if (Build.VERSION.SDK_INT >= 21) {
      setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false);
      getWindow().setStatusBarColor(Color.TRANSPARENT);
    }

    drawerLayout = findViewById(R.id.drawer_layout);
    drawerLayout.setAlpha(0.85f); //배경이미지 투명도 주기
    navigationView = findViewById(R.id.navigationView);

    navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
      @Override
      public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId())
        {
          case R.id.motion:
            AlertDialog.Builder d = new AlertDialog.Builder(MainActivity.this);
            d.setTitle("GESTURE");

            v_d = (View) View.inflate(MainActivity.this, R.layout.setup, null);
            d.setView(v_d);
            d.setPositiveButton("EXIT", null);
            d.show();
            break;
          case R.id.voice_on:
            mRecognizer = SpeechRecognizer.createSpeechRecognizer(MainActivity.this); // 새 SpeechRecognizer 를 만드는 팩토리 메서드
            mRecognizer.setRecognitionListener(listener); // 리스너 설정
            mRecognizer.startListening(intent);
            break;
        }
        return true;
      }
    });
    handler = new ProgressHandler();
    tflite = ModelModule.getTfliteInterpreter(this, "keras_model_1655420664.tflite", Boolean.FALSE);


    AudioManager mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

    if(Build.VERSION.SDK_INT >= 23){
      ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.INTERNET,
              Manifest.permission.RECORD_AUDIO},PERMISSION);
      if (!mAudioManager.isStreamMute(AudioManager.STREAM_MUSIC)){
        mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,AudioManager.ADJUST_SAME,0);
      }
    }
    else{
      mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC,true);
    }

    intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
    intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,getPackageName()); // 여분의 키
    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"ko-KR");


    youTubePlayerView = findViewById(R.id.youtube_player_view);
    getLifecycle().addObserver(youTubePlayerView);



    Intent gt = getIntent();
    videoId = gt.getStringExtra("id");

    btn_yStart = findViewById(R.id.btn_yStart);

    setupLiveDemoUiComponents();

    btn_yStart.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        btn_yStart.setVisibility(View.GONE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
        youTubePlayerView.setVisibility(View.VISIBLE);
        YouTubePlayerListener listener = new AbstractYouTubePlayerListener() {
          @Override
          public void onReady(@NonNull YouTubePlayer youTubePlayer) {
            youTubePlayer.loadVideo(videoId,0);
            youTubePlayerSeekBar = (YouTubePlayerSeekBar) findViewById(R.id.youtube_player_seekbar);
            youTubePlayer.addListener(youTubePlayerSeekBar);
            youTubePlayerSeekBar.setYoutubePlayerSeekBarListener(new YouTubePlayerSeekBarListener() {
              @Override
              public void seekTo(float time) {
                youTubePlayer.seekTo(time);
              }
            });
          }
        };
        IFramePlayerOptions options = new IFramePlayerOptions.Builder().controls(0).build();
        youTubePlayerView.initialize(listener, options);
      }
    });
  }


  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater menuInflater = getMenuInflater();
    menuInflater.inflate(R.menu.toolbar_menu, menu);
    toolbar_menu.getMenu().clear();
    return true;
  }

  //액션바에 메뉴버튼은 기본적으로 android.R.id.home 이라는 id값을 가지고 있다.
  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()){
      case android.R.id.home:
        drawerLayout.openDrawer(GravityCompat.START);
        return true;
    }
    return super.onOptionsItemSelected(item);
  }


  public static void setWindowFlag(Activity activity, final int bits, boolean on) {
    Window win = activity.getWindow();
    WindowManager.LayoutParams winParams = win.getAttributes();
    if (on) {
      winParams.flags |= bits;
    } else {
      winParams.flags &= ~bits;
    }
    win.setAttributes(winParams);
  }

  @Override
  protected void onResume() {
    super.onResume();


    if (inputSource == InputSource.CAMERA) {
      // Restarts the camera and the opengl surface rendering.
      cameraInput = new CameraInput(this);
      cameraInput.setNewFrameListener(textureFrame -> hands.send(textureFrame));
      glSurfaceView.post(this::startCamera);
      glSurfaceView.setVisibility(View.INVISIBLE);
    }
  }

  @Override
  protected void onPause() {
    super.onPause();
    if (inputSource == InputSource.CAMERA) {
      glSurfaceView.setVisibility(View.GONE);
      cameraInput.close();
    }
  }

  /** Sets up the UI components for the live demo with camera input. */
  public void setupLiveDemoUiComponents() {
    if (inputSource == InputSource.CAMERA) {
      glSurfaceView.setVisibility(View.VISIBLE);
      return;
    }
    //stopCurrentPipeline();
    setupStreamingModePipeline(InputSource.CAMERA);

  }

  /** Sets up core workflow for streaming mode. */
  private void setupStreamingModePipeline(InputSource inputSource){
    this.inputSource = inputSource;
    hands =
            new Hands(
                    this,
                    HandsOptions.builder()
                            .setStaticImageMode(false)
                            .setMaxNumHands(1)
                            .setRunOnGpu(RUN_ON_GPU)
                            .setModelComplexity(0)
                            .setMinDetectionConfidence((float)0.3)
                            .setMinTrackingConfidence((float)0.3)
                            .build()
            );
    hands.setErrorListener((message, e) -> Log.e(TAG, "MediaPipe Hands error:" + message));
    if(inputSource == InputSource.CAMERA){
      cameraInput = new CameraInput(this);
      cameraInput.setNewFrameListener(textureFrame -> hands.send(textureFrame));
    }
    glSurfaceView =
            new SolutionGlSurfaceView<>(this, hands.getGlContext(), hands.getGlMajorVersion());
    glSurfaceView.setSolutionResultRenderer(new HandsResultGlRenderer());
    glSurfaceView.setRenderInputImage(true);
    hands.setResultListener(
            handsResult -> {
              logLandmark(handsResult);
              glSurfaceView.setRenderData(handsResult);
              glSurfaceView.requestRender();
            }
    );
    if(inputSource == InputSource.CAMERA){
      glSurfaceView.post(this::startCamera);
    }
    FrameLayout frameLayout = findViewById(R.id.preview_display_layout);
    frameLayout.removeAllViewsInLayout();
    frameLayout.addView(glSurfaceView);
    frameLayout.setRotation(90);

    glSurfaceView.setVisibility(View.VISIBLE);
    frameLayout.requestLayout();
  }

  private void startCamera() {
    cameraInput.start(
            this,
            hands.getGlContext(),
            CameraInput.CameraFacing.FRONT,
            glSurfaceView.getWidth(),
            glSurfaceView.getHeight());
  }

  private void stopCurrentPipeline() {
    if (cameraInput != null) {
      cameraInput.setNewFrameListener(null);
      cameraInput.close();
    }
    if (glSurfaceView != null) {
      glSurfaceView.setVisibility(View.GONE);
    }
    if (hands != null) {
      hands.close();
    }
  }

  private void logLandmark(HandsResult result) {

    // Youtube Player tracker 선언
    YouTubePlayerTracker tracker = new YouTubePlayerTracker();

    if(result.multiHandLandmarks().isEmpty()){
      return;
    }
    int i = 0;
    int j = 0;
    double[] arr = new double[42];
    StringBuilder sb = new StringBuilder();
    List<LandmarkProto.NormalizedLandmarkList> landmark = result.multiHandLandmarks();
    for(LandmarkProto.NormalizedLandmark ls : landmark.get(i).getLandmarkList()){
      arr[j++] = ls.getX();
      arr[j++] = ls.getY();
      i += 1;
    }
    inputs = (ByteBuffer) preprocessing_input(arr);
    if(inputs != null)
      tflite.run(inputs, outputs);
    Log.i(
            TAG,
            String.format(
                    "Vector Output %s",
                    Arrays.deepToString(outputs)
            )
    );
    int idx_max = argMax(outputs);
    if(outputs[0][idx_max] > 0.9){
      seq.add(actions[idx_max]);
    }
    else{
      seq.add("NONE");
    }
    Set<String> set = new HashSet<String>();
    for(int k=0; k < seq.size(); k++){
      set.add(seq.get(k));
      if(set.size() > 1){
        seq.clear();
        set.clear();
      }
      else if(seq.size() > 10){  // frame개수
        Iterator<String> setIter = set.iterator();
        Outputresult = setIter.next();
        youTubePlayerView.getYouTubePlayerWhenReady(youTubePlayer ->
      {
        // 정지 명령어
        if (Outputresult.contains("pause")) {
          youTubePlayer.pause();
          MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
              Toast.makeText(getApplicationContext(),"PAUSE",Toast.LENGTH_SHORT).show();
            }
          });
          seq.clear();
          set.clear();
        }

        // 재생 명령어
        else if (Outputresult.contains("play")) {
          youTubePlayer.play();
          MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
              Toast.makeText(getApplicationContext(),"Play",Toast.LENGTH_SHORT).show();
            }
          });
          seq.clear();
          set.clear();
        }

        // 뒤로 점프 명령어
        else if (Outputresult.contains("rewind")) {
          youTubePlayer.addListener(youTubePlayerSeekBar);
          String str = (String) youTubePlayerSeekBar.getVideoCurrentTimeTextView().getText();
          String[] times = str.split(":");
          int min = Integer.parseInt(times[0]);
          int sec = Integer.parseInt(times[1]);
          Float flag = (float) min*60 + (float) sec - (float) 10.0;
          youTubePlayer.seekTo(flag);

          MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
              Toast.makeText(getApplicationContext(),"Rewind",Toast.LENGTH_SHORT).show();
            }
          });
          seq.clear();
          set.clear();
        }

        // 앞으로 점프 명령어
        else if (Outputresult.contains("advance")) {
          youTubePlayer.addListener(youTubePlayerSeekBar);
          String str = (String) youTubePlayerSeekBar.getVideoCurrentTimeTextView().getText();
          String[] times = str.split(":");
          int min = Integer.parseInt(times[0]);
          int sec = Integer.parseInt(times[1]);
          Float flag = (float) min*60 + (float) sec + (float) 10.0;
          youTubePlayer.seekTo(flag);

          MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
              Toast.makeText(getApplicationContext(),"Advance",Toast.LENGTH_SHORT).show();
            }
          });
          seq.clear();
          set.clear();
        }

        // 타임스탬프
        else if (Outputresult.contains("timestamp_record")) {
          youTubePlayer.addListener(youTubePlayerSeekBar);
          String str = (String) youTubePlayerSeekBar.getVideoCurrentTimeTextView().getText();
          String msg = "[Time Stamp]" + str + "Save!";
          String[] times = str.split(":");
          int min = Integer.parseInt(times[0]);
          int sec = Integer.parseInt(times[1]);
          time_stamp = (float) min*60 + (float) sec;


          MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
              Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
            }
          });
          seq.clear();
          set.clear();
        }

        // 타임스탬프 불러오기
        else if (Outputresult.contains("go_to_tstmp_rcd")) {
          youTubePlayer.seekTo(time_stamp);
          MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
              Toast.makeText(getApplicationContext(),"Load Time Stamp",Toast.LENGTH_SHORT).show();
            }
          });
          seq.clear();
          set.clear();
        }

        /*else if (Outputresult.contains("voice")) {
          mRecognizer = SpeechRecognizer.createSpeechRecognizer(MainActivity.this); // 새 SpeechRecognizer 를 만드는 팩토리 메서드
          mRecognizer.setRecognitionListener(listener); // 리스너 설정
          mRecognizer.startListening(intent);
          MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
              Toast.makeText(getApplicationContext(),"VOICE ON",Toast.LENGTH_SHORT).show();
            }
          });
        }*/
        else
        {
          seq.clear();
          set.clear();
        }

//        else if (Voice_Result.contains("반속")){
//          youTubePlayer.setPlaybackRate(PlayerConstants.PlaybackRate.RATE_0_5);
//        }
//
//        else if (Voice_Result.contains("정속")){
//          youTubePlayer.setPlaybackRate(PlayerConstants.PlaybackRate.RATE_1);
//        }
//
//        else if (Voice_Result.contains("배속")){
//          youTubePlayer.setPlaybackRate(PlayerConstants.PlaybackRate.RATE_2);
//        }

      });
        Log.i(
                TAG,
                String.format(
                        "OutputResult %s",
                        Outputresult
                )
        );
        seq.clear();
        set.clear();
        Outputresult = "재생 중";
      }
    }
  }

  private int argMax(float[][] outputs){
    float max=0;
    int res = 0;
    for(int i=0; i<actions.length; i++){
      if(max < outputs[0][i]) {
        max = outputs[0][i];
        res = i;
      }
    }
    return res;
  }

  class ProgressHandler extends Handler {
    @Override
    public void handleMessage(Message msg) {
      Date.setText(time);
    }
  }

  public ByteBuffer preprocessing_input(double[] input){
    ByteBuffer to_return = ByteBuffer.allocateDirect(4 * input.length);
    to_return.order(ByteOrder.nativeOrder());
    for(int i=0; i<input.length; i++){
      to_return.putFloat((float)input[i]);
    }
    return to_return;
  }
  //---------------------------------------------음성 인식-----------------------------------------------------------------
  private RecognitionListener listener = new RecognitionListener() {
    @Override
    public void onReadyForSpeech(Bundle params) {
      // 말하기 시작할 준비가되면 호출
      Toast.makeText(getApplicationContext(),"음성인식 시작",Toast.LENGTH_SHORT).show();
      Log.d("tst5", "시작");
    }

    @Override
    public void onBeginningOfSpeech() {
      // 말하기 시작했을 때 호출
    }

    @Override
    public void onRmsChanged(float rmsdB) {
      // 입력받는 소리의 크기를 알려줌
    }

    @Override
    public void onBufferReceived(byte[] buffer) {
      // 말을 시작하고 인식이 된 단어를 buffer에 담음
    }

    @Override
    public void onEndOfSpeech() {
      // 말하기를 중지하면 호출
    }

    @Override
    public void onError(int error) {
      // 네트워크 또는 인식 오류가 발생했을 때 호출
      String message;

      switch (error) {
        case SpeechRecognizer.ERROR_AUDIO:
          message = "오디오 에러";
          break;
        case SpeechRecognizer.ERROR_CLIENT:
          message = "클라이언트 에러";
          break;
        case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
          message = "퍼미션 없음";
          break;
        case SpeechRecognizer.ERROR_NETWORK:
          message = "네트워크 에러";
          break;
        case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
          message = "네트웍 타임아웃";
          break;
        case SpeechRecognizer.ERROR_NO_MATCH:
          message = "찾을 수 없음";
          break;
        case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
          message = "RECOGNIZER 가 바쁨";
          break;
        case SpeechRecognizer.ERROR_SERVER:
          message = "서버가 이상함";
          break;
        case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
          message = "말하는 시간초과";
          break;
        default:
          message = "알 수 없는 오류임";
          break;
      }

      Toast.makeText(getApplicationContext(), "에러 발생 : " + message,Toast.LENGTH_SHORT).show();
      Log.d("tst5", "onError: "+message);
    }

    @Override
    public void onResults(Bundle results) {

      // 인식 결과가 준비되면 호출
      // 말을 하면 ArrayList에 단어를 넣고 textView에 단어를 이어줌
      ArrayList<String> matches =
              results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
      Voice_Result = matches.get(0);
      if(Voice_Result.contains("재생")){
        Toast.makeText(getApplicationContext(), "재생", Toast.LENGTH_SHORT).show();
      }
      if(Voice_Result.contains("정지")){
        Toast.makeText(getApplicationContext(), "정지", Toast.LENGTH_SHORT).show();
      }
      if(Voice_Result.contains("앞")){
        Toast.makeText(getApplicationContext(), "앞", Toast.LENGTH_SHORT).show();
      }
      if(Voice_Result.contains("뒤")){
        Toast.makeText(getApplicationContext(), "뒤", Toast.LENGTH_SHORT).show();
      }
      if(Voice_Result.contains("반속")){
        Toast.makeText(getApplicationContext(), "반속", Toast.LENGTH_SHORT).show();
      }
      if(Voice_Result.contains("정속")){
        Toast.makeText(getApplicationContext(), "정속", Toast.LENGTH_SHORT).show();
      }
      if(Voice_Result.contains("배속")){
        Toast.makeText(getApplicationContext(), "배속", Toast.LENGTH_SHORT).show();
      }
      youTubePlayerView.getYouTubePlayerWhenReady(youTubePlayer ->
      {
        // 정지 명령어
        if (Voice_Result.contains("정지")) {
          youTubePlayer.pause();
        }
        // 재생 명령어
        else if (Voice_Result.contains("재생")) {
          youTubePlayer.play();
        }
        else if (Voice_Result.contains("반속")){
          youTubePlayer.setPlaybackRate(PlayerConstants.PlaybackRate.RATE_0_5);
        }
        else if (Voice_Result.contains("정속")){
          youTubePlayer.setPlaybackRate(PlayerConstants.PlaybackRate.RATE_1);
        }
        else if (Voice_Result.contains("배속")){
          youTubePlayer.setPlaybackRate(PlayerConstants.PlaybackRate.RATE_2);
        }
      });

    }

    @Override
    public void onPartialResults(Bundle partialResults) {
      // 부분 인식 결과를 사용할 수 있을 때 호출
    }

    @Override
    public void onEvent(int eventType, Bundle params) {
      // 향후 이벤트를 추가하기 위해 예약
    }
  };

}