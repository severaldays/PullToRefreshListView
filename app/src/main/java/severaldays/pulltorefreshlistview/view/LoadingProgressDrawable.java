package severaldays.pulltorefreshlistview.view;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.widget.ImageView;
import severaldays.pulltorefreshlistview.R;

/**
 * Created by LingJian·HE on 16/7/28.
 */
public class LoadingProgressDrawable extends ImageView {

    private static final int ANIMATION_START = 0;
    private static final int ANIMATION_STOP = 1;
    private float curRotation = 0;
    private boolean isRunning;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case ANIMATION_START:
                    isRunning = true;
                    if (curRotation == 360) {
                        curRotation = 0;
                    } else {
                        curRotation += 10;
                    }
                    invalidate();
                    handler.removeMessages(ANIMATION_START);
                    handler.sendEmptyMessageDelayed(ANIMATION_START, 10);
                    break;
                case ANIMATION_STOP:
                    isRunning = false;
                    curRotation = 0;
                    invalidate();
                    handler.removeMessages(ANIMATION_START);
                    handler.removeMessages(ANIMATION_STOP);
                    break;
                default:
                    break;
            }
        }
    };

    public LoadingProgressDrawable(Context context) {
        this(context, null);
    }

    public LoadingProgressDrawable(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LoadingProgressDrawable(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.translate(getWidth() / 2, getHeight() / 2);
        canvas.rotate(curRotation);
        canvas.translate(-getWidth() / 2, -getHeight() / 2);
        super.onDraw(canvas);

    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (getDrawable() == null) {
            setImageDrawable(getResources().getDrawable(R.drawable.ic_pull_refresh));
        }
    }

    public void start() {
        if (!isRunning) {
            curRotation = getRotation();
            handler.removeMessages(ANIMATION_START);
            handler.sendEmptyMessageDelayed(ANIMATION_START, 10);
        }
    }

    public void stop() {
        if (isRunning) {
            handler.removeMessages(ANIMATION_STOP);
            handler.sendEmptyMessage(ANIMATION_STOP);
        }
    }

    public void show() {
        start();
        setVisibility(VISIBLE);
    }

    public void dismiss() {
        stop();
        setVisibility(GONE);
    }

    public void setProgress(float percent) {
        setVisibility(VISIBLE);
        if (isRunning) {
            stop();
        }
        curRotation = percent * 360 % 360;
        invalidate();
    }

}

