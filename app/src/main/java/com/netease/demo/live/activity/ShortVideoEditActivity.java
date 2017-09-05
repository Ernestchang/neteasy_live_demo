package com.netease.demo.live.activity;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.jaygoo.widget.RangeSeekBar;
import com.netease.demo.live.R;
import com.netease.demo.live.base.BaseActivity;
import com.netease.demo.live.shortvideo.model.MediaCaptureOptions;
import com.netease.demo.live.shortvideo.videoprocess.VideoProcessController;
import com.netease.demo.live.shortvideo.videoprocess.model.VideoProcessOptions;
import com.netease.demo.live.upload.model.VideoItem;
import com.netease.demo.live.util.file.FileUtil;
import com.netease.demo.live.widget.CircleProgressView;
import com.netease.demo.live.widget.MoveImageView;
import com.netease.demo.live.widget.TwoWaysVerticalSeekBar;
import com.netease.nim.uikit.common.ui.dialog.DialogMaker;
import com.netease.nim.uikit.common.ui.dialog.EasyEditDialog;
import com.netease.nim.uikit.common.util.log.LogUtil;
import com.netease.nim.uikit.common.util.storage.StorageType;
import com.netease.nim.uikit.common.util.storage.StorageUtil;
import com.netease.nim.uikit.common.util.sys.ScreenUtil;
import com.netease.nim.uikit.common.util.sys.TimeUtil;
import com.netease.transcoding.TranscodingAPI;
import com.netease.transcoding.TranscodingImpl;
import com.netease.transcoding.TranscodingNative;
import com.netease.transcoding.player.MediaPlayerAPI;
import com.netease.vcloud.video.render.NeteaseView;

import java.io.Serializable;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * 视频编辑界面
 * Created by winnie on 2017/6/20.
 */

public class ShortVideoEditActivity extends BaseActivity implements View.OnClickListener, VideoProcessController.VideoProcessCallback {
    /**
     * constant
     */
    public static final String EXTRA_PATH_LIST = "path_list";
    public static final String EXTRA_TOTAL_TIME = "total_time";
    public static final String EXTRA_MEDIA_OPTIONS = "media_options";
    private static final String ADJUST_TAB = "adjust_tab";
    private static final String SUBSECTION_TAB = "subsection_tab";
    private static final String TEXTURES_TAB = "textures_tab";
    private static final String ACCOMPANY_SOUND_TAB = "accompany_sound_tab";
    public static final int EXTRA_REQUEST_CODE = 1010;
    public static final String EXTRA_EDIT_DONE = "edit_done";

    /**
     * data
     */
    private MediaPlayerAPI mPlayer;
    private String[] arr;
    private List<String> pathList;
    private float totalTime;
    private String currentTab = ADJUST_TAB;
    private int videoIndex = 0;
    private VideoProcessController videoProcessController; // 视频编辑控制器
    private MediaCaptureOptions mediaCaptureOptions;
    private VideoItem videoItem;
    private String displayName; // 视频名称
    // 调整布局参数，亮度，对比度等
    private float brightness = 0.0f;
    private float contrast = 1.0f;
    private float saturation = 1.0f;
    private float colorTemperature = 0;
    private float sharpness = 0;
    // subsection
    private int currentVideoLayout = 0;
    private List<Integer> videoThumbList;
    private int currentPictureLayout = -1;
    private int currentCircle = -1;
    private float volume = 0.3f; // 原声大小
    private boolean isTrasition; // 是否过渡

    // Textures
    private int currentTexturesLayout = -1;
    private String texturePath; // 贴图路径
    private float textureOffset;  //贴图起始时间
    private float textureDuration; // 贴图持续时间

    // accompany_sound
    private int currentSoundLayout = -1;
    private String accompanySoundPath;
    private float accompanyVolume = 0.3f; // 伴音大小

    /**
     * view
     */
    // video view
    private NeteaseView videoView;
    // tab
    private ViewGroup adjustLayout;
    private ViewGroup subsectionLayout;
    private ViewGroup texturesLayout;
    private ViewGroup accompanySoundLayout;
    // tab button
    private TextView adjustText;
    private TextView subsectionText;
    private TextView texturesText;
    private TextView accompanySoundText;
    // edit root
    private ViewGroup editRoot;
    // 调整布局
    private TwoWaysVerticalSeekBar brightnessSeekbar;
    private TwoWaysVerticalSeekBar contrastSeekbar;
    private TwoWaysVerticalSeekBar saturationSeekbar;
    private TwoWaysVerticalSeekBar colorTemperatureSeekbar;
    private TwoWaysVerticalSeekBar sharpnessSeekbar;
    //textures
    private ViewGroup emptyLayout;
    private ViewGroup kissLayout;
    private ViewGroup knifeLayout;
    private ViewGroup grimaceLayout;
    private MoveImageView big_textures;
    private RangeSeekBar textureSeekBar;
    private TextView textureMinText;
    private TextView textureMaxText;
    private ViewGroup showTimeLayout;

    // accompany_sound
    private ViewGroup emptySound;
    private ViewGroup sound_1;
    private ViewGroup sound_2;
    private ViewGroup sound_3;
    private SeekBar accompanySeekBar;
    private ViewGroup soundLayout;


    // 拼接等待界面
    private RelativeLayout combinationLayout; // 拼接等待界面
    private ImageView loadingImage; // 拼接等待图片
    private CircleProgressView loadingView; // 拼接loading
    // subsection
    private ViewGroup firstVideoLayout;
    private ViewGroup secondVideoLayout;
    private ViewGroup thirdVideoLayout;
    private ViewGroup subsectionMovementLayout;
    private TextView moveForwardBtn;
    private TextView moveBackwardBtn;
    private ImageView firstVideoThumb;
    private ImageView secondVideoThumb;
    private ImageView thirdVideoThumb;
    private ViewGroup firstChooseLayout;
    private ViewGroup secondChooseLayout;
    private ViewGroup thirdChooseLayout;
    private ImageView firstChooseImage;
    private ImageView secondChooseImage;
    private ImageView thirdChooseImage;
    private TextView trasitionNone; // 过渡，无
    private TextView trasitionFade; // 过渡，淡入淡出
    private SeekBar volumeSeekBar; // 原声进度条

    private ViewGroup subsectionCircleLayout;

    public static void startActivityForResult(Context context, List<String> pathList,
                                              float totalTime, MediaCaptureOptions mediaCaptureOptions) {
        Intent intent = new Intent();
        intent.setClass(context, ShortVideoEditActivity.class);
        intent.putStringArrayListExtra(EXTRA_PATH_LIST, (ArrayList<String>) pathList);
        intent.putExtra(EXTRA_TOTAL_TIME, totalTime);
        intent.putExtra(EXTRA_MEDIA_OPTIONS, (Serializable) mediaCaptureOptions);
        ((Activity) context).startActivityForResult(intent, EXTRA_REQUEST_CODE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initData();
        updateUI();
    }

    @Override
    protected void onDestroy() {
        stopPlayer();
        super.onDestroy();
    }

    private void stopPlayer() {
        if (mPlayer != null) {
            mPlayer.stop();
            mPlayer.unInit();
        }
        mPlayer = null;
    }

    @Override
    protected void handleIntent(Intent intent) {
        pathList = intent.getStringArrayListExtra(EXTRA_PATH_LIST);
        totalTime = intent.getFloatExtra(EXTRA_TOTAL_TIME, 0);
        mediaCaptureOptions = (MediaCaptureOptions) intent.getSerializableExtra(EXTRA_MEDIA_OPTIONS);

        if (pathList != null) {
            LogUtil.i(TAG, "how many videos:" + pathList.size());
        }
    }

    @Override
    protected int getContentView() {
        return R.layout.short_video_edit_activity;
    }

    @Override
    public void onBackPressed() {
        if (combinationLayout.getVisibility() != View.VISIBLE) {
            Intent intent = new Intent();
            intent.putExtra(EXTRA_EDIT_DONE, false);
            setResult(Activity.RESULT_OK, intent);
            finish();
        }
    }

    @Override
    protected void initView() {
        initToolbar();
        initVideoView();
        // tab
        adjustLayout = findView(R.id.adjust_layout);
        subsectionLayout = findView(R.id.subsection_layout);
        texturesLayout = findView(R.id.textures_layout);
        accompanySoundLayout = findView(R.id.accompany_sound_layout);

        // tab button
        adjustText = findView(R.id.adjust_text);
        subsectionText = findView(R.id.subsection_text);
        texturesText = findView(R.id.textures_text);
        accompanySoundText = findView(R.id.accompany_sound_text);

        // edit root
        editRoot = findView(R.id.edit_layout);

        // 调整布局
        brightnessSeekbar = findView(R.id.brightness_seekbar);
        brightnessSeekbar.setMaxProgress(1.0);
        brightnessSeekbar.setLowestProgress(-1.0);
        brightnessSeekbar.setDefaultValue(0.0);
        contrastSeekbar = findView(R.id.contrast_seekbar);
        contrastSeekbar.setMaxProgress(4.0);
        contrastSeekbar.setLowestProgress(0.0);
        contrastSeekbar.setDefaultValue(1.0);
        saturationSeekbar = findView(R.id.saturation_seekbar);
        saturationSeekbar.setMaxProgress(2.0);
        saturationSeekbar.setLowestProgress(0.0);
        saturationSeekbar.setDefaultValue(1.0);
        colorTemperatureSeekbar = findView(R.id.color_temperature_seekbar);
        colorTemperatureSeekbar.setMaxProgress(360);
        colorTemperatureSeekbar.setLowestProgress(0);
        colorTemperatureSeekbar.setDefaultValue(0);
        sharpnessSeekbar = findView(R.id.sharpness_seekbar);
        sharpnessSeekbar.setMaxProgress(4.0);
        sharpnessSeekbar.setLowestProgress(-4.0);
        sharpnessSeekbar.setDefaultValue(0.0);

        //textures layout
        emptyLayout = findView(R.id.empty_layout);
        kissLayout = findView(R.id.kiss_layout);
        knifeLayout = findView(R.id.knife_layout);
        grimaceLayout = findView(R.id.grimace_layout);
        big_textures = findView(R.id.big_textures);
        textureSeekBar = findView(R.id.texture_seekbar);
        textureMinText = findView(R.id.texture_min_time);
        textureMaxText = findView(R.id.texture_max_time);
        textureMinText.setText(TimeUtil.secondToTime(0));
        textureMaxText.setText(TimeUtil.secondToTime((int) totalTime));
        showTimeLayout = findView(R.id.showtime_layout);

        //accompany_sound
        emptySound = findView(R.id.empty_sound);
        sound_1 = findView(R.id.sound_1);
        sound_2 = findView(R.id.sound_2);
        sound_3 = findView(R.id.sound_3);
        soundLayout = findView(R.id.sound_layout);
        accompanySeekBar = findView(R.id.accompany_volume_seekbar);
        accompanySeekBar.setMax(100);
        accompanySeekBar.setProgress(30);


        // 拼接等待界面
        combinationLayout = findView(R.id.combination_layout);
        loadingImage = findView(R.id.loading_image);
        loadingView = findView(R.id.loading_view);
        // subsection layout
        firstVideoLayout = findView(R.id.picture_1_layout);
        secondVideoLayout = findView(R.id.picture_2_layout);
        thirdVideoLayout = findView(R.id.picture_3_layout);
        subsectionMovementLayout = findView(R.id.subsection_imageview_layout);
        moveForwardBtn = findView(R.id.move_forward);
        moveBackwardBtn = findView(R.id.move_backward);
        firstVideoThumb = findView(R.id.picture_1);
        secondVideoThumb = findView(R.id.picture_2);
        thirdVideoThumb = findView(R.id.picture_3);
        firstChooseLayout = findView(R.id.circle_1_layout);
        secondChooseLayout = findView(R.id.circle_2_layout);
        thirdChooseLayout = findView(R.id.circle_3_layout);
        firstChooseImage = findView(R.id.circle_1);
        secondChooseImage = findView(R.id.circle_2);
        thirdChooseImage = findView(R.id.circle_3);
        trasitionNone = findView(R.id.trasition_none);
        trasitionFade = findView(R.id.trasition_fade);
        volumeSeekBar = findView(R.id.volume_seekbar);
        volumeSeekBar.setMax(100);
        volumeSeekBar.setProgress(30);

        subsectionCircleLayout = findView(R.id.subsection_circle_layout);

        setListener();
    }

    private void initToolbar() {
        findView(R.id.back_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        findView(R.id.done_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNameDialog();
            }
        });
    }

    private void initVideoView() {
        videoView = findView(R.id.video_view);
        arr = new String[pathList.size()];
        arr = pathList.toArray(arr);
        mPlayer = MediaPlayerAPI.getInstance();
        mPlayer.init(getApplicationContext(), arr, videoView);
        mPlayer.start();

    }

    private void setListener() {
        adjustText.setOnClickListener(this);
        subsectionText.setOnClickListener(this);
        texturesText.setOnClickListener(this);
        accompanySoundText.setOnClickListener(this);

        emptyLayout.setOnClickListener(this);
        kissLayout.setOnClickListener(this);
        knifeLayout.setOnClickListener(this);
        grimaceLayout.setOnClickListener(this);
        if (totalTime > 1000) {
            textureSeekBar.setRange(0, totalTime / 1000);
            textureSeekBar.setValue(0, totalTime / 1000);
        }
        textureSeekBar.setOnRangeChangedListener(new RangeSeekBar.OnRangeChangedListener() {
            @Override
            public void onRangeChanged(RangeSeekBar view, float min, float max, boolean isFromUser) {
                textureOffset = min;
                textureDuration = max - textureOffset;
            }
        });

        // accompany sound
        emptySound.setOnClickListener(this);
        sound_1.setOnClickListener(this);
        sound_2.setOnClickListener(this);
        sound_3.setOnClickListener(this);
        accompanySeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                accompanyVolume = (float) progress / 100;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        // 调整布局
        brightnessSeekbar.setOnSeekBarChangeListener(new TwoWaysVerticalSeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressBefore() {

            }

            @Override
            public void onProgressChanged(TwoWaysVerticalSeekBar seekBar, double progress) {
                brightness = (float) progress;
                if (mPlayer != null) {
                    mPlayer.setBrightness(brightness);
                }
            }

            @Override
            public void onProgressAfter() {

            }
        });
        contrastSeekbar.setOnSeekBarChangeListener(new TwoWaysVerticalSeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressBefore() {

            }

            @Override
            public void onProgressChanged(TwoWaysVerticalSeekBar seekBar, double progress) {
                contrast = (float) progress;
                if (mPlayer != null) {
                    mPlayer.setContrast(contrast);
                }
            }

            @Override
            public void onProgressAfter() {

            }
        });
        saturationSeekbar.setOnSeekBarChangeListener(new TwoWaysVerticalSeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressBefore() {

            }

            @Override
            public void onProgressChanged(TwoWaysVerticalSeekBar seekBar, double progress) {
                saturation = (float) progress;
                if (mPlayer != null) {
                    mPlayer.setSaturation(saturation);
                }
            }

            @Override
            public void onProgressAfter() {

            }
        });
        colorTemperatureSeekbar.setOnSeekBarChangeListener(new TwoWaysVerticalSeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressBefore() {

            }

            @Override
            public void onProgressChanged(TwoWaysVerticalSeekBar seekBar, double progress) {
                colorTemperature = (float) progress;
                if (mPlayer != null) {
                    mPlayer.setHue(colorTemperature);
                }
            }

            @Override
            public void onProgressAfter() {

            }
        });
        sharpnessSeekbar.setOnSeekBarChangeListener(new TwoWaysVerticalSeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressBefore() {

            }

            @Override
            public void onProgressChanged(TwoWaysVerticalSeekBar seekBar, double progress) {
                sharpness = (float) progress;
                if (mPlayer != null) {
                    mPlayer.setSharpen(sharpness);
                }
            }

            @Override
            public void onProgressAfter() {

            }
        });
        // subsection
        firstVideoLayout.setOnClickListener(this);
        secondVideoLayout.setOnClickListener(this);
        thirdVideoLayout.setOnClickListener(this);
        moveForwardBtn.setOnClickListener(this);
        moveBackwardBtn.setOnClickListener(this);

        firstChooseLayout.setOnClickListener(this);
        secondChooseLayout.setOnClickListener(this);
        thirdChooseLayout.setOnClickListener(this);

        trasitionNone.setOnClickListener(this);
        trasitionFade.setOnClickListener(this);

        volumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                volume = (float) progress / 100;
                if (mPlayer != null) {
                    mPlayer.setVolume(volume);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }

    private void initData() {
        videoProcessController = new VideoProcessController(this, this);
        videoThumbList = new ArrayList<>();
        videoThumbList.add(R.drawable.icon_gift_rose);
        videoThumbList.add(R.drawable.icon_gift_bear);
        videoThumbList.add(R.drawable.icon_gift_icecream);
        textureOffset = 0;
        textureDuration = totalTime / 1000;
    }

    private void updateUI() {
        if (pathList == null) {
            return;
        }
        firstVideoLayout.setVisibility(pathList.size() > 0 ? View.VISIBLE : View.GONE);
        secondVideoLayout.setVisibility(pathList.size() > 1 ? View.VISIBLE : View.GONE);
        thirdVideoLayout.setVisibility(pathList.size() > 2 ? View.VISIBLE : View.GONE);
        firstChooseImage.setVisibility(pathList.size() > 0 ? View.VISIBLE : View.GONE);
        secondChooseImage.setVisibility(pathList.size() > 1 ? View.VISIBLE : View.GONE);
        thirdChooseImage.setVisibility(pathList.size() > 2 ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.adjust_text:
                currentTab = ADJUST_TAB;
                switchTab();
                break;
            case R.id.subsection_text:
                currentTab = SUBSECTION_TAB;
                switchTab();
                break;
            case R.id.textures_text:
                currentTab = TEXTURES_TAB;
                switchTab();
                break;
            case R.id.accompany_sound_text:
                currentTab = ACCOMPANY_SOUND_TAB;
                switchTab();
                break;
            case R.id.empty_layout:
                updateTexturesLayout(1);
                break;
            case R.id.kiss_layout:
                updateTexturesLayout(2);
                break;
            case R.id.knife_layout:
                updateTexturesLayout(3);
                break;
            case R.id.grimace_layout:
                updateTexturesLayout(4);
                break;
            case R.id.picture_1_layout:
                updatePictureLayout(1);
                break;
            case R.id.picture_2_layout:
                updatePictureLayout(2);
                break;
            case R.id.picture_3_layout:
                updatePictureLayout(3);
                break;
            case R.id.move_forward:
                moveVideoForward();
                break;
            case R.id.move_backward:
                moveVideoBackward();
                break;
            case R.id.empty_sound:
                updateSoundLayout(1);
                break;
            case R.id.sound_1:
                updateSoundLayout(2);
                break;
            case R.id.sound_2:
                updateSoundLayout(3);
                break;
            case R.id.sound_3:
                updateSoundLayout(4);
                break;
            case R.id.circle_1_layout:
                updateCircle(1);
                break;
            case R.id.circle_2_layout:
                updateCircle(2);
                break;
            case R.id.circle_3_layout:
                updateCircle(3);
                break;
            case R.id.trasition_none:
                updateTrasition(false);
                break;
            case R.id.trasition_fade:
                updateTrasition(true);
                break;
        }
    }

    private void switchTab() {
        adjustText.setEnabled(!currentTab.equals(ADJUST_TAB));
        subsectionText.setEnabled(!currentTab.equals(SUBSECTION_TAB));
        texturesText.setEnabled(!currentTab.equals(TEXTURES_TAB));
        accompanySoundText.setEnabled(!currentTab.equals(ACCOMPANY_SOUND_TAB));

        adjustLayout.setVisibility(currentTab.equals(ADJUST_TAB) ? View.VISIBLE : View.GONE);
        subsectionLayout.setVisibility(currentTab.equals(SUBSECTION_TAB) ? View.VISIBLE : View.GONE);
        texturesLayout.setVisibility(currentTab.equals(TEXTURES_TAB) ? View.VISIBLE : View.GONE);
        accompanySoundLayout.setVisibility(currentTab.equals(ACCOMPANY_SOUND_TAB) ? View.VISIBLE : View.GONE);
    }

    //选中伴音中的哪个伴音
    private void updateSoundLayout(int currentSoundLayout) {
        this.currentSoundLayout = currentSoundLayout;
        soundLayout.setVisibility(1 == currentSoundLayout ? View.GONE : View.VISIBLE);

        emptySound.setBackgroundResource(1 == currentSoundLayout ? R.drawable.border : R.color.color_gray_1affffff);
        sound_1.setBackgroundResource(2 == currentSoundLayout ? R.drawable.border : R.color.color_gray_1affffff);
        sound_2.setBackgroundResource(3 == currentSoundLayout ? R.drawable.border : R.color.color_gray_1affffff);
        sound_3.setBackgroundResource(4 == currentSoundLayout ? R.drawable.border : R.color.color_gray_1affffff);

        switch (currentSoundLayout) {
            case 1:
                accompanySoundPath = null;
                break;
            case 2:
                accompanySoundPath = "sdcard/sound/test_1.mp3";
                break;
            case 3:
                accompanySoundPath = "sdcard/sound/test_2.mp3";
                break;
            case 4:
                accompanySoundPath = "sdcard/sound/test_3.mp3";
                break;
        }
    }

    // 选中贴图中的哪个表情
    private void updateTexturesLayout(int currentTexturesLayout) {
        this.currentTexturesLayout = currentTexturesLayout;
        showTimeLayout.setVisibility(1 == currentTexturesLayout ? View.GONE : View.VISIBLE);
        emptyLayout.setBackgroundResource(1 == currentTexturesLayout ? R.drawable.border : R.color.color_gray_1affffff);
        kissLayout.setBackgroundResource(2 == currentTexturesLayout ? R.drawable.border : R.color.color_gray_1affffff);
        knifeLayout.setBackgroundResource(3 == currentTexturesLayout ? R.drawable.border : R.color.color_gray_1affffff);
        grimaceLayout.setBackgroundResource(4 == currentTexturesLayout ? R.drawable.border : R.color.color_gray_1affffff);

        updateBigTextures(currentTexturesLayout, 1 == currentTexturesLayout);

    }

    //选项图片并且贴到图像上
    private void updateBigTextures(int currentTexturesLayout, boolean isHide) {
        switch (currentTexturesLayout) {
            case 1:
                big_textures.setVisibility(View.GONE);
                if (isHide) {
                    texturePath = null;
                }
                break;
            case 2:
                big_textures.setVisibility(View.VISIBLE);
                big_textures.setBackgroundResource(R.drawable.big_kiss);
                texturePath = "sdcard/texture/big_kiss.png";
                break;
            case 3:
                big_textures.setVisibility(View.VISIBLE);
                big_textures.setBackgroundResource(R.drawable.big_knife);
                texturePath = "sdcard/texture/big_knife.png";
                break;
            case 4:
                big_textures.setVisibility(View.VISIBLE);
                big_textures.setBackgroundResource(R.drawable.big_grimace);
                texturePath = "sdcard/texture/big_grimace.png";
                break;
        }

    }

    //更新Circle的layout
    private void updateCircle(int currentCircle) {
        this.currentCircle = currentCircle;

        firstChooseImage.setImageResource(1 == currentCircle ? R.drawable.ic_blue_circle : R.drawable.gray);
        secondChooseImage.setImageResource(2 == currentCircle ? R.drawable.ic_blue_circle : R.drawable.gray);
        thirdChooseImage.setImageResource(3 == currentCircle ? R.drawable.ic_blue_circle : R.drawable.gray);

        updateCircle();
    }

    private void updateCircle() {
        subsectionCircleLayout.setVisibility(View.VISIBLE);
        subsectionMovementLayout.setVisibility(View.GONE);
        firstVideoLayout.setBackgroundResource(R.color.transparent);
        secondVideoLayout.setBackgroundResource(R.color.transparent);
        thirdVideoLayout.setBackgroundResource(R.color.transparent);
    }

    // 选中哪个视频的背景图显示
    private void updatePictureLayout(int currentVideoLayout) {
        this.currentVideoLayout = currentVideoLayout;

        firstVideoLayout.setBackgroundResource(1 == currentVideoLayout ? R.drawable.blue_border : R.color.transparent);
        secondVideoLayout.setBackgroundResource(2 == currentVideoLayout ? R.drawable.blue_border : R.color.transparent);
        thirdVideoLayout.setBackgroundResource(3 == currentVideoLayout ? R.drawable.blue_border : R.color.transparent);

        updateVideoThumb();
        updateMoveLayout();
    }

    private void updateVideoThumb() {
        // 先清空原来现实图片
        firstVideoThumb.setBackgroundResource(0);
        secondVideoThumb.setBackgroundResource(0);
        thirdVideoThumb.setBackgroundResource(0);

        firstVideoThumb.setImageResource(videoThumbList.get(0));
        secondVideoThumb.setImageResource(videoThumbList.get(1));
        thirdVideoThumb.setImageResource(videoThumbList.get(2));
    }

    // 更新排序，往前，往后布局
    private void updateMoveLayout() {
        subsectionMovementLayout.setVisibility(View.VISIBLE);
        subsectionCircleLayout.setVisibility(View.GONE);
        firstChooseImage.setImageResource(R.drawable.gray);
        secondChooseImage.setImageResource(R.drawable.gray);
        thirdChooseImage.setImageResource(R.drawable.gray);
        moveForwardBtn.setEnabled(currentVideoLayout > 1);
        moveBackwardBtn.setEnabled(currentVideoLayout < pathList.size());
    }

    private void moveVideoForward() {
        if (currentVideoLayout <= 1) {
            return;
        }
        Collections.swap(pathList, currentVideoLayout - 2, currentVideoLayout - 1);
        Collections.swap(videoThumbList, currentVideoLayout - 2, currentVideoLayout - 1);
        currentVideoLayout--;
        updatePictureLayout(currentVideoLayout);
    }

    private void moveVideoBackward() {
        if (currentVideoLayout >= pathList.size() || currentVideoLayout >= videoThumbList.size()) {
            return;
        }
        Collections.swap(pathList, currentVideoLayout - 1, currentVideoLayout);
        Collections.swap(videoThumbList, currentVideoLayout - 1, currentVideoLayout);
        currentVideoLayout++;
        updatePictureLayout(currentVideoLayout);
    }

    // 淡入淡出选择
    private void updateTrasition(boolean isChoose) {
        trasitionNone.setEnabled(isChoose);
        trasitionFade.setEnabled(!isChoose);
        isTrasition = isChoose;
    }

    /**
     * ************* 设置完成，开始拼接视频 *****************
     */
    // 设置视频名称
    private void showNameDialog() {
        final EasyEditDialog requestDialog = new EasyEditDialog(ShortVideoEditActivity.this);
        requestDialog.setEditTextMaxLength(200);
        requestDialog.setTitle(getString(R.string.video_name));
        requestDialog.setEditHint("新视频" + TimeUtil.getMonthTimeString(System.currentTimeMillis()));
        requestDialog.setInputType(InputType.TYPE_CLASS_TEXT);
        requestDialog.setCustomTextWatcher(true);
        requestDialog.addNegativeButtonListener(R.string.cancel, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestDialog.dismiss();
            }
        });
        requestDialog.addPositiveButtonListener(R.string.save, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayName = requestDialog.getEditMessage();
                if (TextUtils.isEmpty(displayName)) {
                    Toast.makeText(ShortVideoEditActivity.this, "不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }
                requestDialog.dismiss();
                videoView.setVisibility(View.GONE);
                editRoot.setVisibility(View.GONE);
                DialogMaker.showProgressDialog(ShortVideoEditActivity.this, "等待截图");
                stopPlayer();

                startVideoProcess();
            }
        });
        requestDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
            }
        });
        requestDialog.show();

        getHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.showSoftInput(requestDialog.getmEdit(), 0);
            }
        }, 200);
    }

    private int videoWidth;
    private int videoHeight;

    private void startVideoProcess() {
        TranscodingAPI.SnapshotPara para = new TranscodingAPI.SnapshotPara();
        try {
            para.setInputFile(pathList.get(0));
            TranscodingImpl.SInputFileInfo inputFileInfo = new TranscodingImpl.SInputFileInfo();
            TranscodingNative.FfmpegTranscodingGetInputInfo(pathList.get(0), inputFileInfo);
            videoWidth = inputFileInfo.iVideoWidth;
            videoHeight = inputFileInfo.iVideoHeight;
            para.setOffset(1);
            para.setInterval(0);
            LogUtil.i(TAG, "start snapshot， width:" + videoWidth + ", height:" + videoHeight);
            if ((inputFileInfo.iVideoWidth < 240 || inputFileInfo.iVideoHeight < 320) &&
                    inputFileInfo.iVideoWidth > 0 && inputFileInfo.iVideoHeight > 0) {
                // 截图的大小 要比源文件小
                para.setPicWidth(inputFileInfo.iVideoWidth);
                para.setPicHeight(inputFileInfo.iVideoHeight);
            } else {
                para.setPicWidth(240);
                para.setPicHeight(320);
            }
            para.setOutputFile(FileUtil.getThumbPath(displayName, ".jpg", StorageType.TYPE_THUMB_IMAGE));
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        videoProcessController.startSnapShot(para);
    }

    private void startCombination() {
        VideoProcessOptions videoProcessOptions;
        String outputPath;
        try {
            videoProcessOptions = new VideoProcessOptions(mediaCaptureOptions);
            // 设置待拼接的文件
            TranscodingAPI.InputFilePara inputFilePara = videoProcessOptions.getInputFilePara();
            arr = pathList.toArray(arr);
            inputFilePara.setInputMainFileArray(arr);
            videoProcessOptions.setInputFilePara(inputFilePara);
            // 过渡
            if (isTrasition) {
                inputFilePara.setVideoFadeDuration(1);
            }
            // 设置拼接后文件存储地址
            outputPath = StorageUtil.getWritePath(displayName + ".mp4", StorageType.TYPE_VIDEO);
            TranscodingAPI.OutputFilePara outputFilePara = videoProcessOptions.getOutputFilePara();
            outputFilePara.setOutputFile(outputPath);
            videoProcessOptions.setOutputFilePara(outputFilePara);
            // 设置视频亮度，对比度等
            TranscodingAPI.ColourAdjustPara colourAdjustPara = videoProcessOptions.getColourAdjustPara();
            colourAdjustPara.setBrightness(brightness);
            colourAdjustPara.setContrast(contrast);
            colourAdjustPara.setSaturation(saturation);
            colourAdjustPara.setHue(colorTemperature);
            colourAdjustPara.setSharpenness(sharpness);
            videoProcessOptions.setColourAdjustPara(colourAdjustPara);
            // 原声大小
            TranscodingAPI.MixAudioPara mixAudioPara = videoProcessOptions.getMixAudioPara();
            mixAudioPara.setVolumeLevel(volume);
            // 伴音
            if (!TextUtils.isEmpty(accompanySoundPath)) {
                mixAudioPara.setInputSecondaryFile(accompanySoundPath);
                mixAudioPara.setMixAudioVolumeLevel(accompanyVolume);
            }
            // 贴图
            setTexture(videoProcessOptions);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        // videoItem
        videoItem = new VideoItem();
        videoItem.setId("local" + System.currentTimeMillis());
        videoItem.setFilePath(outputPath);
        videoItem.setDisplayName(displayName);
        videoItem.setDateTaken(TimeUtil.getNowDatetime());
        String path = FileUtil.getThumbPath(displayName, ".jpg", StorageType.TYPE_THUMB_IMAGE);
        videoItem.setUriString(path);

        // 开始拼接
        videoProcessController.startCombination(videoProcessOptions);
    }

    // 设置贴图
    private void setTexture(VideoProcessOptions videoProcessOptions) {
        // 贴图持续时间不能为0
        if (texturePath != null && textureDuration != 0) {
            float xpos;
            float ypos;
            if (big_textures.isMove()) {
                float xratio = (mediaCaptureOptions.mVideoPreviewWidth - big_textures.getWidth()) / (float) ScreenUtil.screenWidth;
                float yratio = (mediaCaptureOptions.mVideoPreviewHeight - big_textures.getHeight()) / (float) ScreenUtil.screenHeight;
                xpos = big_textures.getLastX() * xratio;
                ypos = big_textures.getLastY() * yratio;
            } else {
                xpos = (mediaCaptureOptions.mVideoPreviewWidth - big_textures.getWidth()) / 2;
                ypos = (mediaCaptureOptions.mVideoPreviewHeight - big_textures.getHeight()) / 2;
            }

            TranscodingAPI.WaterMarkPara waterMarkPara = videoProcessOptions.getWaterMarkPara();
            waterMarkPara.setWaterMarkFile(texturePath);
            waterMarkPara.setxPos((int) xpos);
            waterMarkPara.setyPos((int) ypos);
            waterMarkPara.setWaterMarkDuration((int) Math.floor(textureDuration));
            waterMarkPara.setWaterMarkOffset((int) textureOffset);
            LogUtil.i(TAG, "texture===totalTime:" + totalTime / 1000 +
                    ", duration:" + ((int) Math.floor(textureDuration))
                    + ", offset:" + textureOffset);
        }
    }

    /**
     * **************** VideoProcessCallback *******************
     */
    @Override
    public void onVideoProcessSuccess() {
        Toast.makeText(this, "保存成功！", Toast.LENGTH_SHORT).show();
        for (String path : pathList) {
            FileUtil.deleteFile(path);
        }
        getHandler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent();
                intent.putExtra(VideoShootActivity.EXTRA_VIDEO_ITEM, videoItem);
                intent.putExtra(EXTRA_EDIT_DONE, true);
                setResult(Activity.RESULT_OK, intent);
                finish();
            }
        }, 3000);
    }

    @Override
    public void onVideoProcessFailed(int code) {
        Toast.makeText(this, "视频保存失败:" + code, Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public void onVideoSnapshotSuccess(String path) {
        DialogMaker.dismissProgressDialog();
        combinationLayout.setVisibility(View.VISIBLE);
        Glide.with(this).load(pathList.get(0)).into(loadingImage);
        loadingView.setFormat("%");
        loadingView.setProgress(0);
        loadingView.setContent("0");
        startCombination();
    }

    @Override
    public void onVideoSnapshotFailed(int code) {
        DialogMaker.dismissProgressDialog();
        Toast.makeText(this, "截图失败:" + code, Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public void onVideoProcessUpdate(int process, int total) {
        loadingView.setMaxProgress(total);
        loadingView.setProgress(process);
        NumberFormat numberFormat = NumberFormat.getInstance();

        // 设置精确到小数点后0位
        numberFormat.setMaximumFractionDigits(0);

        String result = numberFormat.format((float) process / (float) total * 100);
        loadingView.setContent(result);
    }
}
