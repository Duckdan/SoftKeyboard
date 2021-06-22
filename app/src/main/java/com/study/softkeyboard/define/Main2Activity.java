package com.study.softkeyboard.define;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;

import com.blankj.utilcode.util.KeyboardUtils;
import com.blankj.utilcode.util.Utils;
import com.study.softkeyboard.R;

import androidx.appcompat.app.AppCompatActivity;

/**
 * 总结：点击可输入控件弹出软键盘与点击其它控件弹出软键盘的效果是不一样的
 * 按照getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING | WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);方式设置，
 * getViewTreeObserver无法检测到布局的变化。在清单文件设置android:windowSoftInputMode="stateAlwaysHidden|adjustNothing"也是一样的
 * <p>
 * 点击可输入控件弹出软键盘，会影响布局。
 * 点击其它控件弹出软键盘，不会影响布局。
 */
public class Main2Activity extends AppCompatActivity {
    //输入控件
    public static final String TAG = "MainActivity";
    private InputView inputView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /**
         * 点击可输入控件时的效果：
         * 不改变布局，也不影响总体布局高度。不过如果输入框所在位置与屏幕底部之间的距离小于软键盘的高度，那么将会导致软键盘遮盖住输入框。
         *
         */
//        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING | WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        /**
         * 点击可输入控件时的效果：
         * 1.输入控件与屏幕底部之间的距离不足以放下一个软件盘的高度时，改变布局但是不会修改总体布局的高度。可以看成在输入框后面添加了一个软件盘的布局，但是又不是完全是这样
         * 2.输入控件与屏幕底部之间的距离足以放下一个软键盘高度时，软件盘的弹起不会影响布局
         */
//        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN|WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        /**
         * 点击可输入控件时的效果：
         * 不论输入控件与屏幕底部之间的距离是否足以放下一个软键盘的高度，软键盘的弹起都会改变布局，同时会修改总体布局的高度。
         * 软键盘布局会顶起软键盘弹起时会遮盖住的布局，同时折叠头部与尾部之间控件的布局
         */
//        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setContentView(R.layout.activity_main);

        dealKeyboard(getWindow(), this);
    }

    public void dealKeyboard(Window window, Activity activity) {

        // 方式一：
        inputView = new InputView(this, KeyBoardManager.getKeyboardHeight(this), KeyBoardManager.getKeyboardHeightLandspace(this));
        inputView.add2Window(this);

        inputView.setOnHeightReceivedListener((screenOri, height) -> {
            if (screenOri == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                KeyBoardManager.setKeyboardHeight(this, height);
            } else {
                KeyBoardManager.setKeyboardHeightLandspace(this, height);
            }

        });

        findViewById(R.id.text_chat_content).setOnClickListener(v -> {
            inputView.show();
        });
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        keyboardDismiss();
    }


    @Override
    protected void onUserLeaveHint() {
        keyboardDismiss();
        super.onUserLeaveHint();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        keyboardDismiss();
    }

    private void keyboardDismiss() {
        if (null != inputView) {
            inputView.dismiss();
        }
    }
}
