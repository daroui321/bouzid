# Bouzid – IPTV Player

Professional IPTV player for Android. Users enter their own playlist URL — no hardcoded URLs.

## Build yourself (free, no paid tools required)

### Option 1: GitHub Actions (easiest)

1. Create a GitHub repo and push this code:
   ```bash
   cd Bouzid
   git init
   git add .
   git commit -m "Initial commit"
   git remote add origin https://github.com/YOUR_USERNAME/bouzid.git
   git push -u origin main
   ```

2. Go to **Actions** tab on GitHub → the workflow will automatically start

3. When done, download the APK from the **Summary** page under **Artifacts**

### Option 2: Android Studio

1. Open Android Studio → **File > Open** → select the `Bouzid` folder
2. Wait for Gradle sync
3. Click **Build > Build Bundle(s) / APK(s) > Build APK(s)**

## How it works

1. **First launch** → user pastes their IPTV M3U URL
2. **Activation check** (optional) → if your server has `config.json` with `{"activation": {"enabled": true}}`, it asks for email before proceeding
3. **Player** → channel list + video playback

Users can change their URL anytime via the Settings button in the player.

## Activation setup (optional)

1. Upload `server/config.json` and `server/activate.php` to your host (e.g. `https://yourhost.com/config.json`)
2. Edit `app/src/main/java/com/bouzid/player/data/Config.kt` and change `yourhost.com` to your actual domain:

```kotlin
const val ACTIVATION_CONFIG_URL = "https://yourhost.com/config.json"   // ← change domain
const val ACTIVATION_POST_URL = "https://yourhost.com/activate.php"    // ← change domain
```

**`config.json`** — set `"enabled": true` to turn activation on, `false` to skip it.
**`activate.php`** — saves submitted emails to `emails.txt` on your server.

If the config URL is unreachable or returns `enabled: false`, the app skips activation entirely.

## Requirements

- Android 5.0+ (API 21)
- Internet permission (auto-granted)
