/*
This sketch is for the Freenove ESP32 board and DHT22 sensor.
It reads temperature and humidity with proper formatting and units.

Make sure you have the following libraries installed:
- ESP32 by Espressif Systems
- Adafruit Unified Sensor
- DHT sensor library by Adafruit
*/

#include <DHT.h>

#define DHTPIN 26
#define DHTTYPE DHT22

DHT dht(DHTPIN, DHTTYPE);

void setup() {
  Serial.begin(115200);
  delay(1000);
  dht.begin();
  delay(3000); // Give DHT22 time to initialize
  
  Serial.println("=== DHT22 Temperature & Humidity Monitor ===");
  Serial.println("Time (s) | Temperature | Humidity");
  Serial.println("---------|-------------|----------");
}

void loop() {
  float humidity = dht.readHumidity();
  float temperature = dht.readTemperature(); // Celsius by default

  if (isnan(humidity) || isnan(temperature)) {
    Serial.println("ERROR: Failed to read from DHT sensor!");
  } else {
    // Calculate elapsed time in seconds
    static unsigned long startTime = millis();
    unsigned long elapsedSeconds = (millis() - startTime) / 1000;
    
    // Format and display the readings
    Serial.print("   ");
    if (elapsedSeconds < 10) Serial.print(" ");
    if (elapsedSeconds < 100) Serial.print(" ");
    Serial.print(elapsedSeconds);
    Serial.print("   |   ");
    Serial.print(temperature, 1);
    Serial.print("°C   |   ");
    Serial.print(humidity, 1);
    Serial.println("%");
    
    // Also output CSV format for logging (commented out - uncomment if needed)
    // Serial.print("CSV: ");
    // Serial.print(elapsedSeconds);
    // Serial.print(",");
    // Serial.print(temperature, 2);
    // Serial.print(",");
    // Serial.println(humidity, 2);
  }

  delay(5000); // Read every 5 seconds (change to 60000 for 1 minute)
}