package com.example.whatthe;

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentTransaction;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager.widget.ViewPager;

import com.dinuscxj.progressbar.CircleProgressBar;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class FragHome extends Fragment {
    private View view;

    private Fragment fragFirst;
    private Fragment fragSecond;
    private Fragment fragThird;

    private ViewPager pager;

    CircleProgressBar graph;
    Button next;
    Button previous;
    TextView studyTime;

    String userId;
    String date;

    String mJsonString;

    private static String TAG = "phptest";

    private static final String TAG_JSON="webnautes";
    private static final String TAG_TOTALTIME ="totalTime";
    private static final String TAG_SCORE = "score";
    private static final String TAG_ROUND = "round";

    private Handler mHandler;

    SwipeRefreshLayout layout;

    private int currentPage = 0;
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater Inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState){
        view = Inflater.inflate(R.layout.frag_home, container, false);

        userId = getArguments().getString("userId");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        date = LocalDate.now().format(formatter);

        layout = (SwipeRefreshLayout) view.findViewById(R.id.swipe);
        layout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                refresh();

                layout.setRefreshing(false);

            }
        });

        FragHome.GetData task = new FragHome.GetData();
        task.execute("http://ec2-3-16-89-9.us-east-2.compute.amazonaws.com/homequery.php?table="+userId+"&today="+date);

        graph = (CircleProgressBar)view.findViewById(R.id._graph);
        studyTime = (TextView)view.findViewById(R.id.study_time);

        mHandler = new Handler();

        next = (Button) view.findViewById(R.id.but_next);
        previous = (Button) view.findViewById(R.id.but_pre);

        Bundle bundle = new Bundle(1); // 파라미터는 전달할 데이터 개수
        bundle.putString("userId", userId); // key , value

        fragFirst = new Frag_First();
        fragSecond = new Frag_Second();
        fragThird = new Frag_Third();

        fragFirst.setArguments(bundle);
        fragSecond.setArguments(bundle);
        fragThird.setArguments(bundle);

        pager = (ViewPager) view.findViewById(R.id.pager);
        pager.setAdapter(new PagerAdapter(getChildFragmentManager()));
        pager.setCurrentItem(currentPage);

        next.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                currentPage+=1;
                if(currentPage>=3) currentPage = 2;
                pager.setCurrentItem(currentPage);
            }
        });

        previous.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                currentPage-=1;
                if(currentPage<0) currentPage = 0;
                pager.setCurrentItem(currentPage);
            }
        });

        return view;
    }

    private class PagerAdapter extends FragmentPagerAdapter {
        public PagerAdapter(FragmentManager fm){
            super(fm);
        }

        @Override
        public Fragment getItem(int position){
            if(position == 0){
                return fragFirst;
            }
            else if(position == 1){
                return fragSecond;
            }
            else if(position == 2){
                return fragThird;
            }

            return null;
        }

        @Override
        public int getCount(){
            return 3;
        }
    }

    private class GetData extends AsyncTask<String, Void, String> {
        String errorString = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            Log.d(TAG, "response - " + result);

            if (result == null) {
            } else {
                mJsonString = result;
                showResult();
            }
        }

        @Override
        protected String doInBackground(String... params) {
            String serverURL = params[0];

            try {
                URL url = new URL(serverURL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

                httpURLConnection.setReadTimeout(5000);
                httpURLConnection.setConnectTimeout(5000);
                httpURLConnection.connect();

                int responseStatusCode = httpURLConnection.getResponseCode();
                Log.d(TAG, "response code - " + responseStatusCode);

                InputStream inputStream;
                if (responseStatusCode == HttpURLConnection.HTTP_OK) {
                    inputStream = httpURLConnection.getInputStream();
                } else {
                    inputStream = httpURLConnection.getErrorStream();
                }

                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                StringBuilder sb = new StringBuilder();
                String line;

                while ((line = bufferedReader.readLine()) != null) {
                    sb.append(line);
                }


                bufferedReader.close();


                return sb.toString().trim();
            } catch (Exception e) {

                Log.d(TAG, "InsertData: Error ", e);
                errorString = e.toString();

                return null;
            }
        }
    }

    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
    int score = 0;
    int round = 0;
    ArrayList<String> totalTime_ary;

    class result_ex implements Runnable {
        private int finalScore;
        private ArrayList<String> finalTime;

        public result_ex(int score, int r, ArrayList<String> tta){
            if(r != 0) {
                this.finalScore = score / r;
            }else this.finalScore = 0;
            this.finalTime = tta;
        }
        @Override
        public void run() {
            graph.setProgress(finalScore);
            score = 0;
            round = 0;
            try {
                Calendar cal = Calendar.getInstance();
                if(!finalTime.isEmpty()) {
                    Date date = timeFormat.parse(finalTime.get(0));
                    cal.setTime(date);

                    for (int i = 1; i < finalTime.size(); i++) {
                        String[] array = finalTime.get(i).split(":");
                        cal.add(Calendar.HOUR, Integer.parseInt(array[0]));
                        cal.add(Calendar.MINUTE, Integer.parseInt(array[1]));
                        cal.add(Calendar.SECOND, Integer.parseInt(array[2]));
                    }
                    studyTime.setText(timeFormat.format(cal.getTime()));
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    //참고 http://blog.daum.net/techtip/12415218
    private void showResult() {
        try {
            JSONObject jsonObject = new JSONObject(mJsonString);
            JSONArray jsonArray = jsonObject.getJSONArray(TAG_JSON);

            totalTime_ary = new ArrayList<>();

            for (int i = 0; i < jsonArray.length(); i++) {

                JSONObject item = jsonArray.getJSONObject(i);
                score += item.getInt(TAG_SCORE);
                round = item.getInt(TAG_ROUND);
                totalTime_ary.add(item.getString(TAG_TOTALTIME));
            }
            mHandler.post(new result_ex(score, round, totalTime_ary));

        } catch (JSONException e) {

            Log.d(TAG, "showResult : ", e);
        }

    }

    private void refresh(){
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.detach(this).attach(this).commit();
    }
}
