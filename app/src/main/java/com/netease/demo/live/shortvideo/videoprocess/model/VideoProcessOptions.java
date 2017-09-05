package com.netease.demo.live.shortvideo.videoprocess.model;

import com.netease.demo.live.shortvideo.model.MediaCaptureOptions;
import com.netease.transcoding.TranscodingAPI;

/**
 * Created by hzxuwen on 2017/4/10.
 */



public class VideoProcessOptions {
    /**
     * 输入文件相关参数
     */
    TranscodingAPI.InputFilePara inputFilePara;
    /**
     * 水印相关参数
     */
    TranscodingAPI.WaterMarkPara waterMarkPara;
    /**
     * 动态贴图相关参数
     */
    TranscodingAPI.ChartletPara chartletPara;
    /**
     * 裁剪坐标
     */
    TranscodingAPI.CropPara cropPara;
    /**
     * 文件截图，按照时长和offset
     */
    TranscodingAPI.FileCutPara fileCutPara;
    /**
     * 混音参数
     */
    TranscodingAPI.MixAudioPara mixAudioPara;
    /**
     * 转码信息：输出码率，是否硬件编码，回调函数
     */
    TranscodingAPI.TranscodePara transcodePara;
    /**
     * 输出文件地址
     */
    TranscodingAPI.OutputFilePara outputFilePara;
    /**
     *  色彩参数
     */
    TranscodingAPI.ColourAdjustPara colourAdjustPara;

    public VideoProcessOptions(MediaCaptureOptions mediaCaptureOptions) {
        this.inputFilePara = new TranscodingAPI.InputFilePara();
        this.waterMarkPara = new TranscodingAPI.WaterMarkPara();
        this.chartletPara = new TranscodingAPI.ChartletPara();
        this.cropPara = new TranscodingAPI.CropPara();
        this.fileCutPara = new TranscodingAPI.FileCutPara();
        this.mixAudioPara = new TranscodingAPI.MixAudioPara();
        this.transcodePara = new TranscodingAPI.TranscodePara();
        this.outputFilePara = new TranscodingAPI.OutputFilePara();
        this.colourAdjustPara = new TranscodingAPI.ColourAdjustPara();

        inputFilePara.setVideoEncodeHeight(mediaCaptureOptions.mVideoPreviewWidth);
        inputFilePara.setVideoEncodeWidth(mediaCaptureOptions.mVideoPreviewHeight);
        transcodePara.setOutBitrate(mediaCaptureOptions.bitrate);
    }

    public TranscodingAPI.InputFilePara getInputFilePara() {
        return inputFilePara;
    }

    public void setInputFilePara(TranscodingAPI.InputFilePara inputFilePara) {
        this.inputFilePara = inputFilePara;
    }

    public TranscodingAPI.WaterMarkPara getWaterMarkPara() {
        return waterMarkPara;
    }

    public void setWaterMarkPara(TranscodingAPI.WaterMarkPara waterMarkPara) {
        this.waterMarkPara = waterMarkPara;
    }

    public TranscodingAPI.ChartletPara getChartletPara() {
        return chartletPara;
    }

    public void setChartletPara(TranscodingAPI.ChartletPara chartletPara) {
        this.chartletPara = chartletPara;
    }

    public TranscodingAPI.CropPara getCropPara() {
        return cropPara;
    }

    public void setCropPara(TranscodingAPI.CropPara cropPara) {
        this.cropPara = cropPara;
    }

    public TranscodingAPI.FileCutPara getFileCutPara() {
        return fileCutPara;
    }

    public void setFileCutPara(TranscodingAPI.FileCutPara fileCutPara) {
        this.fileCutPara = fileCutPara;
    }

    public TranscodingAPI.MixAudioPara getMixAudioPara() {
        return mixAudioPara;
    }

    public void setMixAudioPara(TranscodingAPI.MixAudioPara mixAudioPara) {
        this.mixAudioPara = mixAudioPara;
    }

    public TranscodingAPI.TranscodePara getTranscodePara() {
        return transcodePara;
    }

    public void setTranscodePara(TranscodingAPI.TranscodePara transcodePara) {
        this.transcodePara = transcodePara;
    }

    public TranscodingAPI.OutputFilePara getOutputFilePara() {
        return outputFilePara;
    }

    public void setOutputFilePara(TranscodingAPI.OutputFilePara outputFilePara) {
        this.outputFilePara = outputFilePara;
    }

    public TranscodingAPI.ColourAdjustPara getColourAdjustPara() {
        return colourAdjustPara;
    }

    public void setColourAdjustPara(TranscodingAPI.ColourAdjustPara colourAdjustPara) {
        this.colourAdjustPara = colourAdjustPara;
    }
}
