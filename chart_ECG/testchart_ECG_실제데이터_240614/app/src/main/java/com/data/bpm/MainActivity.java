package com.data.bpm;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private LineChart lineChart;
    private LineDataSet dataSet;
    private LineData lineData;
    private Handler handler = new Handler();
    private Runnable updateTask;
    private float time = 0.0f;
    private float vTime = 0.0f;
    private int rawDataIndex = 0;
    private Random random = new Random();

    private String[] rawData = {
            "97", "51", "139", "90", "130", "57", "59", "117", "78", "79",
            "112", "53", "59", "64", "101", "77", "52", "102", "82", "80",
            "76", "78", "70", "94", "75", "82", "48", "100", "90", "50",
            "78", "78", "62", "78", "78", "127", "69", "69", "46", "59",
            "112", "62", "94", "109", "85", "95", "82", "53", "54", "102",
            "73", "94", "68", "84", "73", "79", "82", "82", "82", "91",
            "102", "79", "74", "88", "79", "76", "76", "62", "99", "81",
            "71", "133", "71", "88", "83", "73", "66", "51", "83", "83",
            "58", "47", "80", "81", "80", "78", "86", "48", "105", "62",
            "130", "106", "66", "65", "89", "53", "82", "78", "78", "77",
            "75", "89", "53", "106", "89", "78", "82", "75", "81", "83",
            "78", "134", "58", "85", "83", "78", "82", "80", "80", "76",
            "74", "78", "47", "88", "82", "83", "79", "82", "82", "82",
            "78", "83", "84", "84", "56", "58", "57", "61", "115", "65",
            "125", "86", "84", "86", "77", "82", "83", "87", "106", "106",
            "87", "87", "80", "87", "114", "134"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lineChart = findViewById(R.id.LineChart);
        setupChart();
        setupData();
        startUpdatingChart();
    }

    private void setupChart() {
        lineChart.getDescription().setEnabled(false);
        lineChart.setTouchEnabled(true);
        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(true);
        lineChart.setDrawGridBackground(false);
        lineChart.setPinchZoom(true);

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setDrawGridLines(false);
        xAxis.setAvoidFirstLastClipping(true);
        xAxis.setEnabled(true);

        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setDrawGridLines(false);
        leftAxis.setLabelCount(6, false);
        leftAxis.setAxisMinimum(0f);  // Y축 최소값을 0으로 설정
        leftAxis.setAxisMaximum(150f);  // Y축 최대값을 150으로 설정

        YAxis rightAxis = lineChart.getAxisRight();
        rightAxis.setEnabled(false);
    }

    private void setupData() {
        List<Entry> values = new ArrayList<>();
        dataSet = new LineDataSet(values, "ECG Signal");
        dataSet.setDrawCircles(false);
        dataSet.setColor(Color.BLUE);
        dataSet.setLineWidth(1f);

        lineData = new LineData(dataSet);
        lineChart.setData(lineData);
    }

    private void startUpdatingChart() {
        updateTask = new Runnable() {
            @Override
            public void run() {
                addEntry();
                handler.postDelayed(this, 50); // 50ms 간격으로 업데이트
            }
        };
        handler.post(updateTask);
    }

    private void addEntry() {
        float volt = getNextVoltValue();
        float ecgValue = generateECGValue(vTime, volt);
        dataSet.addEntry(new Entry(time, ecgValue));
        time += 0.01f;
        if (vTime >= 0.0 && vTime < 1.0) {
            vTime += 0.01f;
        } else {
            vTime = 0.0f;
        }

        // 데이터 변경 통지 및 차트 새로고침
        lineData.notifyDataChanged();
        lineChart.notifyDataSetChanged();
        lineChart.setVisibleXRangeMinimum(2f);
        lineChart.setVisibleXRangeMaximum(2f);  // x축의 최대 표시 범위를 설정
        lineChart.moveViewToX(time);
    }

    private float getNextVoltValue() {
        if (rawDataIndex >= rawData.length) {
            rawDataIndex = 0;
        }
        float voltValue = Float.parseFloat(rawData[rawDataIndex]);
        rawDataIndex++;
        return voltValue;
    }

    private float generateECGValue(float time, float voltValue) {
        // P, Q, R, S, T 파형을 생성하여 ECG 그래프를 시뮬레이션
        float centerValue = 75;
        float gap;
        float ecgValue;
        float rate = 0.8f;  //보정률

        // Generate P wave
        if (time % 1.0 > 0.1 && time % 1.0 < 0.2) {
            // volt 데이터가 ECG 그래프 파형과 얼마나 차이나는지 계산
            centerValue += 10f * (float) Math.sin(2 * Math.PI * 5 * (time % 1.0));
            gap = centerValue - voltValue;  //차이
            ecgValue = voltValue + (gap * rate);
        }

        // Generate Q wave
         else if (time % 1.0 > 0.2 && time % 1.0 < 0.25) {
            // volt 데이터가 ECG 그래프 파형과 얼마나 차이나는지 계산
            centerValue += -45f * (float) Math.sin(2 * Math.PI * 50 * (time % 1.0));
            gap = centerValue - voltValue;  //차이
            ecgValue = voltValue + (gap * rate);
        }

        // Generate R wave
        else if (time % 1.0 > 0.25 && time % 1.0 < 0.3) {
            // volt 데이터가 ECG 그래프 파형과 얼마나 차이나는지 계산
            centerValue += 150f * (float) Math.sin(2 * Math.PI * 50 * (time % 1.0));
            gap = centerValue - voltValue;  //차이
            ecgValue = voltValue + (gap * rate);
        }

        // Generate S wave
        else if (time % 1.0 > 0.3 && time % 1.0 < 0.35) {
            // volt 데이터가 ECG 그래프 파형과 얼마나 차이나는지 계산
            centerValue += -30f * (float) Math.sin(2 * Math.PI * 50 * (time % 1.0));
            gap = centerValue - voltValue;  //차이
            ecgValue = voltValue + (gap * rate);
        }

        // Generate T wave
        else if (time % 1.0 > 0.4 && time % 1.0 < 0.6) {
            // volt 데이터가 ECG 그래프 파형과 얼마나 차이나는지 계산
            centerValue += 30f * (float) Math.sin(2 * Math.PI * 5 * (time % 1.0));
            gap = centerValue - voltValue;  //차이
            ecgValue = voltValue + (gap * rate);
        }

        else {
            gap = centerValue - voltValue;
            ecgValue = voltValue + (gap * rate);
        }

        // 값을 0에서 150 사이로 제한
        ecgValue = Math.max(0, Math.min(150, ecgValue));

        return ecgValue;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(updateTask);
    }
}
