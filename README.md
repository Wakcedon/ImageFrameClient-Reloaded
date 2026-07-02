# 🗺️ ImageFrame Client Reloaded

**Turn your Minecraft maps into art.** This mod lets you see HD, full-color images on maps exactly as they were meant to look — no more blurry 16-color pixel art.

Play on a server with the [ImageFrame](https://github.com/LOOHP/ImageFrame) plugin? Every map, painting, and image wall will render in crisp native resolution with true colors. You can even upload your own pictures straight from the game using `/ifc`.

---

## ✨ What You Get

**🎨 HD Maps — For Real This Time**
Vanilla maps cap out at 128×128 and 16 colors. ImageFrame maps display in **full RGB** at the image's original resolution. Every pixel, every shade. Works in item frames, on map walls, in your hand — everywhere.

**🖼️ Tooltip Previews**
Hover over a filled map in your inventory — you'll see its actual content right in the tooltip. Same for ImageFrame multi-tile images and paintings.

**📂 Upload Your Own Images**
Open the Image Manager with `/ifc`, pick a PNG or JPG from your computer, choose how many map tiles wide and tall you want it, and send it straight to the server. No external tools needed.

**💾 Smart Caching**
Downloaded HD textures are saved to `imageframeclient/cache/` inside your game folder. Rejoin the server and they load instantly. Cache too big? Just delete the folder — it'll rebuild as needed.

**🌐 Server ↔ Client Sync**
The mod talks to the ImageFrame plugin through NeoForge custom payloads. When images update on the server, your client follows along automatically.

**⚙️ Tweak to Your Liking**
Set a max resolution cap (128–4096px or no limit) to save VRAM. Toggle map previews, disable the server notification — all in the mod's config screen.

---

## 🔧 Quick Start

1. Install the mod on your client (NeoForge 1.21.1)
2. Join a server that has the **ImageFrame** plugin installed
3. That's it — HD maps work automatically. Try `/ifc` to upload your own images.

---

## 📸 Screenshots

*(Coming soon — in the meantime, check out the screenshots on [Modrinth](https://modrinth.com/mod/imageframeclient-reloaded))*

---

## 🎯 Commands

| Command | What it does |
|---|---|
| `/ifc` | Open the Image Manager — browse, upload, and delete server images |

---

## ⚡ Performance

HD images can be memory-intensive on large map walls. Use the **Max Image Size** config option to cap resolution. The mod packs and scales images efficiently, and the disk cache means you're not re-downloading everything on every login.

---

## 📜 Credits

Originally by **LOOHP** for Fabric. Forked and ported to NeoForge by **Wakcedon**.

Built against [ImageFrame](https://github.com/LOOHP/ImageFrame) server plugin by LOOHP.
