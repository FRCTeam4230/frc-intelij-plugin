## Installation

This release is for **IntelliJ IDEA {{IDE_VERSION}}** (builds {{SINCE_BUILD}} to {{UNTIL_BUILD}}). Download **`{{ZIP_NAME}}`** from the *Assets* below. Do not unzip it for Option 1.

### Option 1: Install from disk (recommended)

1. In IntelliJ IDEA, open **Settings** (<kbd>Ctrl</kbd>+<kbd>Alt</kbd>+<kbd>S</kbd>, or **IntelliJ IDEA ▸ Settings** on macOS) ▸ **Plugins**.
2. Click the **⚙** (gear) icon at the top of the Plugins page and choose **Install Plugin from Disk…**
3. Select the downloaded `{{ZIP_NAME}}` file and click **OK**.
4. Click **Restart IDE** when prompted.

### Option 2: Copy into the plugins directory

1. Close IntelliJ IDEA.
2. Unzip `{{ZIP_NAME}}`. It contains a single `FRC` folder.
3. Move the `FRC` folder into your IntelliJ IDEA plugins directory, replacing any existing `FRC` folder:

   | OS      | Plugins directory                                                   |
   |---------|---------------------------------------------------------------------|
   | Windows | `%APPDATA%\JetBrains\IntelliJIdea{{IDE_VERSION}}\plugins`             |
   | macOS   | `~/Library/Application Support/JetBrains/IntelliJIdea{{IDE_VERSION}}/plugins` |
   | Linux   | `~/.local/share/JetBrains/IntelliJIdea{{IDE_VERSION}}/plugins`        |

   If the folder is not there (for example, with a custom configuration location), use **Help ▸ Show Log in Explorer/Finder** to find it: go up one level from the `log` folder and into `plugins`.
4. Start IntelliJ IDEA.

### Updating

Install the new release the same way; it replaces the previous version. If you previously installed the original FRC plugin from the JetBrains Marketplace, uninstall it first (**Settings ▸ Plugins ▸ Installed ▸ FRC ▸ Uninstall**).

### Verifying the download (optional)

The `{{ZIP_NAME}}.sha256` asset contains the SHA-256 checksum of the zip file.
