#include <ch9.h>
#include <Max3421e.h>
#include <Max3421e_constants.h>
#include <Usb.h>
#include <AndroidAccessory.h>

AndroidAccessory acc("CSTE Project",
		     "Zigbee Bridge",
		     "Android to Zigbee radio bridge",
		     "1.0",
		     "http://www.android.com",
		     "0000000012345678");

void setup() {
  acc.powerOn();
  Serial.begin(115200);
}

void loop() 
{
  byte msg[128];
  int len;
  
  if (acc.isConnected()) 
  {

    // pass data from zigbee to android
    len = 0;
    while(Serial.available()){
      msg[len++] = Serial.read();
    }
    if(len>0)
      acc.write(msg,len);
    
    //pass data from android to zigbee
    len = acc.read(msg, sizeof(msg), 1);
    if(len>0)
      Serial.write(msg,len);
    
    delay(1);
  }
  delay(5);                  
}
