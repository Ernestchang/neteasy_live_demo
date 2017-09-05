package com.netease.demo.live.shortvideo;

import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;

import com.netease.LSMediaFilter.filter.helper.MagicFilterType;
import com.netease.LSMediaFilter.view.CameraSurfaceView;
import com.netease.LSMediaRecord.Statistics;
import com.netease.LSMediaRecord.lsMediaCapture;
import com.netease.LSMediaRecord.lsMessageHandler;
import com.netease.demo.live.server.DemoServerHttpClient;
import com.netease.demo.live.shortvideo.model.MediaCaptureOptions;

/**
 * Created by hzxuwen on 2017/4/6.
 */

public class MediaCaptureController implements lsMessageHandler {
    private static final String TAG = MediaCaptureController.class.getSimpleName();
    private MediaCaptureControllerCallback captureControllerCallback;
    private MediaCaptureWrapper mediaCaptureWrapper;
    private MediaCaptureOptions mediaCaptureOptions;
    private lsMediaCapture mLSMediaCapture;

    private Thread mThread;
    private Context context;

    // data
    private Statistics mStatistics = null; // 统计
    private float mCurrentDistance; // 变焦手势距离差
    private float mLastDistance = -1;

    public interface MediaCaptureControllerCallback {
        /**
         * 预览设置完成
         */
        void onPreviewInited();

        /**
         * 设置预览画面大小
         */
        void setPreviewSize(int videoPreviewWidth, int videoPreviewHeight);

        /**
         * 获取画布SurfaceView
         */
        SurfaceView getSurfaceView();

        /**
         * 可以开始录制了
         */
        void onStartRecording();

        /**
         * 资源释放完毕
         */
        void onRelease();

        /**
         * 异常
         */
        void onError(int code);
    }

    /**
     * 直播状态记录
     */
    private boolean m_tryToStopLiveStreaming;
    private boolean m_startVideoCamera;
    private boolean m_liveStreamingOn;
    private boolean m_liveStreamingInit;
    private boolean m_liveStreamingInitFinished;


    public MediaCaptureController(Context context, MediaCaptureControllerCallback callback, MediaCaptureOptions options) {
        this.context = context;
        this.captureControllerCallback = callback;
        this.mediaCaptureOptions = options;
        mediaCaptureWrapper = new MediaCaptureWrapper(context, this, mediaCaptureOptions);

        initPreview();
    }

    private void initPreview() {
        // 设置预览参数
        captureControllerCallback.setPreviewSize(mediaCaptureOptions.mVideoPreviewWidth, mediaCaptureOptions.mVideoPreviewHeight);
        // 开启预览
        mLSMediaCapture = mediaCaptureWrapper.getmLSMediaCapture();
        if (mLSMediaCapture != null) {
            mediaCaptureWrapper.getmLSMediaCapture().startVideoPreview((CameraSurfaceView) captureControllerCallback.getSurfaceView(), mediaCaptureOptions.cameraPosition);
            m_startVideoCamera = true;
        }
    }

    /**
     * 释放录制资源
     */
    public void release() {
        m_tryToStopLiveStreaming = true;

        //停止录制调用相关API接口
        if (mLSMediaCapture != null && m_liveStreamingOn) {

            //停止录制，释放资源
            mLSMediaCapture.stopRecord();

            //如果音视频或者单独视频录制，需要关闭视频预览
            if (m_startVideoCamera) {
                mLSMediaCapture.stopVideoPreview();
                mLSMediaCapture.destroyVideoPreview();
            }

            //反初始化推流实例，当它与stopLiveStreaming连续调用时，参数为false
            mLSMediaCapture.uninitLsMediaCapture(false);
            mLSMediaCapture = null;
        } else if (mLSMediaCapture != null && m_startVideoCamera) {
            mLSMediaCapture.stopVideoPreview();
            mLSMediaCapture.destroyVideoPreview();

            //反初始化推流实例，当它不与stopLiveStreaming连续调用时，参数为true
            mLSMediaCapture.uninitLsMediaCapture(true);
            mLSMediaCapture = null;
        } else if (!m_liveStreamingInitFinished) {
            //反初始化推流实例，当它不与stopLiveStreaming连续调用时，参数为true
            mLSMediaCapture.uninitLsMediaCapture(true);
        }

        m_liveStreamingOn = false;

        captureControllerCallback.onRelease();
    }

    /**************
     * 视频操作
     *********/

    /**
     * 开始录制
     */
    public void startRecording() {
        if (!m_liveStreamingOn) {
            //8、初始化录制
            if (mThread != null) {
                return;
            }
//            showToast("初始化中。。。");
            mThread = new Thread() {
                public void run() { //正常网络下initLiveStream 1、2s就可完成，当网络很差时initLiveStream可能会消耗5-10s，因此另起线程防止UI卡住
                    m_liveStreamingInitFinished = mLSMediaCapture.initRecord(mediaCaptureWrapper.getmLSLiveStreamingParaCtx(), DemoServerHttpClient.readAppKey());
                    if (m_liveStreamingInitFinished) {
                        startAV();
                    } else {
//                        mHandler.postDelayed(new Runnable() {
//                            @Override
//                            public void run() {
//                                MediaPreviewActivity.this.finish();
//                            }
//                        }, 5000);
                    }
                    mThread = null;
                }
            };
            mThread.start();
        }
    }

    /**
     * 停止录制
     */
    public void stopRecording() {
        if (m_liveStreamingOn) {
            mLSMediaCapture.stopRecord();
            m_liveStreamingOn = false;
        }
    }

    //开始录制
    private void startAV() {
        if (mLSMediaCapture != null && m_liveStreamingInitFinished) {
            mLSMediaCapture.startRecord();
            m_liveStreamingOn = true;
        }
    }

    public void switchCamera() {
        if (mLSMediaCapture != null) {
            mLSMediaCapture.switchCamera();
        }
    }

    /**
     * ***************** 摄像头变焦 ****************
     */

    // 对焦
    public void setCameraFocus(MotionEvent event) {
        if (mLSMediaCapture != null) {
            mLSMediaCapture.setCameraFocus(event);
        }
    }

    private int maxZoomValue = 0;
    private int currenZoomValue = 0;

    // 变焦
    public void setCameraZoomParam(MotionEvent event) {
        /**
         * 首先判断按下手指的个数是不是大于两个。
         * 如果大于两个则执行以下操作（即图片的缩放操作）。
         */
        if (event.getPointerCount() >= 2) {

            float offsetX = event.getX(0) - event.getX(1);
            float offsetY = event.getY(0) - event.getY(1);
            /**
             * 原点和滑动后点的距离差
             */
            mCurrentDistance = (float) Math.sqrt(offsetX * offsetX + offsetY * offsetY);
            if (mLastDistance < 0) {
                mLastDistance = mCurrentDistance;
            } else {
                if (mLSMediaCapture != null) {
                    maxZoomValue = mLSMediaCapture.getCameraMaxZoomValue();
                    currenZoomValue = mLSMediaCapture.getCameraZoomValue();
                }
                /**
                 * 如果当前滑动的距离（currentDistance）比最后一次记录的距离（lastDistance）相比大于5英寸（也可以为其他尺寸），
                 * 那么现实图片放大
                 */
                if (mCurrentDistance - mLastDistance > 5) {
                    //Log.i(TAG, "test: 放大！！！");
                    currenZoomValue++;
                    if (currenZoomValue > maxZoomValue) {
                        currenZoomValue = maxZoomValue;
                    }
                    if (mLSMediaCapture != null) {
                        mLSMediaCapture.setCameraZoomValue(currenZoomValue);
                    }

                    mLastDistance = mCurrentDistance;
                    /**
                     * 如果最后的一次记录的距离（lastDistance）与当前的滑动距离（currentDistance）相比小于5英寸，
                     * 那么图片缩小。
                     */
                } else if (mLastDistance - mCurrentDistance > 5) {
                    currenZoomValue--;
                    if (currenZoomValue < 0) {
                        currenZoomValue = 0;
                    }
                    //Log.i(TAG, "test: 缩小！！！");
                    if (mLSMediaCapture != null) {
                        mLSMediaCapture.setCameraZoomValue(currenZoomValue);
                    }

                    mLastDistance = mCurrentDistance;
                }
            }
        }
    }

    /**
     * ******************** 滤镜选择 *****************
     */
    // 设置滤镜模式
    public void setFilterType(MagicFilterType type) {
        if (mLSMediaCapture != null) {
            mLSMediaCapture.setFilterType(type);
        }
    }

    /*****************************
     * 视频录制相关通知
     ***********************/

    @Override
    public void handleMessage(int msg, Object object) {
        switch (msg) {
            case MSG_AUDIO_PROCESS_ERROR://音频处理出错
                Log.i(TAG, "test: in handleMessage, MSG_AUDIO_PROCESS_ERROR");
                break;
            case MSG_VIDEO_PROCESS_ERROR://视频处理出错
                Log.i(TAG, "test: in handleMessage, MSG_VIDEO_PROCESS_ERROR");
                break;
            case MSG_START_PREVIEW_ERROR://视频预览出错，可能是获取不到camera的使用权限
                Log.i(TAG, "test: in handleMessage, MSG_START_PREVIEW_ERROR");
                break;
            case MSG_AUDIO_RECORD_ERROR://音频采集出错，获取不到麦克风的使用权限
                Log.i(TAG, "test: in handleMessage, MSG_AUDIO_RECORD_ERROR");
                captureControllerCallback.onError(MSG_AUDIO_RECORD_ERROR);
                break;
            case MSG_AUDIO_SAMPLE_RATE_NOT_SUPPORT_ERROR://音频采集参数不支持
                Log.i(TAG, "test: in handleMessage, MSG_AUDIO_SAMPLE_RATE_NOT_SUPPORT_ERROR");
                break;
            case MSG_AUDIO_PARAMETER_NOT_SUPPORT_BY_HARDWARE_ERROR://音频参数不支持
                Log.i(TAG, "test: in handleMessage, MSG_AUDIO_PARAMETER_NOT_SUPPORT_BY_HARDWARE_ERROR");
                break;
            case MSG_NEW_AUDIORECORD_INSTANCE_ERROR://音频实例初始化出错
                Log.i(TAG, "test: in handleMessage, MSG_NEW_AUDIORECORD_INSTANCE_ERROR");
                break;
            case MSG_AUDIO_START_RECORDING_ERROR://音频采集出错
                Log.i(TAG, "test: in handleMessage, MSG_AUDIO_START_RECORDING_ERROR");
                break;
            case MSG_CAMERA_PREVIEW_SIZE_NOT_SUPPORT_ERROR://camera采集分辨率不支持
                Log.i(TAG, "test: in handleMessage: MSG_CAMERA_PREVIEW_SIZE_NOT_SUPPORT_ERROR");
                break;
            case MSG_START_PREVIEW_FINISHED://camera采集预览完成
                //开始本地预览之后才能开始录制
                Log.i(TAG, "test: MSG_START_PREVIEW_FINISHED");
                captureControllerCallback.onPreviewInited();
                break;
            case MSG_STOP_VIDEO_CAPTURE_FINISHED:
                Log.i(TAG, "test: in handleMessage: MSG_STOP_VIDEO_CAPTURE_FINISHED");
                break;
            case MSG_STOP_AUDIO_CAPTURE_FINISHED:
                Log.i(TAG, "test: in handleMessage: MSG_STOP_AUDIO_CAPTURE_FINISHED");
                break;
            case MSG_SWITCH_CAMERA_FINISHED://切换摄像头完成
                break;
            case MSG_GET_STATICS_INFO://获取统计信息的反馈消息
                Log.i(TAG, "test: in handleMessage, MSG_GET_STATICS_INFO");
                mStatistics = (Statistics) object;

                Log.i(TAG, "videoEncodeBitrate:" + mStatistics.videoEncodeBitRate + ", audioEncodeBitrate:" + mStatistics.audioEncodeBitRate);
                break;
            case MSG_SET_CAMERA_ID_ERROR://设置camera出错（对于只有一个摄像头的设备，如果调用了不存在的摄像头，会反馈这个错误消息）
                Log.i(TAG, "test: in handleMessage, MSG_SET_CAMERA_ID_ERROR");
                break;
            case MSG_MIX_AUDIO_FINISHED://伴音一首MP3歌曲结束后的反馈
                Log.i(TAG, "test: in handleMessage, MSG_MIX_AUDIO_FINISHED");
                break;
            case MSG_STOP_RECORD_FINISHED:
                break;
            case MSG_INIT_RECORD_VERIFY_ERROR:
                // 鉴权失败
                Log.e(TAG, "record verify error");
                break;
            case MSG_START_RECORD_FINISHED:
                Log.i(TAG, "test: in handleMessage, MSG_START_RECORD_FINISHED");
                // 真正可以录制了
                captureControllerCallback.onStartRecording();
                break;
            default:
                break;

        }
    }
}
