package com.ybj366533.videoplayer.player;

import android.content.ContentResolver;
import android.content.Context;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.Surface;

import com.ybj366533.videoplayer.model.VideoModel;
import com.ybj366533.videoplayer.model.VideoOptionModel;
import com.ybj366533.videoplayer.utils.Debuger;
import com.ybj366533.videoplayer.utils.RawDataSourceProvider;
import com.ybj366533.videoplayer.utils.VideoType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkLibLoader;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

/**
 * IJKPLayer
 */

public class IJKPlayerManager implements IPlayerManager {

    //log level
    private static int logLevel = IjkMediaPlayer.IJK_LOG_DEFAULT;

    private static IjkLibLoader ijkLibLoader;

    private IjkMediaPlayer mediaPlayer;

    private List<VideoOptionModel> optionModelList;

    private Surface surface;


    @Override
    public IMediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    @Override
    public void initVideoPlayer(Context context, Message msg, List<VideoOptionModel> optionModelList) {
        mediaPlayer = (ijkLibLoader == null) ? new IjkMediaPlayer() : new IjkMediaPlayer(ijkLibLoader);
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnNativeInvokeListener(new IjkMediaPlayer.OnNativeInvokeListener() {
            @Override
            public boolean onNativeInvoke(int i, Bundle bundle) {
                return true;
            }
        });
        try {
            //开启硬解码
            if (VideoType.isMediaCodec()) {
                Debuger.printfLog("enable mediaCodec");
                mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", 1);
                mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-auto-rotate", 1);
                mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-handle-resolution-change", 1);
            }
            String url = ((VideoModel) msg.obj).getUrl();
            if(!TextUtils.isEmpty(url)) {
                Uri uri = Uri.parse(url);
                if(uri.getScheme().equals(ContentResolver.SCHEME_ANDROID_RESOURCE)){
                    RawDataSourceProvider rawDataSourceProvider = RawDataSourceProvider.create(context, uri);
                    mediaPlayer.setDataSource(rawDataSourceProvider);
                } else {
                    mediaPlayer.setDataSource(url, ((VideoModel) msg.obj).getMapHeadData());
                }
            } else {
                mediaPlayer.setDataSource(url, ((VideoModel) msg.obj).getMapHeadData());
            }
            mediaPlayer.setLooping(((VideoModel) msg.obj).isLooping());
            if (((VideoModel) msg.obj).getSpeed() != 1 && ((VideoModel) msg.obj).getSpeed() > 0) {
                mediaPlayer.setSpeed(((VideoModel) msg.obj).getSpeed());
            }
            mediaPlayer.native_setLogLevel(logLevel);
            initIJKOption(mediaPlayer, optionModelList);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void showDisplay(Message msg) {
        if (msg.obj == null && mediaPlayer != null) {
            mediaPlayer.setSurface(null);
        } else {
            Surface holder = (Surface) msg.obj;
            surface = holder;
            if (mediaPlayer != null && holder.isValid()) {
                mediaPlayer.setSurface(holder);
            }
        }
    }

    @Override
    public void setSpeed(float speed, boolean soundTouch) {
        if (speed > 0) {
            try {
                if(mediaPlayer != null) {
                    mediaPlayer.setSpeed(speed);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (soundTouch) {
                VideoOptionModel videoOptionModel =
                        new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "soundtouch", 1);
                List<VideoOptionModel> list = getOptionModelList();
                if (list != null) {
                    list.add(videoOptionModel);
                } else {
                    list = new ArrayList<>();
                    list.add(videoOptionModel);
                }
                setOptionModelList(list);
            }

        }
    }

    @Override
    public void setNeedMute(boolean needMute) {
        if(mediaPlayer != null) {
            if (needMute) {
                mediaPlayer.setVolume(0, 0);
            } else {
                mediaPlayer.setVolume(1, 1);
            }
        }
    }


    @Override
    public void releaseSurface() {
        if (surface != null) {
            surface.release();
            surface = null;
        }
    }

    @Override
    public void release() {
        if(mediaPlayer != null) {
            mediaPlayer.release();
        }
    }

    private void initIJKOption(IjkMediaPlayer ijkMediaPlayer, List<VideoOptionModel> optionModelList) {
        if (optionModelList != null && optionModelList.size() > 0) {
            for (VideoOptionModel videoOptionModel : optionModelList) {
                if (videoOptionModel.getValueType() == VideoOptionModel.VALUE_TYPE_INT) {
                    ijkMediaPlayer.setOption(videoOptionModel.getCategory(),
                            videoOptionModel.getName(), videoOptionModel.getValueInt());
                } else {
                    ijkMediaPlayer.setOption(videoOptionModel.getCategory(),
                            videoOptionModel.getName(), videoOptionModel.getValueString());
                }
            }
        }
    }

    public List<VideoOptionModel> getOptionModelList() {
        return optionModelList;
    }

    public void setOptionModelList(List<VideoOptionModel> optionModelList) {
        this.optionModelList = optionModelList;
    }

    public static IjkLibLoader getIjkLibLoader() {
        return ijkLibLoader;
    }

    public static void setIjkLibLoader(IjkLibLoader ijkLibLoader) {
        IJKPlayerManager.ijkLibLoader = ijkLibLoader;
    }

    public static int getLogLevel() {
        return logLevel;
    }

    public static void setLogLevel(int logLevel) {
        IJKPlayerManager.logLevel = logLevel;
    }
}