from collections import deque
from pathlib import Path
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
GENERATED = ROOT.parent / "generated_images"
OUT = ROOT / "app" / "src" / "main" / "res" / "drawable-nodpi"
HELD = GENERATED / "exec-fd75a4b4-24a4-4be2-9521-8d3bf13339e3.png"
FALL = GENERATED / "exec-011d8d7a-0a66-46e6-81c3-49dd58eeff92.png"
PETTED = GENERATED / "exec-90b44ddb-1de2-43be-9966-7cb81e1660c0.png"


def remove_connected_background(image: Image.Image) -> Image.Image:
    rgba = image.convert("RGBA")
    px = rgba.load()
    width, height = rgba.size
    seen = bytearray(width * height)
    queue = deque()

    def is_background(x: int, y: int) -> bool:
        r, g, b, _ = px[x, y]
        # Generated atlases use a light neutral checkerboard/white backdrop.
        return max(r, g, b) - min(r, g, b) <= 24 and min(r, g, b) >= 132

    for x in range(width):
        queue.extend(((x, 0), (x, height - 1)))
    for y in range(height):
        queue.extend(((0, y), (width - 1, y)))

    while queue:
        x, y = queue.popleft()
        index = y * width + x
        if seen[index] or not is_background(x, y):
            continue
        seen[index] = 1
        px[x, y] = (0, 0, 0, 0)
        for nx, ny in ((x-1,y),(x+1,y),(x,y-1),(x,y+1),(x-1,y-1),(x+1,y-1),(x-1,y+1),(x+1,y+1)):
            if 0 <= nx < width and 0 <= ny < height and not seen[ny * width + nx]:
                queue.append((nx, ny))
    return rgba


def normalize(cell: Image.Image) -> Image.Image:
    cell = remove_connected_background(cell)
    alpha = cell.getchannel("A")
    bounds = alpha.getbbox()
    if not bounds:
        raise ValueError("Empty sprite cell")
    cell = cell.crop(bounds)
    scale = min(220 / cell.width, 220 / cell.height)
    size = (max(1, round(cell.width * scale)), max(1, round(cell.height * scale)))
    cell = cell.resize(size, Image.Resampling.NEAREST)
    result = Image.new("RGBA", (256, 256), (0, 0, 0, 0))
    result.alpha_composite(cell, ((256 - size[0]) // 2, (256 - size[1]) // 2))
    return result


def split(source: Path, columns: int, rows: int, prefix: str) -> None:
    sheet = Image.open(source)
    width, height = sheet.size
    for index in range(columns * rows):
        col, row = index % columns, index // columns
        box = (
            round(col * width / columns), round(row * height / rows),
            round((col + 1) * width / columns), round((row + 1) * height / rows),
        )
        normalize(sheet.crop(box)).save(OUT / f"{prefix}_{index + 1}.png", optimize=True)


def main() -> None:
    OUT.mkdir(parents=True, exist_ok=True)
    split(HELD, 5, 1, "cat_held")
    split(FALL, 4, 2, "cat_fall")
    normalize(Image.open(PETTED)).save(OUT / "cat_petted.png", optimize=True)


if __name__ == "__main__":
    main()
