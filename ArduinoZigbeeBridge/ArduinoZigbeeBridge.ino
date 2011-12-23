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

int sensorPin = A0;    // select the input pin for the potentiometer
int ledPin = 13;      // select the pin for the LED

int sensorValue = 0;  // variable to store the value coming from the sensor

void setup() {
  Serial1.begin(9600);
  acc.powerOn();
  pinMode(ledPin, OUTPUT);  
}

void loop() {
  byte msg[128];
  int len;
  
  if (acc.isConnected()) {
    digitalWrite(ledPin, HIGH);
    
    // pass data from zigbee to android
    len = 0;
    while(Serial1.available()){
      msg[len++] = Serial1.read();
    }
    if(len>0)
      acc.write(msg,len);
    
    //pass data from android to zigbee
    len = acc.read(msg, sizeof(msg), 1);
    if(len>0)
      Serial1.write(msg,len);
    
    delay(1);
  }else {
    digitalWrite(ledPin, LOW);
  }
  delay(10);                  
}
