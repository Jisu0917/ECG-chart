package com.data.bpm;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

public class MainActivity1 extends AppCompatActivity {

    EditText et_ip;
    EditText et_port;

    TextView tv_bpm;
    TextView tv_graph;


    //소켓통신
    Socket socket;

    private ByteBuffer byteBuffer =  ByteBuffer.allocate(1024);
    int n = 0;

    private final Object mSync = new Object();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        et_ip = findViewById(R.id.et_ip);
        et_port = findViewById(R.id.et_port);

        tv_bpm = findViewById(R.id.tv_bpm);
        setBpm(0);

        tv_graph = findViewById(R.id.tv_graph);
        setGraphText();

        initChart();
        ConnectThread thread = new ConnectThread();
        thread.start();

//        testLogic();
    }

    private void testLogic() {
        byte[] testBuffer = new byte[1025];

        fillTestData(testBuffer, 1025);

        setBpm(testBuffer[0]);
        //bpm제외  (1024/64)*8 = 128
        //1024 을 64로 나누면 16개가 나온다.
        //64바이트별 8개의 데이터가 들어가 있어서
        //16*8=128 즉 한번에 128데이터를 가져온다.
        for(int i=0;i<16;i++) {
            //64바이트마다 8개의 데이터 파싱후 그래프 데이터 입력
            getParseData(testBuffer,i*64+1);
        }
    }

    private void fillTestData(byte[] testBuffer, int size) {
        byte min = -127; // 최소 값
        byte max = 127;  // 최대 값

        for(int i =0; i<size;i++) {
            byte randomByte = getRandomByteInRange(min, max);
            testBuffer[i] = randomByte;
        }
    }

    public static byte getRandomByteInRange(byte min, byte max) {
        Random random = new Random();
        int range = max - min + 1; // 범위 계산
        byte randomByte = (byte)(random.nextInt(range) + min); // 범위 내에서 랜덤 값 생성
        return randomByte;
    }

    private void setGraphText() {
        String name = getIntent().getStringExtra("name");
        tv_graph.setText(String.format(getString(R.string.graph),name));
    }

    private void setBpm(int nBpm) {
        String age = getIntent().getStringExtra("age");
        int nAge = 0;
        try{
            nAge = Integer.parseInt(age);
        }catch (Exception e){

        }
        int nGender = getIntent().getIntExtra("gender",1);


        tv_bpm.setText(String.format(getString(R.string.bpm),nBpm));

        String strBpm = ""+nBpm;



        Spannable span = (Spannable) tv_bpm.getText();
        //남성
        if(nGender==1) {
            //55이하
            if(nAge<55){
                //정상
                if(nBpm>=50&& nBpm<=80){
                    span.setSpan(new ForegroundColorSpan(Color.GREEN), 8, 8 + strBpm.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                //서맥
                else if(nBpm>80){
                    span.setSpan(new ForegroundColorSpan(Color.RED), 8, 8 + strBpm.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                //빈맥
                else{
                    span.setSpan(new ForegroundColorSpan(Color.YELLOW), 8, 8 + strBpm.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }else{
                //정상
                if(nBpm>=60&& nBpm<=85){
                    span.setSpan(new ForegroundColorSpan(Color.GREEN), 8, 8 + strBpm.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                //서맥
                else if(nBpm>85){
                    span.setSpan(new ForegroundColorSpan(Color.RED), 8, 8 + strBpm.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                //빈맥
                else{
                    span.setSpan(new ForegroundColorSpan(Color.YELLOW), 8, 8 + strBpm.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }

            }

        }
        //여성
        else{
            //55이하
            if(nAge<55){
                //정상
                if(nBpm>=60&& nBpm<=80){
                    span.setSpan(new ForegroundColorSpan(Color.GREEN), 8, 8 + strBpm.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                //서맥
                else if(nBpm>80){
                    span.setSpan(new ForegroundColorSpan(Color.RED), 8, 8 + strBpm.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                //빈맥
                else{
                    span.setSpan(new ForegroundColorSpan(Color.YELLOW), 8, 8 + strBpm.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }else{
                //정상
                if(nBpm>=65&& nBpm<=85){
                    span.setSpan(new ForegroundColorSpan(Color.GREEN), 8, 8 + strBpm.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                //서맥
                else if(nBpm>85){
                    span.setSpan(new ForegroundColorSpan(Color.RED), 8, 8 + strBpm.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
                //빈맥
                else{
                    span.setSpan(new ForegroundColorSpan(Color.YELLOW), 8, 8 + strBpm.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }

            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            if(socket != null)
                socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    class ReadThread extends Thread{
        public void run(){
            try{

                try {
                    int maxBufferSize = 1025;
                    //버퍼 생성
                    byte[] recvBuffer = new byte[maxBufferSize];
                    //서버로부터 받기 위한 입력 스트림 뚫음
                    InputStream is = socket.getInputStream();


                    while(socket.isConnected()) {

                        int nReadSize = maxBufferSize;//is.read(recvBuffer);

                        //받아온 값이 0보다 클때
                        if (nReadSize > 0) {
                            //받아온 byte를 Object로 변환

                            byte[] b = Arrays.copyOf(recvBuffer,nReadSize);

                            //확인을 위해 출력
                              Util_Data.LogToHexString("testRecv",b,nReadSize);
                              if(b.length==1025){
                                  runOnUiThread(new Runnable() {
                                      public void run() {
                                          if(b[0] > -1) {
                                              setBpm(b[0]);
                                              //bpm제외  (1024/64)*8 = 128
                                              //1024 을 64로 나누면 16개가 나온다.
                                              //64바이트별 8개의 데이터가 들어가 있어서
                                              //16*8=128 즉 한번에 128데이터를 가져온다.
                                              for (int i = 0; i < 16; i++) {
                                                  //64바이트마다 8개의 데이터 파싱후 그래프 데이터 입력
                                                  getParseData(b, i * 64 + 1);
                                              }
                                          }
                                      }
                                  });
                              }
                        }

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }catch(Exception e){
                e.printStackTrace();
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity1.this, "연결이 종료되어 종료됩니다.", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
        }
    }


    public void getParseData(byte[] input,int inidx){


        int i1 = 0;
        int i2 = 0;
        int i3 = 0;
        int i4 = 0;
        int i5 = 0;
        int i6 = 0;
        int i7 = 0;
        int i8 = 0;

        for (int i = 0; i < 64; i++) {
            // 첫번쨰 비트 연산
            if ((input[inidx+i] & 0x00000001) == 0x00000001) {
                i1 = i;
            }
            if ((input[inidx+i] & 0x00000010) == 0x00000010) {
                i2 = i;
            }
            if ((input[inidx+i] & 0x00000100) == 0x00000100) {
                i3 = i;
            }
            if ((input[inidx+i] & 0x00001000) == 0x00001000) {
                i4 = i;
            }
            if ((input[inidx+i] & 0x00010000) == 0x00010000) {
                i5 = i;
            }
            if ((input[inidx+i] & 0x00100000) == 0x00100000) {
                i6 = i;
            }
            if ((input[inidx+i] & 0x01000000) == 0x01000000) {
                i7 = i;
            }
            if ((input[inidx+i] & 0x10000000) == 0x10000000) {
                i8 = i;
            }
        }
        //8개데이터 그래프에 추가
        addEntry(i1);
        addEntry(i2);
        addEntry(i3);
        addEntry(i4);
        addEntry(i5);
        addEntry(i6);
        addEntry(i7);
        addEntry(i8);


        Log.d("TEST"  ,"i " + i1 + " " + i2 + " " + i3 + " " + i4 + " " + i5 + " " + i6 + " " + i7 + " " + i8 + " ");
    }



    class ConnectThread extends Thread{
        public void run(){
            try{
                //소켓 생성
                InetAddress serverAddr = InetAddress.getByName(et_ip.getText().toString());
                socket =  new Socket(serverAddr,Integer.parseInt(et_port.getText().toString()));

                ReadThread readThread  = new ReadThread();
                readThread.start();

            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    private LineChart chart;
    public void initChart(){
        chart = (LineChart) findViewById(R.id.LineChart);

        chart.setDrawGridBackground(true);
        chart.setBackgroundColor(Color.BLACK);
        chart.setGridBackgroundColor(Color.BLACK);

// description text
        chart.getDescription().setEnabled(true);
        Description des = chart.getDescription();
        des.setEnabled(true);
        des.setText("Real-Time DATA");
        des.setTextSize(15f);
        des.setTextColor(Color.WHITE);

// touch gestures (false-비활성화)
        chart.setTouchEnabled(false);

// scaling and dragging (false-비활성화)
        chart.setDragEnabled(false);
        chart.setScaleEnabled(false);

//auto scale
        chart.setAutoScaleMinMaxEnabled(true);

// if disabled, scaling can be done on x- and y-axis separately
        chart.setPinchZoom(false);

//X축
        XAxis xAxis = chart.getXAxis();
        xAxis.setEnabled(true);
        xAxis.setTextColor(getResources().getColor(R.color.white));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        // 눈금 개수를 설정합니다
        ArrayList<String> xAxisValues = new ArrayList<String>();
        for(int i=0; i<150; i++) {
            xAxisValues.add(String.valueOf(i));
        }

        xAxis.setValueFormatter(new IndexAxisValueFormatter(xAxisValues));

//Legend
        Legend l = chart.getLegend();
        l.setEnabled(true);
        l.setFormSize(10f); // set the size of the legend forms/shapes
        l.setTextSize(12f);
        l.setTextColor(Color.WHITE);

//Y축
        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setEnabled(true);
        leftAxis.setTextColor(getResources().getColor(R.color.white));
        leftAxis.setAxisMinimum(0);
        leftAxis.setAxisMaximum(150);
        leftAxis.setGranularity(10);
        leftAxis.setGranularityEnabled(true);
        leftAxis.setLabelCount(16, true);

        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setEnabled(false);


// don't forget to refresh the drawing
        chart.invalidate();
    }

    private void addEntry(double num) {

        LineData data = chart.getData();

        if (data == null) {
            data = new LineData();
            chart.setData(data);
        }

        ILineDataSet set = data.getDataSetByIndex(0);
        // set.addEntry(...); // can be called as well

        if (set == null) {
            set = createSet();
            data.addDataSet(set);
        }



        data.addEntry(new Entry((float)set.getEntryCount(), (float)num), 0);
        data.notifyDataChanged();

        // let the chart know it's data has changed
        chart.notifyDataSetChanged();

        chart.setVisibleXRangeMaximum(127);
        // this automatically refreshes the chart (calls invalidate())
        chart.moveViewTo(data.getEntryCount(), 50f, YAxis.AxisDependency.LEFT);

    }

    @SuppressLint("ResourceType")
    private LineDataSet createSet() {



        LineDataSet set = new LineDataSet(null, "Real-time Line Data");
        set.setLineWidth(1f);
        set.setDrawValues(false);

        set.setValueTextColor(getResources().getColor(R.color.colorgreen));
        set.setColor(getResources().getColor(R.color.trans));
        set.setMode(LineDataSet.Mode.LINEAR);
        set.setDrawCircleHole(true);
        set.setCircleColor(getResources().getColor(R.color.colorgreen));
        set.setHighLightColor(Color.rgb(190, 190, 190));

        return set;
    }

}
