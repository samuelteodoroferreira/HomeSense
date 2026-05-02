#pragma once

// Pinos
static constexpr int PIN_DHT = 4;
static constexpr int PIN_ZMPT_ADC = 34;  // ADC1 — entrada analógica

// DHT11 (Adafruit DHT: modelo 11)
static constexpr int DHT_MODEL = 11;

// Publicação MQTT
static constexpr unsigned long PUBLISH_INTERVAL_MS = 30'000;

// Leitura ZMPT101B (uma “instantânea” RMS)
static constexpr int ZMPT_SAMPLES = 256;
static constexpr int ZMPT_SAMPLE_DELAY_US = 200;

// Debounce de tensão (evita picos / ruído como “queda de energia”)
// Várias leituras RMS espaçadas no tempo; só tratamos como “sem rede” se
// houver sequência consecutiva de leituras abaixo do limiar.
static constexpr int ZMPT_DEBOUNCE_BURST_COUNT = 10;       // nº de RMS instantâneos por ciclo
static constexpr unsigned long ZMPT_DEBOUNCE_GAP_MS = 40; // espaço entre cada RMS instantâneo
static constexpr float ZMPT_VOLTAGE_LOW_V = 25.0f;       // abaixo disso = candidato a “sem tensão” (calibre)
static constexpr int ZMPT_DEBOUNCE_CONSECUTIVE_LOW = 4;    // mínimo de baixos consecutivos para aceitar queda

// Tópicos MQTT
static constexpr const char* TOPIC_ENV = "homesense/edge/%s/env";
static constexpr const char* TOPIC_POWER = "homesense/edge/%s/power";
