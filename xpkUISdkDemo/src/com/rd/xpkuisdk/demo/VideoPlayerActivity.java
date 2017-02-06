package com.rd.xpkuisdk.demo;

import java.util.Formatter;
import java.util.Locale;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.MediaController.MediaPlayerControl;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.rd.xpkuisdk.XpkVideoView;
import com.rd.xpkuisdk.XpkVideoView.XpkPlayerListener;

/**
 * 播放界面
 */
public class VideoPlayerActivity extends Activity {
    public static final String ACTION_PATH = "视频路径";

    private TextView m_tvVideoCurrentPos, m_tvVideoDuration;
    private SeekBar m_sbPlayControl;
    private ImageView m_ivVideoPlayState;
    private XpkVideoView m_xpkVideoView;
    private String path;
    private ProgressDialog m_dlgLoading;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);
        Intent in = getIntent();
        path = in.getStringExtra(ACTION_PATH);

        m_xpkVideoView = (XpkVideoView) findViewById(R.id.svpriview);
        m_xpkVideoView.setPlayerListener(playerListener);
        m_xpkVideoView.setVideoPath(path);
        m_ivVideoPlayState = (ImageView) findViewById(R.id.ivPlayerState);
        m_ivVideoPlayState.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (m_xpkVideoView.isPlaying()) {
                    pauseVideo();
                } else {
                    playVideo();
                }
            }
        });

        m_xpkVideoView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (m_xpkVideoView.isPlaying()) {
                    pauseVideo();
                } else {
                    playVideo();
                }
            }
        });

        m_tvVideoCurrentPos = (TextView) findViewById(R.id.tvEditorCurrentPos);
        m_tvVideoDuration = (TextView) findViewById(R.id.tvEditorDuration);

        findViewById(R.id.left).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        m_sbPlayControl = (SeekBar) findViewById(R.id.sbEditor);
        m_sbPlayControl.setOnSeekBarChangeListener(onSeekbarlistener);
        m_sbPlayControl.setMax(100);
        m_dlgLoading = ProgressDialog.show(this, null, "正在加载视频...");
    }

    private void prepare() {
        if (m_ivVideoPlayState.getVisibility() != View.VISIBLE) {
            m_ivVideoPlayState.setVisibility(View.VISIBLE);
        }
        onSeekTo(0);
        m_tvVideoCurrentPos.setText(getTimeString(0));
        m_tvVideoDuration.setText(getTimeString(m_xpkVideoView.getDuration()));
        m_sbPlayControl.setMax(m_xpkVideoView.getDuration());
        playVideo();
    }

    private String getTimeString(int progress) {
        return stringForTime(progress, false);
    }

    private void onSeekTo(int seekto) {
        m_xpkVideoView.seekTo(seekto);
    }

    private void onComplete() {
        m_sbPlayControl.setProgress(0);
        m_ivVideoPlayState.setImageResource(R.drawable.btn_play);
        m_ivVideoPlayState.setVisibility(View.VISIBLE);
        m_tvVideoCurrentPos.setText(getTimeString(0));
    }

    @Override
    public void onBackPressed() {
        if (null != m_xpkVideoView && m_xpkVideoView.isPlaying()) {
            pauseVideo();
        }
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (null != m_xpkVideoView) {
            pauseVideo();
        }
    }

    @Override
    protected void onDestroy() {
        if (null != m_xpkVideoView) {
            m_xpkVideoView.pause();
        }
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (null != m_xpkVideoView) {
            playVideo();
        }
    }

    private OnSeekBarChangeListener onSeekbarlistener = new OnSeekBarChangeListener() {
        private boolean m_bIsPlayingOnSeek; // Seek时是否播放中...

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress,
                                      boolean fromUser) {
            if (fromUser) {
                m_tvVideoCurrentPos.setText(getTimeString(progress));
                onSeekTo(progress);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            if (m_xpkVideoView.isPlaying()) {
                pauseVideo();
                m_bIsPlayingOnSeek = true;
            } else {
                m_bIsPlayingOnSeek = false;
            }
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            if (m_bIsPlayingOnSeek) {
                playVideo();
            }
        }
    };

    private void playVideo() {
        m_xpkVideoView.start();
        m_ivVideoPlayState.setImageResource(R.drawable.btn_pause);
        Animation an = AnimationUtils.loadAnimation(getApplicationContext(),
                android.R.anim.fade_out);
        m_ivVideoPlayState.setAnimation(an);
        m_ivVideoPlayState.setVisibility(View.INVISIBLE);
    }

    private void pauseVideo() {
        if (m_xpkVideoView.isPlaying()) {
            m_xpkVideoView.pause();
        }
        m_ivVideoPlayState.setImageResource(R.drawable.btn_play);
        m_ivVideoPlayState.setVisibility(View.VISIBLE);
    }

    private XpkPlayerListener playerListener = new XpkPlayerListener() {

        @Override
        public void onPlayerPrepare(MediaPlayerControl mediaPlayerControl) {
            prepare();
            m_dlgLoading.dismiss();
        }

        @Override
        public boolean onPlayerError(MediaPlayerControl mediaPlayerControl,
                                     int what, int extra) {
            m_dlgLoading.dismiss();
            Toast.makeText(VideoPlayerActivity.this,
                    getString(android.R.string.VideoView_error_text_unknown),
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        @Override
        public void onPlayerCompletion(MediaPlayerControl mediaPlayerControl) {
            onComplete();
        }

        @Override
        public void onGetCurrentPosition(MediaPlayerControl mediaPlayerControl,
                                         int nPosition) {
            m_sbPlayControl.setProgress(nPosition);
            m_tvVideoCurrentPos.setText(getTimeString(nPosition));
        }
    };

    private static final StringBuilder m_sbFormator = new StringBuilder();
    private static final Formatter m_formatter = new Formatter(m_sbFormator,
            Locale.getDefault());

    /**
     * 毫秒数转换为时间格式化字符串
     *
     * @param timeMs
     * @return
     */
    public static String stringForTime(long timeMs) {
        return stringForTime(timeMs, false);
    }

    /**
     * 毫秒数转换为时间格式化字符串 支持是否显示小时
     *
     * @param timeMs
     * @return
     */
    public static String stringForTime(long timeMs, boolean existsHours) {
        boolean bNegative = timeMs < 0;// 是否为负数
        if (bNegative) {
            timeMs = -timeMs;
        }
        int totalSeconds = (int) (timeMs / 1000);

        int seconds = totalSeconds % 60;
        int minutes = (totalSeconds / 60) % 60;
        int hours = totalSeconds / 3600;

        m_sbFormator.setLength(0);
        try {
            if (hours > 0 || existsHours) {
                return m_formatter.format("%s%02d:%02d:%02d",
                        bNegative ? "-" : "", hours, minutes, seconds)
                        .toString();
            } else {
                return m_formatter.format("%s%02d:%02d", bNegative ? "- " : "",
                        minutes, seconds).toString();
            }
        } catch (Exception ex) {
            return "";
        }
    }
}
