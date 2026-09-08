from pathlib import Path
from PIL import Image
from split_sprites import remove_connected_checker

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT.parent / "generated_images" / "exec-0a26239e-fa4a-416d-80a1-28f5792b55d1.png"
OUT = ROOT / "app" / "src" / "main" / "res" / "drawable-nodpi"
NAMES = (
    "cat_walk_1", "cat_walk_2", "cat_walk_3", "cat_walk_4",
    "cat_run_1", "cat_run_2", "cat_run_3", "cat_run_4",
)


def main() -> None:
    sheet = remove_connected_checker(Image.open(SOURCE))
    width, height = sheet.size
    for index, name in enumerate(NAMES):
        col, row = index % 4, index // 4
        bounds = (
            round(col * width / 4),
            round(row * height / 2),
            round((col + 1) * width / 4),
            round((row + 1) * height / 2),
        )
        cell = sheet.crop(bounds).resize((220, 220), Image.Resampling.NEAREST)
        padded = Image.new("RGBA", (256, 256), (0, 0, 0, 0))
        padded.alpha_composite(cell, (18, 18))
        padded.save(OUT / f"{name}.png", optimize=True)
    sheet.save(ROOT / "locomotion-sprites-v0.4-transparent.png", optimize=True)


if __name__ == "__main__":
    main()
