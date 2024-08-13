#include <string.h>
#include <TimerOne.h>
#include "HeartSpeed.h"
#include "U8glib.h"

// 픽셀 데이터를 저장하는 유니온 타입 정의
typedef union {
  struct {
    uint8_t bit0:1; uint8_t bit1:1; uint8_t bit2:1; uint8_t bit3:1; uint8_t bit4:1; uint8_t bit5:1; uint8_t bit6:1; uint8_t bit7:1;
  } Bit;
  uint8_t U8;
} Pixel_t;

// 핀 설정
// AD8232 심박 센서
#define PIN_AD8232_LO_P   10
#define PIN_AD8232_LO_N   11
#define PIN_AD8232_OUTPUT A0
HeartSpeed heartspeed(PIN_AD8232_OUTPUT);

// 컴포넌트 초기화
// 128x64 그래픽 LCD 초기화
U8GLIB_ST7920_128X64_1X u8g(38, 36, 34, 32, 30, 28, 26, 24, 40, 44, 42);
uint8_t GLCD_print_data[128][8] = {0, }; // 그래픽 LCD 출력 데이터
uint8_t GLCD_print_index = 0; // 그래픽 LCD 출력 인덱스

// 타이머 변수
uint32_t nowTime = 0;
uint32_t data_lastTime = 0;
uint32_t draw_lastTime = 0;
const uint32_t data_period = 10;    // 10ms
const uint32_t draw_period = 1280;  // 1280ms (1.28초)

// 전송 모드 변수
uint8_t transmit_mode = 0u;  // 0: 없음, 1: GLCD, 2: WIFI

Pixel_t pixel[1024] = {}; // 픽셀 데이터 배열
uint8_t BPM = 0; // 심박수

float vTime = 0.0f;

#define SMOOTHING_WINDOW_SIZE  3 //5 // 이동 평균 필터 윈도우 크기 설정
#define POINT_SPACING 2         // X축 점들 사이의 간격

// 그래픽 LCD에 데이터 그리기 함수
void draw(void)
{
  u8g.firstPage();  // 첫 페이지 초기화
  do { 

    int lastX = -1, lastY = -1; // 이전 점의 좌표 초기화
    int smoothedData[128] = {0}; // 평활화된 데이터를 저장할 배열 초기화

    // 이동 평균 필터를 사용하여 데이터 평활화
    for(uint8_t i = 0; i < 128; i++) {
      int sum = 0;
      int count = 0;

      for(int j = -SMOOTHING_WINDOW_SIZE / 2; j <= SMOOTHING_WINDOW_SIZE / 2; j++) {
        if(i + j >= 0 && i + j < 128) {
          for(uint8_t k = 0; k < 8; k++) {
            if(GLCD_print_data[i + j][k] != 0) {
              sum += k * 8 + __builtin_ctz(GLCD_print_data[i + j][k]);
              count++;
              break;
            }
          }
        }
      }

      if(count > 0) {
        smoothedData[i] = sum / count;
      } else {
        smoothedData[i] = -1; // 유효한 데이터가 없는 경우
      }
    }

    // 그래픽 LCD에 점 연결
    for(uint8_t i = 0; i < 128; i += POINT_SPACING) {
      int currentY = smoothedData[i];

      if (currentY != -1) {
        if (lastX != -1 && lastY != -1) {
          u8g.drawLine(lastX, lastY, i, currentY); // 이전 점과 현재 점을 연결하는 선 그리기
        }
        lastX = i;
        lastY = currentY;
      }
    }

    // 심박 센서 상태 확인 및 심박수 출력
    if((digitalRead(PIN_AD8232_LO_P) == 1) || (digitalRead(PIN_AD8232_LO_N) == 1)) {
      u8g.setFont(u8g_font_unifont);
      u8g.drawStr(0, 10, "BPM:---");
      Serial.println("BPM:---");
    }
    else {
      char temp[16];
      sprintf(temp, "BPM:%03d", BPM);
      Serial.print("BPM:");
      Serial.println(BPM);
      u8g.setFont(u8g_font_unifont);
      u8g.drawStr(0, 10, temp);
    }
  } while( u8g.nextPage() ); // 다음 페이지로 넘어감
}

// ECG 값을 생성하는 함수
float generateECGValue(float time, float voltValue) {
    float centerValue = 22; // 44의 절반으로 조정된 중심값
    float gap;
    float ecgValue;
    float rate = 0.8f;  // 보정률

    // P 파형 생성
    if (fmod(time, 1.0f) > 0.1f && fmod(time, 1.0f) < 0.2f) {
        centerValue += 2.51f * sin(2 * PI * 5 * fmod(time, 1.0f)); // P 파형 계산
        gap = centerValue - voltValue;
        ecgValue = voltValue + (gap * rate);
    }
    // Q 파형 생성
    else if (fmod(time, 1.0f) > 0.2f && fmod(time, 1.0f) < 0.25f) {
        centerValue += -11.26f * sin(2 * PI * 50 * fmod(time, 1.0f)); // Q 파형 계산
        gap = centerValue - voltValue;
        ecgValue = voltValue + (gap * rate);
    }
    // R 파형 생성
    else if (fmod(time, 1.0f) > 0.25f && fmod(time, 1.0f) < 0.3f) {
        centerValue += 37.33f * sin(2 * PI * 50 * fmod(time, 1.0f)); // R 파형 계산
        gap = centerValue - voltValue;
        ecgValue = voltValue + (gap * rate);
    }
    // S 파형 생성
    else if (fmod(time, 1.0f) > 0.3f && fmod(time, 1.0f) < 0.35f) {
        centerValue += -7.47f * sin(2 * PI * 50 * fmod(time, 1.0f)); // S 파형 계산
        gap = centerValue - voltValue;
        ecgValue = voltValue + (gap * rate);
    }
    // T 파형 생성
    else if (fmod(time, 1.0f) > 0.4f && fmod(time, 1.0f) < 0.6f) {
        centerValue += 7.47f * sin(2 * PI * 5 * fmod(time, 1.0f)); // T 파형 계산
        gap = centerValue - voltValue;
        ecgValue = voltValue + (gap * rate);
    }
    else {
        gap = centerValue - voltValue;
        ecgValue = voltValue + (gap * rate);
    }

    // 값을 0에서 44 사이로 제한
    ecgValue = max(0.0f, min(44.0f, ecgValue));

    return ecgValue;
}

// 픽셀 업데이트 함수
void updatePixel(void)
{
  int AD8232_data;
  
  if((digitalRead(PIN_AD8232_LO_P) == 1) || (digitalRead(PIN_AD8232_LO_N) == 1)) {
    AD8232_data = 512; // 센서 상태가 좋지 않으면 중간값 설정
  }
  else {
    AD8232_data = analogRead(PIN_AD8232_OUTPUT); // 센서 데이터 읽기
  }

  // Serial.print("AD8232_data:");
  // Serial.println(AD8232_data);

  // 좌표축 출력 추가 0712
  for(uint16_t i = 0u; i < sizeof(pixel); i++) {
    if((i & 0x003f) == 0x003f) { // X axis 출력
      pixel[i].U8 = 0xff;
    }
    else if(i >= 10 && i < 64) { // Y axis 출력
      pixel[i].U8 |= 0x80;
    }
  }


  // 보정된 ECG 값 생성
  float time = millis() / 1000.0;  // 시간을 초 단위로 계산
  // if (vTime >= 0.0 && vTime < 1.0) {
  //     vTime += 0.01f;
  // } else {
  //     vTime = 0.0f;
  // }
  float voltValue = AD8232_data / 1023.0 * 44;  // 아날로그 값을 0~44 범위로 변환
  float ecgValue = generateECGValue(time, voltValue);

  // ecgValue를 다시 0~1023 범위로 변환
  AD8232_data = ecgValue / 44.0 * 1023;

  int GLCD_index = (AD8232_data / 16) / 8; // 인덱스 계산
  int GLCD_mod = (AD8232_data / 16) % 8; // 모드 계산
  
  for(uint8_t i = 0u; i < 8u; i++) {
    GLCD_print_data[GLCD_print_index][i] = 0; // 초기화
  }
  GLCD_print_data[GLCD_print_index][GLCD_index] = 1 << GLCD_mod; // 데이터 설정
  GLCD_print_index = (GLCD_print_index + 1) % 128; // 인덱스 업데이트
}

// 와이파이 데이터 전송 함수
String WIFI_sendData(String command, const int timeout, bool debug)
{
  String response = "";
  long int t;

  Serial1.print(command); // 명령어 전송

  t = millis();
  while((t+timeout) > millis()) {
    while(Serial1.available()) {
      response += (char)Serial1.read(); // 응답 읽기
    }
  }

  if(debug) {
    Serial.print(response); // 디버그 출력
  }

  return response;
}

// 와이파이로 펄스 데이터 전송 함수
void WIFI_Send_Pulse_Data(void)
{
  WIFI_sendData("AT+CIPSEND=0,129\r\n",50,true); // 데이터 전송 명령어
  
  Serial.write(BPM); // 심박수 전송
  Serial1.write(BPM);
  for(uint16_t i = 0u; i < 128u; i++) {
    Serial1.write(GLCD_print_data[i][0]); // 그래픽 LCD 데이터 전송
  }
}

// 와이파이 초기화 메시지 전송 함수
void WIFI_Send_Init_Msg(void)
{
  const int WIFI_Initial_time = 4000;
  
  WIFI_sendData("AT+RST\r\n",WIFI_Initial_time,true);   // 모듈 리셋 명령어
  WIFI_sendData("AT+CWMODE=3\r\n",WIFI_Initial_time,true);    // STATION, AP모드 동시 사용 명령어
  WIFI_sendData("AT+CWSAP=\"ESP01\",\"asdfghjkl\",5,4\r\n",WIFI_Initial_time,true);   // 와이파이 서버 설정(이름, 암호)
  WIFI_sendData("AT+CIPMUX=1\r\n",WIFI_Initial_time,true);   // MUX On
  WIFI_sendData("AT+CIPSERVER=1,12000\r\n",WIFI_Initial_time,true);  // 서버 구축 Port number 12000
}

// 전송 모드 선택 함수
bool prompt_select_transmit_mode(void)
{
  bool ret = false;
  
  Serial.println("///////////////////////////////////");
  Serial.println("// 1. Graphic LCD                //");
  Serial.println("// 2. WIFI (ESP-10)              //");
  Serial.println("///////////////////////////////////");
  while(1) {
    if(Serial.available()) {
      transmit_mode = Serial.read() - '0';
      switch(transmit_mode) {
        case 1:
          Serial.println("Selected Graphic LCD Mode");
          ret = true;
          break;
        case 2:
          Serial1.begin(115200);
          delay(10);
          Serial.println("Selected WIFI Mode");
          WIFI_Send_Init_Msg();
          ret = true;
          break;
        default:
          Serial.println("Error: Select Correct Mode");
          break;
      }
      if(ret) {
        break;
      }
    }
  }

  return ret;
}

// 심박 이벤트 콜백 함수
void hr_Event(uint8_t rawData, int value)
{
 Serial.print("HeartRate Value = "); Serial.println(value);
  BPM = (uint8_t)value;
}

// 타이머 이벤트 함수
void timerEvent(void)
{
  updatePixel(); // 픽셀 업데이트
}

// 설정 함수
void setup() {
  // 설정 코드 (한 번만 실행됨)
  Serial.begin(115200); 
  Serial1.begin(115200); // 와이파이 모듈 기본 설정

  WIFI_Send_Init_Msg();

//  while(!prompt_select_transmit_mode());
  
  pinMode(PIN_AD8232_LO_P, INPUT);
  pinMode(PIN_AD8232_LO_N, INPUT);

  heartspeed.setCB(hr_Event); // 콜백 함수 설정
  heartspeed.begin(); // 심박 측정 시작

  // 타이머 인터럽트 초기화
  Timer1.initialize(data_period*1000); // 타이머 초기화
  Timer1.attachInterrupt(timerEvent); // 타이머 이벤트 함수 연결
}

// 메인 루프 함수
void loop() {
  // 메인 코드 (반복 실행됨)
  nowTime = millis();
  
  if((unsigned long)(nowTime - draw_lastTime) > draw_period) { // 주기적으로 화면 업데이트
    draw_lastTime = nowTime; // 업데이트 시간 기록

//    noInterrupts();
    draw(); // 화면 그리기 함수 호출
//    interrupts();
WIFI_Send_Pulse_Data(); // 와이파이로 데이터 전송
  }
}