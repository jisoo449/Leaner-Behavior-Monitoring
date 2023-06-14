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
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;


public class Frag_First extends Fragment {
    private View view;

    private static String TAG = "phptest";

    private static final String TAG_JSON="webnautes";
    private static final String TAG_EMOTION = "emotion";
    private static final String TAG_BLINK ="blink";
    private static final String TAG_GAZE = "gaze";
    private static final String TAG_SLOPE = "slope";
    private static final String TAG_HAND = "hand";
    private static final String TAG_SCORE ="score";

    private static final String TAG_TOTALTIME = "totalTime";
    private static final String TAG_E0 = "e0";
    private static final String TAG_E1 = "e1";
    private static final String TAG_E2 = "e2";
    private static final String TAG_E3 = "e3";
    private static final String TAG_E4 = "e4";
    private static final String TAG_E5 = "e5";
    private static final String TAG_E6 = "e6";
    private static final String TAG_E7 = "e7";

    private TextView mTextViewResult;
    ArrayList<HashMap<String, String>> mArrayList;
    String mJsonString;

    HorizontalBarChart cctchart, emochart;
    int UnCct = 0xFFDDD761, Cct20 = 0xFFC1D076, Cct40 = 0xFFA9C98B, Cct60 = 0xFF8FC2A0, Cct = 0xFF76BBB6;
    int Angry = 0xFFE77474, Disgusting = 0xFF50607E, Fearful = 0xFF8268B1, Happy = 0xFFFFDFA1,
            Sad = 0xFF96DFD7, Surprising = 0xFFEAB09E, Neutral = 0xFFB4B4B4, NoPerson = 0xFF3E3E3E;
    int[] color, color_e;
    float[] always;

    String userId;
    String date;

    TextView timeScore, emotionResult, emotionResult2;
    Button _study, _emotion;
    LinearLayout eee;
    boolean _study_open = false, _emotion_open = false;

    Handler _study_view, _emotion_view;

    String time = "0", score = "0", emotion = "0", gaze = "0", blink = "0", slope = "0", hand = "0";
    String angry = "0", disgusting = "0", fearful = "0", happy = "0", sad = "0", surprising = "0", neutral = "0", noperson = "0";

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater Inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = Inflater.inflate(R.layout.first_study, container, false);

        userId = getArguments().getString("userId");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        date = LocalDate.now().format(formatter);

        timeScore = view.findViewById(R.id.timescore);
        emotionResult = view.findViewById(R.id.emotionResult);
        emotionResult2 = view.findViewById(R.id.emotionResult2);
        mTextViewResult = view.findViewById(R.id.textView_main_result);

        eee = view.findViewById(R.id.eee);

        mTextViewResult.setVisibility(View.GONE);

        eee.setVisibility(View.GONE);

        _emotion_view = new Handler();
        _study_view = new Handler();

        _study = (Button)view.findViewById(R.id.add_study);
        _study.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(_study_open == false){
                    _study_open = true;
                    _study_view.post(new Frag_First.result_study());
                }else{
                    _study_open = false;
                    _study_view.post(new Frag_First.result_study());
                }
            }
        });

        _emotion = (Button)view.findViewById(R.id.add_emotion);
        _emotion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(_emotion_open == false){
                    _emotion_open = true;
                    _emotion_view.post(new Frag_First.result_emotion());
                }else{
                    _emotion_open = false;
                    _emotion_view.post(new Frag_First.result_emotion());
                }
            }
        });

        mArrayList = new ArrayList<>();
        GetData task = new GetData();
        GetData_final task_final = new GetData_final();

        task.execute("http://ec2-3-16-89-9.us-east-2.compute.amazonaws.com/query.php?table="+userId+"&date="+date+"&num=0"); //IP 주소 변경
        task_final.execute("http://ec2-3-16-89-9.us-east-2.compute.amazonaws.com/queryfinal.php?table="+userId+"&date="+date+"&num=0");

        cctchart = (HorizontalBarChart) view.findViewById(R.id.concentrationChart);
        emochart = (HorizontalBarChart) view.findViewById(R.id.emotionChart);

        cctchart.setDescription("집중도");
        cctchart.getAxisRight().setDrawLabels(false);
        cctchart.getXAxis().setDrawLabels(false);
        cctchart.getLegend().setEnabled(false);
        cctchart.setTouchEnabled(false);
        cctchart.getXAxis().setDrawLabels(false);

        emochart.setDescription("감정");
        emochart.getAxisRight().setDrawLabels(false);
        emochart.getXAxis().setDrawLabels(false);
        emochart.getLegend().setEnabled(false);
        emochart.setTouchEnabled(false);
        emochart.getXAxis().setDrawLabels(false);

        return view;
    }

    private class GetData_final extends AsyncTask<String, Void, String>{
        String errorString = null;

        @Override
        protected void onPreExecute(){
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String result){
            super.onPostExecute(result);

            Log.d(TAG, "response - "+result);

            if(result.equals("아직 공부를 안했")){
                timeScore.setText("카메라 버튼을 눌러 공부를 시작하세요!");
                time = score = gaze = blink = slope = hand = angry = disgusting = fearful = happy = sad = surprising = neutral = noperson = "0";
            }else{
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    JSONArray jsonArray = jsonObject.getJSONArray(TAG_JSON);
                    JSONObject j = jsonArray.getJSONObject(0);
                    time = j.getString(TAG_TOTALTIME);
                    score = j.getString(TAG_SCORE);
                    gaze = j.getString(TAG_GAZE);
                    blink = j.getString(TAG_BLINK);
                    slope = j.getString(TAG_SLOPE);
                    hand = j.getString(TAG_HAND);

                    angry = j.getString(TAG_E0);
                    disgusting = j.getString(TAG_E1);
                    fearful = j.getString(TAG_E2);
                    happy = j.getString(TAG_E3);
                    sad = j.getString(TAG_E4);
                    surprising = j.getString(TAG_E5);
                    neutral = j.getString(TAG_E6);
                    noperson = j.getString(TAG_E7);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                timeScore.setText("\t 공부 시간 : " + time + "\n\n\t  집중 점수 : " + score + " 점");
                    //mTextViewResult.setText("\t  졸음 시간 : "+blink+" 초 \n\n\t 시선 이탈 : "+gaze+" 회 \n\n\t 자세 불량 : "+slope+" 회 \n\n\t 산만한 태도 : "+hand+" 회");
                    //emotionResult.setText("\t 분노 : "+angry+" % \n\n\t 역겨움 : "+disgusting+" % \n\n\t 공포 : "
                      //      +fearful+" % \n\n\t 행복 : "+happy+" % \n\n\t 슬픔 : "+sad+" % \n\n\t 놀람 : "+surprising+" % \n\n\t 무표정 : "+neutral+" % \n\n\t 사람 없음 : "+noperson+ "%");

            }
        }

        @Override
        protected String doInBackground(String... params){
            String serverURL = params[0];

            try{
                URL url = new URL(serverURL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

                httpURLConnection.setReadTimeout(5000);
                httpURLConnection.setConnectTimeout(5000);
                httpURLConnection.connect();

                int responseStatusCode = httpURLConnection.getResponseCode();
                Log.d(TAG, "response code - "+responseStatusCode);

                InputStream inputStream;
                if(responseStatusCode == HttpURLConnection.HTTP_OK){
                    inputStream = httpURLConnection.getInputStream();
                }else{
                    inputStream = httpURLConnection.getErrorStream();
                }

                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                StringBuilder sb = new StringBuilder();
                String line;

                while((line = bufferedReader.readLine()) != null){
                    sb.append(line);
                }


                bufferedReader.close();


                return sb.toString().trim();
            }catch (Exception e) {

                Log.d(TAG, "InsertData: Error ", e);
                errorString = e.toString();

                return null;
            }
        }
    }

    private class GetData extends AsyncTask<String, Void, String>{
        String errorString = null;

        @Override
        protected void onPreExecute(){
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String result){
            super.onPostExecute(result);

            //mTextViewResult.setText(result);
            Log.d(TAG, "response - "+result);

            if(result == null){
                //mTextViewResult.setText(errorString);
            }else{
                mJsonString = result;
                showResult();
            }
        }

        @Override
        protected String doInBackground(String... params){
            String serverURL = params[0];

            try{
                URL url = new URL(serverURL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();

                httpURLConnection.setReadTimeout(5000);
                httpURLConnection.setConnectTimeout(5000);
                httpURLConnection.connect();

                int responseStatusCode = httpURLConnection.getResponseCode();
                Log.d(TAG, "response code - "+responseStatusCode);

                InputStream inputStream;
                if(responseStatusCode == HttpURLConnection.HTTP_OK){
                    inputStream = httpURLConnection.getInputStream();
                }else{
                    inputStream = httpURLConnection.getErrorStream();
                }

                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                StringBuilder sb = new StringBuilder();
                String line;

                while((line = bufferedReader.readLine()) != null){
                    sb.append(line);
                }


                bufferedReader.close();


                return sb.toString().trim();
            }catch (Exception e) {

                Log.d(TAG, "InsertData: Error ", e);
                errorString = e.toString();

                return null;
            }
        }
    }

    //참고 http://blog.daum.net/techtip/12415218
    private void showResult(){
        try {
            JSONObject jsonObject = new JSONObject(mJsonString);
            JSONArray jsonArray = jsonObject.getJSONArray(TAG_JSON);
            int jsonLength = jsonArray.length();

            color = new int[jsonLength];
            color_e = new int[jsonLength];
            always = new float[jsonLength];
            for(int i = 0;i<jsonLength;i++){
                always[i] = 5f;
            }

            for(int i=0;i<jsonArray.length();i++){

                JSONObject item = jsonArray.getJSONObject(i);

                //String emotion = item.getString(TAG_EMOTION);
                //String blink = item.getString(TAG_BLINK);
                //String gaze = item.getString(TAG_GAZE);

                int ccts = Integer.parseInt(item.getString(TAG_SCORE));

                if(ccts<20) color[i] = UnCct;
                else if(ccts >= 20 && ccts < 40) color[i] = Cct20;
                else if(ccts >= 40 && ccts < 60) color[i] = Cct40;
                else if(ccts >= 60 && ccts < 80) color[i] = Cct60;
                else color[i] = Cct;

                String emotion = item.getString(TAG_EMOTION);
                if(emotion.contains("0")) color_e[i] = Angry;
                else if(emotion.contains("1")) color_e[i] = Disgusting;
                else if(emotion.contains("2")) color_e[i] = Fearful;
                else if(emotion.contains("3")) color_e[i] = Happy;
                else if(emotion.contains("4")) color_e[i] = Sad;
                else if(emotion.contains("5")) color_e[i] = Surprising;
                else if(emotion.contains("6")) color_e[i] = Neutral;
                else color_e[i] = NoPerson;

                //HashMap<String,String> hashMap = new HashMap<>();

                //hashMap.put(TAG_EMOTION, emotion);
                //hashMap.put(TAG_BLINK, blink);
                //hashMap.put(TAG_GAZE, gaze);

                //mArrayList.add(hashMap);
            }
/*
            ListAdapter adapter = new SimpleAdapter(
                    getActivity().getApplicationContext(), mArrayList, R.layout.item_list,
                    new String[]{TAG_EMOTION,TAG_BLINK, TAG_GAZE},
                    new int[]{R.id.textView_list_id, R.id.textView_list_name, R.id.textView_list_address}
            );*/

            setCctGraph();

            //mlistView.setAdapter(adapter);

        } catch (JSONException e) {

            Log.d(TAG, "showResult : ", e);
        }

    }

    private void setCctGraph(){
        ArrayList<BarEntry> cct = new ArrayList<BarEntry>();
        cct.add(new BarEntry(always,0));

        ArrayList year = new ArrayList();
        year.add("2008");

        BarDataSet bardataset = new BarDataSet(cct, "");
        cctchart.animateX(5000);
        BarData data = new BarData(year, bardataset);
        data.setDrawValues(false);
        bardataset.setColors(color);
        cctchart.setData(data);

        ArrayList<BarEntry> emo = new ArrayList<BarEntry>();
        emo.add(new BarEntry(always,0));

        BarDataSet bardataset_e = new BarDataSet(emo, "");
        emochart.animateX(5000);
        BarData data_e = new BarData(year, bardataset_e);
        data_e.setDrawValues(false);
        bardataset_e.setColors(color_e);
        emochart.setData(data_e);
    }

    class result_study implements Runnable {

        public result_study(){
        }
        @Override
        public void run() {
            if(_study_open == true){
                _study.setText("접기");
                mTextViewResult.setText("\t 졸음 시간 : "+blink+" 초 \n\n\t 시선 이탈 : "+gaze+" 회 \n\n\t 자세 불량 : "+slope+" 회 \n\n\t 산만한 태도 : "+hand+" 회");
                mTextViewResult.setVisibility(View.VISIBLE);
            }else{
                _study.setText("더보기");
                mTextViewResult.setVisibility(View.GONE);
            }
        }
    }

    class result_emotion implements Runnable {

        public result_emotion(){
        }
        @Override
        public void run() {
            if(_emotion_open == true){
                _emotion.setText("접기");
                emotionResult.setText("\t 분노 : "+angry+" % \n\n\t 역겨움 : "+disgusting+" % \n\n\t 공포 : "
                     +fearful+" % \n\n\t 행복 : "+happy+" %");
                emotionResult2.setText("\t 슬픔 : "+sad+" % \n\n\t 놀람 : "+surprising+" % \n\n\t 무표정 : "+neutral+" % \n\n\t etc. : "+noperson+ "%");

                eee.setVisibility(View.VISIBLE);

            }else{

                _emotion.setText("더보기");

                eee.setVisibility(View.GONE);
            }
        }
    }
}
