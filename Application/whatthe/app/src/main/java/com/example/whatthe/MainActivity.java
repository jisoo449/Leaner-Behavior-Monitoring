package com.example.whatthe;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.whatthe.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import static com.example.whatthe.R.id.main_frame;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private FragmentManager fm;
    private FragmentTransaction ft;
    private FragHome fragHome;
    private FragDash fragDash;

    String getID = null;

    ImageButton but_camera;
    Intent permi;

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getID = getIntent().getStringExtra("ID");

        permi = new Intent(getApplicationContext(), PermissionActivity.class);
        permi.putExtra("userID",getID);
        but_camera = (ImageButton) findViewById(R.id.cameraButton);
        but_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(permi);
            }
        });

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()){
                    case R.id.home:
                        setFrag(0);
                        break;
                    case R.id.dashboard:
                        setFrag(1);
                        break;
                    case R.id.logout:
                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle("로그아웃").setMessage("로그아웃 하시겠습니까?")
                                .setPositiveButton("로그아웃", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        Intent i = new Intent(MainActivity.this, LoginActivity.class);
                                        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                        startActivity(i);
                                    }
                                })
                                .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {

                                    }
                                })
                                .show();

                        break;
                }
                return true;
            }
        });

        Bundle bundle = new Bundle(1); // 파라미터는 전달할 데이터 개수
        bundle.putString("userId", getID); // key , value

        fragHome = new FragHome();
        fragDash = new FragDash();

        fragHome.setArguments(bundle);
        fragDash.setArguments(bundle);

        setFrag(0); //첫 프래그먼트 화면을 무엇으로 지정해줄 것인지 선택
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    //public native String stringFromJNI();

    private void setFrag(int n) {
        fm = getSupportFragmentManager();
        ft = fm.beginTransaction(); //실제적인 프레그먼트 교체에서 사용
        switch (n) {
            case 0:
                ft.replace(main_frame,fragHome);
                ft.commit(); //저장의미
                break;
            case 1:
                ft.replace(main_frame, fragDash);
                ft.commit(); //저장의미
                break;
        }
    }

    long pressTime;
    @Override
    public void onBackPressed() {

        if(System.currentTimeMillis() - pressTime <2000){
            finishAffinity();
            return;
        }
        Toast.makeText(this,"한 번 더 누르시면 앱이 종료됩니다",Toast.LENGTH_LONG).show();
        pressTime = System.currentTimeMillis();

    }
}