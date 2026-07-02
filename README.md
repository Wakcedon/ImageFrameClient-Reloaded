# ImageFrame Client Reloaded

https://modrinth.com/mod/imageframeclient-reloaded

A NeoForge client-side mod complementary to servers running the [ImageFrame](https://github.com/LOOHP/ImageFrame) plugin. Originally created by LOOHP for Fabric, this fork ports the mod to **NeoForge 1.21.1** with additional features and ongoing development.

> **The server must have the [ImageFrame](https://www.spigotmc.org/resources/106031/) plugin installed.** This mod works on both client and server — the client displays HD images and provides a management GUI, while the optional server component stores uploaded images for use with ImageFrame.

---

## Features

### HD Native Resolution Map Rendering
When the server sends full-color, high-resolution map data, the mod replaces the vanilla 128×128, 16-color map texture with the actual image at its native resolution. Works in item frames, on map walls, in hands — everywhere maps are rendered.

### Inventory Tooltip Previews
- **Filled Maps** — See a live 64×64 preview of any filled map in your inventory.
- **Image Maps** — For ImageFrame's multi-tile image items (paper with `CombinedImageMap` data), renders the full grid of map tiles in the tooltip at the correct scale.
- **Paintings** — Preview a painting's artwork before placing it.

### Image Manager GUI (`/ifc`)
Opens a graphical interface for managing server images directly in-game:
- **Browse** — View all uploaded images with names and file sizes.
- **Upload** — Select a local PNG/JPG file via system file dialog, choose tile dimensions (width × height in map tiles), and send it to the server.
- **Delete** — Remove images from the server.
- **Refresh** — Reload the image list from the server.

The server component stores uploaded images in `<server>/imageframeclient/images/` and optionally syncs them with the ImageFrame plugin's `plugins/ImageFrame/images/` directory for seamless integration.

### Disk Cache
Downloaded HD map textures are cached to disk in `<gameDir>/imageframeclient/cache/`. Cache is automatically used on reconnection — just delete the folder to clear it.

### High-Quality Image Scaling
When images exceed the configured maximum resolution, bicubic interpolation (anti-aliased) is used instead of nearest-neighbor, producing significantly smoother downscaled results.

### Multi-Language Support
Fully translatable interface. Ships with English and Russian translations. Easily add your own language by creating a `lang/<code>.json` file.

### Server Support Notification
When joining a server with ImageFrame installed, a toast notification confirms HD images are available.

### Configurable Performance
Limit the maximum HD image resolution (128–4096px or native) to balance visual quality against GPU memory usage. Toggle map/painting previews and the server notification individually.

---

## Commands

| Command | Description | Side |
|---|---|---|
| `/ifc` | Opens the ImageFrame Manager GUI (upload, browse, delete images) | Client |

---

## Configuration

All options are client-side and accessible via the NeoForge mod settings screen or `config/imageframeclient-client.toml`:

| Option | Default | Description |
|---|---|---|
| `useNativeResMapImages` | `true` | Enable HD full-color map images from the server |
| `maxImageSize` | `NATIVE` | Maximum resolution cap (128, 256, 512, 1024, 2048, 4096, or NATIVE) |
| `previewMapsInTooltip` | `true` | Show map previews in inventory tooltips |
| `previewPaintingsInTooltip` | `true` | Show painting previews in inventory tooltips |
| `notifyWhenServerSupports` | `true` | Show a toast on servers with ImageFrame |

---

## Building from Source

```bash
./gradlew build
```

The compiled JAR will be in `build/libs/`.

---

## How It Works

This mod communicates with the ImageFrame server plugin via NeoForge custom payloads:

- **Server → Client**: HD image data, multipart transfers, image map details, update signals
- **Client → Server**: HD image requests, image map detail requests, image management (list/upload/delete)

The management protocol allows the client GUI to list, upload, and delete images on the server, storing them in a format compatible with the ImageFrame plugin.

---

## License

GPL-3.0. Originally by LOOHP, now maintained by Wakcedon.
