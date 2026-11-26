#!/usr/bin/env python3
"""Split an RGBA PNG heightmap into 16x16 tiles for KyushuRealScale.

Input requirements:
- PNG width/height are in metres at 1 pixel per metre.
- Opaque pixels are land. Transparent pixels are sea.
- Land pixel brightness (0..255) encodes relative elevation:
  0 = 64 blocks high, 255 = 224 blocks high.
- The PNG should already be projected so horizontal pixels represent equal
  ground distances (for example, a suitable local projected CRS), not raw
  latitude/longitude degrees.

Usage:
  python3 split_heightmap.py kyushu_heightmap.png /path/to/config/kyushurealscale/tiles
"""
from pathlib import Path
from PIL import Image
import sys

TILE = 16

def main():
    if len(sys.argv) != 3:
        raise SystemExit("Usage: python3 split_heightmap.py INPUT.png OUTPUT_TILES_DIR")
    src = Path(sys.argv[1])
    out = Path(sys.argv[2])
    out.mkdir(parents=True, exist_ok=True)

    im = Image.open(src).convert("RGBA")
    w, h = im.size
    print(f"Input: {w} x {h} pixels. At 1 px/m: {w}m x {h}m.")
    tiles_x = (w + TILE - 1) // TILE
    tiles_z = (h + TILE - 1) // TILE

    for tz in range(tiles_z):
        for tx in range(tiles_x):
            tile = Image.new("RGBA", (TILE, TILE), (0, 0, 0, 0))
            crop = im.crop((tx*TILE, tz*TILE, min((tx+1)*TILE, w), min((tz+1)*TILE, h)))
            tile.alpha_composite(crop, (0, 0))
            tile.save(out / f"{tx}_{tz}.png")
        print(f"Finished tile row {tz+1}/{tiles_z}")
    print(f"Created {tiles_x * tiles_z} tiles in {out}")

if __name__ == "__main__":
    main()
