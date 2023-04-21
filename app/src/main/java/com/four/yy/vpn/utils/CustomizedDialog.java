package com.four.yy.vpn.utils;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.airbnb.lottie.LottieAnimationView;
import com.four.yy.vpn.R;


public class CustomizedDialog extends Dialog {
    private String filePath;
    private boolean isCancel;
    private boolean isBackCancel;

    public CustomizedDialog(@NonNull Context context, String filePath, boolean isCancel, boolean isBackCancel) {
        super(context);
        this.filePath = filePath;
        this.isCancel = isCancel;
        this.isBackCancel = isBackCancel;
    }

    private OnClick onClick;

    public void setOnClick(OnClick onClick) {
        this.onClick = onClick;
    }

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_lead);
        // 将弹窗背景色设置成透明
        getWindow().setBackgroundDrawableResource(R.color.transparent);
        // 弹窗外部蒙层不可取消弹窗
        setCanceledOnTouchOutside(isCancel);
        setCancelable(isBackCancel);
        LottieAnimationView lottieAnimationView = findViewById(R.id.animation_view);
        //这个可有可无，如果不涉及本地图片做动画可忽略
        lottieAnimationView.setImageAssetsFolder("images/");
        //设置动画文件
//        lottieAnimationView.setAnimation("images/main_lead.json");
        lottieAnimationView.setAnimation(filePath);
        //是否循环执行
        lottieAnimationView.loop(true);
        //执行动画
        lottieAnimationView.playAnimation();
        lottieAnimationView.setOnClickListener(view -> {
            if (onClick != null) {
                onClick.onClick();

            }
        });
    }

    public interface OnClick {
        void onClick();
    }
}
