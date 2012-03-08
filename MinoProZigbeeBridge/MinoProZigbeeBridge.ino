#include <avrpins.h>
#include <max3421e.h>
#include <usbhost.h>
#include <usb_ch9.h>
#include <Usb.h>
#include <usbhub.h>
#include <avr/pgmspace.h>
#include <address.h>
#include <avr/wdt.h>
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
 // pinMode(10, OUTPUT);     
  //digitalWrite(10, HIGH);   // set the LED on
  //wdt_disable();
  //wdt_reset();
  Serial.begin(115200);

  while(Usb.Init() == -1) 
    delay( 100 );  
}

uint8_t msg[256] = { 0x00 };
int test = 0;
uint16_t len = 128;
uint8_t rcode;

void loop()
{
  Usb.Task();
  if( adk.isReady() == true )
  {
    //pass data from android to zigbee
    
    len = 128;
    rcode = adk.RcvData(&len,msg);
    if(len>0)
      Serial.write(msg,len);
    

    len = 0; 
    //while(Serial.available() && len < 256)
    //{ 
      while(Serial.available())
          msg[len++] = Serial.read();
      //delay(1);       
    //}
    
    if(len>0)
          adk.SndData(len+1,msg); 
  } 
  delay( 0 );     
}

