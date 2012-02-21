#include <avrpins.h>
#include <max3421e.h>
#include <usbhost.h>
#include <usb_ch9.h>
#include <Usb.h>
#include <usbhub.h>
#include <avr/pgmspace.h>
#include <address.h>

#include <adk.h>

USB Usb;

ADK adk(&Usb,
        "CSTE Project",
        "Zigbee Bridge",
        "Android to Zigbee radio bridge",
        "1.0",
        "http://www.android.com",
        "0000000012345678");
        
#define  LED_PIN       3
uint8_t LED_STATE = 0;

void setup();
void loop();

void mirrorSerialMsg();
void init_leds();
void toggleLED();

void setup()
{
  Serial.begin(115200);
  if (Usb.Init() == -1) 
  {
    Serial.println("OSCOKIRQ failed to assert");
    while(1); //halt
  }//if (Usb.Init() == -1...
}

void loop()
{
  uint8_t rcode;
  uint16_t len = 64;
  uint8_t msg[128] = { 0x00 };
  
  uint8_t test[] = { 0x01 , 0x02 , 0x03 , 0x04 };

  Usb.Task();
  
  if( adk.isReady() == false )
     return;
 
  //pass data from android to zigbee
  rcode = adk.RcvData(&len,msg);
  if(len>0)
    Serial.write(msg,len);
      
  // pass data from zigbee to android
  len = 0;
  while(Serial.available())
    msg[len++] = Serial.read();
  if(len>0)
    adk.SndData(len,msg);
    
  delay( 10 );     
}

void init_leds()
{
  LED_STATE = 0;
  pinMode(LED_PIN, OUTPUT);
  digitalWrite(LED_PIN, LED_STATE);
}

void toggleLED()
{
  LED_STATE = LED_STATE ? 0 : 1;
  digitalWrite(LED_PIN, LED_STATE);
}

void mirrorSerialMsg()
{
  uint8_t msg[128];
  int i;
  while(true)
  {
    i = 0;
    while(Serial.available())
      msg[i++] = Serial.read()+1;
    if( i>0)
      Serial.write(msg,i);
    delay( 10 );  
  }
}

