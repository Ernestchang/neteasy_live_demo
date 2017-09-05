package com.netease.demo.live.shortvideo;

import android.content.Context;
import android.media.AudioFormat;

import com.netease.LSMediaRecord.lsLogUtil;
import com.netease.LSMediaRecord.lsMediaCapture;
import com.netease.LSMediaRecord.lsMessageHandler;
import com.netease.demo.live.shortvideo.model.MediaCaptureOptions;

import static com.netease.demo.live.liveStreaming.MediaCaptureWrapper.HAVE_AV;
import static com.netease.demo.live.liveStreaming.MediaCaptureWrapper.LS_AUDIO_CODEC_AAC;
import static com.netease.demo.live.liveStreaming.MediaCaptureWrapper.LS_VIDEO_CODEC_AVC;

/**
 * Created by hzxuwen on 2017/4/6.
 */

public class MediaCaptureWrapper {

    // 初始化参数
    private lsMediaCapture mLSMediaCapture;
    private lsMediaCapture.LSRecordParaCtx mLSLiveStreamingParaCtx = null;
    private MediaCaptureOptions mediaCaptureOptions;

    public MediaCaptureWrapper(Context context, lsMessageHandler messageHandler, MediaCaptureOptions mediaCaptureOptions) {
        this.mediaCaptureOptions = mediaCaptureOptions;
        //1、创建录制实例
        initMediaCapture(context, messageHandler);
        //2、设置录制参数
        paraSet();
    }

    private void initMediaCapture(Context context, lsMessageHandler messageHandler) {
        lsMediaCapture.LsMediaCapturePara lsMediaCapturePara = new lsMediaCapture.LsMediaCapturePara();
        lsMediaCapturePara.Context = context.getApplicationContext();
        lsMediaCapturePara.lsMessageHandler = messageHandler;
        lsMediaCapturePara.videoPreviewWidth = mediaCaptureOptions.mVideoPreviewWidth;
        lsMediaCapturePara.videoPreviewHeight = mediaCaptureOptions.mVideoPreviewHeight;
        lsMediaCapturePara.videoPreviewFrameRate = mediaCaptureOptions.mVideoPreviewFrameRate;
        lsMediaCapturePara.logLevel = lsLogUtil.LogLevel.INFO;
        mLSMediaCapture = new lsMediaCapture(lsMediaCapturePara);
    }

    //音视频参数设置
    public void paraSet() {
        //创建参数实例
        mLSLiveStreamingParaCtx = mLSMediaCapture.new LSRecordParaCtx();
        mLSLiveStreamingParaCtx.eOutFormatType = mLSLiveStreamingParaCtx.new OutputFormatType();
        mLSLiveStreamingParaCtx.eOutStreamType = mLSLiveStreamingParaCtx.new OutputStreamType();
        mLSLiveStreamingParaCtx.sLSAudioParaCtx = mLSLiveStreamingParaCtx.new LSAudioParaCtx();
        mLSLiveStreamingParaCtx.sLSAudioParaCtx.codec = mLSLiveStreamingParaCtx.sLSAudioParaCtx.new LSAudioCodecType();
        mLSLiveStreamingParaCtx.sLSVideoParaCtx = mLSLiveStreamingParaCtx.new LSVideoParaCtx();
        mLSLiveStreamingParaCtx.sLSVideoParaCtx.codec = mLSLiveStreamingParaCtx.sLSVideoParaCtx.new LSVideoCodecType();
        mLSLiveStreamingParaCtx.sLSVideoParaCtx.cameraPosition = mLSLiveStreamingParaCtx.sLSVideoParaCtx.new CameraPosition();
        mLSLiveStreamingParaCtx.sLSVideoParaCtx.interfaceOrientation = mLSLiveStreamingParaCtx.sLSVideoParaCtx.new CameraOrientation();

        //设置摄像头信息，并开始本地视频预览
        mLSLiveStreamingParaCtx.sLSVideoParaCtx.cameraPosition.cameraPosition = mediaCaptureOptions.cameraPosition;//默认后置摄像头，用户可以根据需要调整

        //输出格式：视频、音频和音视频
        mLSLiveStreamingParaCtx.eOutStreamType.outputStreamType = HAVE_AV;//默认音视频推流

        //输出封装格式
        mLSLiveStreamingParaCtx.eOutFormatType.outputFormatType = mediaCaptureOptions.mFileType; //录制文件封装格式

        //输出录制文件的路径和名称
        mLSLiveStreamingParaCtx.eOutFormatType.outputFormatFileName = mediaCaptureOptions.mFilePath; //录制文件路径

        //音频编码参数配置
        mLSLiveStreamingParaCtx.sLSAudioParaCtx.samplerate = 44100;
        mLSLiveStreamingParaCtx.sLSAudioParaCtx.bitrate = 64000;
        mLSLiveStreamingParaCtx.sLSAudioParaCtx.frameSize = 2048;
        mLSLiveStreamingParaCtx.sLSAudioParaCtx.audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
        mLSLiveStreamingParaCtx.sLSAudioParaCtx.channelConfig = AudioFormat.CHANNEL_IN_MONO;
        mLSLiveStreamingParaCtx.sLSAudioParaCtx.codec.audioCODECType = LS_AUDIO_CODEC_AAC;

        //如下是编码分辨率等信息的设置
        mLSLiveStreamingParaCtx.sLSVideoParaCtx.fps = 30;//20;
        mLSLiveStreamingParaCtx.sLSVideoParaCtx.bitrate = mediaCaptureOptions.bitrate;//1500000;
        mLSLiveStreamingParaCtx.sLSVideoParaCtx.codec.videoCODECType = LS_VIDEO_CODEC_AVC;
        mLSLiveStreamingParaCtx.sLSVideoParaCtx.width = mediaCaptureOptions.mVideoPreviewWidth;
        mLSLiveStreamingParaCtx.sLSVideoParaCtx.height = mediaCaptureOptions.mVideoPreviewHeight;
    }

    public lsMediaCapture getmLSMediaCapture() {
        return mLSMediaCapture;
    }

    public lsMediaCapture.LSRecordParaCtx getmLSLiveStreamingParaCtx() {
        return mLSLiveStreamingParaCtx;
    }
}
