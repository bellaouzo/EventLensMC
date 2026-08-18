#!/usr/bin/env python3
"""Mechanical Minecraft 26.2 client API renames for EventLens loader mods."""
from __future__ import annotations

import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
DIRS = [
    ROOT / "eventlens-neoforge" / "src",
    ROOT / "eventlens-forge" / "src",
    ROOT / "eventlens-fabric" / "src",
]

REPLACEMENTS = [
    ("import net.minecraft.resources.ResourceLocation;", "import net.minecraft.resources.Identifier;"),
    ("ResourceLocation", "Identifier"),
    (".gui.getScreen()", ".gui.screen()"),
    (".gui.getChat()", ".gui.hud.getChat()"),
    ("minecraft.getToasts()", "minecraft.gui.toastManager()"),
    (".getGameProfile().getName()", ".getName().getString()"),
    (".dimension().location()", ".dimension().identifier()"),
    ("renderListBackground", "extractListBackground"),
    ("renderListSeparators", "extractListSeparators"),
    ("renderBackground", "extractBackground"),
    ("renderComponentTooltip", "setComponentTooltipForNextFrame"),
    (".getInventory().selected", ".getInventory().getSelectedSlot()"),
    (".getPos().x", ".getPos().x()"),
    (".getPos().z", ".getPos().z()"),
    ("public boolean isPausing()", "public boolean isPauseScreen()"),
    ("protected void renderSelection(", "protected void extractSelection("),
]

METHOD_RENDER_TO_EXTRACT = re.compile(
    r"(@Override\s+)?public void render\(\s*GuiGraphicsExtractor graphics,\s*"
    r"int mouseX,\s*int mouseY,\s*float partialTick\)"
)


def patch_file(path: Path) -> bool:
    text = path.read_text(encoding="utf-8")
    original = text
    for old, new in REPLACEMENTS:
        text = text.replace(old, new)
    if "extends Screen" in text or "class EventLensScreen" in text:
        text = text.replace("public void render(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick)", "public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick)")
        text = text.replace("super.render(graphics, mouseX, mouseY, partialTick)", "super.extractRenderState(graphics, mouseX, mouseY, partialTick)")
    changed = text != original
    if changed:
        path.write_text(text, encoding="utf-8")
    return changed


def main() -> None:
    count = 0
    for base in DIRS:
        if not base.exists():
            continue
        for path in base.rglob("*.java"):
            if patch_file(path):
                count += 1
                print(f"patched {path.relative_to(ROOT)}")
    print(f"done: {count} files")


if __name__ == "__main__":
    main()
