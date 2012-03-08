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

void setup();
void loop();
void serialInterrupt();

uint8_t msg_in[128];
uint8_t msg_out[128];
uint8_t sizeOut = 0;

void setup()
{
  Serial.begin(115200);
  while(Usb.Init() == -1) 
    delay( 100 );  

  sizeOut = 0;
}

int test = 0;
uint16_t len = 128;
uint8_t rcode;

void loop()
{
  while(Serial.available())
    msg_out[sizeOut++] = Serial.read();
  
  Usb.Task();
  if( adk.isReady() == false )
    return;

  len = 128;
  rcode = adk.RcvData(&len,msg_in);
  if(len>0)
    Serial.write(msg_in,len);

  if(sizeOut>0)
  {
    adk.SndData(sizeOut,msg_out);
    sizeOut = 0; 
  }
}
