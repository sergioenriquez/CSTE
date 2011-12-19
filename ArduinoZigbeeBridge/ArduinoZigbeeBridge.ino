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
int led1Pin = 10;      // select the pin for the LED
int led2Pin = 11;      // select the pin for the LED
int sensorValue = 0;  // variable to store the value coming from the sensor

void setup() {
  
  Serial.begin(115200);
  Serial.print("\r\nStart");
  acc.powerOn();

  // declare the ledPin as an OUTPUT:
  pinMode(led1Pin, OUTPUT);  
  pinMode(led2Pin, OUTPUT); 
}

void loop() {
  byte msg[3];
  
  if (acc.isConnected()) {
    //int len = acc.read(msg, sizeof(msg), 1);
    sensorValue = analogRead(sensorPin);
    msg[0] = 0x01;
    msg[1] = sensorValue >> 8;
    msg[2] = sensorValue & 0xFF;
    acc.write(msg,3);

  digitalWrite(led1Pin,HIGH);
  digitalWrite(led2Pin,LOW);
    
  }else {
    digitalWrite(led1Pin,LOW);
    digitalWrite(led2Pin,HIGH);
//    analogWrite(led2Pin,(sensorValue/1024.0)*255);
  }
  // read the value from the sensor:
  //sensorValue = analogRead(sensorPin);    
   
  //analogWrite(led1Pin,(sensorValue/1024.0)*255);   
  // stop the program for for <sensorValue> milliseconds:
  delay(100);                  
}
