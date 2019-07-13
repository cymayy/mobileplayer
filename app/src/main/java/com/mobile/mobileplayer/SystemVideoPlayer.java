package com.mobile.mobileplayer;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.mobile.mobileplayer.domain.MediaItem;
import com.mobile.mobileplayer.utils.LogUtil;
import com.mobile.mobileplayer.utils.Utils;
import com.mobile.mobileplayer.view.VideoView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class SystemVideoPlayer extends Activity implements View.OnClickListener {

    //进度更新
    private static final int PROGRESS = 0;
    //隐藏控制面板
    private static final int HIDE_MEDIACONTROLLER = 2;
    //默认播放
    private static final int DEFAULT_SCREEN = 3;
    //全屏播放
    private static final int FULL_SCREEN = 4;

    //是否全屏播放
    private boolean isFullScreen = false;
    private Uri uri;
    private VideoView videoView;

    private LinearLayout llTop;
    private TextView tvName;
    private TextView tvTime;
    private Button btnVoice;
    private SeekBar seekbarVoice;
    private ImageView ivBattery;
    private Button btnSwitchPlayer;
    private LinearLayout llBottom;
    private TextView tvCurrentTime;
    private SeekBar seekbarVideo;
    private TextView tvDuration;
    private Button btnVideoExit;
    private Button btnVideoPre;
    private Button btnVideoStartPause;
    private Button btnVideoNext;
    private Button btnVideoSwitchScreen;
    private Utils utils;
    private BatteryReceiver receiver;

    private ArrayList<MediaItem> mediaItems;
    private int position;
    //定义手势识别器
    private GestureDetector detector;
    private int screenWidth;
    private int screenHeight;
    private int videoHeight;
    private int videoWidth;

    private AudioManager am;
    //当前的音量
    private int currentVolume;
    private int maxVolume;
    //是否是静音
    private boolean isMute = false;


    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case HIDE_MEDIACONTROLLER:
                    hideMediaController();
                    break;
                case PROGRESS:
                    //得到当前的播放进度
                    int currentPosition = videoView.getCurrentPosition();
                    seekbarVideo.setProgress(currentPosition);

                    tvCurrentTime.setText(utils.stringForTime(currentPosition));
                    //更新系统时间
                    tvTime.setText(getSystemTime());
                    //每一秒更新一次
                    handler.removeMessages(PROGRESS);
                    handler.sendEmptyMessageDelayed(PROGRESS,1000);
                    break;
            }
        }
    };


    private String getSystemTime() {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        return format.format(new Date());
    }



    /**
     * Find the Views in the layout<br />
     * <br />
     * Auto-created on 2019-07-12 16:13:48 by Android Layout Finder
     * (http://www.buzzingandroid.com/tools/android-layout-finder)
     */
    private void findViews() {
        setContentView(R.layout.activity_system_video_player);
        videoView = findViewById(R.id.videoView);
        llTop = findViewById( R.id.ll_top );
        tvName = findViewById( R.id.tv_name );
        tvTime = findViewById( R.id.tv_time );
        btnVoice = findViewById( R.id.btn_voice );
        seekbarVoice = findViewById( R.id.seekbar_voice );
        btnSwitchPlayer = findViewById( R.id.btn_switch_player );
        llBottom = findViewById( R.id.ll_bottom );
        tvCurrentTime = findViewById( R.id.tv_current_time );
        seekbarVideo = findViewById( R.id.seekbar_video );
        tvDuration = findViewById( R.id.tv_duration );
        btnVideoExit = findViewById( R.id.btn_video_exit );
        btnVideoPre = findViewById( R.id.btn_video_pre );
        btnVideoStartPause = findViewById( R.id.btn_video_start_pause );
        btnVideoNext = findViewById( R.id.btn_video_next );
        btnVideoSwitchScreen = findViewById( R.id.btn_video_switch_screen );
        ivBattery = findViewById(R.id.iv_battery);

        btnVoice.setOnClickListener( this );
        btnSwitchPlayer.setOnClickListener( this );
        btnVideoExit.setOnClickListener( this );
        btnVideoPre.setOnClickListener( this );
        btnVideoStartPause.setOnClickListener( this );
        btnVideoNext.setOnClickListener( this );
        btnVideoSwitchScreen.setOnClickListener( this );

        //设置最大值
        seekbarVoice.setMax(maxVolume);
        //设置默认值
        seekbarVoice.setProgress(currentVolume);
    }

    /**
     * Handle button click events<br />
     * <br />
     * Auto-created on 2019-07-12 16:13:48 by Android Layout Finder
     * (http://www.buzzingandroid.com/tools/android-layout-finder)
     */
    @Override
    public void onClick(View v) {
        if ( v == btnVoice ) {
            // Handle clicks for btnVoice
            isMute = !isMute;
            updateVolume(currentVolume);
        } else if ( v == btnSwitchPlayer ) {
            // Handle clicks for btnSwitchPlayer
        } else if ( v == btnVideoExit ) {
            // Handle clicks for btnVideoExit
            finish();
        } else if ( v == btnVideoPre ) {
            // Handle clicks for btnVideoPre
            setPlayPre();
        } else if ( v == btnVideoStartPause ) {
            // Handle clicks for btnVideoStartPause
            startAndPause();
        } else if ( v == btnVideoNext ) {
            // Handle clicks for btnVideoNext
            setPlayNext();
        } else if ( v == btnVideoSwitchScreen ) {
            // Handle clicks for btnVideoSwitchScreen
            if(isFullScreen){
                setVideoType(DEFAULT_SCREEN);
            }else{
                setVideoType(FULL_SCREEN);
            }
        }

        handler.removeMessages(HIDE_MEDIACONTROLLER);
        handler.sendEmptyMessageDelayed(HIDE_MEDIACONTROLLER,5000);
    }

    private void startAndPause() {
        if(videoView.isPlaying()){
            //暂停
            //按钮设置为播放状态
            videoView.pause();
            btnVideoStartPause.setBackgroundResource(R.drawable.btn_video_play_selector);
        }else{
            //播放
            //按钮设置为暂停状态
            videoView.start();
            btnVideoStartPause.setBackgroundResource(R.drawable.btn_video_pause_selector);
        }
    }

    private void setPlayPre() {
        if(mediaItems != null && mediaItems.size() > 0){
            //播放上一个
            position--;
            if(position >= 0){
                MediaItem mediaItem = mediaItems.get(position);
                videoView.setVideoPath(mediaItem.getData());//设置播放地址
                tvName.setText(mediaItem.getName());

                setButtonState();
            }
        }
    }

    private void setPlayNext() {
        if(mediaItems != null && mediaItems.size() > 0){
            //播放下一个
            position++;
            if(position < mediaItems.size()){
                MediaItem mediaItem = mediaItems.get(position);
                videoView.setVideoPath(mediaItem.getData());//设置播放地址
                tvName.setText(mediaItem.getName());

                setButtonState();

                if(position == mediaItems.size()-1){
                    Toast.makeText(SystemVideoPlayer.this,"已经是最后一个视频了",Toast.LENGTH_SHORT).show();
                }
            }else{
                finish();
            }
        }else if(uri != null){
            //退出播放器
            finish();
        }
    }

    /**
     * 设置上一个和下一个按钮的状态
     */
    private void setButtonState() {
        if(mediaItems != null && mediaItems.size() > 0){

            if(position == 0){
                btnVideoPre.setEnabled(false);
                btnVideoPre.setBackgroundResource(R.drawable.btn_pre_gray);
            }else if(position == mediaItems.size() -1){
                btnVideoNext.setEnabled(false);
                btnVideoNext.setBackgroundResource(R.drawable.btn_next_gray);
            }else{
                btnVideoNext.setEnabled(true);
                btnVideoNext.setBackgroundResource(R.drawable.btn_video_next_selector);
                btnVideoPre.setEnabled(true);
                btnVideoPre.setBackgroundResource(R.drawable.btn_video_pre_selector);
            }
        }else if(uri != null){

            btnVideoNext.setEnabled(false);
            btnVideoNext.setBackgroundResource(R.drawable.btn_next_gray);
            btnVideoPre.setEnabled(false);
            btnVideoPre.setBackgroundResource(R.drawable.btn_pre_gray);

        }else{
            Toast.makeText(this,"没有播放地址",Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtil.e("=========onCreate方法=======");
        findViews();
        initData();
//        uri = getIntent().getData();
        getData();
        setData();

        setListener();

        videoView.setVideoSize(300,200);
//        videoView.setMediaController(new MediaController(this));
    }

    private void setData() {
        if(mediaItems != null && mediaItems.size() > 0){
            MediaItem mediaItem = mediaItems.get(position);
            videoView.setVideoPath(mediaItem.getData());
            tvName.setText(mediaItem.getName());
        }else if(uri != null){
            videoView.setVideoURI(uri);
            tvName.setText(uri.toString());
        }
        setButtonState();

        //设置不锁屏
        videoView.setKeepScreenOn(true);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void getData() {
        //得到一个地址：文件浏览器，浏览器，相册
        uri = getIntent().getData();
        mediaItems = (ArrayList<MediaItem>) getIntent().getSerializableExtra("videolist");
        position = getIntent().getIntExtra("position",0);
    }

    private void setListener() {
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
//                mp.setLooping(true);

                videoHeight = mp.getVideoHeight();
                videoWidth = mp.getVideoWidth();
                //得到视频的总时长和SeekBar.setMax();
                int duration = videoView.getDuration();
                seekbarVideo.setMax(duration);
                //设置总时长
                tvDuration.setText(utils.stringForTime(duration));
                //发消息更新
                handler.sendEmptyMessage(PROGRESS);
                videoView.start();

                hideMediaController();

                setVideoType(DEFAULT_SCREEN);
            }
        });

        videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                Toast.makeText(SystemVideoPlayer.this,"播放出错了",Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
//                Toast.makeText(SystemVideoPlayer.this,"播放完成",Toast.LENGTH_SHORT).show();
//                finish();

                setPlayNext();
            }
        });

        seekbarVideo.setOnSeekBarChangeListener(new VideoOnSeekBarChangeListener());

        seekbarVoice.setOnSeekBarChangeListener(new VoiceOnSeekBarChangeListener());
    }

    class VoiceOnSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener{
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if(fromUser){
                updateVolumeProgress(progress);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            handler.removeMessages(HIDE_MEDIACONTROLLER);
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            handler.sendEmptyMessageDelayed(HIDE_MEDIACONTROLLER,5000);
        }
    }

    /**
     * 根据音量传入的值修改音量
     * @param volume
     */
    private void updateVolume(int volume) {
        if (isMute) {
            am.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
            seekbarVoice.setProgress(0);//设置seekBar进度
        } else {
            am.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);
            seekbarVoice.setProgress(volume);//设置seekBar进度
            currentVolume = volume;
        }
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            currentVolume--;
            updateVolumeProgress(currentVolume);
            handler.removeMessages(HIDE_MEDIACONTROLLER);
            handler.sendEmptyMessageDelayed(HIDE_MEDIACONTROLLER, 5000);
//            return  true;//返回true不让系统弹出来
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            currentVolume++;
            updateVolumeProgress(currentVolume);
            handler.removeMessages(HIDE_MEDIACONTROLLER);
            handler.sendEmptyMessageDelayed(HIDE_MEDIACONTROLLER, 5000);
//            return  true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void updateVolumeProgress(int volume) {
        am.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);
        seekbarVoice.setProgress(volume);//设置seekBar进度
        currentVolume = volume;
        if (volume <= 0) {
            isMute = true;
        } else {
            isMute = false;
        }
    }

    private void setVideoType(int type) {
        switch (type){
            case FULL_SCREEN:

                videoView.setVideoSize(screenWidth,screenHeight);
                isFullScreen = true;

                btnVideoSwitchScreen.setBackgroundResource(R.drawable.btn_switch_screen_default_selector);
                break;
            case DEFAULT_SCREEN:
                //真实视频的高和宽
                int mVideoWidth = videoWidth;
                int mVideoHeight = videoHeight;

                int height = screenHeight;
                int width = screenWidth;

                if(mVideoHeight > 0 && mVideoWidth > 0){
                    if(mVideoWidth * height < width * mVideoWidth){
                        width = height * mVideoWidth / mVideoHeight;
                    }else if(mVideoWidth * height > width * mVideoHeight){
                        height = width * mVideoHeight / mVideoWidth;
                    }

                    videoView.setVideoSize(width,height);
                }

                btnVideoSwitchScreen.setBackgroundResource(R.drawable.btn_switch_screen_full_selector);
                isFullScreen = false;
                break;
        }
    }

    class VideoOnSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener{
        /**
         * 当进度更新的时候回调这个方法
         * @param seekBar
         * @param progress  当前进度
         * @param fromUser  是否是由用户引起的
         */
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if(fromUser){
                videoView.seekTo(progress);
            }
        }

        /**
         * 当手触碰SeekBar的时候回调这个方法
         * @param seekBar
         */
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            handler.removeMessages(HIDE_MEDIACONTROLLER);
        }

        /**
         * 当手指离开SeekBar的时候，执行这个方法
         * @param seekBar
         */
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            handler.sendEmptyMessageDelayed(HIDE_MEDIACONTROLLER,5000);
        }
    }

    private void initData() {

        //实例化AudioManager
        am = (AudioManager) getSystemService(AUDIO_SERVICE);
        currentVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
        maxVolume = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        //得到屏幕的高和宽
//        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
//        screenWidth = wm.getDefaultDisplay().getWidth();
//        screenHeight = wm.getDefaultDisplay().getHeight();

        DisplayMetrics displayMetrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        screenWidth = displayMetrics.widthPixels;
        screenHeight = displayMetrics.heightPixels;

        utils = new Utils();
        //注册监听电量广播
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        receiver = new BatteryReceiver();
        registerReceiver(receiver,intentFilter);

        //实例化手势识别器
        detector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener(){
            @Override
            public void onLongPress(MotionEvent e) {
                super.onLongPress(e);
                startAndPause();
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                if(isFullScreen){
                    setVideoType(DEFAULT_SCREEN);
                }else{
                    setVideoType(FULL_SCREEN);
                }

                return super.onDoubleTap(e);
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                if(isShowMediaController){
                    hideMediaController();
                    handler.removeMessages(HIDE_MEDIACONTROLLER);
                }else{
                    showMediaController();
                    handler.sendEmptyMessageDelayed(HIDE_MEDIACONTROLLER,5000);
                }
                return super.onSingleTapConfirmed(e);
            }
        });
    }

    class BatteryReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            int level = intent.getIntExtra("level",0);//电量0~100
            //主线程
            setBattery(level);
        }
    }

    private void setBattery(int level) {
        if(level <= 0){
            ivBattery.setImageResource(R.drawable.ic_battery_0);
        }else if(level <= 10){
            ivBattery.setImageResource(R.drawable.ic_battery_10);
        }else if(level <= 20){
            ivBattery.setImageResource(R.drawable.ic_battery_20);
        }else if(level <= 40){
            ivBattery.setImageResource(R.drawable.ic_battery_40);
        }else if(level <= 60){
            ivBattery.setImageResource(R.drawable.ic_battery_60);
        }else if(level <= 80){
            ivBattery.setImageResource(R.drawable.ic_battery_80);
        }else if(level <= 100){
            ivBattery.setImageResource(R.drawable.ic_battery_100);
        }else{
            ivBattery.setImageResource(R.drawable.ic_battery_100);
        }
    }

    //是否隐藏控制面板 true:显示 false:隐藏
    private boolean isShowMediaController = false;

    private void hideMediaController(){
        llBottom.setVisibility(View.GONE);
        llTop.setVisibility(View.GONE);
        isShowMediaController = false;
    }

    private void showMediaController(){
        llBottom.setVisibility(View.VISIBLE);
        llTop.setVisibility(View.VISIBLE);
        isShowMediaController = true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        LogUtil.e("=========onStart=========");
    }

    @Override
    protected void onResume() {
        super.onResume();
        LogUtil.e("=========onResume=========");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        LogUtil.e("=========onRestart=========");
    }

    @Override
    protected void onPause() {
        super.onPause();
        LogUtil.e("=========onPause=========");
    }

    @Override
    protected void onStop() {
        super.onStop();
        LogUtil.e("=========onStop=========");
    }

    @Override
    protected void onDestroy() {

        if(receiver != null){
            unregisterReceiver(receiver);
            receiver = null;
        }

        super.onDestroy();
        LogUtil.e("=========onDestroy=========");

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //把事件给手势识别器解析
        detector.onTouchEvent(event);
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
//                Intent intent = new Intent(this,TestActivity.class);
//                startActivity(intent);
                break;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }


}
