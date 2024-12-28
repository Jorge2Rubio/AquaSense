#include <ESP8266WiFi.h>
#include <string> 
#include <ESP8266HTTPClient.h>
#include <WiFiClientSecureBearSSL.h>

#define POWER_PIN  D7
#define SIGNAL_PIN A0

const char* ssid = "Dismiss";
const char* password = "123456789";
const char* firebaseUrl = "https://water-sensor-614db-default-rtdb.asia-southeast1.firebasedatabase.app/Levels.json?auth=AIzaSyBFEu756DrcLBRhYUpQi-3h3EQ7BOe3kVY";

void setup() {
  Serial.begin(9600);
  pinMode(POWER_PIN, OUTPUT);   // Configure D7 pin as an OUTPUT
  digitalWrite(POWER_PIN, LOW); // turn the sensor OFF
  delay(100);

  // Connect to Wi-Fi
  Serial.print("Connecting to ");
  Serial.println(ssid);
  WiFi.begin(ssid, password);
  
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  Serial.println("\nWiFi connected");
}

void loop() {
  int rawValue = readSensor();
  int scaledValue;
  
  if (rawValue >= 8 && rawValue <= 354) {
    // Map raw values from 8 to 282 to scaled values from 0 to 49%
    scaledValue = map(rawValue, 8, 354, 0, 49);
  } else if (rawValue > 354 && rawValue <= 405) {
    // Map raw values from 282 to 375 to scaled values from 50 to 89%
    scaledValue = map(rawValue, 354, 405, 50, 89);
  } else if (rawValue > 405 && rawValue <= 474) {
    // Map raw values from 375 to 430 to scaled values from 90 to 100%
    scaledValue = map(rawValue, 375, 474, 90, 100);
  } else {
    // Handle values outside the specified ranges
    // Here, we can simply clip the values to the appropriate range
    if (rawValue < 8)
      scaledValue = 0;
    else if (rawValue > 474)
      scaledValue = 100;
  }

  Serial.print("Raw value: ");
  Serial.println(rawValue);
  Serial.print("Scaled value: ");
  Serial.println(scaledValue);
  sendDataToFirebase(scaledValue);
  delay(0);  // Adjust delay according to your needs
}
int readSensor() {
  digitalWrite(POWER_PIN, HIGH);  // turn the sensor ON
  delay(10);                      // wait 10 milliseconds
  int value = analogRead(SIGNAL_PIN); // read the analog value from sensor
  digitalWrite(POWER_PIN, LOW);   // turn the sensor OFF
  return value;
}

void sendDataToFirebase(int value) {
  std::unique_ptr<BearSSL::WiFiClientSecure> client(new BearSSL::WiFiClientSecure);
  client->setInsecure(); // Ignore SSL certificate verification (not recommended in production)
  HTTPClient https;

  https.begin(*client, firebaseUrl);
  https.addHeader("Content-Type", "application/json");

  String payload = "{\"Levels\":" + String(value) + "}";
  int httpResponseCode = https.PUT(payload);

  if (httpResponseCode > 0) {
    Serial.print("HTTP Response code: ");
    Serial.println(httpResponseCode);
  } else {
    Serial.print("Error code: ");
    Serial.println(httpResponseCode);
  }

  https.end();
}
