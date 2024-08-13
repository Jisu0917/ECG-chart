package com.data.bpm;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class InputActivity extends AppCompatActivity {

    EditText et_name;
    EditText et_age;
    RadioGroup radioGroup;
    RadioButton radio1,radio2;
    Button btn_conn;
    int gender = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input);

        et_name = findViewById(R.id.et_name);
        et_age = findViewById(R.id.et_age);
        radioGroup = findViewById(R.id.radiogroup);
        radio1 = findViewById(R.id.radio1);
        radio2 = findViewById(R.id.radio2);
        
        initData();


        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                gender = checkedId;
                Log.e("checkId",checkedId+" ");

            }
        });


        btn_conn = findViewById(R.id.btn_conn);
        btn_conn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(InputActivity.this, MainActivity.class);
                intent.putExtra("name",et_name.getText().toString());
                intent.putExtra("age",et_age.getText().toString());
                intent.putExtra("gender",gender);

                saveData();

                startActivity(intent);
            }
        });
    }

    private void saveData() {
        DataSharedPref.setSharedPrefName(this,et_name.getText().toString());
        DataSharedPref.setSharedPrefAge(this,et_age.getText().toString());
        DataSharedPref.setSharedPrefGender(this,gender);
    }

    private void initData() {
        et_name.setText(DataSharedPref.getSharedPrefName(this));
        et_age.setText(DataSharedPref.getSharedPrefAge(this));
        if(DataSharedPref.getSharedPrefGender(this)==1){
            radio1.setChecked(true);
            radio2.setChecked(false);
        }else{
            radio1.setChecked(false);
            radio2.setChecked(true);
        }
    }
}