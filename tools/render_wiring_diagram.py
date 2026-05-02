#!/usr/bin/env python3
"""
Gera docs/wiring-home-sense-schematic.png — esquema ilustrado (3× supersample + LANCZOS).

O ficheiro docs/wiring-home-sense.png fica reservado ao export Fritzing ou à foto
com legendas (ver README); este script não o sobrescreve.
"""
from __future__ import annotations

from pathlib import Path

from PIL import Image, ImageDraw, ImageFont

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / "docs" / "wiring-home-sense-schematic.png"

LOG_W, LOG_H = 1600, 900
OUT_W, OUT_H = 1600, 900
SCALE = 3

BG_TOP = (8, 11, 18)
BG_BOT = (16, 22, 32)
PCB = (34, 38, 44)
PCB_HI = (48, 54, 62)
PCB_EDGE = (72, 82, 96)
COPPER = (175, 118, 68)
SILVER = (168, 172, 182)
USB_METAL = (88, 92, 102)
DHT_BODY = (26, 105, 158)
DHT_HI = (50, 145, 205)
ZMPT_PCB = (24, 42, 56)
TOROID_DARK = (10, 48, 82)
TOROID_MID = (22, 78, 125)
TOROID_HI = (65, 135, 195)
BRASS = (198, 155, 48)
TEXT = (235, 240, 248)
MUTED = (130, 140, 152)
ACCENT = (80, 160, 255)
WARN = (215, 155, 40)
AC_L = (205, 92, 42)
AC_N = (62, 138, 198)
WIRE_RED = (248, 78, 72)
WIRE_ORG = (222, 112, 42)
WIRE_YEL = (212, 158, 42)
WIRE_BLK = (44, 50, 58)


def load_font(size: int) -> ImageFont.FreeTypeFont | ImageFont.ImageFont:
    size = int(size * SCALE)
    for path in (
        "/System/Library/Fonts/Supplemental/Arial Unicode.ttf",
        "/System/Library/Fonts/Helvetica.ttc",
        "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf",
    ):
        p = Path(path)
        if p.exists():
            return ImageFont.truetype(str(p), max(size, 8))
    return ImageFont.load_default()


def S(v: float) -> int:
    return int(round(v * SCALE))


def fill_vertical_gradient(img: Image.Image, top: tuple[int, int, int], bot: tuple[int, int, int]) -> None:
    w, h = img.size
    px = img.load()
    for y in range(h):
        t = y / max(h - 1, 1)
        c = (
            int(top[0] + (bot[0] - top[0]) * t),
            int(top[1] + (bot[1] - top[1]) * t),
            int(top[2] + (bot[2] - top[2]) * t),
        )
        for x in range(w):
            px[x, y] = c


def cubic_bezier(
    p0: tuple[float, float],
    p1: tuple[float, float],
    p2: tuple[float, float],
    p3: tuple[float, float],
    n: int = 140,
) -> list[tuple[float, float]]:
    pts = []
    for i in range(n + 1):
        t = i / n
        u = 1 - t
        x = u**3 * p0[0] + 3 * u**2 * t * p1[0] + 3 * u * t**2 * p2[0] + t**3 * p3[0]
        y = u**3 * p0[1] + 3 * u**2 * t * p1[1] + 3 * u * t**2 * p2[1] + t**3 * p3[1]
        pts.append((x, y))
    return pts


def draw_bezier_wire(
    dr: ImageDraw.ImageDraw,
    p0: tuple[float, float],
    p1: tuple[float, float],
    p2: tuple[float, float],
    p3: tuple[float, float],
    fill: tuple[int, int, int],
    width: int,
) -> None:
    w = max(S(width), 2)
    pts = cubic_bezier(p0, p1, p2, p3)
    sh = (max(0, fill[0] - 45), max(0, fill[1] - 45), max(0, fill[2] - 45))
    for i in range(len(pts) - 1):
        dr.line((pts[i][0], pts[i][1], pts[i + 1][0], pts[i + 1][1]), fill=sh, width=w + S(3))
    for i in range(len(pts) - 1):
        dr.line((pts[i][0], pts[i][1], pts[i + 1][0], pts[i + 1][1]), fill=fill, width=w)
    # reflexo fino no fio
    hi = tuple(min(255, c + 55) for c in fill)
    for i in range(0, len(pts) - 1, 3):
        dr.line((pts[i][0], pts[i][1] - S(1), pts[i + 1][0], pts[i + 1][1] - S(1)), fill=hi, width=max(S(1), w // 5))


def soft_shadow(dr: ImageDraw.ImageDraw, xy: tuple[int, int, int, int], radius: int) -> None:
    x0, y0, x1, y1 = xy
    o = S(6)
    for k, alpha in [(S(10), 28), (S(6), 40), (S(3), 55)]:
        dr.rounded_rectangle(
            (x0 + o + k, y0 + o + k, x1 + o + k, y1 + o + k),
            radius=radius,
            fill=(alpha, alpha + 2, alpha + 6),
        )


def draw_power_bank(dr: ImageDraw.ImageDraw, cx: int, cy: int, font: ImageFont.ImageFont, fsm: ImageFont.ImageFont) -> tuple[int, int]:
    """Cápsula horizontal + micro-USB; retorna ponta do plug para cabo."""
    w, h = S(240), S(88)
    x0, y0 = cx - w // 2, cy - h // 2
    x1, y1 = x0 + w, y0 + h
    r = h // 2
    soft_shadow(dr, (x0, y0, x1, y1), r)
    dr.rounded_rectangle((x0, y0, x1, y1), radius=r, fill=(46, 52, 62), outline=PCB_EDGE, width=S(2))
    # reflexo superior
    dr.rounded_rectangle((x0 + S(6), y0 + S(6), x1 - S(6), y0 + h // 3), radius=r - S(6), fill=(68, 76, 90))
    dr.rounded_rectangle((x0 + S(18), y0 + S(36), x1 - S(18), y0 + S(48)), radius=S(5), fill=(32, 95, 165))
    dr.text((x0 + S(28), y0 + S(22)), "Power bank 5 V", fill=TEXT, font=font)
    dr.text((x0 + S(28), y0 + S(58)), "USB", fill=MUTED, font=fsm)
    # micro-USB metal
    pw, ph = S(42), S(32)
    px = (x0 + x1) // 2 - pw // 2
    py = y1
    dr.rounded_rectangle((px, py, px + pw, py + ph), radius=S(5), fill=(40, 42, 48), outline=SILVER, width=S(1))
    dr.rectangle((px + S(10), py + S(5), px + pw - S(10), py + ph - S(5)), fill=USB_METAL)
    return (px + pw // 2, py + ph)


def draw_esp32_devkit(
    dr: ImageDraw.ImageDraw,
    cx: int,
    cy: int,
    font: ImageFont.ImageFont,
    fsm: ImageFont.ImageFont,
) -> dict[str, tuple[int, int]]:
    bw, bh = S(308), S(432)
    x0, y0 = cx - bw // 2, cy - bh // 2
    x1, y1 = x0 + bw, y0 + bh
    ch = S(22)
    outline = [
        (x0 + ch, y0),
        (x1 - ch, y0),
        (x1, y0 + ch),
        (x1, y1 - ch),
        (x1 - ch, y1),
        (x0 + ch, y1),
        (x0, y1 - ch),
        (x0, y0 + ch),
    ]
    soft_shadow(dr, (x0, y0, x1, y1), S(18))
    dr.polygon(outline, fill=PCB, outline=ACCENT, width=S(2))
    # bisel superior PCB
    dr.polygon(
        [(x0 + ch, y0 + S(3)), (x1 - ch, y0 + S(3)), (x1 - S(4), y0 + S(28)), (x0 + S(4), y0 + S(28))],
        fill=PCB_HI,
    )
    # shield Wi-Fi / chip
    sx0, sy0 = x0 + S(78), y0 + S(48)
    sx1, sy1 = x1 - S(78), y0 + S(168)
    dr.rounded_rectangle((sx0, sy0, sx1, sy1), radius=S(8), fill=(42, 46, 54), outline=(65, 72, 85), width=S(1))
    dr.rounded_rectangle((sx0 + S(6), sy0 + S(6), sx1 - S(6), sy1 - S(6)), radius=S(5), fill=(28, 30, 36))
    dr.text((sx0 + S(58), sy0 + S(48)), "ESP32", fill=TEXT, font=font)
    # antena PCB (trilhas cobre)
    ax0 = x0 + S(36)
    for i in range(6):
        dr.rounded_rectangle((ax0 + i * S(13), sy1 + S(8), ax0 + i * S(13) + S(8), sy1 + S(62)), radius=S(2), fill=COPPER)
    # USB inferior
    uw, uh = S(56), S(40)
    ux = (x0 + x1) // 2 - uw // 2
    uy = y1 - uh - S(10)
    dr.rounded_rectangle((ux, uy, ux + uw, uy + uh), radius=S(6), fill=(26, 26, 30), outline=SILVER, width=S(2))
    dr.rectangle((ux + S(12), uy + S(8), ux + uw - S(12), uy + uh - S(8)), fill=(18, 18, 22))
    dr.text((ux + S(6), uy + uh + S(4)), "USB", fill=MUTED, font=fsm)
    # pinos duas colunas
    pin_y0 = y0 + S(208)
    step = S(23)
    for i in range(7):
        py = pin_y0 + i * step
        dr.ellipse((x0 + S(10), py, x0 + S(24), py + S(14)), fill=COPPER, outline=(110, 75, 40), width=S(1))
        dr.ellipse((x1 - S(24), py, x1 - S(10), py + S(14)), fill=COPPER, outline=(110, 75, 40), width=S(1))
    labs_l = [("GPIO 4", 0), ("3,3 V", 1), ("GND", 2)]
    for lab, idx in labs_l:
        dr.text((x0 + S(30), pin_y0 + idx * step - S(2)), lab, fill=TEXT, font=fsm)
    for lab, idx in [("GPIO 34", 4), ("GND", 5)]:
        dr.text((x1 - S(102), pin_y0 + idx * step - S(2)), lab, fill=TEXT, font=fsm)
    return {
        "usb_top": (ux + uw // 2, uy),
        "L0": (x0 + S(17), pin_y0 + S(7)),
        "L1": (x0 + S(17), pin_y0 + step + S(7)),
        "L2": (x0 + S(17), pin_y0 + 2 * step + S(7)),
        "R4": (x1 - S(17), pin_y0 + 4 * step + S(7)),
        "R5": (x1 - S(17), pin_y0 + 5 * step + S(7)),
    }


def draw_dht11_pill(
    dr: ImageDraw.ImageDraw,
    cx: int,
    cy: int,
    font: ImageFont.ImageFont,
    fsm: ImageFont.ImageFont,
) -> dict[str, tuple[int, int]]:
    w, h = S(104), S(138)
    x0, y0 = cx - w // 2, cy - h // 2
    x1, y1 = x0 + w, y0 + h
    r = w // 2 - S(2)
    soft_shadow(dr, (x0, y0, x1, y1), r)
    dr.rounded_rectangle((x0, y0, x1, y1), radius=r, fill=DHT_BODY, outline=(18, 75, 115), width=S(2))
    dr.rounded_rectangle((x0 + S(5), y0 + S(5), x1 - S(5), y0 + h // 2 - S(8)), radius=r - S(5), fill=DHT_HI)
    gx, gy = x0 + S(20), y0 + S(28)
    for row in range(4):
        for col in range(3):
            dr.rounded_rectangle(
                (gx + col * S(24), gy + row * S(17), gx + col * S(24) + S(15), gy + row * S(17) + S(11)),
                radius=S(2),
                fill=(14, 52, 82),
            )
    dr.text((x0 + S(22), y0 + S(98)), "DHT11", fill=TEXT, font=font)
    pr = S(7)
    px0 = x1 + S(6)
    yv, yd, yg = y0 + S(30), y0 + S(56), y0 + S(82)
    for py in (yv, yd, yg):
        dr.rectangle((px0, py - pr, px0 + S(32), py + pr), fill=COPPER, outline=(95, 65, 35), width=S(1))
    dr.text((px0 + S(40), yv - S(8)), "VCC", fill=MUTED, font=fsm)
    dr.text((px0 + S(40), yd - S(8)), "DATA", fill=MUTED, font=fsm)
    dr.text((px0 + S(40), yg - S(8)), "GND", fill=MUTED, font=fsm)
    return {"vcc": (px0 + S(32), yv), "data": (px0 + S(32), yd), "gnd": (px0 + S(32), yg)}


def draw_zmpt_board(
    dr: ImageDraw.ImageDraw,
    cx: int,
    cy: int,
    font: ImageFont.ImageFont,
    fsm: ImageFont.ImageFont,
) -> dict[str, tuple[int, int]]:
    bw, bh = S(352), S(372)
    x0, y0 = cx - bw // 2, cy - bh // 2
    x1, y1 = x0 + bw, y0 + bh
    ch = S(16)
    poly = [
        (x0 + ch, y0),
        (x1 - ch, y0),
        (x1, y0 + ch),
        (x1, y1 - ch),
        (x1 - ch, y1),
        (x0 + ch, y1),
        (x0, y1 - ch),
        (x0, y0 + ch),
    ]
    soft_shadow(dr, (x0, y0, x1, y1), S(18))
    dr.polygon(poly, fill=ZMPT_PCB, outline=ACCENT, width=S(2))
    dr.text((x0 + S(72), y0 + S(12)), "ZMPT101B", fill=TEXT, font=font)
    tcx, tcy = x0 + S(125), y0 + S(135)
    rx, ry = S(82), S(56)
    for k, col, lw in [(0, TOROID_DARK, S(14)), (S(14), TOROID_MID, S(10)), (S(26), TOROID_HI, S(5))]:
        dr.ellipse((tcx - rx + k, tcy - ry + k, tcx + rx - k, tcy + ry - k), outline=col, width=lw)
    dr.ellipse((tcx - S(30), tcy - S(22), tcx + S(30), tcy + S(22)), fill=ZMPT_PCB, outline=TOROID_MID, width=S(2))
    dr.text((tcx - S(12), tcy - S(9)), "AC", fill=WARN, font=fsm)
    lx = x0 + S(28)
    ly_out = y0 + S(238)
    ly_g = y0 + S(278)
    ly_v = y0 + S(318)
    for ly, lab in ((ly_out, "OUT"), (ly_g, "GND"), (ly_v, "VCC")):
        dr.rounded_rectangle((lx - S(6), ly - S(8), lx + S(6), ly + S(8)), radius=S(3), fill=COPPER, outline=(100, 68, 38))
        dr.text((lx + S(16), ly - S(10)), lab, fill=TEXT, font=fsm)
    bx = x1 - S(56)
    yL, yN = y0 + S(212), y0 + S(272)
    for ly, lab in ((yL, "L"), (yN, "N")):
        dr.rounded_rectangle((bx - S(10), ly - S(20), bx + S(32), ly + S(20)), radius=S(5), fill=BRASS, outline=(130, 95, 28), width=S(2))
        dr.line((bx + S(4), ly - S(10), bx + S(20), ly + S(10)), fill=(70, 52, 18), width=S(2))
        dr.text((bx + S(42), ly - S(10)), lab, fill=TEXT, font=fsm)
    return {"out": (lx, ly_out), "gnd": (lx, ly_g), "vcc": (lx, ly_v), "L": (bx + S(32), yL), "N": (bx + S(32), yN)}


def draw_ac_panel(dr: ImageDraw.ImageDraw, cx: int, cy: int, font: ImageFont.ImageFont, fsm: ImageFont.ImageFont) -> tuple[tuple[int, int], tuple[int, int]]:
    w, h = S(210), S(138)
    x0, y0 = cx - w // 2, cy - h // 2
    x1, y1 = x0 + w, y0 + h
    soft_shadow(dr, (x0, y0, x1, y1), S(16))
    dr.rounded_rectangle((x0, y0, x1, y1), radius=S(16), fill=(48, 36, 24), outline=WARN, width=S(2))
    dr.text((x0 + S(16), y0 + S(12)), "Rede AC", fill=WARN, font=font)
    dr.text((x0 + S(16), y0 + S(44)), "~110 / ~127 / ~220 V", fill=TEXT, font=fsm)
    dr.text((x0 + S(16), y0 + S(68)), "monofásico · só no módulo", fill=MUTED, font=fsm)
    qx, qy = x0 + w // 2, y1 - S(34)
    dr.rounded_rectangle((qx - S(38), qy - S(24), qx + S(38), qy + S(24)), radius=S(8), fill=(32, 32, 36), outline=SILVER, width=S(2))
    dr.ellipse((qx - S(20), qy - S(9), qx - S(6), qy + S(9)), outline=SILVER, width=S(2))
    dr.ellipse((qx + S(6), qy - S(9), qx + S(20), qy + S(9)), outline=SILVER, width=S(2))
    lug_l = (qx - S(14), y1 + S(2))
    lug_n = (qx + S(14), y1 + S(2))
    for p, col in ((lug_l, AC_L), (lug_n, AC_N)):
        dr.ellipse((p[0] - S(8), p[1] - S(8), p[0] + S(8), p[1] + S(8)), fill=col, outline=TEXT, width=S(1))
    return lug_l, lug_n


def apply_vignette_and_gloss(rgb: Image.Image) -> Image.Image:
    w, h = rgb.size
    gloss = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    gd = ImageDraw.Draw(gloss)
    for x in range(0, w, S(4)):
        a = int(12 + 18 * (1 - x / w))
        gd.line((x, 0, x, S(120)), fill=(255, 255, 255, a))
    comp = Image.alpha_composite(rgb.convert("RGBA"), gloss)
    # vinheta escura nas bordas
    vig = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    vd = ImageDraw.Draw(vig)
    for i in range(0, S(56), S(3)):
        a = min(90, i // max(S(1), 1))
        vd.rectangle((i, i, w - i, h - i), outline=(0, 0, 0, a))
    comp = Image.alpha_composite(comp, vig)
    return comp.convert("RGB")


def main() -> None:
    iw, ih = LOG_W * SCALE, LOG_H * SCALE
    img = Image.new("RGB", (iw, ih), BG_TOP)
    fill_vertical_gradient(img, BG_TOP, BG_BOT)
    dr = ImageDraw.Draw(img)

    f_title = load_font(22)
    f_sub = load_font(12)
    f_lg = load_font(15)
    f_md = load_font(13)
    f_sm = load_font(11)

    dr.text((S(40), S(26)), "HOME_SENSE", fill=ACCENT, font=f_title)
    dr.text((S(40), S(58)), "Diagrama de montagem (referência)", fill=TEXT, font=f_md)
    dr.text((S(40), S(84)), "Ilustração esquemática — confira sempre o datasheet do seu módulo e placa.", fill=MUTED, font=f_sub)

    pb_tip = draw_power_bank(dr, S(800), S(122), f_md, f_sm)
    esp = draw_esp32_devkit(dr, S(798), S(498), f_lg, f_sm)
    dht = draw_dht11_pill(dr, S(252), S(498), f_lg, f_sm)
    zm = draw_zmpt_board(dr, S(1310), S(502), f_lg, f_sm)
    lug_l, lug_n = draw_ac_panel(dr, S(1305), S(118), f_lg, f_sm)

    ut = esp["usb_top"]
    draw_bezier_wire(
        dr,
        (float(pb_tip[0]), float(pb_tip[1])),
        (float(pb_tip[0]), float(pb_tip[1] + S(70))),
        (float(ut[0]), float(ut[1] - S(90))),
        (float(ut[0]), float(ut[1])),
        (115, 120, 128),
        5,
    )
    dr.text((S(830), S(300)), "alimentação ESP32", fill=MUTED, font=f_sm)

    draw_bezier_wire(
        dr,
        (float(dht["vcc"][0]), float(dht["vcc"][1])),
        (float(esp["L1"][0] - S(90)), float(dht["vcc"][1])),
        (float(esp["L1"][0] - S(40)), float(esp["L1"][1])),
        (float(esp["L1"][0]), float(esp["L1"][1])),
        WIRE_RED,
        4,
    )
    draw_bezier_wire(
        dr,
        (float(dht["data"][0]), float(dht["data"][1])),
        (float(esp["L0"][0] - S(100)), float(dht["data"][1])),
        (float(esp["L0"][0] - S(45)), float(esp["L0"][1])),
        (float(esp["L0"][0]), float(esp["L0"][1])),
        WIRE_ORG,
        4,
    )
    draw_bezier_wire(
        dr,
        (float(dht["gnd"][0]), float(dht["gnd"][1])),
        (float(esp["L2"][0] - S(95)), float(dht["gnd"][1])),
        (float(esp["L2"][0] - S(42)), float(esp["L2"][1])),
        (float(esp["L2"][0]), float(esp["L2"][1])),
        WIRE_BLK,
        4,
    )

    draw_bezier_wire(
        dr,
        (float(zm["out"][0]), float(zm["out"][1])),
        (float(zm["out"][0] + S(130)), float(zm["out"][1] - S(24))),
        (float(esp["R4"][0] + S(110)), float(esp["R4"][1])),
        (float(esp["R4"][0]), float(esp["R4"][1])),
        WIRE_YEL,
        4,
    )
    draw_bezier_wire(
        dr,
        (float(zm["gnd"][0]), float(zm["gnd"][1])),
        (float(zm["gnd"][0] + S(110)), float(zm["gnd"][1] + S(28))),
        (float(esp["R5"][0] + S(100)), float(esp["R5"][1])),
        (float(esp["R5"][0]), float(esp["R5"][1])),
        WIRE_BLK,
        4,
    )

    draw_bezier_wire(
        dr,
        (float(S(910)), float(S(188))),
        (float(S(1040)), float(S(235))),
        (float(zm["vcc"][0] - S(50)), float(zm["vcc"][1] - S(70))),
        (float(zm["vcc"][0]), float(zm["vcc"][1])),
        WIRE_RED,
        3,
    )
    dr.text((S(960), S(248)), "5 V — VCC do módulo (datasheet)", fill=WARN, font=f_sm)

    draw_bezier_wire(
        dr,
        (float(lug_l[0]), float(lug_l[1])),
        (float(lug_l[0]), float(lug_l[1] + S(110))),
        (float(zm["L"][0] - S(50)), float(zm["L"][1] - S(90))),
        (float(zm["L"][0]), float(zm["L"][1])),
        AC_L,
        9,
    )
    draw_bezier_wire(
        dr,
        (float(lug_n[0]), float(lug_n[1])),
        (float(lug_n[0]), float(lug_n[1] + S(110))),
        (float(zm["N"][0] - S(50)), float(zm["N"][1] - S(90))),
        (float(zm["N"][0]), float(zm["N"][1])),
        AC_N,
        9,
    )
    dr.text((S(1160), S(288)), "L fase", fill=AC_L, font=f_sm)
    dr.text((S(1260), S(288)), "N neutro", fill=AC_N, font=f_sm)

    fy = ih - S(54)
    dr.rounded_rectangle((S(28), fy, iw - S(28), ih - S(16)), radius=S(12), fill=(42, 32, 16), outline=WARN, width=S(1))
    dr.text(
        (S(44), fy + S(12)),
        "Tensão de rede: L e N apenas nos bornes AC do módulo. Nunca leve AC ao ESP32.",
        fill=WARN,
        font=f_sm,
    )

    img = apply_vignette_and_gloss(img)
    out_img = img.resize((OUT_W, OUT_H), Image.Resampling.LANCZOS)
    OUT.parent.mkdir(parents=True, exist_ok=True)
    out_img.save(OUT, "PNG", optimize=True)
    print(f"Escrito: {OUT} ({OUT_W}x{OUT_H}, scale={SCALE}x)")


if __name__ == "__main__":
    main()
