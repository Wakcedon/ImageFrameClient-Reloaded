# ImageFrame Client Reloaded

https://modrinth.com/mod/imageframeclient-reloaded

A NeoForge client-side mod complementary to servers running the [ImageFrame](https://github.com/LOOHP/ImageFrame) plugin. Originally created by LOOHP for Fabric, this fork ports the mod to **NeoForge 1.21.1** with additional improvements and ongoing development.

> **The server must have the [ImageFrame](https://www.spigotmc.org/resources/106031/) plugin installed.** This mod is client-side only — it enhances what you see without changing gameplay logic.

---

## Features

### HD Native Resolution Map Rendering
When the server sends full-color, high-resolution map data, the mod replaces the vanilla 128×128, 16-color map texture with the actual image at its native resolution. Works in item frames, on map walls, in hands — everywhere maps are rendered.

### Inventory Tooltip Previews
- **Filled Maps** — See a live 64×64 preview of any filled map in your inventory.
- **Image Maps** — For ImageFrame's multi-tile image items (paper with `CombinedImageMap` data), renders the full grid of map tiles in the tooltip at the correct scale.
- **Paintings** — Preview a painting's artwork before placing it.

### Server Support Notification
When joining a server with ImageFrame installed, a toast notification confirms HD images are available.

### Configurable Performance
Limit the maximum HD image resolution (128–4096px or native) to balance visual quality against GPU memory usage. Toggle map/painting previews and the server notification individually.

---

## Difference from the Original

This is a port of LOOHP's [ImageFrame Client](https://modrinth.com/mod/imageframeclient) (originally Fabric/Quilt) to **NeoForge**. Beyond the platform migration, this fork aims to expand the client-server integration with more features over time (see proposed improvements below).

---

## Downloads

| Platform | Link |
|---|---|
| Modrinth | https://modrinth.com/mod/imageframeclient-reloaded |
| GitHub Releases | *(coming soon)* |

---

## Building from Source

```bash
./gradlew build
```

The compiled JAR will be in `build/libs/`.

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

## License

GPL-3.0. Originally by LOOHP, now maintained by Wakcedon.
