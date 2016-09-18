package severaldays.pulltorefreshlistview.activity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.widget.ArrayAdapter;
import severaldays.pulltorefresh.view.PullToRefreshListView;
import severaldays.pulltorefreshlistview.R;

/**
 * Created by LingJian·HE on 16/7/28.
 */
public class MainActivity extends AppCompatActivity {

    private PullToRefreshListView listView;
    private ArrayAdapter<String> arrayAdapter;
    private Handler handler = new Handler();
    private Runnable refreshSuccessRunnable = new Runnable() {
        @Override
        public void run() {
            arrayAdapter.clear();
            arrayAdapter.addAll(generateDate());
            listView.onRefreshComplete();
        }
    };
    private PullToRefreshListView.OnRefreshListener onRefreshListener = new PullToRefreshListView.OnRefreshListener() {
        @Override
        public void onRefresh() {
            refresh();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initListView();
    }

    private void initListView() {
        listView = (PullToRefreshListView) findViewById(R.id.list_view);
        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, generateDate());
        listView.setAdapter(arrayAdapter);
        listView.setonRefreshListener(onRefreshListener);
    }

    private void refresh() {
        handler.postDelayed(refreshSuccessRunnable, 3000);
    }

    private List<String> generateDate() {
        List<String> listItems = new ArrayList<String>();
        for (int i = 0; i < 50; i++) {
            listItems.add("第 " + i + " 个 : "
                    + Calendar.getInstance().get(Calendar.MINUTE) + " : "
                    + Calendar.getInstance().get(Calendar.SECOND));
        }
        return listItems;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(refreshSuccessRunnable);
    }
}
