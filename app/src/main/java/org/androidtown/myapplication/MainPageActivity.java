package org.androidtown.myapplication;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONObject;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Time;
import java.util.Iterator;

public class MainPageActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String FCM_MESSAGE_URL= "https://fcm.googleapis.com/fcm/send";

    //서버키 나중에 비공개로: 디비에 저장해놓고 앱 실행하면 불러오기-->MainActivity에 넣는게 나으려나?
    private static final String ServerKey = "AAAA-cEJ1Gk:APA91bHFBUbnklycU40BQT6FoNzzVRylTKDmahM1nMiVFzB0dlmfQSJH_7BwbEFKvrI94YTLTLPvusd7IJUn1qAi1dbJFUJ3G_bueEotOqKxJihlqYT3WDMRz1XBjjBah7gNZ7QxQ3VX";

    TextView userName;
    TextView userID;

    private FirebaseDatabase database;
    private DatabaseReference databaseReference;
    private DatabaseReference databaseReferenceForPushMsgTest;
    //private DatabaseReference databaseReferenceForServerKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nav_header_main_page);

        database = FirebaseDatabase.getInstance();
        //databaseReferenceForServerKey = database.getReference("ServerKey");
        //ServerKey = databaseReferenceForServerKey.getKey();

        /*databaseReferenceForServerKey.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot){
                ServerKey = (String)dataSnapshot.getValue();


            }
            @Override
            public void onCancelled(DatabaseError databaseError){

            }

        });*/

        userName = (TextView) findViewById(R.id.UserName);
        userID = (TextView) findViewById(R.id.UserID);

        //메뉴에 사용자 정보 띄워줄라 했는데 안됨
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        String name = bundle.getString("name");
        userName.setText(name);
        String id = bundle.getString("ID");
        userID.setText(id);

        //database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference("users/"+id);

        //로그인되면 스마트폰 주소 받아오기
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        databaseReference.child("fcmToken").setValue(refreshedToken);


        setContentView(R.layout.activity_main_page);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //슬라이드 메뉴 회원 이름, 아이디 보여주기
        /*Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        String name = bundle.getString("name");
        userName.setText(name);
        String id = bundle.getString("ID");
        userID.setText(id);*/
        //안되고있음.

        //푸쉬메세지 버튼 테스트
        Button pushTestBtn = (Button)findViewById(R.id.pushTestBtn);
        /*pushTestBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                sendPostToFCM("push message test");
            }
        });*/

        pushTestBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                sendPostToFCM("push message test");
            }
        });


    }

    //슬라이드 메뉴 다시 넣는 부분
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    //슬라이드 메뉴에서 메뉴를 선택했을 때
    public boolean onNavigationItemSelected(MenuItem item) {
        String USERID = (String)userID.getText().toString();
        Bundle bundle = new Bundle();
        bundle.putString("ID", USERID);

        // Handle navigation view item clicks here.
        int id = item.getItemId();

        //홈으로 가는 버튼. Go to main page
        if (id == R.id.home) {
            // Handle the camera action
        }

        //습관 등록
        else if (id == R.id.enroll_habit) {
            Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
            intent.putExtras(bundle);
            startActivity(intent);
        }

        //타임라인 보기
        else if (id == R.id.timeline) {
            Intent intent = new Intent(getApplicationContext(), TimelineActivity.class);
            intent.putExtras(bundle);
            startActivity(intent);
        }

        //회원 정보 수정하기
        else if (id == R.id.user_info_change) {
            Intent intent = new Intent(getApplicationContext(), ModifyInfoActivity.class);
            startActivity(intent);
        }

        //앱 설정 변경, Go Application setting page
        else if (id == R.id.app_setting) {
            Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
            startActivity(intent);

            //finish();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void sendPostToFCM(final String message){
        databaseReferenceForPushMsgTest = database.getReference("users/"+userID.getText().toString());//users/id/habits/habitIdx/friendName으로 바꿔야함.
        databaseReferenceForPushMsgTest.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //받는 사람 주소
                final String fcmToken = (String)dataSnapshot.child("fcmToken").getValue();
                new Thread(new Runnable(){
                    @Override
                    public void run(){
                        try{
                            Toast.makeText(getApplication(), "fcm 생성 시작", Toast.LENGTH_SHORT).show();
                            //FMC 메세지 생성 start
                            JSONObject root = new JSONObject();
                            JSONObject notification = new JSONObject();
                            notification.put("body", message);
                            notification.put("title", "Success plz");
                            root.put("notification", notification);
                            root.put("to", fcmToken);
                            //FMC 메세지 생성 end

                            Toast.makeText(getApplication(), "fcm 생성 완료", Toast.LENGTH_SHORT).show();

                            URL Url = new URL(FCM_MESSAGE_URL);
                            HttpURLConnection conn = (HttpURLConnection)Url.openConnection();
                            conn.setRequestMethod("POST");
                            conn.setDoOutput(true);
                            conn.setDoInput(true);
                            conn.addRequestProperty("Authorization", "key="+ServerKey);
                            conn.setRequestProperty("Accept", "application/json");
                            conn.setRequestProperty("Content-type", "application/json");
                            OutputStream os = conn.getOutputStream();
                            os.write(root.toString().getBytes("utf-8"));
                            os.flush();
                            conn.getResponseCode();
                        } catch (Exception e){
                            //Toast.makeText(getApplication(), "catch에 잡힘", Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                        }
                    }
                }).start();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }


}