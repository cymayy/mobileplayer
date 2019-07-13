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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
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

/**
 * 作者：杨光福 on 2016/5/21 15:16
 * 微信：yangguangfu520
 * QQ号：541433511
 * 作用：系统播放器
 */
public class SystemVideoPlayer extends Activity implements View.OnClickListener {
    /**
     * 进度更新
     */
    private static final int PROGRESS = 0;
    /**
     * 隐藏控制面板
     */
    private static final int HIDE_MEDIACONTROLLER = 2;
    /**
     * 默认播放
     */
    private static final int DEFAULT_SCREEN = 3;
    /**
     * 全屏播放
     */
    private static final int FULL_SCREEN = 4;
    /**
     * 显示网速
     */
    private static final int SHOW_SPEED = 5;
    private VideoView videoview;
    private Uri uri;

    private LinearLayout llTop;
    private TextView tvName;
    private TextView tvTime;
    private Button btnVoice;
    private SeekBar seekbarVoice;
    private ImageView iv_battery;
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
    private RelativeLayout rl_loading;
    private LinearLayout ll_buffer;

    private TextView tv_buffer_netspeed;
    private TextView tv_loading_netspeed;

    private Utils utils;
    private BatteryReceiver receiver;

    /**
     * 是否全屏播放
     */
    private boolean isFullScreen = false;

    private int prePosition = 0;


    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case SHOW_SPEED:
                    String netSpeed = utils.getNetSpeed(SystemVideoPlayer.this);
                    tv_buffer_netspeed.setText("缓存中.."+netSpeed);
                    tv_loading_netspeed.setText("正在玩命加载中..."+netSpeed);
                    handler.sendEmptyMessageDelayed(SHOW_SPEED,1000);
                    break;
                case HIDE_MEDIACONTROLLER://隐藏控制面板
                    hideMediaController();
                    break;
                case PROGRESS:

                    //得到当前的播放进度
                    int currentPosition = videoview.getCurrentPosition();
                    seekbarVideo.setProgress(currentPosition);

                    tvCurrentTime.setText(utils.stringForTime(currentPosition));


                    //更新系统时间
                    tvTime.setText(getSysteTime());


                    //设置缓冲效果
                    if (isNetUri) {

                        int buffer = videoview.getBufferPercentage();//0~100;

                        int totalBuffer = seekbarVideo.getMax() * buffer;

                        int secondaryProgress = totalBuffer / 100;

                        seekbarVideo.setSecondaryProgress(secondaryProgress);

                    } else {
                        seekbarVideo.setSecondaryProgress(0);
                    }


                    int buffer = currentPosition - prePosition;

                    if(videoview.isPlaying()){
                        if(buffer < 500){
                            ll_buffer.setVisibility(View.VISIBLE);
                        }else{
                            ll_buffer.setVisibility(View.GONE);
                        }
                    }


                    prePosition = currentPosition;

                    //每一秒更新一次
                    removeMessages(PROGRESS);
                    sendEmptyMessageDelayed(PROGRESS, 1000);
                    break;
            }
        }
    };
    private ArrayList<MediaItem> mediaItems;
    private int position;
    //1.定义手势识别器
    private GestureDetector detector;
    /**
     * 屏幕的宽
     */
    private int screenWidth;
    /**
     * 屏幕的高
     */
    private int screenHeight;
    /**
     * 视频的本身的宽和高
     */
    private int videoWidth;
    private int videoHeight;
    private AudioManager am;
    /**
     * 当前的音量：0~15
     */
    private int currentVolume;
    /**
     * 最大音量
     */
    private int maxVolume;
    /**
     * 是否是静音
     */
    private boolean isMute = false;
    /**
     * 是否是网络资源
     */
    private boolean isNetUri = false;

    /**
     * 得到系统时间
     *
     * @return
     */
    private String getSysteTime() {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        return format.format(new Date());
    }

    /**
     * Find the Views in the layout<br />
     * <br />
     * Auto-created on 2016-05-23 11:42:48 by Android Layout Finder
     * (http://www.buzzingandroid.com/tools/android-layout-finder)
     */
    private void findViews() {
        setContentView(R.layout.activity_system_video_player);
        llTop =  findViewById(R.id.ll_top);
        videoview =  findViewById(R.id.videoview);
        tvName =  findViewById(R.id.tv_name);
        tvTime =  findViewById(R.id.tv_time);
        btnVoice =  findViewById(R.id.btn_voice);
        seekbarVoice =  findViewById(R.id.seekbar_voice);
        iv_battery =  findViewById(R.id.iv_battery);
        btnSwitchPlayer =  findViewById(R.id.btn_switch_player);
        llBottom =  findViewById(R.id.ll_bottom);
        tvCurrentTime =  findViewById(R.id.tv_current_time);
        seekbarVideo =  findViewById(R.id.seekbar_video);
        tvDuration =  findViewById(R.id.tv_duration);
        btnVideoExit =  findViewById(R.id.btn_video_exit);
        btnVideoPre =  findViewById(R.id.btn_video_pre);
        btnVideoStartPause =  findViewById(R.id.btn_video_start_pause);
        btnVideoNext =  findViewById(R.id.btn_video_next);
        btnVideoSwitchScreen =  findViewById(R.id.btn_video_switch_screen);
        rl_loading =  findViewById(R.id.rl_loading);
        ll_buffer =  findViewById(R.id.ll_buffer);
        tv_buffer_netspeed =  findViewById(R.id.tv_buffer_netspeed);
        tv_loading_netspeed =  findViewById(R.id.tv_loading_netspeed);

        btnVoice.setOnClickListener(this);
        btnSwitchPlayer.setOnClickListener(this);
        btnVideoExit.setOnClickListener(this);
        btnVideoPre.setOnClickListener(this);
        btnVideoStartPause.setOnClickListener(this);
        btnVideoNext.setOnClickListener(this);
        btnVideoSwitchScreen.setOnClickListener(this);


        //设置最大值
        seekbarVoice.setMax(maxVolume);
        //设置默认值
        seekbarVoice.setProgress(currentVolume);

        handler.sendEmptyMessage(SHOW_SPEED);

    }

    /**
     * Handle button click events<br />
     * <br />
     * Auto-created on 2016-05-23 11:42:48 by Android Layout Finder
     * (http://www.buzzingandroid.com/tools/android-layout-finder)
     */
    @Override
    public void onClick(View v) {
        if (v == btnVoice) {
            // Handle clicks for btnVoice
            isMute = !isMute;
            updateVolume(currentVolume);
        } else if (v == btnSwitchPlayer) {
            // Handle clicks for btnSwitchPlayer
        } else if (v == btnVideoExit) {
            // Handle clicks for btnVideoExit
            finish();
        } else if (v == btnVideoPre) {
            // Handle clicks for btnVideoPre
            setPlayPre();
        } else if (v == btnVideoStartPause) {
            startAndPause();
            // Handle clicks for btnVideoStartPause
        } else if (v == btnVideoNext) {
            // Handle clicks for btnVideoNext
            setPlayNext();
        } else if (v == btnVideoSwitchScreen) {
            // Handle clicks for btnVideoSwitchScreen
            if (isFullScreen) {
                setVideType(DEFAULT_SCREEN);
            } else {
                setVideType(FULL_SCREEN);
            }
        }

        handler.removeMessages(HIDE_MEDIACONTROLLER);
        handler.sendEmptyMessageDelayed(HIDE_MEDIACONTROLLER, 5000);
    }

    private void startAndPause() {
        if (videoview.isPlaying()) {
            //暂停
            videoview.pause();
            //按钮设置播放状态
            btnVideoStartPause.setBackgroundResource(R.drawable.btn_video_play_selector);
        } else {
            //播放
            videoview.start();
            //按钮设置暂停状态
            btnVideoStartPause.setBackgroundResource(R.drawable.btn_video_pause_selector);
        }

    }

    private void setPlayPre() {
        if (mediaItems != null && mediaItems.size() > 0) {
            //播放上一个
            position--;
            if (position >= 0) {

                MediaItem mediaItem = mediaItems.get(position);
                videoview.setVideoPath(mediaItem.getData());//设置播放地址-
                tvName.setText(mediaItem.getName());
                isNetUri = utils.isNetUri(mediaItem.getData());

                setButtonState();
                rl_loading.setVisibility(View.VISIBLE);
            }
        }
    }

    private void setPlayNext() {
        if (mediaItems != null && mediaItems.size() > 0) {
            //播放下一个
            position++;
            if (position < mediaItems.size()) {

                MediaItem mediaItem = mediaItems.get(position);
                videoview.setVideoPath(mediaItem.getData());//设置播放地址-
                tvName.setText(mediaItem.getName());
                isNetUri = utils.isNetUri(mediaItem.getData());

                setButtonState();

                if (position == mediaItems.size() - 1) {
                    Toast.makeText(SystemVideoPlayer.this, "已经是最后一个视频了", Toast.LENGTH_SHORT).show();
                }
                rl_loading.setVisibility(View.VISIBLE);
            } else {
                finish();
            }
        } else if (uri != null) {
            //退出播放器
            finish();
        }
    }

    /**
     * 设置上一个和下一个按钮的状态
     */
    private void setButtonState() {

        //播放列表
        if (mediaItems != null && mediaItems.size() > 0) {

            if (position == 0) {//第一个视频
                btnVideoPre.setEnabled(false);
                btnVideoPre.setBackgroundResource(R.drawable.btn_pre_gray);
            } else if (position == mediaItems.size() - 1) {//最后一个视频
                btnVideoNext.setEnabled(false);
                btnVideoNext.setBackgroundResource(R.drawable.btn_next_gray);
            } else {
                btnVideoNext.setEnabled(true);
                btnVideoNext.setBackgroundResource(R.drawable.btn_video_next_selector);
                btnVideoPre.setEnabled(true);
                btnVideoPre.setBackgroundResource(R.drawable.btn_video_pre_selector);
            }

        } else if (uri != null) {

            btnVideoNext.setEnabled(false);
            btnVideoNext.setBackgroundResource(R.drawable.btn_next_gray);
            btnVideoPre.setEnabled(false);
            btnVideoPre.setBackgroundResource(R.drawable.btn_pre_gray);
        } else {
            Toast.makeText(this, "没有播放地址", Toast.LENGTH_SHORT).show();
        }

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LogUtil.e("onCreate---");

        initData();

        findViews();

        getData();
        setData();
        setListener();


        //设置控制面板
//        videoview.setMediaController(new MediaController(this));

    }

    private void setData() {
        if (mediaItems != null && mediaItems.size() > 0) {
            MediaItem mediaItem = mediaItems.get(position);
            videoview.setVideoPath(mediaItem.getData());
            tvName.setText(mediaItem.getName());
            isNetUri = utils.isNetUri(mediaItem.getData());
        } else if (uri != null) {
            videoview.setVideoURI(uri);
            tvName.setText(uri.toString());
            isNetUri = utils.isNetUri(uri.toString());
        }

        setButtonState();

        //设置不锁屏
        videoview.setKeepScreenOn(true);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

    }

    private void getData() {
        uri = getIntent().getData();//得到一个地址：文件浏览器，浏览器，相册
        mediaItems = (ArrayList<MediaItem>) getIntent().getSerializableExtra("videolist");
        position = getIntent().getIntExtra("position", 0);//列表中的位置
    }

    private void setListener() {
        //当底层解码器准备好的时候，回调这个方法
        videoview.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {

//                mp.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
//                    @Override
//                    public void onSeekComplete(MediaPlayer mp) {
//                        Toast.makeText(SystemVideoPlayer.this,"拖动完成",Toast.LENGTH_SHORT).show();
//                    }
//                });

//                mp.setLooping(true);
                videoWidth = mp.getVideoWidth();
                videoHeight = mp.getVideoHeight();

                //1.得到视频的总时长和SeeKBar.setMax();
                int duration = videoview.getDuration();
                seekbarVideo.setMax(duration);

                //设置总时长
                tvDuration.setText(utils.stringForTime(duration));

                //2.发消息更新
                handler.sendEmptyMessage(PROGRESS);

                videoview.start();//开始播放

                hideMediaController();

                setVideType(DEFAULT_SCREEN);

                //隐藏加载页面
                rl_loading.setVisibility(View.GONE);
            }
        });

        //当播放出错的时候回调这个方法
        videoview.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                Toast.makeText(SystemVideoPlayer.this, "播放出错了", Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        //当播放完成的时候回调这个方法
        videoview.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
//                Toast.makeText(SystemVideoPlayer.this, "播放完成", Toast.LENGTH_SHORT).show();
//                finish();
                setPlayNext();

            }
        });

        seekbarVideo.setOnSeekBarChangeListener(new VideoOnSeekBarChangeListener());

        seekbarVoice.setOnSeekBarChangeListener(new VoiceOnSeekBarChangeListener());

        //设置监听卡
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
//            videoview.setOnInfoListener(new MyOnInfoListener());
//        }
    }

    class MyOnInfoListener implements MediaPlayer.OnInfoListener{

        @Override
        public boolean onInfo(MediaPlayer mp, int what, int extra) {
            switch (what){
                case MediaPlayer.MEDIA_INFO_BUFFERING_START://开始卡了，拖动卡了
                    ll_buffer.setVisibility(View.VISIBLE);
                    break;

                case MediaPlayer.MEDIA_INFO_BUFFERING_END://卡结束了，拖动卡结束
                    ll_buffer.setVisibility(View.GONE);
                    break;

            }
            return true;
        }
    }

    class VoiceOnSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                updateVolumeProgress(progress);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            handler.removeMessages(HIDE_MEDIACONTROLLER);
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            handler.sendEmptyMessageDelayed(HIDE_MEDIACONTROLLER, 5000);

        }
    }

    /**
     * 根据传入的值修改音量
     *
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


    private void setVideType(int type) {
        switch (type) {
            case FULL_SCREEN://全屏

                videoview.setVideoSize(screenWidth, screenHeight);
                isFullScreen = true;

                btnVideoSwitchScreen.setBackgroundResource(R.drawable.btn_switch_screen_default_selector);
                break;
            case DEFAULT_SCREEN://默认

                //真实视频的高和宽
                int mVideoWidth = videoWidth;
                int mVideoHeight = videoHeight;


                /**
                 * 要播放视频的宽和高
                 */

                int width = screenWidth;
                int height = screenHeight;
                if (mVideoWidth > 0 && mVideoHeight > 0) {
                    // for compatibility, we adjust size based on aspect ratio
                    if (mVideoWidth * height < width * mVideoHeight) {
                        //Log.i("@@@", "image too wide, correcting");
                        width = height * mVideoWidth / mVideoHeight;
                    } else if (mVideoWidth * height > width * mVideoHeight) {
                        //Log.i("@@@", "image too tall, correcting");
                        height = width * mVideoHeight / mVideoWidth;
                    }

                    videoview.setVideoSize(width, height);

                }

                isFullScreen = false;
                btnVideoSwitchScreen.setBackgroundResource(R.drawable.btn_switch_screen_full_selector);
                break;
        }
    }

    class VideoOnSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {

        /**
         * 当进度跟新的时候回调这个方法
         *
         * @param seekBar
         * @param progress 当前进度
         * @param fromUser 是否是由用于引起
         */
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                videoview.seekTo(progress);
            }

        }

        /**
         * 当手触碰SeekBar的时候回调这个方法
         *
         * @param seekBar
         */
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            handler.removeMessages(HIDE_MEDIACONTROLLER);
        }

        /**
         * 当手指离开SeeKbar的时候回调这个方法
         *
         * @param seekBar
         */
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            handler.sendEmptyMessageDelayed(HIDE_MEDIACONTROLLER, 5000);
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
        registerReceiver(receiver, intentFilter);

        //2.实例化手势识别器
        detector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public void onLongPress(MotionEvent e) {
                super.onLongPress(e);
//                Toast.makeText(SystemVideoPlayer.this, "我被长按了", Toast.LENGTH_SHORT).show();
                startAndPause();
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
//                Toast.makeText(SystemVideoPlayer.this, "我被双击了", Toast.LENGTH_SHORT).show();
                if (isFullScreen) {
                    setVideType(DEFAULT_SCREEN);
                } else {
                    setVideType(FULL_SCREEN);
                }
                return super.onDoubleTap(e);
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                if (isShowMediaController) {
                    //隐藏
                    hideMediaController();
                    handler.removeMessages(HIDE_MEDIACONTROLLER);
                } else {
                    //显示
                    showMediaController();
                    handler.sendEmptyMessageDelayed(HIDE_MEDIACONTROLLER, 5000);
                }
//                Toast.makeText(SystemVideoPlayer.this, "我被点击了", Toast.LENGTH_SHORT).show();
                return super.onSingleTapConfirmed(e);
            }
        });
    }

    class BatteryReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            int level = intent.getIntExtra("level", 0);//电量：0~100
            //主线程
            setBattery(level);
        }
    }

    private void setBattery(int level) {
        if (level <= 0) {
            iv_battery.setImageResource(R.drawable.ic_battery_0);
        } else if (level <= 10) {
            iv_battery.setImageResource(R.drawable.ic_battery_10);
        } else if (level <= 20) {
            iv_battery.setImageResource(R.drawable.ic_battery_20);
        } else if (level <= 40) {
            iv_battery.setImageResource(R.drawable.ic_battery_40);
        } else if (level <= 60) {
            iv_battery.setImageResource(R.drawable.ic_battery_60);
        } else if (level <= 80) {
            iv_battery.setImageResource(R.drawable.ic_battery_80);
        } else if (level <= 100) {
            iv_battery.setImageResource(R.drawable.ic_battery_100);
        } else {
            iv_battery.setImageResource(R.drawable.ic_battery_100);
        }
    }

    /**
     * 是否隐藏控制面板
     * true:显示
     * false:隐藏
     */
    private boolean isShowMediaController = false;

    private void hideMediaController() {
        llBottom.setVisibility(View.GONE);
        llTop.setVisibility(View.GONE);
        isShowMediaController = false;
    }


    private void showMediaController() {
        llBottom.setVisibility(View.VISIBLE);
        llTop.setVisibility(View.VISIBLE);
        isShowMediaController = true;
    }


    @Override
    protected void onStart() {
        super.onStart();
        LogUtil.e("onStart---");
    }

    @Override
    protected void onResume() {
        super.onResume();
        LogUtil.e("onResume---");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        LogUtil.e("onRestart---");
    }

    @Override
    protected void onPause() {
        super.onPause();
        LogUtil.e("onPause---");
    }

    @Override
    protected void onStop() {
        super.onStop();
        LogUtil.e("onStop---");
    }

    @Override
    protected void onDestroy() {

        if (receiver != null) {
            unregisterReceiver(receiver);
            receiver = null;
        }

        super.onDestroy();
        LogUtil.e("onDestroy---");


    }

    private float startY;

    private float touchRang;

    private int mVol;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //3.把事件给手势识别器解析
        detector.onTouchEvent(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //1.按下的时候记录初始值
                startY = event.getY();
                touchRang = Math.min(screenHeight, screenWidth);//screenHeight
                mVol = am.getStreamVolume(AudioManager.STREAM_MUSIC);
                handler.removeMessages(HIDE_MEDIACONTROLLER);
                break;
            case MotionEvent.ACTION_MOVE:
                //2.来到新的坐标
                float endY = event.getY();
                //3.计算偏移量
                float distanceY = startY - endY;
                //屏幕滑动的距离： 总距离 = 改变的声音： 最大音量
                float changVolume = (distanceY / touchRang) * maxVolume;

                //最终的声音= 原来的音量 + 改变的声音；
                float volume = Math.min(Math.max(mVol + changVolume, 0), maxVolume);

                if (changVolume != 0) {
                    updateVolumeProgress((int) volume);
                }

//                startY = event.getY();
                break;
            case MotionEvent.ACTION_UP:
                handler.sendEmptyMessageDelayed(HIDE_MEDIACONTROLLER, 5000);
                break;
        }
        return super.onTouchEvent(event);
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
}
