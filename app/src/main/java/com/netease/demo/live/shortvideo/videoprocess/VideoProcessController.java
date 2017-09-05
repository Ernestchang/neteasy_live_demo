package com.netease.demo.live.shortvideo.videoprocess;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;

import com.netease.demo.live.server.DemoServerHttpClient;
import com.netease.demo.live.shortvideo.videoprocess.model.VideoProcessOptions;
import com.netease.nim.uikit.common.util.log.LogUtil;
import com.netease.transcoding.TranscodingAPI;
import com.netease.transcoding.TranscodingNative;

import java.lang.ref.WeakReference;

import static com.netease.transcoding.TranscodingAPI.LSMediaTrascodingErrCode_AudioVolumeAdjustParamError;
import static com.netease.transcoding.TranscodingAPI.LSMediaTrascodingErrCode_WaterMarkTimeTooLongParamError;

/**
 * 视频处理管理
 * Created by hzxuwen on 2017/4/10.
 */

public class VideoProcessController {
    private static final String TAG = VideoProcessController.class.getSimpleName();

    public interface VideoProcessCallback {
        void onVideoProcessSuccess();

        void onVideoProcessFailed(int code);

        void onVideoSnapshotSuccess(String path);

        void onVideoSnapshotFailed(int code);

        void onVideoProcessUpdate(int process, int total);
    }

    private Context context;
    private AsyncTask videoProcessTask;
    private AsyncTask snapshotTask;
    private static VideoProcessCallback callback;

    public VideoProcessController(Context context, VideoProcessCallback callback) {
        this.context = context;
        this.callback = callback;
        TranscodingAPI.getInstance().init(context, DemoServerHttpClient.readAppKey());
    }

    /**
     * 视频处理
     *
     * @param videoProcessOptions 视频处理参数
     */
    public void startCombination(VideoProcessOptions videoProcessOptions) {
        videoProcessTask = new CombinationAsyncTask((Activity) context)
                .execute(videoProcessOptions.getInputFilePara(),
                        videoProcessOptions.getWaterMarkPara(),
                        videoProcessOptions.getChartletPara(),
                        videoProcessOptions.getCropPara(),
                        videoProcessOptions.getFileCutPara(),
                        videoProcessOptions.getMixAudioPara(),
                        videoProcessOptions.getColourAdjustPara(),
                        videoProcessOptions.getTranscodePara(),
                        videoProcessOptions.getOutputFilePara());

    }

    /**
     * 视频截图
     *
     * @param para 截图参数
     */
    public void startSnapShot(final TranscodingAPI.SnapshotPara para) {
        snapshotTask = new SnapShotAsyncTask((Activity) context, para).execute(para);
    }

    private static class CombinationAsyncTask extends AsyncTask<Object, Integer, Integer> {
        private WeakReference<Activity> activityWeakReference;

        public CombinationAsyncTask(Activity activity) {
            activityWeakReference = new WeakReference<>(activity);
        }

        @Override
        protected Integer doInBackground(Object... params) {
            final TranscodingAPI.InputFilePara inputFilePara = (TranscodingAPI.InputFilePara) params[0];
            final TranscodingAPI.WaterMarkPara waterMarkPara = (TranscodingAPI.WaterMarkPara) params[1];
            final TranscodingAPI.ChartletPara chartletPara = (TranscodingAPI.ChartletPara) params[2];
            final TranscodingAPI.CropPara cropPara = (TranscodingAPI.CropPara) params[3];
            final TranscodingAPI.FileCutPara fileCutPara = (TranscodingAPI.FileCutPara) params[4];
            final TranscodingAPI.MixAudioPara mixAudioPara = (TranscodingAPI.MixAudioPara) params[5];
            final TranscodingAPI.ColourAdjustPara colourAdjustPara = (TranscodingAPI.ColourAdjustPara) params[6];
            final TranscodingAPI.TranscodePara transcodePara = (TranscodingAPI.TranscodePara) params[7];
            final TranscodingAPI.OutputFilePara outputFilePara = (TranscodingAPI.OutputFilePara) params[8];
            final TranscodingAPI.ScalePara scalePara = new TranscodingAPI.ScalePara();
            transcodePara.setCallBack(new TranscodingNative.NativeCallBack() {
                @Override
                public void progress(int progress, int total) {
                    publishProgress(progress, total);
                }
            });
            return TranscodingAPI.getInstance().VODProcess(inputFilePara,
                    waterMarkPara, chartletPara,
                    cropPara, scalePara, fileCutPara,
                    mixAudioPara, colourAdjustPara,
                    transcodePara, outputFilePara,
                    activityWeakReference.get().getApplicationContext());
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onCancelled() {
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            if (values[0] <= values[1]) {
                callback.onVideoProcessUpdate(values[0], values[1]);
            }
        }

        @Override
        protected void onPostExecute(Integer ret) {
            LogUtil.e(TAG, "短视频处理：" + ret);
            if (ret == TranscodingAPI.LSMediaTrascodingErrCode_InputFileParseError) {
                LogUtil.e(TAG, "短视频处理失败，解析输入文件失败");
            } else if (ret == TranscodingAPI.LSMediaTrascodingErrCode_InputFileParamError) {
                LogUtil.e(TAG, "短视频处理失败，输入文件参数出错");
            } else if (ret == TranscodingAPI.LSMediaTrascodingErrCode_MultiInputFileFadeTimeParamError) {
                LogUtil.e(TAG, "短视频处理失败，多输入文件时淡入淡出参数出错");
            } else if (ret == TranscodingAPI.LSMediaTrascodingErrCode_CutFileTimeParamError) {
                LogUtil.e(TAG, "短视频处理失败，裁剪文件时长参数出错");
            } else if (ret == TranscodingAPI.LSMediaTrascodingErrCode_MixAudioVolumeParamError) {
                LogUtil.e(TAG, "短视频处理失败，混音音量参数出错");
            } else if (ret == TranscodingAPI.LSMediaTrascodingErrCode_MixAudioFadeTimeParamError) {
                LogUtil.e(TAG, "短视频处理失败，混音淡入淡出参数出错");
            } else if (ret == TranscodingAPI.LSMediaTrascodingErrCode_CropSizeTooLargeParamError) {
                LogUtil.e(TAG, "短视频处理失败，裁剪的尺寸和位置参数出错");
            } else if (ret == TranscodingAPI.LSMediaTrascodingErrCode_WaterMarkSizeTooLargeOrSmallParamError) {
                LogUtil.e(TAG, "短视频处理失败，水印位置和尺寸参数出错");
            } else if (ret == LSMediaTrascodingErrCode_WaterMarkTimeTooLongParamError) {
                LogUtil.e(TAG, "短视频处理失败，水印显示时间不在文件时长范围内");
            } else if (ret == LSMediaTrascodingErrCode_AudioVolumeAdjustParamError) {
                LogUtil.e(TAG, "短视频处理失败，视频音量调节参数出错");
            } else if (ret == TranscodingAPI.LSMediaTrascodingErrCode_NO) {
                LogUtil.e(TAG, "短视频处理结束");
                callback.onVideoProcessSuccess();
                return;
            }
            callback.onVideoProcessFailed(ret);
        }
    }

    private static class SnapShotAsyncTask extends AsyncTask<TranscodingAPI.SnapshotPara, Integer, Integer> {
        private WeakReference<Activity> activityWeakReference;
        private TranscodingAPI.SnapshotPara para;

        public SnapShotAsyncTask(Activity activity, TranscodingAPI.SnapshotPara para) {
            activityWeakReference = new WeakReference<>(activity);
            this.para = para;
        }


        @Override
        protected Integer doInBackground(TranscodingAPI.SnapshotPara... params) {
            final TranscodingAPI.SnapshotPara snapshotPara = params[0];
            snapshotPara.setCallBack(new TranscodingNative.NativeCallBack() {
                @Override
                public void progress(int progress, int total) {
                    publishProgress(progress);
                }
            });
            return TranscodingAPI.getInstance().snapShot(snapshotPara);
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onCancelled() {
        }

        @Override
        protected void onProgressUpdate(Integer[] values) {
        }

        @Override
        protected void onPostExecute(Integer ret) {
            if (ret == TranscodingAPI.LSMediaTrascodingErrCode_InputFileParseError) {
                LogUtil.e(TAG, "截图失败，解析输入文件失败");
            } else if (ret != TranscodingAPI.LSMediaTrascodingErrCode_NO) {
                LogUtil.e(TAG, "截图失败：" + ret);
            } else {
                LogUtil.i(TAG, "截图已结束");
                callback.onVideoSnapshotSuccess(para.getOutputFile());
                return;
            }
            callback.onVideoSnapshotFailed(ret);
        }
    }
}
