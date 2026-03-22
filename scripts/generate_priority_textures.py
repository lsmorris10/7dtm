#!/usr/bin/env python3
from PIL import Image, ImageDraw
import os

REPO_ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
BASE = "src/main/resources/assets/sevendaystominecraft/textures"
ITEM = os.path.join(BASE, "item")
GUI = os.path.join(BASE, "gui")
BLOCK = os.path.join(BASE, "block")

def px(img, x, y, color):
    if 0 <= x < img.width and 0 <= y < img.height:
        img.putpixel((x, y), color)

def fill_rect(img, x1, y1, x2, y2, color):
    for x in range(max(0,x1), min(img.width,x2+1)):
        for y in range(max(0,y1), min(img.height,y2+1)):
            px(img, x, y, color)

def shade(c, amount):
    return tuple(max(0, min(255, c[i]+amount)) for i in range(3)) + (c[3] if len(c)>3 else 255,)

def outline_rect(img, x1, y1, x2, y2, color):
    for x in range(x1, x2+1):
        px(img, x, y1, color); px(img, x, y2, color)
    for y in range(y1, y2+1):
        px(img, x1, y, color); px(img, x2, y, color)

def add_noise(img, region=None, intensity=15):
    import random
    random.seed(42)
    x1,y1,x2,y2 = region if region else (0,0,img.width-1,img.height-1)
    for x in range(x1, x2+1):
        for y in range(y1, y2+1):
            if 0<=x<img.width and 0<=y<img.height:
                r,g,b,a = img.getpixel((x,y))
                if a > 0:
                    n = random.randint(-intensity, intensity)
                    img.putpixel((x,y), (max(0,min(255,r+n)), max(0,min(255,g+n)), max(0,min(255,b+n)), a))

def save(img, path):
    full = os.path.join(REPO_ROOT, path)
    img.save(full)
    sz = os.path.getsize(full)
    status = "OK" if sz >= 200 else "SMALL"
    print(f"  [{status}] {path} ({sz} bytes)")

# ============================================================
# MELEE WEAPONS (16x16)
# ============================================================
def make_melee():
    print("=== MELEE WEAPONS ===")

    # Baseball Bat
    img = Image.new("RGBA", (16,16), (0,0,0,0))
    brown = (139,90,43,255); dark = (101,67,33,255); light = (178,123,60,255); outline = (70,45,20,255)
    for i in range(13):
        x,y = 2+i, 13-i
        if 0<=x<16 and 0<=y<16:
            px(img, x, y, brown)
            if x+1<16: px(img, x+1, y, light)
            if y+1<16: px(img, x, y+1, outline)
    for i in range(4):
        px(img, 2+i, 13-i, dark)
        px(img, 3+i, 13-i, dark)
    for i in range(5):
        x = 11+i if 11+i<16 else 15
        y = 4-i if 4-i>=0 else 0
        px(img, x, y, light)
        if x-1>=0: px(img, x-1, y, brown)
    fill_rect(img, 12, 0, 15, 3, light)
    fill_rect(img, 13, 1, 14, 2, brown)
    outline_rect(img, 12, 0, 15, 3, outline)
    add_noise(img, intensity=10)
    save(img, f"{ITEM}/baseball_bat.png")

    # Fists
    img = Image.new("RGBA", (16,16), (0,0,0,0))
    skin = (228,185,140,255); dark = (198,155,110,255); shadow = (170,130,90,255); out = (120,80,50,255)
    fill_rect(img, 3, 4, 12, 12, skin)
    fill_rect(img, 4, 5, 11, 11, dark)
    fill_rect(img, 5, 3, 10, 4, skin)
    fill_rect(img, 6, 6, 9, 8, shadow)
    for x in range(3,13): px(img, x, 13, out)
    for y in range(4,13): px(img, 2, y, out); px(img, 13, y, out)
    px(img, 5, 3, out); px(img, 10, 3, out)
    px(img, 6, 6, (240,200,160,255)); px(img, 7, 5, (240,200,160,255))
    add_noise(img, intensity=8)
    save(img, f"{ITEM}/fists.png")

    # Hunting Knife
    img = Image.new("RGBA", (16,16), (0,0,0,0))
    blade = (200,200,210,255); edge = (170,170,180,255); shadow = (140,140,150,255)
    handle = (101,67,33,255); hd = (70,45,20,255); guard = (80,80,80,255)
    for i in range(9):
        x,y = 6+i, 9-i
        if 0<=x<16 and 0<=y<16:
            px(img, x, y, blade)
            if x+1<16: px(img, x+1, y, edge)
            if y+1<16 and x-1>=0: px(img, x-1, y+1, shadow)
    for i in range(5):
        x,y = 2+i, 13-i
        px(img, x, y, handle); px(img, x+1, y, hd)
    px(img, 6, 9, guard); px(img, 5, 10, guard); px(img, 7, 8, guard)
    px(img, 6, 10, guard)
    add_noise(img, intensity=12)
    save(img, f"{ITEM}/hunting_knife.png")

    # Iron Spear
    img = Image.new("RGBA", (16,16), (0,0,0,0))
    shaft = (139,119,101,255); sd = (110,90,72,255); tip = (180,180,190,255); te = (150,150,160,255)
    for i in range(14):
        x,y = 1+i, 14-i
        if 0<=x<16 and 0<=y<16:
            px(img, x, y, shaft)
            if x+1<16: px(img, x+1, y, sd)
    for i in range(3):
        x,y = 13+i if 13+i<16 else 15, 2-i if 2-i>=0 else 0
        px(img, x, y, tip)
        if x-1>=0: px(img, x-1, y, te)
    px(img, 14, 0, tip); px(img, 15, 0, (210,210,220,255))
    fill_rect(img, 1, 13, 3, 15, (120,100,80,255))
    add_noise(img, intensity=10)
    save(img, f"{ITEM}/iron_spear.png")

    # Machete
    img = Image.new("RGBA", (16,16), (0,0,0,0))
    blade = (190,195,200,255); edge = (210,215,220,255); shadow = (150,155,160,255)
    handle = (60,40,20,255); hd = (40,25,10,255)
    for i in range(11):
        x,y = 4+i, 11-i
        if 0<=x<16 and 0<=y<16:
            px(img, x, y, blade)
            if x+1<16: px(img, x+1, y, edge)
            if y+1<16: px(img, x, y+1, shadow)
    for i in range(4):
        px(img, 1+i, 14-i, handle); px(img, 2+i, 14-i, hd)
    px(img, 4, 11, (100,100,110,255))
    add_noise(img, intensity=10)
    save(img, f"{ITEM}/machete.png")

    # Nailgun
    img = Image.new("RGBA", (16,16), (0,0,0,0))
    yellow = (220,200,50,255); dy = (190,170,30,255); black = (40,40,40,255)
    gray = (130,130,140,255); dg = (90,90,100,255)
    fill_rect(img, 2, 4, 12, 9, yellow)
    fill_rect(img, 3, 5, 11, 8, dy)
    fill_rect(img, 12, 5, 15, 8, gray)
    fill_rect(img, 13, 6, 14, 7, dg)
    fill_rect(img, 4, 10, 8, 14, black)
    fill_rect(img, 5, 11, 7, 13, (55,55,55,255))
    fill_rect(img, 2, 2, 5, 4, gray)
    fill_rect(img, 3, 3, 4, 3, dg)
    outline_rect(img, 2, 4, 12, 9, (30,30,30,255))
    add_noise(img, intensity=10)
    save(img, f"{ITEM}/nailgun.png")

    # Sledgehammer
    img = Image.new("RGBA", (16,16), (0,0,0,0))
    shaft = (139,90,43,255); sd = (110,70,30,255)
    head = (110,110,120,255); hd = (80,80,90,255); hl = (140,140,150,255)
    for i in range(11):
        x,y = 2+i, 14-i
        if 0<=x<16 and 0<=y<16:
            px(img, x, y, shaft)
            if x+1<16: px(img, x+1, y, sd)
    fill_rect(img, 10, 0, 15, 6, head)
    fill_rect(img, 11, 1, 14, 5, hd)
    fill_rect(img, 12, 1, 13, 2, hl)
    outline_rect(img, 10, 0, 15, 6, (60,60,70,255))
    add_noise(img, intensity=10)
    save(img, f"{ITEM}/sledgehammer.png")

    # Steel Knuckles
    img = Image.new("RGBA", (16,16), (0,0,0,0))
    steel = (180,180,190,255); ds = (140,140,150,255); skin = (228,185,140,255); dskin = (198,155,110,255)
    fill_rect(img, 2, 6, 13, 11, skin)
    fill_rect(img, 3, 7, 12, 10, dskin)
    for i in range(4):
        fill_rect(img, 3+i*3, 3, 4+i*3, 6, steel)
        px(img, 3+i*3, 4, ds); px(img, 4+i*3, 5, ds)
    fill_rect(img, 2, 5, 13, 6, ds)
    outline_rect(img, 2, 3, 13, 11, (100,100,110,255))
    add_noise(img, intensity=8)
    save(img, f"{ITEM}/steel_knuckles.png")

    # Stone Axe
    img = Image.new("RGBA", (16,16), (0,0,0,0))
    wood = (139,90,43,255); wd = (110,70,30,255)
    stone = (140,140,140,255); sd = (110,110,110,255); sl = (170,170,170,255)
    for i in range(12):
        x,y = 2+i, 14-i
        if 0<=x<16 and 0<=y<16:
            px(img, x, y, wood)
            if x+1<16: px(img, x+1, y, wd)
    fill_rect(img, 10, 0, 15, 5, stone)
    fill_rect(img, 11, 1, 14, 4, sd)
    px(img, 12, 1, sl); px(img, 11, 2, sl)
    px(img, 9, 3, stone); px(img, 9, 4, stone)
    outline_rect(img, 10, 0, 15, 5, (80,80,80,255))
    fill_rect(img, 2, 13, 4, 15, (120,100,80,255))
    add_noise(img, intensity=12)
    save(img, f"{ITEM}/stone_axe.png")

    # Stun Baton
    img = Image.new("RGBA", (16,16), (0,0,0,0))
    shaft = (70,70,80,255); sd = (50,50,60,255); grip = (40,40,45,255)
    elec = (80,180,255,255); bright = (160,220,255,255)
    for i in range(11):
        x,y = 3+i, 13-i
        if 0<=x<16 and 0<=y<16:
            px(img, x, y, shaft)
            if x+1<16: px(img, x+1, y, sd)
    for i in range(4):
        px(img, 3+i, 13-i, grip); px(img, 4+i, 13-i, grip)
    px(img, 12, 3, elec); px(img, 13, 2, bright); px(img, 14, 1, elec)
    px(img, 11, 4, elec); px(img, 13, 3, bright)
    px(img, 14, 2, (120,200,255,255))
    fill_rect(img, 12, 1, 14, 3, elec)
    px(img, 13, 2, bright); px(img, 13, 1, bright)
    add_noise(img, intensity=10)
    save(img, f"{ITEM}/stun_baton.png")

    # Wooden Club
    img = Image.new("RGBA", (16,16), (0,0,0,0))
    wood = (150,105,55,255); dark = (120,80,40,255); light = (175,130,70,255); out = (80,50,25,255)
    for i in range(13):
        x,y = 2+i, 14-i
        if 0<=x<16 and 0<=y<16:
            px(img, x, y, wood)
            if x+1<16: px(img, x+1, y, dark)
            if x-1>=0 and y-1>=0: px(img, x-1, y-1, light)
    fill_rect(img, 10, 0, 15, 5, dark)
    fill_rect(img, 11, 1, 14, 4, wood)
    fill_rect(img, 12, 1, 13, 2, light)
    outline_rect(img, 10, 0, 15, 5, out)
    add_noise(img, intensity=12)
    save(img, f"{ITEM}/wooden_club.png")

    # Wrench
    img = Image.new("RGBA", (16,16), (0,0,0,0))
    metal = (180,180,190,255); dark = (130,130,140,255); handle = (110,110,120,255)
    light = (210,210,220,255); out = (70,70,80,255)
    for i in range(9):
        x,y = 4+i, 12-i
        if 0<=x<16 and 0<=y<16:
            px(img, x, y, handle)
            if x+1<16: px(img, x+1, y, dark)
    fill_rect(img, 11, 0, 15, 4, metal)
    fill_rect(img, 12, 1, 14, 3, dark)
    px(img, 13, 2, (0,0,0,0))
    px(img, 11, 1, light); px(img, 12, 0, light)
    outline_rect(img, 11, 0, 15, 4, out)
    px(img, 13, 2, (0,0,0,0))
    fill_rect(img, 4, 11, 6, 13, (90,90,100,255))
    add_noise(img, intensity=10)
    save(img, f"{ITEM}/wrench.png")

# ============================================================
# RANGED WEAPONS (16x16) - ak47 already has real texture
# ============================================================
def make_ranged():
    print("=== RANGED WEAPONS ===")

    # Blunderbuss
    img = Image.new("RGBA", (16,16), (0,0,0,0))
    wood = (120,75,35,255); wd = (90,55,25,255); barrel = (85,85,90,255)
    bd = (60,60,65,255); out = (40,30,15,255)
    fill_rect(img, 0, 5, 7, 10, wood)
    fill_rect(img, 1, 6, 6, 9, wd)
    fill_rect(img, 2, 7, 5, 8, (100,60,28,255))
    fill_rect(img, 7, 5, 14, 9, barrel)
    fill_rect(img, 8, 6, 13, 8, bd)
    fill_rect(img, 14, 4, 15, 10, barrel)
    px(img, 15, 4, (100,100,105,255)); px(img, 15, 10, (100,100,105,255))
    outline_rect(img, 0, 5, 15, 10, out)
    px(img, 14, 4, out); px(img, 15, 4, out); px(img, 14, 10, out); px(img, 15, 10, out)
    add_noise(img, intensity=12)
    save(img, f"{ITEM}/blunderbuss.png")

    # Compound Bow
    img = Image.new("RGBA", (16,16), (0,0,0,0))
    limb = (45,45,50,255); ld = (30,30,35,255); string = (210,210,210,255)
    cam = (110,110,120,255); grip = (60,60,65,255)
    for i in range(14):
        px(img, 4, 1+i, limb)
        px(img, 3, 1+i, ld)
    fill_rect(img, 2, 0, 5, 2, cam); fill_rect(img, 2, 13, 5, 15, cam)
    px(img, 3, 1, (140,140,150,255)); px(img, 3, 14, (140,140,150,255))
    for i in range(12):
        px(img, 8, 2+i, string)
    fill_rect(img, 4, 5, 7, 10, grip)
    fill_rect(img, 5, 6, 6, 9, (75,75,80,255))
    px(img, 9, 7, (180,100,50,255)); px(img, 10, 7, (180,100,50,255))
    px(img, 11, 7, (150,150,155,255))
    add_noise(img, intensity=8)
    save(img, f"{ITEM}/compound_bow.png")

    # Compound Crossbow
    img = Image.new("RGBA", (16,16), (0,0,0,0))
    body = (65,65,70,255); bd = (45,45,50,255); limb = (85,85,90,255)
    string = (210,210,210,255); grip = (100,70,40,255)
    fill_rect(img, 5, 3, 9, 14, body)
    fill_rect(img, 6, 4, 8, 13, bd)
    fill_rect(img, 1, 2, 13, 4, limb)
    px(img, 0, 2, limb); px(img, 14, 2, limb)
    outline_rect(img, 1, 2, 13, 4, (35,35,40,255))
    for x in range(2, 13):
        px(img, x, 5, string)
    fill_rect(img, 6, 12, 8, 15, grip)
    fill_rect(img, 7, 13, 7, 14, (120,85,50,255))
    px(img, 7, 3, (140,140,145,255))
    add_noise(img, intensity=10)
    save(img, f"{ITEM}/compound_crossbow.png")

    # Hunting Rifle
    img = Image.new("RGBA", (16,16), (0,0,0,0))
    wood = (120,80,40,255); wd = (90,60,28,255); barrel = (85,85,90,255)
    bd = (60,60,65,255); scope = (55,55,60,255); out = (40,30,15,255)
    fill_rect(img, 0, 6, 7, 10, wood)
    fill_rect(img, 1, 7, 6, 9, wd)
    fill_rect(img, 7, 6, 15, 8, barrel)
    fill_rect(img, 8, 7, 14, 7, bd)
    fill_rect(img, 8, 4, 13, 5, scope)
    fill_rect(img, 9, 4, 12, 4, (70,70,75,255))
    px(img, 1, 11, wood); px(img, 2, 11, wood); px(img, 3, 11, wd)
    outline_rect(img, 0, 6, 15, 10, out)
    outline_rect(img, 8, 4, 13, 5, (35,35,40,255))
    add_noise(img, intensity=10)
    save(img, f"{ITEM}/hunting_rifle.png")

    # M60
    img = Image.new("RGBA", (16,16), (0,0,0,0))
    body = (75,80,75,255); bd = (50,55,50,255); barrel = (65,65,70,255)
    dark = (40,45,40,255); belt = (180,160,50,255)
    fill_rect(img, 0, 5, 9, 10, body)
    fill_rect(img, 1, 6, 8, 9, bd)
    fill_rect(img, 9, 6, 15, 8, barrel)
    fill_rect(img, 10, 7, 14, 7, dark)
    fill_rect(img, 3, 11, 6, 14, body)
    fill_rect(img, 4, 12, 5, 13, dark)
    fill_rect(img, 8, 9, 11, 11, dark)
    fill_rect(img, 9, 9, 10, 10, belt)
    outline_rect(img, 0, 5, 9, 10, (30,35,30,255))
    px(img, 1, 5, (95,100,95,255)); px(img, 2, 5, (95,100,95,255))
    add_noise(img, intensity=10)
    save(img, f"{ITEM}/m60.png")

    # Pipe Pistol
    img = Image.new("RGBA", (16,16), (0,0,0,0))
    pipe = (120,110,100,255); pd = (90,82,75,255); rust = (150,95,55,255)
    tape = (80,80,80,255); out = (55,50,45,255)
    fill_rect(img, 4, 5, 13, 8, pipe)
    fill_rect(img, 5, 6, 12, 7, pd)
    fill_rect(img, 13, 6, 15, 7, pipe)
    fill_rect(img, 4, 9, 8, 14, pipe)
    fill_rect(img, 5, 10, 7, 13, pd)
    fill_rect(img, 9, 5, 10, 5, tape); fill_rect(img, 6, 8, 7, 8, tape)
    px(img, 11, 6, rust); px(img, 8, 7, rust); px(img, 13, 6, rust)
    outline_rect(img, 4, 5, 13, 8, out)
    outline_rect(img, 4, 8, 8, 14, out)
    add_noise(img, intensity=12)
    save(img, f"{ITEM}/pipe_pistol.png")

    # Pipe Rifle
    img = Image.new("RGBA", (16,16), (0,0,0,0))
    pipe = (120,110,100,255); pd = (90,82,75,255); rust = (150,95,55,255)
    tape = (80,80,80,255); out = (55,50,45,255)
    fill_rect(img, 1, 6, 14, 9, pipe)
    fill_rect(img, 2, 7, 13, 8, pd)
    fill_rect(img, 14, 7, 15, 8, pipe)
    fill_rect(img, 3, 10, 7, 13, pipe)
    fill_rect(img, 4, 11, 6, 12, pd)
    px(img, 8, 6, rust); px(img, 12, 8, rust); px(img, 5, 6, rust)
    fill_rect(img, 9, 6, 10, 6, tape)
    outline_rect(img, 1, 6, 14, 9, out)
    outline_rect(img, 3, 9, 7, 13, out)
    add_noise(img, intensity=12)
    save(img, f"{ITEM}/pipe_rifle.png")

    # Pipe Shotgun
    img = Image.new("RGBA", (16,16), (0,0,0,0))
    pipe = (115,105,95,255); pd = (85,78,70,255); rust = (145,90,50,255)
    wide = (105,100,90,255); out = (55,50,45,255)
    fill_rect(img, 1, 5, 14, 10, pipe)
    fill_rect(img, 2, 6, 13, 9, pd)
    fill_rect(img, 14, 5, 15, 10, wide)
    px(img, 15, 5, (130,125,115,255)); px(img, 15, 10, (130,125,115,255))
    fill_rect(img, 3, 11, 7, 14, pipe)
    fill_rect(img, 4, 12, 6, 13, pd)
    px(img, 9, 6, rust); px(img, 6, 9, rust); px(img, 12, 7, rust)
    outline_rect(img, 1, 5, 15, 10, out)
    outline_rect(img, 3, 10, 7, 14, out)
    add_noise(img, intensity=12)
    save(img, f"{ITEM}/pipe_shotgun.png")

    # Pistol
    img = Image.new("RGBA", (16,16), (0,0,0,0))
    body = (60,60,65,255); bd = (40,40,45,255); barrel = (75,75,80,255)
    grip = (45,40,35,255); gd = (55,50,45,255); out = (25,25,30,255)
    fill_rect(img, 3, 4, 13, 8, body)
    fill_rect(img, 4, 5, 12, 7, bd)
    fill_rect(img, 13, 5, 15, 7, barrel)
    fill_rect(img, 14, 6, 15, 6, (90,90,95,255))
    fill_rect(img, 4, 9, 8, 14, grip)
    fill_rect(img, 5, 10, 7, 13, gd)
    px(img, 3, 4, (90,90,95,255)); px(img, 4, 4, (85,85,90,255))
    fill_rect(img, 9, 5, 10, 5, (80,80,85,255))
    outline_rect(img, 3, 4, 13, 8, out)
    outline_rect(img, 4, 8, 8, 14, out)
    add_noise(img, intensity=8)
    save(img, f"{ITEM}/pistol.png")

    # Primitive Bow
    img = Image.new("RGBA", (16,16), (0,0,0,0))
    wood = (145,95,48,255); wd = (115,72,32,255); string = (190,180,160,255)
    bark = (130,85,38,255)
    for i in range(15):
        px(img, 4, i, wood)
        if i%3==0: px(img, 3, i, bark)
        px(img, 5, i, wd)
    px(img, 3, 0, wood); px(img, 3, 15, wood)
    for i in range(13):
        px(img, 8, 1+i, string)
    fill_rect(img, 4, 6, 7, 9, wd)
    fill_rect(img, 5, 7, 6, 8, (100,60,25,255))
    px(img, 9, 7, (160,140,100,255)); px(img, 10, 7, (140,140,145,255))
    add_noise(img, intensity=10)
    save(img, f"{ITEM}/primitive_bow.png")

    # Rocket Launcher
    img = Image.new("RGBA", (16,16), (0,0,0,0))
    tube = (80,85,75,255); td = (55,60,50,255); sight = (110,110,115,255)
    dark = (40,45,35,255); out = (25,30,20,255)
    fill_rect(img, 0, 4, 14, 11, tube)
    fill_rect(img, 1, 5, 13, 10, td)
    fill_rect(img, 2, 6, 12, 9, dark)
    fill_rect(img, 14, 5, 15, 10, tube)
    px(img, 0, 4, (100,105,95,255)); px(img, 0, 11, (100,105,95,255))
    fill_rect(img, 4, 12, 8, 14, tube)
    fill_rect(img, 5, 13, 7, 13, td)
    fill_rect(img, 6, 2, 9, 4, sight)
    fill_rect(img, 7, 3, 8, 3, (130,130,135,255))
    outline_rect(img, 0, 4, 15, 11, out)
    add_noise(img, intensity=10)
    save(img, f"{ITEM}/rocket_launcher.png")

    # Shotgun
    img = Image.new("RGBA", (16,16), (0,0,0,0))
    wood = (125,85,42,255); wd = (95,62,28,255); barrel = (75,75,80,255)
    bd = (55,55,60,255); pump = (95,95,100,255); out = (40,30,15,255)
    fill_rect(img, 0, 6, 7, 10, wood)
    fill_rect(img, 1, 7, 6, 9, wd)
    fill_rect(img, 2, 7, 4, 8, (105,68,32,255))
    fill_rect(img, 7, 6, 15, 9, barrel)
    fill_rect(img, 8, 7, 14, 8, bd)
    fill_rect(img, 8, 9, 12, 10, pump)
    fill_rect(img, 9, 9, 11, 9, (110,110,115,255))
    px(img, 1, 11, wood); px(img, 2, 11, wd)
    outline_rect(img, 0, 6, 15, 10, out)
    add_noise(img, intensity=10)
    save(img, f"{ITEM}/shotgun.png")

    # SMG
    img = Image.new("RGBA", (16,16), (0,0,0,0))
    body = (65,65,70,255); bd = (45,45,50,255); barrel = (80,80,85,255)
    mag = (55,55,60,255); grip = (75,70,60,255); out = (30,30,35,255)
    fill_rect(img, 2, 4, 12, 9, body)
    fill_rect(img, 3, 5, 11, 8, bd)
    fill_rect(img, 12, 5, 15, 8, barrel)
    fill_rect(img, 13, 6, 14, 7, (95,95,100,255))
    fill_rect(img, 5, 10, 9, 15, mag)
    fill_rect(img, 6, 11, 8, 14, (40,40,45,255))
    fill_rect(img, 3, 5, 5, 8, grip)
    px(img, 2, 4, (85,85,90,255)); px(img, 3, 4, (85,85,90,255))
    outline_rect(img, 2, 4, 12, 9, out)
    outline_rect(img, 5, 9, 9, 15, out)
    add_noise(img, intensity=8)
    save(img, f"{ITEM}/smg.png")

    # Wooden Bow
    img = Image.new("RGBA", (16,16), (0,0,0,0))
    wood = (165,115,58,255); wd = (130,88,42,255); string = (210,200,180,255)
    for i in range(15):
        px(img, 5, i, wood)
        px(img, 4, i, wd)
    px(img, 3, 0, wood); px(img, 3, 15, wood); px(img, 6, 0, wd); px(img, 6, 15, wd)
    for i in range(13):
        px(img, 9, 1+i, string)
    fill_rect(img, 5, 6, 7, 9, wd)
    fill_rect(img, 6, 7, 6, 8, (100,65,28,255))
    px(img, 10, 7, (160,130,80,255))
    add_noise(img, intensity=10)
    save(img, f"{ITEM}/wooden_bow.png")

    # Repair Hammer
    img = Image.new("RGBA", (16,16), (0,0,0,0))
    handle = (139,90,43,255); hd = (110,70,30,255)
    head = (150,150,160,255); dark = (110,110,120,255); light = (180,180,190,255)
    out = (70,70,80,255)
    for i in range(10):
        x,y = 3+i, 14-i
        if 0<=x<16 and 0<=y<16:
            px(img, x, y, handle)
            if x+1<16: px(img, x+1, y, hd)
    fill_rect(img, 10, 1, 15, 6, head)
    fill_rect(img, 11, 2, 14, 5, dark)
    px(img, 12, 2, light); px(img, 11, 3, light)
    outline_rect(img, 10, 1, 15, 6, out)
    px(img, 7, 4, (220,180,50,255))
    add_noise(img, intensity=10)
    save(img, f"{ITEM}/repair_hammer.png")

# ============================================================
# AMMO (16x16)
# ============================================================
def make_ammo():
    print("=== AMMO ===")

    def bullet(name, tip_color, casing_color):
        img = Image.new("RGBA", (16,16), (0,0,0,0))
        tc_dark = shade(tip_color, -30)
        cc_dark = shade(casing_color, -30)
        cc_light = shade(casing_color, 25)
        primer = (140,140,145,255)
        px(img, 7, 1, tip_color); px(img, 8, 1, tip_color)
        fill_rect(img, 6, 2, 9, 4, tip_color)
        fill_rect(img, 7, 2, 8, 3, tc_dark)
        fill_rect(img, 5, 5, 10, 12, casing_color)
        fill_rect(img, 6, 6, 9, 11, cc_dark)
        fill_rect(img, 6, 5, 7, 6, cc_light)
        fill_rect(img, 5, 13, 10, 13, primer)
        px(img, 7, 13, shade(primer, -20)); px(img, 8, 13, shade(primer, -20))
        outline_rect(img, 5, 1, 10, 13, (50,45,30,255))
        px(img, 7, 0, tip_color); px(img, 8, 0, tip_color)
        add_noise(img, intensity=8)
        save(img, f"{ITEM}/{name}")

    bullet("44_magnum_round.png", (185,165,55,255), (205,175,55,255))
    bullet("762mm_round.png", (175,155,45,255), (195,170,60,255))
    bullet("9mm_round.png", (190,170,60,255), (200,175,65,255))

    # AP Ammo - black tip armor piercing
    img = Image.new("RGBA", (16,16), (0,0,0,0))
    black = (35,35,40,255); casing = (195,170,60,255); cd = (165,140,40,255)
    px(img, 7, 0, black); px(img, 8, 0, black)
    fill_rect(img, 6, 1, 9, 4, black)
    fill_rect(img, 7, 2, 8, 3, (55,55,60,255))
    fill_rect(img, 5, 5, 10, 12, casing)
    fill_rect(img, 6, 6, 9, 11, cd)
    fill_rect(img, 5, 13, 10, 13, (140,140,145,255))
    outline_rect(img, 5, 0, 10, 13, (30,25,15,255))
    add_noise(img, intensity=8)
    save(img, f"{ITEM}/ap_ammo.png")

    # HP Ammo - red hollow point
    img = Image.new("RGBA", (16,16), (0,0,0,0))
    red = (190,45,45,255); casing = (195,170,60,255); cd = (165,140,40,255)
    px(img, 7, 0, red); px(img, 8, 0, red)
    fill_rect(img, 6, 1, 9, 4, red)
    fill_rect(img, 7, 1, 8, 2, (220,70,70,255))
    fill_rect(img, 5, 5, 10, 12, casing)
    fill_rect(img, 6, 6, 9, 11, cd)
    fill_rect(img, 5, 13, 10, 13, (140,140,145,255))
    outline_rect(img, 5, 0, 10, 13, (30,25,15,255))
    add_noise(img, intensity=8)
    save(img, f"{ITEM}/hp_ammo.png")

    # Arrow
    img = Image.new("RGBA", (16,16), (0,0,0,0))
    shaft = (165,135,85,255); sd = (135,108,65,255)
    tip = (150,150,155,255); tl = (180,180,185,255)
    feather = (210,210,210,255); fd = (180,180,180,255)
    for i in range(11):
        px(img, 7, 3+i, shaft); px(img, 8, 3+i, sd)
    px(img, 7, 1, tip); px(img, 8, 1, tip); px(img, 7, 2, tip); px(img, 8, 2, tip)
    px(img, 7, 0, tl); px(img, 8, 0, tl)
    px(img, 6, 1, tip); px(img, 9, 1, tip)
    px(img, 5, 13, feather); px(img, 6, 13, feather); px(img, 9, 13, feather); px(img, 10, 13, feather)
    px(img, 4, 14, fd); px(img, 5, 14, fd); px(img, 10, 14, fd); px(img, 11, 14, fd)
    px(img, 4, 15, feather); px(img, 11, 15, feather)
    add_noise(img, intensity=10)
    save(img, f"{ITEM}/arrow.png")

    # Explosive Arrow - arrow with red explosive tip
    img = Image.new("RGBA", (16,16), (0,0,0,0))
    shaft = (165,135,85,255); sd = (135,108,65,255)
    red = (210,55,35,255); dark_r = (170,40,25,255)
    feather = (210,210,210,255)
    for i in range(9):
        px(img, 7, 5+i, shaft); px(img, 8, 5+i, sd)
    fill_rect(img, 6, 1, 9, 4, red)
    fill_rect(img, 7, 2, 8, 3, dark_r)
    px(img, 7, 0, red); px(img, 8, 0, red)
    px(img, 5, 2, (255,200,50,255)); px(img, 10, 3, (255,200,50,255))
    px(img, 5, 13, feather); px(img, 6, 13, feather); px(img, 9, 13, feather); px(img, 10, 13, feather)
    px(img, 4, 14, feather); px(img, 11, 14, feather)
    add_noise(img, intensity=10)
    save(img, f"{ITEM}/explosive_arrow.png")

    # Fire Arrow
    img = Image.new("RGBA", (16,16), (0,0,0,0))
    shaft = (165,135,85,255); sd = (135,108,65,255)
    flame = (250,160,35,255); bright = (255,230,80,255); red = (220,70,20,255)
    feather = (210,210,210,255)
    for i in range(9):
        px(img, 7, 5+i, shaft); px(img, 8, 5+i, sd)
    fill_rect(img, 6, 2, 9, 4, flame)
    px(img, 7, 1, bright); px(img, 8, 1, bright)
    px(img, 7, 0, red); px(img, 5, 1, red); px(img, 10, 2, red)
    px(img, 7, 3, bright); px(img, 8, 2, bright)
    px(img, 5, 13, feather); px(img, 6, 13, feather); px(img, 9, 13, feather); px(img, 10, 13, feather)
    px(img, 4, 14, feather); px(img, 11, 14, feather)
    add_noise(img, intensity=10)
    save(img, f"{ITEM}/fire_arrow.png")

    # Burning Shaft
    img = Image.new("RGBA", (16,16), (0,0,0,0))
    shaft = (125,85,35,255); sd = (95,60,20,255)
    flame = (255,160,35,255); bright = (255,230,80,255); red = (220,80,25,255)
    for i in range(11):
        px(img, 7, 4+i, shaft); px(img, 8, 4+i, sd)
    fill_rect(img, 5, 0, 10, 4, flame)
    fill_rect(img, 6, 1, 9, 3, bright)
    px(img, 7, 0, red); px(img, 8, 0, red)
    px(img, 5, 1, red); px(img, 10, 1, red)
    px(img, 5, 3, red); px(img, 10, 3, red)
    px(img, 7, 2, (255,255,150,255))
    add_noise(img, intensity=10)
    save(img, f"{ITEM}/burning_shaft.png")

    # Blunderbuss Ammo - metal ball cluster
    img = Image.new("RGBA", (16,16), (0,0,0,0))
    ball = (150,150,160,255); dark = (110,110,120,255); light = (180,180,190,255)
    positions = [(5,4),(9,4),(3,7),(7,7),(11,7),(5,10),(9,10),(7,13)]
    for cx,cy in positions:
        fill_rect(img, cx, cy, cx+1, cy+1, ball)
        px(img, cx, cy, light)
        px(img, cx+1, cy+1, dark)
    add_noise(img, intensity=10)
    save(img, f"{ITEM}/blunderbuss_ammo.png")

    # Bolt - crossbow bolt
    img = Image.new("RGBA", (16,16), (0,0,0,0))
    shaft = (130,130,135,255); sd = (100,100,105,255)
    tip = (170,170,180,255); tl = (200,200,210,255)
    fin = (85,85,90,255)
    for i in range(11):
        px(img, 7, 3+i, shaft); px(img, 8, 3+i, sd)
    px(img, 7, 1, tip); px(img, 8, 1, tip); px(img, 7, 2, tip); px(img, 8, 2, tip)
    px(img, 7, 0, tl); px(img, 8, 0, tl)
    px(img, 6, 2, tip); px(img, 9, 2, tip)
    px(img, 5, 13, fin); px(img, 6, 13, fin); px(img, 9, 13, fin); px(img, 10, 13, fin)
    px(img, 4, 14, fin); px(img, 11, 14, fin)
    px(img, 5, 15, (100,100,105,255)); px(img, 10, 15, (100,100,105,255))
    add_noise(img, intensity=8)
    save(img, f"{ITEM}/bolt.png")

    # Junk Turret Dart
    img = Image.new("RGBA", (16,16), (0,0,0,0))
    body = (110,110,120,255); bd = (80,80,90,255)
    tip = (170,170,180,255); fin = (90,90,95,255)
    for i in range(11):
        px(img, 7, 3+i, body); px(img, 8, 3+i, bd)
    px(img, 7, 1, tip); px(img, 8, 1, tip); px(img, 7, 2, tip); px(img, 8, 2, tip)
    px(img, 7, 0, (190,190,200,255))
    px(img, 6, 12, fin); px(img, 9, 12, fin)
    px(img, 5, 13, fin); px(img, 10, 13, fin)
    px(img, 6, 14, fin); px(img, 9, 14, fin)
    add_noise(img, intensity=10)
    save(img, f"{ITEM}/junk_turret_dart.png")

    # Rocket Ammo
    img = Image.new("RGBA", (16,16), (0,0,0,0))
    body = (80,85,75,255); bd = (55,60,50,255)
    tip = (210,65,45,255); td = (170,45,30,255)
    fin = (110,110,120,255); flame = (255,190,50,255)
    fill_rect(img, 5, 4, 10, 11, body)
    fill_rect(img, 6, 5, 9, 10, bd)
    fill_rect(img, 6, 2, 9, 3, tip)
    fill_rect(img, 7, 2, 8, 2, td)
    px(img, 7, 1, tip); px(img, 8, 1, tip)
    fill_rect(img, 4, 12, 11, 13, fin)
    px(img, 3, 12, fin); px(img, 12, 12, fin)
    px(img, 6, 14, flame); px(img, 7, 14, flame); px(img, 8, 14, flame); px(img, 9, 14, flame)
    px(img, 7, 15, (255,140,30,255)); px(img, 8, 15, (255,140,30,255))
    outline_rect(img, 5, 1, 10, 13, (35,40,30,255))
    add_noise(img, intensity=10)
    save(img, f"{ITEM}/rocket_ammo.png")

    # Shotgun Shell
    img = Image.new("RGBA", (16,16), (0,0,0,0))
    red = (185,35,35,255); rd = (150,25,25,255)
    brass = (210,180,65,255); bd = (175,150,45,255)
    cap = (195,170,60,255)
    fill_rect(img, 5, 2, 10, 3, brass)
    fill_rect(img, 6, 2, 9, 2, (230,200,85,255))
    fill_rect(img, 5, 4, 10, 11, red)
    fill_rect(img, 6, 5, 9, 10, rd)
    fill_rect(img, 5, 12, 10, 13, cap)
    fill_rect(img, 6, 12, 9, 12, bd)
    outline_rect(img, 5, 2, 10, 13, (100,20,20,255))
    add_noise(img, intensity=8)
    save(img, f"{ITEM}/shotgun_shell.png")

# ============================================================
# GUI / HUD TEXTURES
# ============================================================
def make_gui():
    print("=== GUI / HUD ===")

    # --- HUD Background Elements (16x16 panels) ---
    def hud_panel(name, bg_color, border_color):
        img = Image.new("RGBA", (16,16), (0,0,0,0))
        fill_rect(img, 0, 0, 15, 15, bg_color)
        border_light = shade(border_color, 30)
        border_dark = shade(border_color, -30)
        for x in range(16):
            px(img, x, 0, border_light); px(img, x, 15, border_dark)
            px(img, x, 1, shade(bg_color, 10)); px(img, x, 14, shade(bg_color, -10))
        for y in range(16):
            px(img, 0, y, border_light); px(img, 15, y, border_dark)
            px(img, 1, y, shade(bg_color, 10)); px(img, 14, y, shade(bg_color, -10))
        px(img, 0, 0, border_color); px(img, 15, 0, border_color)
        px(img, 0, 15, border_color); px(img, 15, 15, border_color)
        add_noise(img, intensity=5)
        save(img, f"{GUI}/{name}")

    hud_panel("ammo_counter_bg.png", (20,20,25,180), (80,80,90,200))
    hud_panel("day_counter_bg.png", (15,15,30,180), (70,70,100,200))
    hud_panel("horde_timer_bg.png", (40,10,10,180), (120,40,40,200))
    hud_panel("vehicle_hud_bg.png", (20,25,20,180), (60,80,60,200))

    # --- Stat Icons (16x16) ---
    # Fuel icon
    img = Image.new("RGBA", (16,16), (0,0,0,0))
    red = (200,50,40,255); dark = (160,35,25,255); cap = (130,130,140,255)
    out = (100,25,15,255)
    fill_rect(img, 3, 4, 12, 14, red)
    fill_rect(img, 4, 5, 11, 13, dark)
    fill_rect(img, 5, 6, 10, 12, shade(dark, -15))
    fill_rect(img, 5, 2, 8, 4, cap)
    fill_rect(img, 6, 3, 7, 3, (150,150,160,255))
    px(img, 9, 3, cap); px(img, 10, 4, cap)
    outline_rect(img, 3, 4, 12, 14, out)
    px(img, 7, 8, (240,200,50,255)); px(img, 8, 8, (240,200,50,255))
    px(img, 7, 9, (220,180,40,255)); px(img, 8, 9, (220,180,40,255))
    add_noise(img, intensity=8)
    save(img, f"{GUI}/fuel_icon.png")

    # Food icon - drumstick
    img = Image.new("RGBA", (16,16), (0,0,0,0))
    meat = (185,105,65,255); dark = (155,80,45,255); crispy = (165,95,55,255)
    bone = (235,225,205,255); bd = (200,190,170,255)
    fill_rect(img, 2, 3, 10, 10, meat)
    fill_rect(img, 3, 4, 9, 9, dark)
    fill_rect(img, 4, 5, 8, 8, crispy)
    fill_rect(img, 10, 6, 14, 8, bone)
    fill_rect(img, 11, 7, 13, 7, bd)
    px(img, 15, 6, bone); px(img, 15, 8, bone)
    outline_rect(img, 2, 3, 10, 10, (120,60,30,255))
    px(img, 5, 5, (200,120,75,255))
    add_noise(img, intensity=10)
    save(img, f"{GUI}/food_icon.png")

    # Water icon - water drop
    img = Image.new("RGBA", (16,16), (0,0,0,0))
    blue = (65,145,225,255); light = (105,185,245,255); dark = (40,105,185,255)
    out = (25,70,130,255)
    px(img, 7, 1, blue); px(img, 8, 1, blue)
    fill_rect(img, 6, 2, 9, 4, blue)
    fill_rect(img, 5, 4, 10, 7, blue)
    fill_rect(img, 4, 6, 11, 11, blue)
    fill_rect(img, 5, 11, 10, 12, blue)
    fill_rect(img, 6, 13, 9, 13, blue)
    fill_rect(img, 5, 7, 7, 9, light)
    px(img, 6, 5, light); px(img, 7, 4, light)
    fill_rect(img, 8, 9, 10, 11, dark)
    for x,y in [(7,0),(8,0),(6,1),(9,1),(5,3),(10,3),(4,5),(11,5),(3,7),(12,7),(3,11),(12,11),(5,13),(10,13),(7,14),(8,14)]:
        px(img, x, y, out)
    add_noise(img, intensity=8)
    save(img, f"{GUI}/water_icon.png")

    # Temperature icon - thermometer
    img = Image.new("RGBA", (16,16), (0,0,0,0))
    glass = (210,210,220,255); gd = (180,180,190,255)
    mercury = (210,55,45,255); md = (175,40,30,255)
    fill_rect(img, 6, 0, 9, 10, glass)
    fill_rect(img, 7, 1, 8, 9, gd)
    fill_rect(img, 7, 3, 8, 10, mercury)
    fill_rect(img, 5, 10, 10, 14, mercury)
    fill_rect(img, 6, 11, 9, 13, md)
    px(img, 7, 12, (230,80,60,255))
    outline_rect(img, 6, 0, 9, 10, (150,150,160,255))
    outline_rect(img, 5, 10, 10, 14, (150,30,20,255))
    add_noise(img, intensity=8)
    save(img, f"{GUI}/temperature_icon.png")

    # Heat indicator - flame
    img = Image.new("RGBA", (16,16), (0,0,0,0))
    orange = (245,155,35,255); yellow = (255,225,65,255); red = (225,65,25,255)
    dark_r = (180,40,15,255)
    px(img, 7, 1, yellow); px(img, 8, 1, yellow)
    fill_rect(img, 6, 2, 9, 4, orange)
    fill_rect(img, 5, 4, 10, 8, orange)
    fill_rect(img, 4, 7, 11, 11, orange)
    fill_rect(img, 5, 11, 10, 13, red)
    fill_rect(img, 6, 13, 9, 14, dark_r)
    fill_rect(img, 6, 4, 8, 7, yellow)
    px(img, 7, 3, (255,245,130,255))
    fill_rect(img, 8, 8, 10, 10, red)
    px(img, 5, 5, red); px(img, 10, 5, red)
    add_noise(img, intensity=10)
    save(img, f"{GUI}/heat_indicator.png")

    # --- Health/Stamina/XP Bars (64x8) ---
    def make_bar(name, fill_color, bg_color, border_color):
        # Background
        img = Image.new("RGBA", (64, 8), (0,0,0,0))
        d = ImageDraw.Draw(img)
        d.rectangle([0,0,63,7], fill=bg_color, outline=border_color)
        d.rectangle([1,1,62,6], fill=shade(bg_color, -10))
        d.rectangle([2,2,61,5], fill=bg_color)
        add_noise(img, intensity=5)
        save(img, f"{GUI}/{name}_bg.png")
        # Fill
        img = Image.new("RGBA", (64, 8), (0,0,0,0))
        d = ImageDraw.Draw(img)
        d.rectangle([0,0,63,7], fill=fill_color)
        d.rectangle([0,0,63,2], fill=shade(fill_color, 30))
        d.rectangle([0,5,63,7], fill=shade(fill_color, -20))
        d.line([(0,1),(63,1)], fill=shade(fill_color, 50))
        add_noise(img, intensity=5)
        save(img, f"{GUI}/{name}_fill.png")

    make_bar("health_bar", (205,45,45,255), (30,10,10,200), (80,30,30,255))
    make_bar("stamina_bar", (45,185,45,255), (10,20,10,200), (30,80,30,255))
    make_bar("xp_bar", (85,85,225,255), (10,10,30,200), (40,40,100,255))

    # --- Heart Icons (9x9) ---
    def heart_pixels():
        return [(1,1),(2,0),(3,1),(4,2),(5,1),(6,0),(7,1),
                (0,2),(1,2),(2,1),(3,2),(5,2),(6,1),(7,2),(8,2),
                (0,3),(1,3),(2,3),(3,3),(4,3),(5,3),(6,3),(7,3),(8,3),
                (1,4),(2,4),(3,4),(4,4),(5,4),(6,4),(7,4),
                (2,5),(3,5),(4,5),(5,5),(6,5),
                (3,6),(4,6),(5,6),
                (4,7)]

    def make_heart(name, color, inner=None, half_empty=False):
        img = Image.new("RGBA", (9,9), (0,0,0,0))
        pts = heart_pixels()
        for x,y in pts:
            px(img, x, y, color)
        if inner:
            for x,y in [(2,2),(3,2),(6,2),(3,3),(4,3),(5,3),(4,4),(3,4),(5,4),(4,5)]:
                px(img, x, y, inner)
        if half_empty:
            for x,y in pts:
                if x >= 5:
                    c = img.getpixel((x,y))
                    if c[3] > 0:
                        px(img, x, y, (60,20,20,255))
        # outline
        for x,y in pts:
            for dx,dy in [(-1,0),(1,0),(0,-1),(0,1)]:
                nx,ny = x+dx, y+dy
                if 0<=nx<9 and 0<=ny<9 and img.getpixel((nx,ny))[3]==0:
                    px(img, nx, ny, (40,10,10,200))
        save(img, f"{GUI}/{name}")

    make_heart("heart_full.png", (205,45,45,255), (240,90,90,255))
    make_heart("heart_half.png", (205,45,45,255), (240,90,90,255), half_empty=True)
    make_heart("heart_empty.png", (60,20,20,255), (80,30,30,255))
    make_heart("heart_low.png", (165,35,35,255), (125,25,25,255))

    # --- Armor Icons (9x9) ---
    def shield_pixels():
        return [(1,0),(2,0),(3,0),(4,0),(5,0),(6,0),(7,0),
                (0,1),(1,1),(2,1),(3,1),(4,1),(5,1),(6,1),(7,1),(8,1),
                (0,2),(1,2),(2,2),(3,2),(4,2),(5,2),(6,2),(7,2),(8,2),
                (0,3),(1,3),(2,3),(3,3),(4,3),(5,3),(6,3),(7,3),(8,3),
                (1,4),(2,4),(3,4),(4,4),(5,4),(6,4),(7,4),
                (2,5),(3,5),(4,5),(5,5),(6,5),
                (3,6),(4,6),(5,6),
                (4,7)]

    def make_shield(name, color, inner=None, half_empty=False):
        img = Image.new("RGBA", (9,9), (0,0,0,0))
        pts = shield_pixels()
        for x,y in pts:
            px(img, x, y, color)
        if inner:
            for x,y in [(2,2),(3,2),(4,2),(5,2),(6,2),(3,3),(4,3),(5,3),(4,4),(4,5),(3,4),(5,4)]:
                px(img, x, y, inner)
        if half_empty:
            for x,y in pts:
                if x >= 5:
                    c = img.getpixel((x,y))
                    if c[3] > 0:
                        px(img, x, y, (50,50,55,255))
        for x,y in pts:
            for dx,dy in [(-1,0),(1,0),(0,-1),(0,1)]:
                nx,ny = x+dx, y+dy
                if 0<=nx<9 and 0<=ny<9 and img.getpixel((nx,ny))[3]==0:
                    px(img, nx, ny, (30,30,35,200))
        save(img, f"{GUI}/{name}")

    make_shield("armor_full.png", (145,145,155,255), (185,185,195,255))
    make_shield("armor_half.png", (145,145,155,255), (185,185,195,255), half_empty=True)
    make_shield("armor_empty.png", (50,50,55,255), (70,70,75,255))

    # --- Food status icons (9x9) ---
    def drumstick_pixels():
        return [(0,2),(1,1),(1,2),(1,3),(2,1),(2,2),(2,3),(2,4),(3,1),(3,2),(3,3),(3,4),
                (4,2),(4,3),(4,4),(5,3),(5,4),(6,3),(6,4),(7,4)]

    def make_drumstick(name, color, bone_c=(235,225,205,255), half_empty=False, is_low=False):
        img = Image.new("RGBA", (9,9), (0,0,0,0))
        pts = drumstick_pixels()
        for x,y in pts:
            if x <= 4:
                px(img, x, y, color)
            else:
                px(img, x, y, bone_c)
        if is_low:
            inner_c = shade(color, -25)
        else:
            inner_c = shade(color, 20)
        for x,y in [(2,2),(3,2),(2,3),(3,3)]:
            px(img, x, y, inner_c)
        if half_empty:
            for x,y in pts:
                if x >= 5:
                    c = img.getpixel((x,y))
                    if c[3] > 0:
                        px(img, x, y, (60,35,15,255))
        for x,y in pts:
            for dx,dy in [(-1,0),(1,0),(0,-1),(0,1)]:
                nx,ny = x+dx, y+dy
                if 0<=nx<9 and 0<=ny<9 and img.getpixel((nx,ny))[3]==0:
                    px(img, nx, ny, (80,40,15,200))
        save(img, f"{GUI}/{name}")

    make_drumstick("food_full.png", (185,105,55,255))
    make_drumstick("food_half.png", (185,105,55,255), half_empty=True)
    make_drumstick("food_empty.png", (60,35,15,255), (80,75,65,255))
    make_drumstick("food_low.png", (145,75,35,255), is_low=True)
    make_drumstick("food_half_low.png", (145,75,35,255), half_empty=True, is_low=True)

    # --- Water status icons (9x9) ---
    def drop_pixels():
        return [(3,0),(4,0),(2,1),(3,1),(4,1),(5,1),(1,2),(2,2),(3,2),(4,2),(5,2),(6,2),
                (1,3),(2,3),(3,3),(4,3),(5,3),(6,3),
                (1,4),(2,4),(3,4),(4,4),(5,4),(6,4),
                (2,5),(3,5),(4,5),(5,5),
                (3,6),(4,6)]

    def make_drop(name, color, half_empty=False, is_low=False):
        img = Image.new("RGBA", (9,9), (0,0,0,0))
        pts = drop_pixels()
        for x,y in pts:
            px(img, x, y, color)
        if is_low:
            inner_c = shade(color, -20)
        else:
            inner_c = shade(color, 30)
        for x,y in [(2,2),(3,2),(3,1),(2,3),(3,3)]:
            px(img, x, y, inner_c)
        if half_empty:
            for x,y in pts:
                if x >= 4:
                    c = img.getpixel((x,y))
                    if c[3] > 0:
                        px(img, x, y, (25,50,80,255))
        for x,y in pts:
            for dx,dy in [(-1,0),(1,0),(0,-1),(0,1)]:
                nx,ny = x+dx, y+dy
                if 0<=nx<9 and 0<=ny<9 and img.getpixel((nx,ny))[3]==0:
                    px(img, nx, ny, (15,40,80,200))
        save(img, f"{GUI}/{name}")

    make_drop("water_full.png", (65,145,225,255))
    make_drop("water_half.png", (65,145,225,255), half_empty=True)
    make_drop("water_empty.png", (25,50,80,255))
    make_drop("water_low.png", (45,105,175,255), is_low=True)
    make_drop("water_half_low.png", (45,105,175,255), half_empty=True, is_low=True)

    # --- Quality Tier Icons (16x16 stars) ---
    quality_colors = {
        "quality_poor.png": (150,150,150,255),
        "quality_good.png": (50,180,50,255),
        "quality_great.png": (50,100,220,255),
        "quality_excellent.png": (160,50,200,255),
        "quality_superior.png": (220,160,30,255),
        "quality_legendary.png": (220,50,30,255),
    }
    star_pts = [(7,0),(8,0),(6,1),(7,1),(8,1),(9,1),
                (5,2),(6,2),(7,2),(8,2),(9,2),(10,2),
                (1,4),(2,4),(3,4),(4,4),(5,4),(6,4),(7,4),(8,4),(9,4),(10,4),(11,4),(12,4),(13,4),(14,4),
                (2,5),(3,5),(4,5),(5,5),(6,5),(7,5),(8,5),(9,5),(10,5),(11,5),(12,5),(13,5),
                (3,6),(4,6),(5,6),(6,6),(7,6),(8,6),(9,6),(10,6),(11,6),(12,6),
                (4,7),(5,7),(6,7),(7,7),(8,7),(9,7),(10,7),(11,7),
                (4,8),(5,8),(6,8),(7,8),(8,8),(9,8),(10,8),(11,8),
                (3,9),(4,9),(5,9),(6,9),(9,9),(10,9),(11,9),(12,9),
                (3,10),(4,10),(5,10),(10,10),(11,10),(12,10),
                (2,11),(3,11),(4,11),(11,11),(12,11),(13,11),
                (2,12),(3,12),(12,12),(13,12),
                (1,3),(2,3),(3,3),(4,3),(5,3),(6,3),(7,3),(8,3),(9,3),(10,3),(11,3),(12,3),(13,3),(14,3)]
    for name, color in quality_colors.items():
        img = Image.new("RGBA", (16,16), (0,0,0,0))
        bright = shade(color, 50)
        dark = shade(color, -40)
        for x,y in star_pts:
            px(img, x, y, color)
        for x,y in [(7,1),(8,1),(7,2),(8,2),(6,3),(7,3),(8,3),(9,3),(6,4),(7,4),(8,4),(9,4)]:
            px(img, x, y, bright)
        for x,y in [(4,8),(5,9),(3,10),(2,11),(11,8),(10,9),(12,10),(13,11)]:
            px(img, x, y, dark)
        for x,y in star_pts:
            for dx,dy in [(-1,0),(1,0),(0,-1),(0,1)]:
                nx,ny = x+dx, y+dy
                if 0<=nx<16 and 0<=ny<16 and img.getpixel((nx,ny))[3]==0:
                    px(img, nx, ny, shade(color, -80))
        add_noise(img, intensity=5)
        save(img, f"{GUI}/{name}")

    # --- Attribute Icons (16x16) ---
    # Agility - lightning bolt
    img = Image.new("RGBA", (16,16), (0,0,0,0))
    c = (55,205,55,255); b = (110,235,110,255); d = (35,150,35,255)
    bolt_pts = [(8,0),(9,0),(7,1),(8,1),(9,1),(6,2),(7,2),(8,2),(5,3),(6,3),(7,3),(8,3),(9,3),(10,3),(11,3),
                (7,4),(8,4),(9,4),(10,4),(11,4),(9,5),(10,5),(8,6),(9,6),(7,7),(8,7),(6,8),(7,8),
                (5,9),(6,9),(7,9),(8,9),(9,9),(10,9),(8,10),(9,10),(7,11),(8,11),(6,12),(7,12),(5,13),(6,13)]
    for x,y in bolt_pts:
        px(img, x, y, c)
    for x,y in [(8,1),(7,2),(7,3),(8,4),(9,5),(8,9),(7,10),(6,11)]:
        px(img, x, y, b)
    for x,y in [(6,3),(10,3),(5,9),(10,9),(5,13)]:
        px(img, x, y, d)
    for x,y in bolt_pts:
        for dx,dy in [(-1,0),(1,0),(0,-1),(0,1)]:
            nx,ny = x+dx, y+dy
            if 0<=nx<16 and 0<=ny<16 and img.getpixel((nx,ny))[3]==0:
                px(img, nx, ny, (20,80,20,200))
    add_noise(img, intensity=8)
    save(img, f"{GUI}/attribute_agility.png")

    # Fortitude - shield with cross
    img = Image.new("RGBA", (16,16), (0,0,0,0))
    c = (205,165,45,255); inner = (245,205,85,255); d = (160,120,30,255)
    # Shield outline
    fill_rect(img, 2, 1, 13, 10, c)
    fill_rect(img, 3, 11, 12, 11, c)
    fill_rect(img, 4, 12, 11, 12, c)
    fill_rect(img, 5, 13, 10, 13, c)
    fill_rect(img, 6, 14, 9, 14, c)
    px(img, 7, 15, c); px(img, 8, 15, c)
    # Inner darker
    fill_rect(img, 3, 2, 12, 9, d)
    fill_rect(img, 4, 10, 11, 10, d)
    fill_rect(img, 5, 11, 10, 11, d)
    fill_rect(img, 6, 12, 9, 12, d)
    # Cross
    fill_rect(img, 7, 3, 8, 11, inner)
    fill_rect(img, 4, 6, 11, 7, inner)
    for x,y in [(2,1),(13,1),(2,10),(13,10)]:
        px(img, x, y, d)
    add_noise(img, intensity=8)
    save(img, f"{GUI}/attribute_fortitude.png")

    # Intellect - gear/cog
    img = Image.new("RGBA", (16,16), (0,0,0,0))
    c = (85,145,225,255); inner = (125,185,245,255); d = (55,105,180,255)
    fill_rect(img, 4, 3, 11, 12, c)
    fill_rect(img, 3, 4, 12, 11, c)
    fill_rect(img, 5, 4, 10, 11, d)
    fill_rect(img, 6, 6, 9, 9, inner)
    fill_rect(img, 7, 7, 8, 8, d)
    # Gear teeth
    for pos in [(7,1),(8,1),(7,14),(8,14),(1,7),(1,8),(14,7),(14,8)]:
        px(img, pos[0], pos[1], c)
    for pos in [(3,3),(12,3),(3,12),(12,12)]:
        px(img, pos[0], pos[1], c)
    px(img, 7, 2, c); px(img, 8, 2, c); px(img, 7, 13, c); px(img, 8, 13, c)
    px(img, 2, 7, c); px(img, 2, 8, c); px(img, 13, 7, c); px(img, 13, 8, c)
    add_noise(img, intensity=8)
    save(img, f"{GUI}/attribute_intellect.png")

    # Perception - eye
    img = Image.new("RGBA", (16,16), (0,0,0,0))
    c = (185,65,65,255); white = (235,235,240,255); pupil = (45,45,55,255)
    d = (145,45,45,255)
    fill_rect(img, 2, 5, 13, 10, white)
    fill_rect(img, 3, 4, 12, 4, white)
    fill_rect(img, 3, 11, 12, 11, white)
    px(img, 1, 7, c); px(img, 1, 8, c); px(img, 14, 7, c); px(img, 14, 8, c)
    px(img, 0, 7, d); px(img, 15, 7, d)
    fill_rect(img, 5, 5, 10, 10, c)
    fill_rect(img, 6, 6, 9, 9, pupil)
    fill_rect(img, 7, 7, 8, 8, (20,20,25,255))
    px(img, 7, 6, (80,80,100,255))
    outline_rect(img, 2, 4, 13, 11, d)
    add_noise(img, intensity=8)
    save(img, f"{GUI}/attribute_perception.png")

    # Strength - flexed arm
    img = Image.new("RGBA", (16,16), (0,0,0,0))
    c = (205,85,45,255); b = (235,125,75,255); d = (165,60,30,255)
    fill_rect(img, 2, 9, 6, 14, c)
    fill_rect(img, 3, 10, 5, 13, d)
    fill_rect(img, 5, 4, 9, 9, c)
    fill_rect(img, 6, 5, 8, 8, d)
    fill_rect(img, 9, 1, 13, 5, c)
    fill_rect(img, 10, 2, 12, 4, d)
    fill_rect(img, 10, 5, 14, 9, c)
    fill_rect(img, 11, 6, 13, 8, d)
    px(img, 7, 5, b); px(img, 11, 3, b); px(img, 4, 10, b)
    for pts in [(2,9),(6,9),(2,14),(6,14),(5,4),(9,4),(9,1),(13,1),(10,5),(14,5),(14,9),(10,9)]:
        px(img, pts[0], pts[1], (140,45,20,255))
    add_noise(img, intensity=10)
    save(img, f"{GUI}/attribute_strength.png")

# ============================================================
# WORKSTATION BLOCKS (16x16)
# ============================================================
def make_workstations():
    print("=== WORKSTATION BLOCKS ===")

    # Cement Mixer
    img = Image.new("RGBA", (16,16), (0,0,0,0))
    body = (145,145,150,255); dark = (105,105,110,255); drum = (165,165,170,255)
    out = (70,70,75,255)
    fill_rect(img, 1, 3, 14, 13, body)
    fill_rect(img, 2, 4, 13, 12, dark)
    fill_rect(img, 3, 5, 12, 11, (90,90,95,255))
    fill_rect(img, 3, 1, 12, 3, drum)
    fill_rect(img, 4, 2, 11, 2, (185,185,190,255))
    fill_rect(img, 4, 14, 11, 15, body)
    fill_rect(img, 5, 14, 6, 15, dark); fill_rect(img, 9, 14, 10, 15, dark)
    outline_rect(img, 1, 3, 14, 13, out)
    outline_rect(img, 3, 1, 12, 3, out)
    px(img, 7, 7, (200,190,160,255)); px(img, 8, 7, (200,190,160,255))
    px(img, 7, 8, (180,170,140,255)); px(img, 8, 8, (180,170,140,255))
    add_noise(img, intensity=10)
    save(img, f"{BLOCK}/cement_mixer.png")

    # Chemistry Station
    img = Image.new("RGBA", (16,16), (0,0,0,0))
    table = (105,95,85,255); td = (75,68,58,255)
    flask = (185,205,185,255); fd = (155,175,155,255)
    liquid = (85,205,85,255); ld = (60,170,60,255)
    out = (50,45,35,255)
    fill_rect(img, 0, 9, 15, 10, table)
    fill_rect(img, 1, 9, 14, 10, td)
    fill_rect(img, 1, 11, 2, 15, td); fill_rect(img, 13, 11, 14, 15, td)
    # Flask left
    fill_rect(img, 3, 4, 6, 9, flask)
    fill_rect(img, 4, 5, 5, 8, fd)
    fill_rect(img, 4, 6, 5, 8, liquid)
    px(img, 4, 7, ld)
    px(img, 4, 3, flask); px(img, 5, 3, flask)
    # Flask right (taller)
    fill_rect(img, 9, 2, 12, 9, flask)
    fill_rect(img, 10, 3, 11, 8, fd)
    fill_rect(img, 10, 5, 11, 8, liquid)
    px(img, 10, 6, ld)
    px(img, 10, 1, flask); px(img, 11, 1, flask)
    # Tube connecting
    fill_rect(img, 6, 5, 9, 5, (160,160,165,255))
    outline_rect(img, 3, 3, 6, 9, out)
    outline_rect(img, 9, 1, 12, 9, out)
    add_noise(img, intensity=10)
    save(img, f"{BLOCK}/chemistry_station.png")

    # Campfire Station
    img = Image.new("RGBA", (16,16), (0,0,0,0))
    stone = (105,100,90,255); sd = (80,75,65,255)
    flame = (245,155,35,255); bright = (255,225,65,255); red = (205,65,25,255)
    fill_rect(img, 2, 10, 13, 14, stone)
    fill_rect(img, 3, 11, 12, 13, sd)
    fill_rect(img, 1, 11, 1, 13, sd); fill_rect(img, 14, 11, 14, 13, sd)
    fill_rect(img, 3, 15, 12, 15, stone)
    fill_rect(img, 4, 6, 11, 10, flame)
    fill_rect(img, 5, 4, 10, 6, flame)
    fill_rect(img, 6, 2, 9, 4, flame)
    px(img, 7, 1, bright); px(img, 8, 1, bright)
    fill_rect(img, 6, 4, 9, 7, bright)
    px(img, 7, 3, (255,245,130,255)); px(img, 8, 3, (255,245,130,255))
    fill_rect(img, 4, 9, 11, 10, red)
    fill_rect(img, 5, 8, 6, 9, red)
    px(img, 10, 5, red); px(img, 5, 5, red)
    outline_rect(img, 2, 10, 13, 14, (55,50,40,255))
    px(img, 4, 12, (120,110,95,255)); px(img, 9, 11, (120,110,95,255))
    add_noise(img, intensity=10)
    save(img, f"{BLOCK}/campfire_station.png")

    # Forge Station
    img = Image.new("RGBA", (16,16), (0,0,0,0))
    iron = (85,85,95,255); dark = (55,55,65,255); light = (115,115,125,255)
    glow = (225,125,35,255); bright = (255,185,55,255)
    out = (35,35,45,255)
    fill_rect(img, 1, 7, 14, 13, iron)
    fill_rect(img, 2, 8, 13, 12, dark)
    fill_rect(img, 3, 14, 5, 15, iron); fill_rect(img, 10, 14, 12, 15, iron)
    fill_rect(img, 4, 4, 11, 7, iron)
    fill_rect(img, 5, 5, 10, 6, dark)
    fill_rect(img, 5, 9, 10, 12, glow)
    fill_rect(img, 6, 10, 9, 11, bright)
    px(img, 7, 10, (255,220,100,255)); px(img, 8, 10, (255,220,100,255))
    outline_rect(img, 1, 7, 14, 13, out)
    outline_rect(img, 4, 4, 11, 7, out)
    px(img, 2, 8, light); px(img, 3, 8, light)
    px(img, 5, 5, light)
    add_noise(img, intensity=10)
    save(img, f"{BLOCK}/forge_station.png")

make_melee()
make_ranged()
make_ammo()
make_gui()
make_workstations()

print("\n=== DONE ===")
print("All priority textures generated!")
