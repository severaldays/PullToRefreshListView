package severaldays.pulltorefreshlistview.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ListView;
import android.widget.TextView;
import severaldays.pulltorefreshlistview.R;

/**
 * Created by LingJian·HE on 16/7/28.
 */
public class PullToRefreshListView extends ListView {

    public static final int RELEASE_TO_REFRESH = 0;
    public static final int PULL_TO_REFRESH = 1;
    public static final int REFRESHING = 2;
    public static final int RESET = 3;
    public static final int RATIO = 2;
    private View pullToRefreshView;
    private View pullToRefreshViewContainer;
    private int pullToRefreshHeight;
    private boolean isPullToRefreshing;
    private int lastMotionY;
    private int state;
    private OnRefreshListener refreshListener;
    private ValueAnimator pullAnimator;
    private LoadingProgressDrawable loadingProgressDrawable;
    private TextView loadingProgressText;

    public PullToRefreshListView(Context context) {
        this(context, null);
    }

    public PullToRefreshListView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PullToRefreshListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        pullToRefreshHeight = getResources().getDimensionPixelSize(R.dimen.pullto_refresh_header_view_height);
        pullToRefreshView = LayoutInflater.from(getContext()).inflate(R.layout.pullto_refresh_head_view, null, false);
        pullToRefreshView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, 0));
        pullToRefreshViewContainer = pullToRefreshView.findViewById(R.id.ptr_container);
        pullToRefreshView.setPadding(0, -pullToRefreshHeight, 0, 0);
        loadingProgressText = (TextView) pullToRefreshView.findViewById(R.id.pull_to_refresh_text);
        loadingProgressDrawable =
                (LoadingProgressDrawable) pullToRefreshView.findViewById(R.id.loading_progress_drawable);
        addHeaderView(pullToRefreshView);
        state = RESET;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (getFirstVisiblePosition() == 0 && !isPullToRefreshing) {
                    isPullToRefreshing = true;
                    lastMotionY = (int) event.getY();
                }
                break;

            case MotionEvent.ACTION_UP:
                if (state != REFRESHING) {
                    if (state == PULL_TO_REFRESH) {
                        setState(RESET);

                    }
                    if (state == RELEASE_TO_REFRESH) {
                        setState(REFRESHING);
                    }
                }
                isPullToRefreshing = false;
                break;

            case MotionEvent.ACTION_MOVE:
                int y = (int) event.getY();
                if (!isPullToRefreshing && getFirstVisiblePosition() == 0) {
                    isPullToRefreshing = true;
                    lastMotionY = y;
                }
                int deltaY = y - lastMotionY;
                if (state != REFRESHING && isPullToRefreshing) {
                    if (state == RELEASE_TO_REFRESH) {
                        if ((deltaY / RATIO < pullToRefreshHeight) && deltaY > 0) {
                            setState(PULL_TO_REFRESH);
                        } else if (deltaY <= 0) {
                            setState(RESET);
                        }
                    }
                    if (state == PULL_TO_REFRESH) {
                        if (deltaY / RATIO >= pullToRefreshViewContainer.getMeasuredHeight()) {
                            setState(RELEASE_TO_REFRESH);
                        } else if (deltaY <= 0) {
                            setState(RESET);
                        }
                    }
                    if (state == RESET) {
                        if (deltaY > 0) {
                            setState(PULL_TO_REFRESH);
                        }
                    }
                    if (state == PULL_TO_REFRESH) {
                        updateHeaderView(-1 * pullToRefreshHeight + deltaY / RATIO);
                    } else if (state == RELEASE_TO_REFRESH) {
                        updateHeaderView(deltaY / RATIO - pullToRefreshHeight);
                    }
                }
                break;
            default:
                break;
        }
        return super.onTouchEvent(event);
    }

    private void updateHeaderView(int paddingTop) {
        pullToRefreshView.setPadding(0, paddingTop, 0, 0);
        float process = (paddingTop + pullToRefreshHeight) * 1.0f / pullToRefreshHeight;
        loadingProgressDrawable.setProgress(process);
    }

    private void setState(int state) {
        this.state = state;
        switch (state) {
            case RELEASE_TO_REFRESH:
                onReleaseToRefresh();
                break;
            case PULL_TO_REFRESH:
                onPullToRefresh();
                break;
            case REFRESHING:
                onRefresh();
                break;
            case RESET:
                onReset();
                break;
            default:
                break;
        }
    }

    private void onReleaseToRefresh() {
        loadingProgressText.setText(getResources().getString(R.string.refresh_release_label));
    }

    private void onPullToRefresh() {
        loadingProgressText.setText(getResources().getString(R.string.refresh_pull_label));
    }

    private void onReset() {
        animationToRefreshPosition(-1 * pullToRefreshHeight);
    }

    public void onRefreshComplete() {
        setState(RESET);
    }

    public void onRefreshFailed() {
        setState(RESET);
    }

    private void onRefresh() {
        animationToRefreshPosition(pullToRefreshViewContainer.getMeasuredHeight() - pullToRefreshHeight);
        if (refreshListener != null) {
            refreshListener.onRefresh();
            if (loadingProgressDrawable != null) {
                loadingProgressDrawable.start();
            }
            loadingProgressText.setText(getResources().getString(R.string.refresh_refreshing_label));
        }
    }

    private void animationToRefreshPosition(int toPosition) {
        pullAnimator = ValueAnimator.ofFloat(pullToRefreshView.getPaddingTop(), toPosition);
        pullAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                pullToRefreshView.setPadding(0, (int) value, 0, 0);
                if (-1 * pullToRefreshHeight == value) {
                    loadingProgressDrawable.stop();
                }
            }
        });
        pullAnimator.setInterpolator(new DecelerateInterpolator());
        pullAnimator.start();
    }

    public void setonRefreshListener(OnRefreshListener refreshListener) {
        this.refreshListener = refreshListener;
    }

    public interface OnRefreshListener {
        void onRefresh();
    }

}