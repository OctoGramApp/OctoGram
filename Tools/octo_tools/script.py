import os
import re
import shutil

from PIL import Image

DENSITY_MAP = {
    64: "drawable-mdpi",
    96: "drawable-hdpi",
    128: "drawable-xhdpi",
    192: "drawable-xxhdpi",
    256: "drawable-xxxhdpi"
}

INPUT_FOLDER = "/home/octogram/assets/generated"
OUTPUT_FOLDER = "/home/octogram/OctoGram/TMessagesProj/src/main/res"


def clean_filename(name):
    if "OctoGram" in name:
        return "ic_unsized_octo.png".lower()
    return re.sub(r"(@\d+x|-?\d+)(?=\..+)", "", name).lower()


def get_closest_density(width):
    return DENSITY_MAP[min(DENSITY_MAP.keys(), key=lambda x: abs(x - width))]


for folder in DENSITY_MAP.values():
    os.makedirs(os.path.join(OUTPUT_FOLDER, folder), exist_ok=True)

for filename in os.listdir(INPUT_FOLDER):
    if filename.lower().endswith((".png", ".jpg", ".jpeg")):
        filepath = os.path.join(INPUT_FOLDER, filename)
        clean_name = clean_filename(filename)

        with Image.open(filepath) as img:
            folder_name = get_closest_density(img.width)
            dest_folder = os.path.join(OUTPUT_FOLDER, folder_name)
            new_filepath = os.path.join(dest_folder, clean_name)

            shutil.copy(filepath, new_filepath)
            print(f"✅ {filename} ({img.width}px) → {folder_name} (renamed to {clean_name})")

print("🎉 Organization completed!")
