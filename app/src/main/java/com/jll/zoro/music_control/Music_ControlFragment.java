package com.jll.zoro.music_control;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @Author : Zoro.
 * @Date : 2017/4/6.
 * @Describe :
 */

public class Music_ControlFragment extends Fragment implements View.OnClickListener,
        Music_ControlAdapter.showMusic_Control, Music_ControlAdapter.startMusic_Control,
        Music_ControlAdapter.pauseMusic_Control, Music_ControlAdapter.restartMusic_Control {
    private static int CURRENT_POSITION = 0;
    private static final int MSG_BUTTOM = 2;
    private static final int MSG_TOP = 1;
    private static final int MSG_TIMER = 3;

    private float oldY = 0, newY = 0;
    private int lastVisibleItemPosition = -1;
    private View view;
    private BaseListView listView;
    private TextView customer_Nnumber;
    private EditText find_name;
    private ImageView clear_find;
    private LinearLayout music_Control;
    private TranslateAnimation mShowAction;
    private TranslateAnimation mHiddenAction;
    //底部控制器
    private TextView current_Timer, total_Timer;
    private SeekBar seekBar;
    private ImageView last_Music, next_Music, start_Music;
    private boolean isChanging = false;//互斥变量，防止定时器与SeekBar拖动时进度冲突
    int min = 0, sec = 0;
    private Timer mTimer = new Timer(); // 计时器
    private int position=0;//媒体音乐所播放的位置

    private ProgressDialog dialog;
    private SwipeRefreshLayout refresh;
    private boolean isShow = false;//是否已经显示
    private boolean isHide = false;//是否已经隐藏
    private boolean show_Click = false;
    private List<Boolean> list = new ArrayList<>();
    private Music_ControlAdapter adapter;
    private boolean isScrolledToTop = false;
    private boolean isScrolledToBottom = false;
    private MediaPlayer mp = new MediaPlayer();       //媒体播放器对象
    private boolean start_Move = false;         //当第一次进来没有选定语音的时候或者刷新加载后都没有曲目了就滑动不出现底部控制器
    private boolean click_Show = false;         //当第一次进入或者刷新加载后的第一次点击 item，底部控制器会有出现动画，不然就没有,不控制的话每次点击都会有动画看起来很难受
    private TimerTask timerTask = new TimerTask() {

        @Override
        public void run() {
            //不用担心一直发送，这边处理了只有在播放状态中或者没有在拉seekBar的时候才会发送
            if (mp == null)
                return;
            if (mp.isPlaying() && seekBar.isPressed() == false) {
                handler.sendEmptyMessage(MSG_TIMER); // 发送消息
            }
        }
    };
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_TOP:
                    isScrolledToTop = false;
                    list.clear();
                    for (int i = 1; i < 10; i++) {
                        list.add(false);
                    }
                    adapter.notify(999999999, true);
                    adapter.changeState();
                    stopMediaplayer();
                    music_Control.setVisibility(View.GONE);
                    start_Move = false;
                    click_Show = false;
                    refresh.setRefreshing(false);
                    customer_Nnumber.setText("报名总数(人)：" + list.size());
                    break;
                case MSG_BUTTOM:
                    music_Control.setVisibility(View.GONE);
                    isScrolledToBottom = false;
                    for (int i = 20; i < 40; i++) {
                        list.add(false);
                    }
                    adapter.notify(999999999, true);
                    adapter.changeState();
                    stopMediaplayer();
                    start_Move = false;
                    click_Show = false;
                    customer_Nnumber.setText("报名总数(人)：" + list.size());
                    break;
                case MSG_TIMER:
                    if(null != mp){
                        int position = mp.getCurrentPosition();
                        int duration = mp.getDuration();

                        if (duration > 0) {
                            // 计算进度（获取进度条最大刻度*当前音乐播放位置 / 当前音乐时长）
                            int pos = seekBar.getMax() * position / duration;
                            seekBar.setProgress(pos);
                            min = position / 60000;
                            sec = (position / 1000) % 60;
                            if (min < 10 && sec < 10) {
                                current_Timer.setText("0" + min + ":0" + sec);
                            } else if (min < 10 && sec >= 10) {
                                current_Timer.setText("0" + min + ":" + sec);
                            } else if (min >= 10 && sec >= 10) {
                                current_Timer.setText(min + ":" + sec);
                            } else {
                                current_Timer.setText(min + ":0" + sec);
                            }
                        }
                    }else{
                     //不处理
                    }
                    break;
            }
            dialog.stop();
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_music_control, null);
        initView();
        initData();
        return view;
    }

    private void initData() {
        for (int i = 0; i < 30; i++) {
            list.add(false);
        }
        adapter = new Music_ControlAdapter(getActivity(), list, this, this, this, this);
        TextView textView = new TextView(getActivity());
        textView.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 80));
        listView.addFooterView(textView);
        listView.setAdapter(adapter);
    }

    private void initView() {
        dialog = new ProgressDialog(getActivity());
        listView = (BaseListView) view.findViewById(R.id.recording_listView);
        customer_Nnumber = (TextView) view.findViewById(R.id.customer_Number);
        find_name = (EditText) view.findViewById(R.id.find_name);
        clear_find = (ImageView) view.findViewById(R.id.clear_find);
        music_Control = (LinearLayout) view.findViewById(R.id.music_Control);
        music_Control.getBackground().setAlpha(230);
        last_Music = (ImageView) view.findViewById(R.id.last_Music);
        next_Music = (ImageView) view.findViewById(R.id.next_Music);
        start_Music = (ImageView) view.findViewById(R.id.start_Music);
        refresh = (SwipeRefreshLayout) view.findViewById(R.id.refresh);
        current_Timer = (TextView) view.findViewById(R.id.current_Time);
        total_Timer = (TextView) view.findViewById(R.id.total_Time);
        seekBar = (SeekBar) view.findViewById(R.id.seekbar);
        mShowAction = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
                1.0f, Animation.RELATIVE_TO_SELF, 0.0f);
        mShowAction.setDuration(100);
        mHiddenAction = new TranslateAnimation(Animation.RELATIVE_TO_SELF,
                0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
                1.0f);
        mHiddenAction.setDuration(100);
        mTimer.schedule(timerTask, 0, 1000);
        initListener();
    }

    private void initListener() {
        seekBar.setOnSeekBarChangeListener(new MySeekbar());
        listView.setOnScrollListener(new AbsListView.OnScrollListener()

        {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                // 当不滚动时
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                    // 判断是否滚动到底部
                    if (view.getLastVisiblePosition() == view.getCount() - 1) {
                        dialog.start();
                        handler.sendEmptyMessageDelayed(MSG_BUTTOM, 2000);
                    }
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                                 int totalItemCount) {
            }

        });
        listView.setOnTouchListener(new View.OnTouchListener()

        {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // 触摸按下时的操作
                        oldY = event.getY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        break;
                    case MotionEvent.ACTION_UP:
                        // 触摸抬起时的操作
                        newY = event.getY();
                        break;
                }
                if ((newY - oldY) > 3) {
                    if (!isShow && start_Move) {
                        music_Control.startAnimation(mShowAction);
                        music_Control.setVisibility(View.VISIBLE);
                        isShow = true;
                        isHide = false;
                        click_Show = true;
                    }
                } else if ((newY - oldY) < -3) {
                    if (!isHide && start_Move) {
                        music_Control.startAnimation(mHiddenAction);
                        music_Control.setVisibility(View.GONE);
                        isShow = false;
                        isHide = true;
                        click_Show = false;
                    }
                }
                return false;
            }
        });
        last_Music.setOnClickListener(this);
        next_Music.setOnClickListener(this);
        start_Music.setOnClickListener(this);
        refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener()

        {
            @Override
            public void onRefresh() {
                refresh.setRefreshing(true);
                dialog.start();
                handler.sendEmptyMessageDelayed(MSG_TOP, 2000);
            }
        });
    }

    //暂停音乐

    public void pauseMediaplayer(int i) {
        if (mp != null) {
            mp.pause();
        }
    }

    //停止音乐
    public void stopMediaplayer() {
        if (mp != null) {
            mp.stop();
        }
    }

    //继续音乐
    public void restartMediaplayer(int index) {
        if (mp != null) {
            mp.start();
        }
    }

    //开始音乐
    public void startMediaplayer(int i) {
        try {
            mp.reset();
            if (i % 2 == 0) {
                mp.setDataSource("http://183.131.55.11/mp3.9ku.com/m4a/88340.m4a");
            } else {
                mp.setDataSource("http://115.231.38.140/mp3.9ku.com/m4a/637791.m4a");
            }
            mp.prepareAsync();
            mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    // 装载完毕回调
                    mp.start();
                    int total_Time = mp.getDuration() / 1000;
                    seekBar.setProgress(0);
                    current_Timer.setText("00:00");
                    min = total_Time / 60;
                    sec = total_Time % 60;
                    if (min < 10 && sec < 10) {
                        total_Timer.setText("0" + min + ":0" + sec);
                    } else if (min < 10 && sec >= 10) {
                        total_Timer.setText("0" + min + ":" + sec);
                    } else if (min >= 10 && sec >= 10) {
                        total_Timer.setText(min + ":" + sec);
                    } else {
                        total_Timer.setText(min + ":0" + sec);
                    }
                    // 每一秒触发一次
                }
            });
            mp.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
                @Override
                public void onSeekComplete(MediaPlayer mp) {
                    mp.start();
                }
            });
            mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mp.stop();
                    //每段语音播放完更换所有的播放状态
                    adapter.notify(999999999, false);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.last_Music:
                //处于第一个时候特殊处理
                if (CURRENT_POSITION == 0) {
                    CURRENT_POSITION = list.size() - 1;
                } else {
                    CURRENT_POSITION--;
                }
                listView.setSelection(CURRENT_POSITION);
                adapter.notify(CURRENT_POSITION, true);
                break;
            case R.id.next_Music:
                //处于最后一个时候特殊处理
                if (CURRENT_POSITION == list.size() - 1) {
                    CURRENT_POSITION = 0;
                } else {
                    CURRENT_POSITION++;
                }
                listView.setSelection(CURRENT_POSITION);
                adapter.notify(CURRENT_POSITION, true);
                break;
            case R.id.start_Music:
                if (start_Music.getDrawable().getCurrent().getConstantState().equals(ContextCompat.getDrawable(getActivity(), R.drawable.control_start).getConstantState())) {
                    start_Music.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.control_stop));
                    pauseMediaplayer(1);
                    //点击底部控制器更改item的状态
                    list.set(CURRENT_POSITION, false);
                    adapter.notify(CURRENT_POSITION, false);
                } else {
                    start_Music.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.control_start));
                    restartMediaplayer(1);
                    list.set(CURRENT_POSITION, true);
                    adapter.notify(CURRENT_POSITION, false);
                }
                break;
        }
    }

    //是否要显示底部控制器
    public void showControl() {
        music_Control.startAnimation(mShowAction);
        music_Control.setVisibility(View.VISIBLE);
        isShow = true;
        isHide = false;
        show_Click = true;
    }

    public void hideControl() {
        music_Control.startAnimation(mHiddenAction);
        music_Control.setVisibility(View.GONE);
        isShow = false;
        isHide = true;
        show_Click = false;
    }

    //进度条处理
    class MySeekbar implements SeekBar.OnSeekBarChangeListener {
        public void onProgressChanged(SeekBar seekBar, int progress,
                                      boolean fromUser) {
            position=progress;
            mp.start();
        }

        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        public void onStopTrackingTouch(SeekBar seekBar) {
            mp.seekTo((int)(((double)position/100)*mp.getDuration()));
        }

    }

    //点击了item以后顺便更改底部控制器的状态
    private void change_MusicState(int state) {
        start_Music.setImageDrawable(ContextCompat.getDrawable(getActivity(), state));
    }

    @Override
    public void show(int index) {
        start_Move = true;
        //当click_Show为true的时候，点击就不会再次显示底部控制器了，不然看起来很难受
        if (!click_Show) {
            showControl();
            click_Show = true;
        } else {
            if (!show_Click) {
                showControl();
            }
        }
        CURRENT_POSITION = index;
        adapter.notify(index, true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null != mTimer) {
            mTimer.cancel();
        }
        if (null != mp) {
            mp.stop();
            mp.release();
        }
        if (null != timerTask) {
            timerTask.cancel(); //将原任务从队列中移除
        }
    }

    @Override
    public void start(int index) {
        startMediaplayer(index);
        change_MusicState(R.drawable.control_start);
    }

    @Override
    public void pause(int index) {
        pauseMediaplayer(index);
        change_MusicState(R.drawable.control_stop);
    }

    @Override
    public void restart(int index) {
        restartMediaplayer(index);
        change_MusicState(R.drawable.control_start);
    }
}
