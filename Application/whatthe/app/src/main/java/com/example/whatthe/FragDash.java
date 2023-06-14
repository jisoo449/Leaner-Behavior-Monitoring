package com.example.whatthe;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

public class FragDash extends Fragment {
    private View view;

    private static String TAG = "phptest";

    private static final String TAG_JSON="webnautes";
    private static final String TAG_TIMESTAMP = "timestamp";
    private static final String TAG_ROUND = "round";
    private static final String TAG_TOTALTIME ="totalTime";
    private static final String TAG_SCORE = "score";
    private static final String TAG_FEEDBACK = "feedback";

    private static final String TAG_BLINK ="blink";
    private static final String TAG_GAZE = "gaze";
    private static final String TAG_SLOPE = "slope";
    private static final String TAG_HAND = "hand";

    private static final String TAG_E0 = "e0";
    private static final String TAG_E1 = "e1";
    private static final String TAG_E2 = "e2";
    private static final String TAG_E3 = "e3";
    private static final String TAG_E4 = "e4";
    private static final String TAG_E5 = "e5";
    private static final String TAG_E6 = "e6";
    private static final String TAG_E7 = "e7";

    private TextView mTextViewResult;
    String mJsonString;
    ArrayList<HashMap<String, studyData>> mArrayList;
    ArrayList<String> dateArray;

    private LineChart chart;
    ArrayList<Entry> values;
    ArrayList<String> xVals;
    XAxis xAxis;

    TextView feedbackTV, emotionResult, emotionResult2;

    LinearLayout eee;

    String userId;

    private Handler mHandler;

    SwipeRefreshLayout layout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater Inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = Inflater.inflate(R.layout.frag_dashboard, container, false);

        userId = getArguments().getString("userId");

        mHandler = new Handler();

        chart = (LineChart) view.findViewById(R.id.Lchart);//layout의 id
        chart.setDescription(null);
        chart.getLegend().setEnabled(false);
        xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setAxisMinValue(-0.03f);
        xAxis.setAxisMaxValue(-0.03f);

        layout = (SwipeRefreshLayout) view.findViewById(R.id.swipe);
        layout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                refresh();

                layout.setRefreshing(false);

            }
        });

        YAxis yAxisRight = chart.getAxisRight(); //Y축의 오른쪽면 설정
        yAxisRight.setDrawLabels(false);
        yAxisRight.setDrawAxisLine(false);
        yAxisRight.setDrawGridLines(false);
        YAxis yAxisLeft = chart.getAxisLeft();
        yAxisLeft.setAxisMinValue(0);
        yAxisLeft.setAxisMaxValue(100);


        feedbackTV = (TextView) view.findViewById(R.id.feedbackTV);

        mTextViewResult = view.findViewById(R.id.textView_main_result);
        emotionResult = view.findViewById(R.id.emotionResult);
        emotionResult2 = view.findViewById(R.id.emotionResult2);

        eee = view.findViewById(R.id.eee);
        eee.setVisibility(View.GONE);

        FragDash.GetData task = new FragDash.GetData();

        task.execute("http://ec2-3-16-89-9.us-east-2.compute.amazonaws.com/dashquery.php?table="+userId); //IP 주소 변경


        return view;
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

            //mTextViewResult.setText(result);
            Log.d(TAG, "response - " + result);

            if (result == null) {
                mTextViewResult.setText(errorString);
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

    class studyData{
        float dscore;
        String dtotalTime, dfeedback;
        String dblink, dgaze, dslope, dhand;
        String dangry, ddisgusting, dfearful, dhappy, dsad, dsurprising, dnuetral, dnoperson;

        studyData(String t, float s, String f, String b, String g, String sl, String h, String e0, String e1, String e2, String e3, String e4, String e5, String e6, String e7){
            this.dtotalTime = t;
            this.dscore = s;
            this.dfeedback = f;

            this.dblink = b;
            this.dgaze = g;
            this.dslope = sl;
            this.dhand = h;
            this.dangry = e0;
            this.ddisgusting = e1;
            this.dfearful = e2;
            this.dhappy = e3;
            this.dsad = e4;
            this.dsurprising = e5;
            this.dnuetral = e6;
            this.dnoperson = e7;

        }
    }

    //참고 http://blog.daum.net/techtip/12415218
    private void showResult() {
        try {
            JSONObject jsonObject = new JSONObject(mJsonString);
            JSONArray jsonArray = jsonObject.getJSONArray(TAG_JSON);

            dateArray = new ArrayList<>();
            mArrayList = new ArrayList<>();

            for (int i = 0; i < jsonArray.length(); i++) {

                JSONObject item = jsonArray.getJSONObject(i);

                HashMap<String, studyData> hashMap = new HashMap<>();
                hashMap.put(item.getString(TAG_TIMESTAMP),new studyData(
                        item.getString(TAG_TOTALTIME),item.getInt(TAG_SCORE),item.getString(TAG_FEEDBACK),item.getString(TAG_BLINK),
                        item.getString(TAG_GAZE),item.getString(TAG_SLOPE),item.getString(TAG_HAND),item.getString(TAG_E0),
                        item.getString(TAG_E1),item.getString(TAG_E2),item.getString(TAG_E3),item.getString(TAG_E4),
                        item.getString(TAG_E5),item.getString(TAG_E6),item.getString(TAG_E7)));

                String d = item.getString(TAG_TIMESTAMP);

                dateArray.add(d);
                mArrayList.add(hashMap);
            }

            setCctGraph();

        } catch (JSONException e) {

            Log.d(TAG, "showResult : ", e);
        }

    }

    private void setCctGraph() {

        values = new ArrayList<>();
        xVals = new ArrayList<String>();

        for(int i = 0;i<dateArray.size();i++){
            String date = dateArray.get(i);
            studyData s = mArrayList.get(i).get(date);
            values.add(new Entry(s.dscore, i));
            xVals.add(date.substring(4,6)+"월"+date.substring(6)+"일");
        }

        //values.add(new Entry(Float.parseFloat(item.getString(TAG_SCORE)), i));

        LineDataSet set1 = new LineDataSet(values, "DataSet 1");
        set1.setColor(ContextCompat.getColor(getContext(), R.color.etoos)); //LineChart에서 Line Color 설정
        set1.setCircleColor(ContextCompat.getColor(getContext(), R.color.etoos)); // LineChart에서 Line Circle Color 설정
        set1.setLineWidth(2); // 선 굵기
        set1.setCircleRadius(6); // 곡률
        set1.setHighLightColor(ContextCompat.getColor(getContext(), R.color.colorAccent));
        set1.setHighlightLineWidth(2);

        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(set1); // add the data sets

        LineData data = new LineData(xVals, dataSets);
        data.setValueTextSize(10);
        chart.setData(data);
        chart.setTouchEnabled(true);
        CustomMarkerView mv = new CustomMarkerView (getContext(), R.layout.frag_dashboard);
        chart.setMarkerView(mv);
        chart.invalidate();
    }

    class CustomMarkerView extends MarkerView {

        public CustomMarkerView (Context context, int layoutResource) {
            super(context, layoutResource);
            // this markerview only displays a textview
        }

        // callbacks everytime the MarkerView is redrawn, can be used to update the
        // content (user-interface)
        @Override
        public void refreshContent(Entry e, Highlight highlight) {
            int i = e.getXIndex();
            studyData sdate = mArrayList.get(i).get(dateArray.get(i));
            mHandler.post(new FragDash.result_ex(sdate));
        }

        @Override
        public int getXOffset(float xpos) {
            return 0;
        }

        @Override
        public int getYOffset(float ypos) {
            return 0;
        }
    }

    class result_ex implements Runnable {
        String feedback, totalTime;
        long blink, gaze, slope, hand;
        Double angry, disgusting, fearful, happy, sad, surprising, nuetral, noperson;


        public result_ex(studyData sd){
            this.feedback = sd.dfeedback;
            this.totalTime = sd.dtotalTime;
            this.blink = Math.round(Double.parseDouble(sd.dblink));
            this.gaze = Math.round(Double.parseDouble(sd.dgaze));
            this.slope = Math.round(Double.parseDouble(sd.dslope));
            this.hand = Math.round(Double.parseDouble(sd.dhand));
            this.angry = Math.round(Double.parseDouble(sd.dangry)*10)/10.0;
            this.disgusting = Math.round(Double.parseDouble(sd.ddisgusting)*10)/10.0;
            this.fearful = Math.round(Double.parseDouble(sd.dfearful)*10)/10.0;
            this.happy = Math.round(Double.parseDouble(sd.dhappy)*10)/10.0;
            this.sad = Math.round(Double.parseDouble(sd.dsad)*10)/10.0;
            this.surprising = Math.round(Double.parseDouble(sd.dsurprising)*10)/10.0;
            this.nuetral = Math.round(Double.parseDouble(sd.dnuetral)*10)/10.0;
            this.noperson = Math.round(Double.parseDouble(sd.dnoperson)*10)/10.0;
        }
        @Override
        public void run() {
            if (feedback.length() == 0 || feedback.contains("null")) {
                feedbackTV.setText("피드백이 아직 없습니다.");
                feedbackTV.setTextColor(Color.parseColor("#AAAAAA"));
            } else {
                feedbackTV.setText(feedback);
                feedbackTV.setTextColor(Color.parseColor("#000000"));
            }

            String[] time_ary = totalTime.split(",");
            ArrayList<String> finalTime = new ArrayList<>();
            for (int i = 0; i < time_ary.length; i++) {
                finalTime.add(time_ary[i].trim());
            }
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
            Calendar cal = Calendar.getInstance();
            if (!finalTime.isEmpty()) {
                Date date = null;
                try {
                    date = timeFormat.parse(finalTime.get(0));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                cal.setTime(date);

                for (int i = 1; i < finalTime.size(); i++) {
                    String[] array = finalTime.get(i).split(":");
                    cal.add(Calendar.HOUR, Integer.parseInt(array[0]));
                    cal.add(Calendar.MINUTE, Integer.parseInt(array[1]));
                    cal.add(Calendar.SECOND, Integer.parseInt(array[2]));
                }

                mTextViewResult.setText("\t 총 공부 시간 : " + timeFormat.format(cal.getTime()) + "\n\n\t 졸음 시간 : " + blink + " 초 \n\n\t 시선 이탈 : " + gaze + " 회 \n\n\t 자세 불량 : " + slope + " 회 \n\n\t 산만한 태도 : " + hand + " 회");
                emotionResult.setText("\t 분노 : "+angry+" % \n\n\t 역겨움 : "+disgusting+" % \n\n\t 공포 : "
                        +fearful+" % \n\n\t 행복 : "+happy+" %");
                emotionResult2.setText("\t 슬픔 : "+sad+" % \n\n\t 놀람 : "+surprising+" % \n\n\t 무표정 : "+nuetral+" % \n\n\t etc. : "+noperson+ "%");

                eee.setVisibility(View.VISIBLE);
            }
            }
        }

    private void refresh(){
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.detach(this).attach(this).commit();
    }
}
