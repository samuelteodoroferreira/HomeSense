#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Simula o ESP32 HOME_SENSE: publica JSON de clima e tensão via MQTT.

Uso típico (com Fog + Cloud assinando edge/#):
  pip install -r requirements.txt
  python mqtt_esp32_simulator.py --host 127.0.0.1 --device-id DEMO01

Só Cloud (sem Fog em execução): publicar no prefixo fog, com campo "q" no env:
  python mqtt_esp32_simulator.py --host 127.0.0.1 --prefix fog --device-id demo-device

Demonstração de queda e retorno da tensão (vários ciclos a ~0 V e depois rede):
  python mqtt_esp32_simulator.py --demo-outage
"""

from __future__ import annotations

import argparse
import json
import math
import random
import sys
import time
from typing import Literal

import paho.mqtt.client as mqtt

Prefix = Literal["edge", "fog"]


def build_topics(prefix: Prefix, device_id: str) -> tuple[str, str]:
    if prefix == "edge":
        env = f"homesense/edge/{device_id}/env"
        power = f"homesense/edge/{device_id}/power"
    else:
        env = f"homesense/fog/{device_id}/env"
        power = f"homesense/fog/{device_id}/power"
    return env, power


def payload_env(prefix: Prefix, t: float, h: float, ts: int) -> str:
    body: dict = {"t": round(t, 2), "h": round(h, 2), "ts": ts}
    if prefix == "fog":
        body["q"] = "OK"
    return json.dumps(body, separators=(",", ":"))


def payload_power(v_rms: float, ts: int) -> str:
    return json.dumps({"v_rms": round(v_rms, 2), "ts": ts}, separators=(",", ":"))


def parse_args() -> argparse.Namespace:
    p = argparse.ArgumentParser(description="Simula telemetria MQTT do ESP32 (HOME_SENSE).")
    p.add_argument("--host", default="127.0.0.1", help="Broker MQTT (ex.: 127.0.0.1 ou IP do Raspberry).")
    p.add_argument("--port", type=int, default=1883, help="Porta MQTT (1883 por padrão).")
    p.add_argument("--user", default="", help="Usuário MQTT (opcional).")
    p.add_argument("--password", default="", help="Palavra-passe MQTT (opcional).")
    p.add_argument(
        "--device-id",
        default="DEMO01",
        help="ID do dispositivo (deve coincidir com o que o app consulta na API, ex.: demo-device).",
    )
    p.add_argument(
        "--prefix",
        choices=("edge", "fog"),
        default="edge",
        help="edge = como o ESP (Fog/Cloud encadeados). fog = direto para a Cloud sem Fog.",
    )
    p.add_argument(
        "--interval",
        type=float,
        default=30.0,
        help="Segundos entre cada par de publicações (clima + tensão).",
    )
    p.add_argument(
        "--demo-outage",
        action="store_true",
        help="Após alguns ciclos normais, simula queda (tensão ~0) e depois retorno da energia (para vídeo / backend).",
    )
    return p.parse_args()


def main() -> int:
    args = parse_args()
    env_topic, power_topic = build_topics(args.prefix, args.device_id)

    client = mqtt.Client(client_id=f"homesense-sim-{args.device_id}")
    if args.user:
        client.username_pw_set(args.user, args.password or None)

    connected = {"ok": False, "err": None}

    def on_connect(client, userdata, flags, rc):
        if rc == 0:
            connected["ok"] = True
            print(f"[MQTT] Ligado a {args.host}:{args.port}", flush=True)
        else:
            connected["err"] = rc
            print(f"[MQTT] Falha na ligação: rc={rc}", flush=True)

    def on_disconnect(client, userdata, rc):
        connected["ok"] = False
        print(f"[MQTT] Desligado (rc={rc})", flush=True)

    client.on_connect = on_connect
    client.on_disconnect = on_disconnect

    try:
        client.connect(args.host, args.port, keepalive=60)
    except OSError as e:
        print(f"Erro ao ligar ao broker: {e}", file=sys.stderr)
        return 1

    client.loop_start()
    deadline = time.time() + 10.0
    while time.time() < deadline and not connected["ok"] and connected["err"] is None:
        time.sleep(0.05)
    if not connected["ok"]:
        print("Timeout à espera de MQTT.", file=sys.stderr)
        client.loop_stop()
        return 1

    cycle = 0
    start = time.time()
    print(f"Tópicos: {env_topic} | {power_topic}", flush=True)
    print(f"Intervalo: {args.interval}s | prefixo={args.prefix}", flush=True)

    try:
        while True:
            ts = int(time.time())
            phase = (time.time() - start) * 0.15

            # Variação suave + ruído pequeno (parece sensor real)
            t_base = 22.0 + 2.0 * math.sin(phase)
            h_base = 50.0 + 5.0 * math.cos(phase * 0.7)
            t = t_base + random.uniform(-0.4, 0.4)
            h = max(15.0, min(85.0, h_base + random.uniform(-1.0, 1.0)))

            v: float
            if args.demo_outage:
                # Ciclos 0–4: normal; 5–12: queda; 13+: energia de volta
                if cycle < 5:
                    v = 227.0 + random.uniform(-3, 3)
                elif cycle < 13:
                    v = random.uniform(0.0, 8.0)
                else:
                    v = 227.0 + random.uniform(-2, 2)
            else:
                v = 220.0 + 10.0 * math.sin(phase * 0.3) + random.uniform(-2, 2)
                v = max(0.0, v)

            pe = payload_env(args.prefix, t, h, ts)
            pp = payload_power(v, ts)
            client.publish(env_topic, pe, qos=1, retain=False)
            client.publish(power_topic, pp, qos=1, retain=False)

            print(f"[{ts}] env={pe} | power={pp}", flush=True)
            cycle += 1
            time.sleep(args.interval)
    except KeyboardInterrupt:
        print("\nA terminar…", flush=True)
    finally:
        client.loop_stop()
        try:
            client.disconnect()
        except Exception:
            pass
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
