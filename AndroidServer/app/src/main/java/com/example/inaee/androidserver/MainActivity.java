package com.example.inaee.androidserver;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
//import android.provider.SyncStateContract.Constants;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Context context;
    Button playPauseButton;
    Button nextButton;
    Button previousButton;
    TextView mousePad;

    private boolean isConnected=false;
    private boolean mouseMoved=false;
    private Socket socket;
    private PrintWriter out;

    private float initX =0;
    private float initY =0;
    private float disX =0;
    private float disY =0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = this; //Toast 메시지를 표시 할 컨텍스트를 저장

        //모든 버튼에 대한 참조 가져오기
        playPauseButton = (Button)findViewById(R.id.playPauseButton);
        nextButton = (Button)findViewById(R.id.nextButton);
        previousButton = (Button)findViewById(R.id.previousButton);

        // 이 액티비티는 View.OnClickListener를 확장하고 이것을 onClickListener로 설정
        // 모든 버튼 용
        playPauseButton.setOnClickListener(this);
        nextButton.setOnClickListener(this);
        previousButton.setOnClickListener(this);

        // 마우스 패드로 작동하는 TextView에 대한 참조를 가져온다
        mousePad = (TextView)findViewById(R.id.mousePad);

        // 텍스트 탭에서 손가락 탭과 움직임을 캡처
        mousePad.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(isConnected && out!=null){
                    switch(event.getAction()){
                        case MotionEvent.ACTION_DOWN:
                            //사용자가 TextView를 터치하면 X 및 Y 위치를 저장
                            initX =event.getX();
                            initY =event.getY();
                            mouseMoved=false;
                            break;
                        case MotionEvent.ACTION_MOVE:
                            disX = event.getX()- initX; //x 방향의 마우스 움직임
                            disY = event.getY()- initY; //y 방향으로 마우스 이동
                            // init를 새로운 위치로 설정하여 마우스의 지속적인 이동 캡처 됨
                            initX = event.getX();
                            initY = event.getY();
                            if(disX !=0|| disY !=0){
                                out.println(disX +","+ disY); // 서버로 마우스 이동을 보낸다.
                            }
                            mouseMoved=true;
                            break;
                        case MotionEvent.ACTION_UP:
                            // ACTION_DOWN 뒤에 usr이 마우스를 움직이지 않은 경우에만 탭을 고려
                            if(!mouseMoved){
                                out.println(Constants.MOUSE_LEFT_CLICK);
                            }
                    }
                }
                return true;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // 메뉴를 팽창시킵니다. 작업 표시 줄이 있으면 항목을 작업 표시 줄에 추가
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // 액션 바 항목 클릭을 처리합니다. 액션 바는
        // 자동으로 홈 / 업 버튼의 클릭을 처리하므로 너무 길다.
        // AndroidManifest.xml에서 부모 액티비티를 지정
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if(id == R.id.action_connect) {
            ConnectPhoneTask connectPhoneTask = new ConnectPhoneTask();
            connectPhoneTask.execute(Constants.SERVER_IP); // 다른 스레드에서 서버에 연결하려고 시도
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // 버튼 중 하나를 누르면 OnClick 메서드가 호출
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.playPauseButton:
                if (isConnected && out!=null) {
                    out.println(Constants.PLAY);// "play"를 서버에 보냅니다.
                }
                break;
            case R.id.nextButton:
                if (isConnected && out!=null) {
                    out.println(Constants.NEXT); // 서버에 "next"를 보냅니다.
                }
                break;
            case R.id.previousButton:
                if (isConnected && out!=null) {
                    out.println(Constants.PREVIOUS); // 서버에 "previous"를 보냅니다.
                }
                break;
        }

    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        if(isConnected && out!=null) {
            try {
                out.println("exit"); // 서버에 종료 지시
                socket.close(); // 소켓 닫기
            } catch (IOException e) {
                Log.e("remotedroid", "Error in closing socket", e);
            }
        }
    }

    public class ConnectPhoneTask extends AsyncTask<String,Void,Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            boolean result = true;
            try {
                InetAddress serverAddr = InetAddress.getByName(params[0]);
                socket = new Socket(serverAddr, Constants.SERVER_PORT);// 서버 IP 및 포트에서 소켓 열기
            } catch (IOException e) {
                Log.e("remotedroid", "Error while connecting", e);
                result = false;
            }
            return result;
        }

        @Override
        protected void onPostExecute(Boolean result)
        {
            isConnected = result;
            Toast.makeText(context,isConnected?"Connected to server!":"Error while connecting",Toast.LENGTH_LONG).show();
            try {
                if(isConnected) {
                    out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket
                            .getOutputStream())), true); // 서버에 데이터를 보내기위한 출력 스트림을 만듭니다.
                }
            }catch (IOException e){
                Log.e("remotedroid", "Error while creating OutWriter", e);
                Toast.makeText(context,"Error while connecting",Toast.LENGTH_LONG).show();
            }
        }
    }


}
