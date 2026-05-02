/**
 * HOME_SENSE — ESP32 (Arduino framework)
 * DHT no GPIO 4, ZMPT101B no ADC1 GPIO 34.
 * Wi-Fi + MQTT: JSON a cada 30 s.
 * Debounce simples na tensão (sequência consecutiva abaixo do limiar).
 */
#include <Arduino.h>
#include <cmath>
#include <algorithm>
#include <WiFi.h>
#include <PubSubClient.h>
#include <DHT.h>

#include "config.h"

#if __has_include("secrets.h")
#include "secrets.h"
#else
#include "secrets.example.h"
#endif

WiFiClient wifiClient;
PubSubClient mqtt(wifiClient);
DHT dht(PIN_DHT, DHT_MODEL);

char topicEnv[64];
char topicPower[64];
char chipId[16];

/** Uma estimativa instantânea de RMS (V) a partir do ADC. */
float readZmptRmsVoltsOnce() {
  double sumSq = 0;
  const int n = ZMPT_SAMPLES;
  for (int i = 0; i < n; ++i) {
    int raw = analogRead(PIN_ZMPT_ADC);
    double v = (raw / 4095.0) * 3.3;
    sumSq += v * v;
    delayMicroseconds(ZMPT_SAMPLE_DELAY_US);
  }
  double meanSq = sumSq / n;
  double rms = sqrt(meanSq);
  const float calibration = 180.0f;  // calibrar com multímetro na sua montagem
  return static_cast<float>(rms * calibration);
}

/** Mediana de um array pequeno (cópia ordenada in-place do buffer auxiliar). */
static float medianSmall(float* buf, int n) {
  if (n <= 0) return 0.0f;
  std::sort(buf, buf + n);
  if ((n & 1) == 1) {
    return buf[n / 2];
  }
  return 0.5f * (buf[n / 2 - 1] + buf[n / 2]);
}

/**
 * Debounce: várias leituras RMS ao longo de ~400 ms.
 * Só devolve tensão “de queda” (valor baixo) se existir sequência consecutiva
 * de leituras < ZMPT_VOLTAGE_LOW_V; caso contrário devolve a mediana (menos sensível a picos).
 */
float readZmptRmsDebounced() {
  const int k = ZMPT_DEBOUNCE_BURST_COUNT;
  float burst[ZMPT_DEBOUNCE_BURST_COUNT];

  for (int i = 0; i < k; ++i) {
    burst[i] = readZmptRmsVoltsOnce();
    if (i + 1 < k) {
      delay(ZMPT_DEBOUNCE_GAP_MS);
    }
  }

  int maxConsecutiveLow = 0;
  int cur = 0;
  for (int i = 0; i < k; ++i) {
    if (burst[i] < ZMPT_VOLTAGE_LOW_V) {
      ++cur;
      if (cur > maxConsecutiveLow) {
        maxConsecutiveLow = cur;
      }
    } else {
      cur = 0;
    }
  }

  if (maxConsecutiveLow >= ZMPT_DEBOUNCE_CONSECUTIVE_LOW) {
    float minLow = ZMPT_VOLTAGE_LOW_V;
    for (int i = 0; i < k; ++i) {
      if (burst[i] < ZMPT_VOLTAGE_LOW_V && burst[i] < minLow) {
        minLow = burst[i];
      }
    }
    return minLow < 0.0f ? 0.0f : minLow;
  }

  float copy[ZMPT_DEBOUNCE_BURST_COUNT];
  for (int i = 0; i < k; ++i) {
    copy[i] = burst[i];
  }
  return medianSmall(copy, k);
}

void ensureMqttTopics() {
  uint64_t mac = ESP.getEfuseMac();
  snprintf(chipId, sizeof(chipId), "%04X%08X", (uint16_t)(mac >> 32), (uint32_t)mac);
  snprintf(topicEnv, sizeof(topicEnv), TOPIC_ENV, chipId);
  snprintf(topicPower, sizeof(topicPower), TOPIC_POWER, chipId);
}

void reconnectMqtt() {
  constexpr int kMaxAttempts = 15;
  int attempts = 0;
  while (!mqtt.connected() && attempts < kMaxAttempts) {
    if (WiFi.status() != WL_CONNECTED) {
      WiFi.reconnect();
      delay(500);
      ++attempts;
      continue;
    }
    if (mqtt.connect(MQTT_CLIENT_ID, MQTT_USER[0] ? MQTT_USER : nullptr,
                     MQTT_PASS[0] ? MQTT_PASS : nullptr)) {
      return;
    }
    delay(2000);
    ++attempts;
  }
}

void setup() {
  Serial.begin(115200);
  analogSetAttenuation(ADC_11db);
  dht.begin();
  ensureMqttTopics();

  WiFi.mode(WIFI_STA);
  WiFi.setAutoReconnect(true);
  WiFi.begin(WIFI_SSID, WIFI_PASS);
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
  }

  mqtt.setServer(MQTT_HOST, MQTT_PORT);
}

void loop() {
  if (WiFi.status() != WL_CONNECTED) {
    if (mqtt.connected()) {
      mqtt.disconnect();
    }
    delay(200);
    return;
  }

  if (!mqtt.connected()) {
    reconnectMqtt();
  }
  mqtt.loop();

  static unsigned long lastPublish = 0;
  unsigned long now = millis();
  if (now - lastPublish < PUBLISH_INTERVAL_MS) {
    delay(10);
    return;
  }
  lastPublish = now;

  float h = dht.readHumidity();
  float t = dht.readTemperature();
  float v = readZmptRmsDebounced();

  char payloadEnv[112];
  snprintf(payloadEnv, sizeof(payloadEnv),
           "{\"t\":%.2f,\"h\":%.2f,\"ts\":%lu}",
           isnan(t) ? -99.0f : t, isnan(h) ? -99.0f : h, now / 1000UL);

  char payloadPower[72];
  snprintf(payloadPower, sizeof(payloadPower), "{\"v_rms\":%.2f,\"ts\":%lu}", v, now / 1000UL);

  if (!mqtt.publish(topicEnv, payloadEnv, true)) {
    Serial.println(F("MQTT publish env falhou (buffer?)"));
  }
  if (!mqtt.publish(topicPower, payloadPower, true)) {
    Serial.println(F("MQTT publish power falhou (buffer?)"));
  }
}
