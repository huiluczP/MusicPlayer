package com.demo.musicplayer;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.Image;
import android.media.MediaPlayer;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private CircleImageView circleimg;
    private ImageView startbtn;
    private SeekBar process;
    private TextView title;
    private TextView timenow;
    private TextView timeall;
    private ImageView resumebtn;
    private LinearLayout mainlayout;
    private ImageView repeat;

    Animation rotate;//动画对象
    Animation rotate2;
    Boolean on=false;//是否开始
    Boolean single=false;//是否循环

    private MediaPlayer musicplayer=new MediaPlayer();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        circleimg=(CircleImageView)findViewById(R.id.cirimg);
        startbtn=(ImageView)findViewById(R.id.start);
        resumebtn=(ImageView)findViewById(R.id.resume);
        process=(SeekBar)findViewById(R.id.process);
        timenow=(TextView)findViewById(R.id.timenow);
        timeall=(TextView)findViewById(R.id.timeall);
        title=(TextView)findViewById(R.id.title);
        repeat=(ImageView)findViewById(R.id.repeat);

        mainlayout=(LinearLayout)findViewById(R.id.mainlayout);
        mainlayout.getBackground().setAlpha(18);

        initAnim();
        startbtn.setOnClickListener(new startListener());
        resumebtn.setOnClickListener(new resumelistener());
        repeat.setOnClickListener(new repeatlistener());
        //判断sd卡读取权限
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
        }
        else{
            initmusicplayer();
        }
    }

    private void initAnim(){
        //设置动画对象
        rotate = AnimationUtils.loadAnimation(this, R.anim.pic_rotate);
        LinearInterpolator lin = new LinearInterpolator();//匀速效果
        rotate .setInterpolator(lin);//设置速率

        rotate2 = AnimationUtils.loadAnimation(this, R.anim.btn_rotate);
        rotate2.setInterpolator(lin);//设置速率
    }

    class startListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            //未开始，点击开启动画效果，设置为开始
            if(!on) {
                //转起来
                if (rotate != null) {
                    circleimg.startAnimation(rotate);
                } else {
                    circleimg.setAnimation(rotate);
                    circleimg.startAnimation(rotate);
                }
                //开启播放器
                musicplayer.start();
                new ProgressThread().start();
                process.setOnSeekBarChangeListener(new processListener());
                on=true;
                startbtn.setImageResource(R.drawable.pause01);
            }
            //已开始，关闭动画效果，设置为停止
            else{
                //暂停音乐播放器
                musicplayer.pause();
                //停一下
                circleimg.clearAnimation();
                on=false;
                startbtn.setImageResource(R.drawable.start01);
            }
        }
    }

    //重新播放，按钮变为start，状态变为false，重置进度条
    class resumelistener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            synchronized (this){
                musicplayer.seekTo(0);
                musicplayer.pause();
                process.setProgress(0);
            }
            startbtn.setImageResource(R.drawable.start01);
            on=false;
            circleimg.clearAnimation();
        }
    }

    //循环监听器，主要是修改一个全局变量
    class repeatlistener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
           if(!single){
               single=true;
               repeat.startAnimation(rotate2);
               musicplayer.setLooping(true);
           }
           else{
               single=false;
               repeat.clearAnimation();
               musicplayer.setLooping(false);
           }
        }
    }

    private void initmusicplayer() {
        File file=new File(Environment.getExternalStorageDirectory(),"亀岡夏海 - 暁の水平線に勝利を！.mp3");
        try {
            musicplayer.setDataSource(file.getPath());
            musicplayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //获取歌曲时间，初始化进度条最大值,初始化时间
        int max = musicplayer.getDuration();
        process.setMax(max);
        timeall.setText(processToTime(max));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,String[] permissions,int[] grantResults) {
        switch(requestCode){
            case 1:
                //用户同意权限
                if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    initmusicplayer();
                }
                //拒绝权限，直接关闭应用
                else{
                    Toast.makeText(this,"权限申请已拒绝",Toast.LENGTH_SHORT).show();
                    finish();
                }
        }
    }

    //为了实时修改时间，编写handler来修改ui界面
    Handler handle=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            String time=(String)msg.obj;
            timenow.setText(time);
        }
    };

    //该线程用来监控歌曲播放时间，并且实时反映到进度条
    class ProgressThread extends Thread {
        @Override
        public void run() {
            while(true){
                synchronized (this){
                    process.setProgress(musicplayer.getCurrentPosition());//永真循环获取当前歌曲播放时间反映到进度条
                    //使用handler和message来进行异步修改
                    Message msg=new Message();
                    msg.obj=processToTime(musicplayer.getCurrentPosition());
                    handle.sendMessage(msg);
                }
                try{
                    //每0.1秒执行一次防止占用过多内存
                    this.sleep(100);
                }catch(InterruptedException ex){
                    ex.printStackTrace();
                }
            }
        }
    }

    //seekbar的监听器，拖动seekbar来设置播放音乐位置
    class processListener implements SeekBar.OnSeekBarChangeListener {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            synchronized (this){
            }
        }
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            synchronized (this){
                //音乐播放到进度条位置(可能不太好，考虑放到stop)
                musicplayer.seekTo(seekBar.getProgress());
                //设置总时间
                timenow.setText(processToTime(seekBar.getProgress()));
            }
        }
    }

    //将毫秒转化为时间格式字符串
    private String processToTime(int process){
        String time=null;
        //小于一分钟
        if(process<60000){
            return "00"+":"+getStr(process/1000);
        }
        //大于一分钟，小于一小时
        else if((process>=60000)&&(process<3600000)){
            return getStr(process/60000)+":"+getStr((process % 60000 )/1000);
        }
        else {
          return "太长算了算了";
        }
    }

    String getStr(int process){
        String m="";
        if(process>0){
            if(process<10){
                m="0"+process;
            }else{
                m=process+"";
            }
        }
        else{
            m="00";
        }
        return m;
    }
}
