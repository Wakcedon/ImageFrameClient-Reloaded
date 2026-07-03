# ImageFrame Client Reloaded

https://modrinth.com/mod/imageframeclient-reloaded

A NeoForge client-side mod complementary to servers running the [ImageFrame](https://github.com/LOOHP/ImageFrame) plugin. Originally created by LOOHP for Fabric, this fork ports the mod to **NeoForge 1.21.1** with additional features and ongoing development.

> **The server must have the [ImageFrame](https://www.spigotmc.org/resources/106031/) plugin installed.** This mod works on both client and server — the client displays HD images and provides a management GUI, while the optional server component stores uploaded images for use with ImageFrame.

---

## Features

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

The server component stores uploaded images in `<server>/imageframeclient/images/` and optionally syncs them with the ImageFrame plugin's `plugins/ImageFrame/images/` directory for seamless integration.

### Advanced Disk Cache
Downloaded HD map textures are cached to disk in `<gameDir>/imageframeclient/cache/`.
- **TTL-based expiration** — Old cache files are automatically cleaned up (configurable, default 7 days).
- **Size limit** — Cache stops growing past a configurable limit (default 100 MB), deleting oldest files first.
- Background cleanup runs periodically to keep things tidy.

### High-Quality Image Scaling
When images exceed the configured maximum resolution, bicubic interpolation (anti-aliased) is used instead of nearest-neighbor, producing significantly smoother downscaled results.

### Per-Server Configuration
Override mod settings per server — config changes on one server don't affect others. Overrides are stored in `<gameDir>/imageframeclient/server_overrides.json`.

### Multi-Language Support
Fully translatable interface. Ships with English and Russian translations. Easily add your own language by creating a `lang/<code>.json` file.

### Server Support Notification
When joining a server with ImageFrame installed, a toast notification confirms HD images are available.

### Configurable Performance
Limit the maximum HD image resolution (128-4096px or native) to balance visual quality against GPU memory usage. Toggle map/painting previews and the server notification individually. Fine-tune cache TTL and size limits.

---

## Commands

| Command | Description | Side |
|---|---|---|
| `/ifc` | Opens the ImageFrame Manager GUI (upload, browse, delete images) | Client |

---

## Configuration

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

## Building from Source

```bash
./gradlew build
```

The compiled JAR will be in `build/libs/`.

---

## How It Works

This mod communicates with the ImageFrame server plugin via NeoForge custom payloads:

- **Server -> Client**: HD image data, multipart transfers, image map details, update signals
- **Client -> Server**: HD image requests, image map detail requests, image management (list/upload/delete)

The management protocol allows the client GUI to list, upload, and delete images on the server, storing them in a format compatible with the ImageFrame plugin.

GIF images are detected by the client and animated via a client tick handler — no server-side changes needed.

---

## License

GPL-3.0. Originally by LOOHP, now maintained by Wakcedon.
