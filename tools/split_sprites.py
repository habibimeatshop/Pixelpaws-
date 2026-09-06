from collections import deque
from pathlib import Path
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT.parent / "generated_images" / "exec-08ec10da-8ace-4972-ad16-1d729a0cee82.png"
OUT = ROOT / "app" / "src" / "main" / "res" / "drawable-nodpi"
NAMES = [
    "cat_idle_1", "cat_idle_2", "cat_idle_3", "cat_idle_4",
    "cat_walk_1", "cat_walk_2", "cat_run_1", "cat_run_2",
    "cat_ball_1", "cat_ball_2", "cat_hungry", "cat_eat",
    "cat_affection", "cat_petted", "cat_sleep_1", "cat_sleep_2",
]


def remove_connected_checker(image: Image.Image) -> Image.Image:
    rgba = image.convert("RGBA")
    px = rgba.load()
    width, height = rgba.size
    seen = bytearray(width * height)
    queue = deque()

    def background(x: int, y: int) -> bool:
        r, g, b, _ = px[x, y]
        return max(r, g, b) - min(r, g, b) <= 18 and min(r, g, b) >= 135

    for x in range(width):
        if background(x, 0): queue.append((x, 0))
        if background(x, height - 1): queue.append((x, height - 1))
    for y in range(height):
        if background(0, y): queue.append((0, y))
        if background(width - 1, y): queue.append((width - 1, y))

    while queue:
        x, y = queue.popleft()
        index = y * width + x
        if seen[index] or not background(x, y):
            continue
        seen[index] = 1
        px[x, y] = (0, 0, 0, 0)
        for nx, ny in ((x-1,y),(x+1,y),(x,y-1),(x,y+1),(x-1,y-1),(x+1,y-1),(x-1,y+1),(x+1,y+1)):
            if 0 <= nx < width and 0 <= ny < height and not seen[ny * width + nx]:
                queue.append((nx, ny))
    return rgba


def main() -> None:
    OUT.mkdir(parents=True, exist_ok=True)
    sheet = remove_connected_checker(Image.open(SOURCE))
    width, height = sheet.size
    for index, name in enumerate(NAMES):
        col, row = index % 4, index // 4
        left, right = round(col * width / 4), round((col + 1) * width / 4)
        top, bottom = round(row * height / 4), round((row + 1) * height / 4)
        cell = sheet.crop((left, top, right, bottom))
        cell = cell.resize((256, 256), Image.Resampling.NEAREST)
        cell.save(OUT / f"{name}.png", optimize=True)

    sheet.save(ROOT / "sprite-sheet-v0.3-transparent.png", optimize=True)


if __name__ == "__main__":
    main()
