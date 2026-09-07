from pathlib import Path
from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT.parent / "generated_images" / "exec-8d85a6ae-4175-4be8-849d-4cc677642b6d.png"
OUT = ROOT / "app" / "src" / "main" / "res" / "drawable-nodpi"
NAMES = ("cat_run_1", "cat_run_2", "cat_ball_1", "cat_ball_2")


def main() -> None:
    sheet = Image.open(SOURCE).convert("RGBA")
    width, height = sheet.size
    for index, name in enumerate(NAMES):
        col, row = index % 2, index // 2
        bounds = (
            round(col * width / 2),
            round(row * height / 2),
            round((col + 1) * width / 2),
            round((row + 1) * height / 2),
        )
        cell = sheet.crop(bounds).resize((220, 220), Image.Resampling.NEAREST)
        padded = Image.new("RGBA", (256, 256), (0, 0, 0, 0))
        padded.alpha_composite(cell, (18, 18))
        padded.save(OUT / f"{name}.png", optimize=True)

    sheet.save(ROOT / "motion-sprites-v0.3.3-transparent.png", optimize=True)


if __name__ == "__main__":
    main()
