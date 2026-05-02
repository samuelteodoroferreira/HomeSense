#!/usr/bin/env python3
"""
Gera docs/wiring-home-sense.png — diagrama de montagem de referência (estilo esquemático).
Inclui fios AC monofásicos (fase L / neutro N) até o módulo ZMPT101B (~110 V / ~220 V conforme região).
"""
from __future__ import annotations

from pathlib import Path

from PIL import Image, ImageDraw, ImageFont

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / "docs" / "wiring-home-sense.png"

W, H = 1400, 820
BG = "#0d1117"
PANEL = "#161b22"
TEXT = "#e6edf3"
MUTED = "#8b949e"
ACCENT = "#58a6ff"
WARN = "#d4a72c"
AC_L = "#b5651d"
AC_N = "#3d7ea6"
WIRE_RED = "#f85149"
WIRE_BLK = "#30363d"
WIRE_ORG = "#db6d28"
WIRE_YEL = "#d29922"


def load_font(size: int) -> ImageFont.FreeTypeFont | ImageFont.ImageFont:
    for path in (
        "/System/Library/Fonts/Supplemental/Arial Unicode.ttf",
        "/System/Library/Fonts/Helvetica.ttc",
        "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf",
    ):
        p = Path(path)
        if p.exists():
            return ImageFont.truetype(str(p), size)
    return ImageFont.load_default()


def main() -> None:
    img = Image.new("RGB", (W, H), BG)
    d = ImageDraw.Draw(img)
    f_title = load_font(26)
    f_lg = load_font(18)
    f_md = load_font(15)
    f_sm = load_font(13)

    d.text((40, 24), "HOME_SENSE — diagrama de montagem (referência)", fill=ACCENT, font=f_title)
    d.text((40, 58), "Não substitui datasheet do módulo ZMPT101B nem pinout da sua placa ESP32.", fill=MUTED, font=f_sm)

    # Power bank
    pb = (560, 88, 720, 168)
    d.rounded_rectangle(pb, radius=12, fill=PANEL, outline=MUTED, width=2)
    d.text((pb[0] + 24, pb[1] + 28), "Power bank 5 V (USB)", fill=TEXT, font=f_md)

    # ESP32
    esp = (480, 260, 800, 620)
    d.rounded_rectangle(esp, radius=14, fill=PANEL, outline=ACCENT, width=2)
    d.text((esp[0] + 120, esp[1] + 20), "ESP32 DevKit", fill=TEXT, font=f_lg)
    d.text((esp[0] + 24, esp[1] + 70), "USB", fill=MUTED, font=f_sm)
    d.text((esp[0] + 24, esp[1] + 200), "GPIO 4", fill=TEXT, font=f_sm)
    d.text((esp[0] + 24, esp[1] + 230), "3,3 V", fill=TEXT, font=f_sm)
    d.text((esp[0] + 24, esp[1] + 260), "GND", fill=TEXT, font=f_sm)
    d.text((esp[0] + 24, esp[1] + 320), "GPIO 34 (ADC1)", fill=TEXT, font=f_sm)
    d.text((esp[0] + 24, esp[1] + 350), "GND", fill=TEXT, font=f_sm)

    # USB cable power bank -> ESP32 (center top of esp)
    usb_top = ((esp[0] + esp[2]) // 2, esp[1])
    pb_bottom = ((pb[0] + pb[2]) // 2, pb[3])
    d.line((pb_bottom[0], pb_bottom[1], usb_top[0], usb_top[1]), fill=MUTED, width=4)
    d.text((usb_top[0] + 12, (pb_bottom[1] + usb_top[1]) // 2 - 8), "alim. ESP32", fill=MUTED, font=f_sm)

    # DHT11
    dht = (120, 320, 300, 500)
    d.rounded_rectangle(dht, radius=12, fill=PANEL, outline=MUTED, width=2)
    d.text((dht[0] + 48, dht[1] + 20), "DHT11", fill=TEXT, font=f_lg)
    d.text((dht[0] + 20, dht[1] + 70), "VCC", fill=MUTED, font=f_sm)
    d.text((dht[0] + 20, dht[1] + 100), "DATA", fill=MUTED, font=f_sm)
    d.text((dht[0] + 20, dht[1] + 130), "GND", fill=MUTED, font=f_sm)

    # Wires DHT -> ESP (right side of dht to left of esp)
    x_d = dht[2]
    y_v, y_d, y_g = dht[1] + 78, dht[1] + 108, dht[1] + 138
    x_e = esp[0] + 8
    y_e_v, y_e_d, y_e_g = esp[1] + 208, esp[1] + 238, esp[1] + 268
    for (ya, yb, col) in ((y_v, y_e_v, WIRE_RED), (y_d, y_e_d, WIRE_ORG), (y_g, y_e_g, WIRE_BLK)):
        d.line((x_d, ya, x_e, yb), fill=col, width=5)

    # ZMPT module
    zm = (920, 260, 1280, 600)
    d.rounded_rectangle(zm, radius=14, fill="#1a2f45", outline=ACCENT, width=2)
    d.text((zm[0] + 60, zm[1] + 16), "Módulo ZMPT101B", fill=TEXT, font=f_lg)
    # transformer block
    d.rounded_rectangle((zm[0] + 40, zm[1] + 55, zm[0] + 200, zm[1] + 200), radius=8, fill="#0d2137", outline=MUTED)
    d.text((zm[0] + 70, zm[1] + 115), "AC", fill=WARN, font=f_sm)
    # DC side pins (left of module toward ESP)
    d.text((zm[0] + 220, zm[1] + 240), "OUT", fill=TEXT, font=f_sm)
    d.text((zm[0] + 220, zm[1] + 280), "GND", fill=TEXT, font=f_sm)
    d.text((zm[0] + 220, zm[1] + 320), "VCC", fill=TEXT, font=f_sm)
    # AC terminals (right edge — fios rede)
    d.text((zm[2] - 120, zm[1] + 240), "L (fase)", fill=TEXT, font=f_sm)
    d.text((zm[2] - 120, zm[1] + 280), "N (neutro)", fill=TEXT, font=f_sm)

    # ZMPT OUT/GND -> ESP
    x_z_out = zm[0] + 200
    y_z_out, y_z_g = zm[1] + 248, zm[1] + 288
    x_e_r = esp[0] + 8
    y_e_34, y_e_g2 = esp[1] + 328, esp[1] + 358
    d.line((x_z_out, y_z_out, x_e_r, y_e_34), fill=WIRE_YEL, width=5)
    d.line((x_z_out, y_z_g, x_e_r, y_e_g2), fill=WIRE_BLK, width=5)

    # 5V to ZMPT VCC (from power path — schematic: from PB area)
    y_z_v = zm[1] + 328
    d.line((pb[2] - 20, pb[3] + 30, zm[0] + 260, y_z_v), fill=WIRE_RED, width=4)
    d.text((zm[0] + 230, y_z_v - 22), "5 V (VCC do módulo — ver datasheet)", fill=WARN, font=f_sm)

    # --- Rede AC: tomada + dois fios grossos até L e N ---
    ac_box = (1020, 40, 1340, 200)
    d.rounded_rectangle(ac_box, radius=10, fill="#2d1f0d", outline=WARN, width=2)
    d.text((ac_box[0] + 16, ac_box[1] + 12), "Rede AC monofásica (só no módulo)", fill=WARN, font=f_md)
    d.text((ac_box[0] + 16, ac_box[1] + 44), "~110 V · ~127 V · ~220 V (conforme região)", fill=TEXT, font=f_sm)
    d.text((ac_box[0] + 16, ac_box[1] + 68), "Dois fios: fase L e neutro N", fill=MUTED, font=f_sm)

    # Lugs on AC box
    lug_l = (ac_box[0] + 80, ac_box[3] - 8)
    lug_n = (ac_box[2] - 80, ac_box[3] - 8)
    d.ellipse((lug_l[0] - 8, lug_l[1] - 8, lug_l[0] + 8, lug_l[1] + 8), fill=AC_L, outline=TEXT)
    d.ellipse((lug_n[0] - 8, lug_n[1] - 8, lug_n[0] + 8, lug_n[1] + 8), fill=AC_N, outline=TEXT)

    # Target points on ZMPT (L, N screw area — right side)
    pt_l = (zm[2] - 24, zm[1] + 252)
    pt_n = (zm[2] - 24, zm[1] + 292)

    # Thick AC wires
    d.line((lug_l[0], lug_l[1], pt_l[0], pt_l[1]), fill=AC_L, width=8)
    d.line((lug_n[0], lug_n[1], pt_n[0], pt_n[1]), fill=AC_N, width=8)

    d.text((lug_l[0] - 30, lug_l[1] + 14), "L", fill=AC_L, font=f_lg)
    d.text((lug_n[0] - 8, lug_n[1] + 14), "N", fill=AC_N, font=f_lg)

    # Warning strip
    d.rounded_rectangle((40, H - 72, W - 40, H - 24), radius=8, fill="#3d2a00", outline=WARN, width=1)
    d.text(
        (56, H - 58),
        "⚠ Tensão de rede — ligue fase (L) e neutro (N) apenas nos bornes AC do módulo. Nunca leve AC ao ESP32.",
        fill=WARN,
        font=f_sm,
    )

    OUT.parent.mkdir(parents=True, exist_ok=True)
    img.save(OUT, "PNG", optimize=True)
    print(f"Escrito: {OUT}")


if __name__ == "__main__":
    main()
