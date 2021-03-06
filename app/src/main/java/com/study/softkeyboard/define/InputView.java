package com.study.softkeyboard.define;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.blankj.utilcode.util.BarUtils;
import com.study.softkeyboard.MainActivity;
import com.study.softkeyboard.R;

/**
 * Created by huanan on 2016/3/14.
 */
public class InputView {

    private static final String TAG = "InputView";
    private View contentView;
    private EditText et_content;
    private TextView tv_send;
    private View bg;
    Context context;
    Activity activity;
    private boolean hasVirtual = false; // 是否有虚拟按键
    private int virtualHeight = 0;  // 虚拟按键的高度
    private int keyboardHeight = 0;

    private int limitNo = 280;
    private ClickCallback mCallback;



    private int keyboardHeight_portrait = 0;
    private int keyboardHeight_landspace = 0;

    private InputUser user = null;

    public View getContentView() {
        return contentView;
    }

    //发送点击回调
    public interface SendMsgClickListener {
        void onSendClick(String msg, InputUser user);
    }

    SendMsgClickListener onSendClickListener;

    public void setOnSendClickListener(SendMsgClickListener onSendClickListener) {
        this.onSendClickListener = onSendClickListener;
    }

    //键盘高度获取回调
    public interface KeyboardHeightListener {
        void onHeightReceived(int screenOri, int height);
    }

    KeyboardHeightListener onHeightReceivedListener;

    public void setOnHeightReceivedListener(KeyboardHeightListener onHeightReceivedListener) {
        this.onHeightReceivedListener = onHeightReceivedListener;
    }


    public InputView(Context context, int protraitHeight, int landspaceHeight) {
        this.context = context;
        keyboardHeight_portrait = protraitHeight;
        keyboardHeight_landspace = landspaceHeight;
        initView();
        hasVirtual = KeyBoardManager.hasVirtualButton(context);
        if (hasVirtual) {
            virtualHeight = KeyBoardManager.getVirtualButtonHeight(context);
        }
    }


    public void initView() {
        contentView = View.inflate(context, R.layout.show_input_window_layout, null);
        et_content = (EditText) contentView.findViewById(R.id.et_content);
        tv_send = (TextView) contentView.findViewById(R.id.tv_send);
        contentView.setVisibility(View.GONE);
        bg = (View) contentView.findViewById(R.id.view_bg);

        bg.setOnClickListener(v -> dismiss());


        et_content.setOnClickListener(v -> {
            show();
        });
        tv_send.setOnClickListener(v -> {
            if (onSendClickListener != null) {
                String msg = et_content.getText().toString();
                if (msg.contains("@") && msg.contains(":")) {
                    msg = msg.substring(msg.indexOf(":") + 1);
                }
                if (user != null) {
                    String text = "@" + user.userName + ":";
                    msg = text + msg;
                }
                if (TextUtils.isEmpty(msg.trim())) {
                    Toast.makeText(context, "", Toast.LENGTH_SHORT).show();
                    return;
                }
                onSendClickListener.onSendClick(msg, user);
                et_content.setText("");
                dismiss();
            }
        });
        et_content.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
                if (arg1 == EditorInfo.IME_ACTION_SEND
                        || (arg2 != null && arg2.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    tv_send.performClick();
                    return true;
                }
                return false;
            }
        });

        et_content.setHint("我来说两句");
    }



    public void add2Window(Activity activity) {
        this.activity = activity;
        FrameLayout layout = (FrameLayout) activity.getWindow().getDecorView().findViewById(android.R.id.content);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.BOTTOM;
        layout.addView(contentView, params);
        observeSoftKeyboard(activity);
    }

    public void show() {
        if (user != null) {
            String text = "@" + user.userName + ":";
            et_content.setText(text);
            et_content.setSelection(et_content.getText().length());
        }
        et_content.requestFocus();

        showKeyboard();

    }

    private void showEmoji() {
        if (null != mCallback) {
            mCallback.onEmojiClick();
        }
        final FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) contentView.getLayoutParams();
        KeyBoardManager.closeKeyboard(et_content, activity);
        if (keyboardHeight > 0) {
        }
        contentView.setVisibility(View.VISIBLE);
        new Handler() {//延时0.2秒显示表情
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                params.setMargins(0, 0, 0, 0);
                contentView.setLayoutParams(params);

            }
        }.sendEmptyMessageDelayed(1, 200);
    }

    private void showKeyboard() {
        final FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) contentView.getLayoutParams();
        if (activity.getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            if (keyboardHeight_portrait == 0) {
//                int height = Screen.getScreenHeight(activity);
                params.setMargins(0, 0, 0, 800);
                contentView.setLayoutParams(params);
                KeyBoardManager.openKeyboard(et_content, activity);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        params.setMargins(0, 0, 0, keyboardHeight_portrait);
                        contentView.setLayoutParams(params);

                        contentView.setVisibility(View.VISIBLE);
                    }
                }, 300);
                return;
            }
            params.setMargins(0, 0, 0, keyboardHeight_portrait);
        } else {
            if (keyboardHeight_landspace == 0) {
                keyboardHeight_landspace = 840;
            }
            params.setMargins(0, 0, 0, keyboardHeight_landspace);

        }
        if (contentView.getVisibility() == View.VISIBLE) {
            contentView.setLayoutParams(params);

            contentView.setVisibility(View.VISIBLE);
            KeyBoardManager.openKeyboard(et_content, activity);
            KeyBoardManager.openKeyboard(et_content, activity);//保留
        } else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    contentView.setLayoutParams(params);

                    contentView.setVisibility(View.VISIBLE);
                    KeyBoardManager.openKeyboard(et_content, activity);
                }
            }, 300);
        }


    }

    public void dismiss() {
        if (contentView.getVisibility() == View.GONE) {
            return;
        }
        KeyBoardManager.closeKeyboard(et_content, activity);
        new Handler() {//延时0.2秒显示表情
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) contentView.getLayoutParams();
                params.setMargins(0, 0, 0, 0);
                contentView.setLayoutParams(params);

                contentView.setVisibility(View.GONE);
            }
        }.sendEmptyMessageDelayed(1, 200);


    }

    public void observeSoftKeyboard(final Activity activity) {
        final View decorView = activity.getWindow().getDecorView();
        decorView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            int previousKeyboardHeight = -1;

            @Override
            public void onGlobalLayout() {
                Rect rect = new Rect();
                decorView.getWindowVisibleDisplayFrame(rect);
                int displayHeight = rect.bottom - rect.top;
                int height = decorView.getHeight();
                /** 是否存在虚拟键盘*/
                if (hasVirtual && virtualHeight > 0) {
                    keyboardHeight = height - displayHeight - rect.top - virtualHeight;
                } else {
                    keyboardHeight = height - displayHeight - rect.top;
                }
                Log.e(MainActivity.TAG, keyboardHeight + "===1==" + previousKeyboardHeight);
                if (previousKeyboardHeight != keyboardHeight) {
                    boolean hide = (double) displayHeight / height > 0.8;
                    if (hide) {
                        dismiss();
                    }
                    if (!hide) {
                        final FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) contentView.getLayoutParams();
                        if (activity.getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
                            keyboardHeight_portrait = keyboardHeight + BarUtils.getStatusBarHeight();
                            if (onHeightReceivedListener != null) {
                                onHeightReceivedListener.onHeightReceived(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT, keyboardHeight_portrait);
                            }
                            Log.e(MainActivity.TAG, keyboardHeight + "===2==" + BarUtils.getStatusBarHeight());
                            params.setMargins(0, 0, 0, keyboardHeight_portrait);
                        } else if (activity.getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                            keyboardHeight_landspace = keyboardHeight + BarUtils.getStatusBarHeight();
                            if (onHeightReceivedListener != null) {
                                onHeightReceivedListener.onHeightReceived(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE, keyboardHeight_landspace);
                            }
                            Log.e(MainActivity.TAG, keyboardHeight + "===3==" + BarUtils.getStatusBarHeight());
                            params.setMargins(0, 0, 0, keyboardHeight_landspace);
                        }
                        contentView.setLayoutParams(params);
                    }
                }
                previousKeyboardHeight = keyboardHeight;
            }
        });
    }

    public interface ClickCallback {
        void onEmojiClick();
    }
}
