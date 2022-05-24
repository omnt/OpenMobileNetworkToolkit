package de.fraunhofer.fokus.OpenMobileNetworkToolkit;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.lang.ref.WeakReference;
import java.security.Provider;



public class DebuggerService extends Service {

    private boolean isMinimized = true;
    private boolean isShowing = true;

    private WindowManager mWindowManager;
    RelativeLayout root_view;
    private View mView;
    private TextView textView;
    private ScrollView scrollView;
    private AppCompatImageView btnResize;
    private AppCompatImageView btnClose;
    private AppCompatImageView btnReset;
    private CharSequence msg = "";
    private Spanned spanColor;

    int paramWidth;
    int paramHeight;
    float scale;

    private static WeakReference<DebuggerService> mWeakReferenceContext;

    public static void init(DebuggerService ctx) {
        mWeakReferenceContext = new WeakReference<>(ctx);
    }

    /**
     * get WeakReference instance of {@link DebuggerService} class
     */

    public static DebuggerService getContext() {
        try {
            if (null != mWeakReferenceContext && null != mWeakReferenceContext.get()) {
                return mWeakReferenceContext.get();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground(1, new Notification());
        }

        init(this);

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        paramWidth = (int) (metrics.widthPixels * 0.7f);
        paramHeight = (int) (metrics.heightPixels * 0.45f);

        scale = metrics.density;

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        initUI();
        moveView();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        try {
            if (null != mView && null != mWindowManager) {
                mWindowManager.removeView(mView);
                mWindowManager = null;
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }


        super.onDestroy();
    }

    WindowManager.LayoutParams mWindowsParams;

    private void moveView() {


        initializeLayoutParams(paramWidth, paramHeight);


        mWindowsParams.gravity = Gravity.TOP | Gravity.LEFT;
        mWindowsParams.y = 100;
        mWindowManager.addView(mView, mWindowsParams);


        mView.setOnTouchListener(new View.OnTouchListener() {

            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;

            long startTime = System.currentTimeMillis();


            @Override
            public boolean onTouch(View v, MotionEvent event) {


                if (System.currentTimeMillis() - startTime <= 300) {
                    return false;
                }


//                switch (event.getAction()) {
                switch (event.getActionMasked()) {

                    case MotionEvent.ACTION_DOWN:
                        initialX = mWindowsParams.x;
                        initialY = mWindowsParams.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        break;

                    case MotionEvent.ACTION_UP:
                        break;

                    case MotionEvent.ACTION_MOVE:
                        mWindowsParams.x = initialX + (int) (event.getRawX() - initialTouchX);
                        mWindowsParams.y = initialY + (int) (event.getRawY() - initialTouchY);
                        mWindowManager.updateViewLayout(mView, mWindowsParams);
                        break;
                }
//                mView.invalidate();
                return false;
            }
        });


        btnResize.setOnClickListener(view -> {
            if (isMinimized) {
                initializeLayoutParams(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
                btnResize.setImageResource(R.drawable.ic_maximize);
                isMinimized = false;
            } else {
                initializeLayoutParams(paramWidth, paramHeight);
                btnResize.setImageResource(R.drawable.ic_minimize);
                isMinimized = true;
            }
            mWindowManager.updateViewLayout(mView, mWindowsParams);
        });


        mView.findViewById(R.id.btnDrop).setOnClickListener(view -> {
            if (isShowing) {
//                scrollView.setLayoutParams(new RelativeLayout.LayoutParams(dropSize, dropSize));
                btnClose.setVisibility(View.GONE);
                btnResize.setVisibility(View.GONE);
                btnReset.setVisibility(View.GONE);
                root_view.getLayoutParams().width = (int) (55 * scale + 0.5f);
                root_view.getLayoutParams().height = (int) (40 * scale + 0.5f);
                isShowing = false;

            } else {
                btnClose.setVisibility(View.VISIBLE);
                btnResize.setVisibility(View.VISIBLE);
                btnReset.setVisibility(View.VISIBLE);
                root_view.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
                root_view.getLayoutParams().width = ViewGroup.LayoutParams.WRAP_CONTENT;
                btnResize.setImageResource(R.drawable.ic_minimize);
                isMinimized = true;
                initializeLayoutParams(paramWidth, paramHeight);
                isShowing = true;
            }
            mWindowManager.updateViewLayout(mView, mWindowsParams);
        });


    }

    private void initializeLayoutParams(int width, int height) {
        // if you got any problems with FLAG_NOT_FOCUSABLE replace it with FLAG_WATCH_OUTSIDE_TOUCH
        // FLAG_WATCH_OUTSIDE_TOUCH will cause keyboard will not pop up and back press will not work
        // this is why I replace it with FLAG_NOT_FOCUSABLE
        mWindowsParams =
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                        ?
                        new WindowManager.LayoutParams(width, height, WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT)
                        :
                        new WindowManager.LayoutParams(width, height, WindowManager.LayoutParams.TYPE_PHONE, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT);
    }


    private void initUI() {
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (layoutInflater != null) {
            mView = layoutInflater.inflate(R.layout.debugger_layout, null);
            root_view = mView.findViewById(R.id.root_debugger);
            scrollView = mView.findViewById(R.id.scroll_view);
            btnResize = mView.findViewById(R.id.btnResize);
            btnClose = mView.findViewById(R.id.btnClose);
            btnReset = mView.findViewById(R.id.btnReset);
            btnClose.setOnClickListener(view -> stopSelf());
            btnReset.setOnClickListener(view -> {
                if (null != textView) {
                    textView.setText(null);
                }
                msg = "";
                spanColor = null;
                textView = null;
            });
        }
    }


    /**
     * Don't Change the order of this method code lines.
     */
    private void addTextViewToLayout(String text, int textColor) {
        spanColor = setSpanColor(text, ContextCompat.getColor(this, textColor));
        msg = TextUtils.concat(msg, "\n\n", spanColor);
        if (null == textView) {
            textView = mView.findViewById(R.id.message_output);
            textView.setGravity(Gravity.LEFT);
        }
        textView.setText(msg);

        if (!scrollView.canScrollVertically(1)) {
            scrollView.post(() -> {
                scrollView.smoothScrollTo(0, scrollView.getHeight());
                scrollView.fullScroll(ScrollView.FOCUS_DOWN);
            });
        }
        mWindowManager.updateViewLayout(mView, mWindowsParams);
    }

    private Spanned setSpanColor(String s, int color) {
        SpannableString ss = new SpannableString(s);
        ss.setSpan(new ForegroundColorSpan(color), 0, s.length(), 0);
        return ss;
    }

    /*public static void setDebugText(String msg, int textColor) {
        if (null != getContext()) getContext().addTextViewToLayout(msg, textColor);
    }*/


    public static void stop() {
        if (null != getContext()) getContext().stopSelf();
    }

}
