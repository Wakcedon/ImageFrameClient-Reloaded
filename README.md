# 🗺️ ImageFrame Client Reloaded

**Turn your Minecraft maps into art.** This mod lets you see HD, full-color images on maps exactly as they were meant to look — no more blurry 16-color pixel art.

Play on a server with the [ImageFrame](https://github.com/LOOHP/ImageFrame) plugin? Every map, painting, and image wall will render in crisp native resolution with true colors. You can even upload your own pictures straight from the game using `/ifc`.

---

## ✨ What You Get

### HD Native Resolution Map Rendering
When the server sends full-color, high-resolution map data, the mod replaces the vanilla 128x128, 16-color map texture with the actual image at its native resolution. Works in item frames, on map walls, in hands — everywhere maps are rendered.

### GIF Animation Support
Animated GIFs sent by the server are detected automatically and rendered as animated textures in-game using Java's built-in GIF parser.

### Inventory Tooltip Previews
- **Filled Maps** — See a live 64x64 preview of any filled map in your inventory.
- **Image Maps** — For ImageFrame's multi-tile image items (paper with `CombinedImageMap` data), renders the full grid of map tiles in the tooltip at the correct scale.
- **Paintings** — Preview a painting's artwork before placing it.

### Image Manager GUI (`/ifc`)
Opens a graphical interface for managing server images directly in-game:
- **Browse** — View all uploaded images with names and file sizes.
- **Upload** — Select a local PNG/JPG/GIF file via system file dialog, choose tile dimensions (width x height in map tiles), and send it to the server.
- **Delete** — Remove images from the server.
- **Refresh** — Reload the image list from the server.
- **Transform** — Rotate 90, flip horizontally/vertically before uploading.

### Advanced Disk Cache
Downloaded HD map textures are cached to disk in `<gameDir>/imageframeclient/cache/`.
- **TTL-based expiration** — Old cache files are automatically cleaned up (configurable, default 7 days).
- **Size limit** — Cache stops growing past a configurable limit (default 100 MB), deleting oldest files first.
- Background cleanup runs periodically to keep things tidy.

### High-Quality Image Scaling
When images exceed the configured maximum resolution, bicubic interpolation (anti-aliased) is used instead of nearest-neighbor, producing significantly smoother downscaled results.

### Per-Server Configuration
Override mod settings per server — config changes on one server don't affect others. Overrides are stored in `<gameDir>/imageframeclient/server_overrides.json`.

### Server ↔ Client Sync
The mod talks to the ImageFrame plugin through NeoForge custom payloads. When images update on the server, your client follows along automatically.

### Multi-Language Support
Fully translatable interface. Ships with English and Russian translations. Easily add your own language by creating a `lang/<code>.json` file.

### Configurable Performance
Limit the maximum HD image resolution (128-4096px or native) to balance visual quality against GPU memory usage. Toggle map/painting previews and the server notification individually. Fine-tune cache TTL and size limits.

---

## 🔧 Quick Start

1. Install the mod on your client (NeoForge 1.21.1)
2. Join a server that has the **ImageFrame** plugin installed
3. That's it — HD maps work automatically. Try `/ifc` to upload your own images.

---

## 📸 Screenshots

All options are client-side and accessible via the NeoForge mod settings screen or `config/imageframeclient-client.toml`:

### General

| Option | Default | Description |
|---|---|---|
| `useNativeResMapImages` | `true` | Enable HD full-color map images from the server |
| `maxImageSize` | `NATIVE` | Maximum resolution cap (128, 256, 512, 1024, 2048, 4096, or NATIVE) |
| `previewMapsInTooltip` | `true` | Show map previews in inventory tooltips |
| `previewPaintingsInTooltip` | `true` | Show painting previews in inventory tooltips |
| `notifyWhenServerSupports` | `true` | Show a toast on servers with ImageFrame |

### Cache

| Option | Default | Description |
|---|---|---|
| `cacheTtlDays` | `7` | Maximum days to keep cached HD textures (0 = forever) |
| `cacheMaxMb` | `100` | Maximum cache size in MB (0 = unlimited) |

---

## 🎯 Commands

| Command | What it does |
|---|---|
| `/ifc` | Open the Image Manager — browse, upload, and delete server images |

---

## ⚡ Performance

HD images can be memory-intensive on large map walls. Use the **Max Image Size** config option to cap resolution. The mod packs and scales images efficiently, and the disk cache means you're not re-downloading everything on every login.

This mod communicates with the ImageFrame server plugin via NeoForge custom payloads:

- **Server -> Client**: HD image data, multipart transfers, image map details, update signals
- **Client -> Server**: HD image requests, image map detail requests, image management (list/upload/delete)

The management protocol allows the client GUI to list, upload, and delete images on the server, storing them in a format compatible with the ImageFrame plugin.

GIF images are detected by the client and animated via a client tick handler — no server-side changes needed.

---

## 📜 Credits

Originally by **LOOHP** for Fabric. Forked and ported to NeoForge by **Wakcedon**.

Built against [ImageFrame](https://github.com/LOOHP/ImageFrame) server plugin by LOOHP.
