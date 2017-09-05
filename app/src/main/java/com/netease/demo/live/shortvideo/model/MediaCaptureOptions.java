package com.netease.demo.live.shortvideo.model;

import java.io.Serializable;

/**
 * Created by hzxuwen on 2017/4/6.
 */

public class MediaCaptureOptions implements Serializable {
    /**
     * 分辨率 长
     */
    public int mVideoPreviewWidth = 1280;
    /**
     * 分辨率 宽
     */
    public int mVideoPreviewHeight = 720;
    /**
     * 帧率
     */
    public int mVideoPreviewFrameRate = 30;
    /**
     * 默认摄像头，后置摄像头：1，前置摄像头：0
     */
    public int cameraPosition = 1;
    /**
     * 录制文件类型，MP4:1, FLV:0
     */
    public int mFileType = 1;
    /**
     *  录制文件路径
     */
    public String mFilePath = "/sdcard/media.mp4";
    /**
     * 码率
     */
    public int bitrate = 10000000;
}
