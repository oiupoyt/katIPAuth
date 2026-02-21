# 🛡️ KatIPAuth (KIA)

[![Download on Modrinth](https://img.shields.io/modrinth/dt/katipauth?logo=modrinth)](https://modrinth.com/plugin/katipauth) [![GitHub](https://img.shields.io/github/v/release/oiupoyt/katIPAuth)](https://github.com/oiupoyt/katIPAuth)

**KatIPAuth** is a Paper plugin for **Minecraft 1.19 to 1.21.x** that locks accounts to IPs so little gremlins can’t just hack accounts and use them.  
No passwords. No auth plugins. Just **IP says yes or go home**.

Built for speed. Optimized. Async. Doesn’t freeze your server like half the plugins on Spigot.

## 📥 Downloads

- **Modrinth**: [Download from Modrinth](https://modrinth.com/plugin/katipauth)
- **GitHub Releases**: [Download from GitHub](https://github.com/oiupoyt/katIPAuth/releases)

---

## 🚀 What This Plugin Does (aka why this exists)

servers have one big issue:

> upon a account being hacked, its achievements are obtained unfairly

KatIPAuth fixes that by **binding each username to an IP address**.

### Core behavior
- First join → IP gets saved
- Next joins → must be same IP
- Different IP?
  - ❌ Login blocked **before they enter**
  - 📢 Discord webhook alert gets fired
  - 🧍 Player stays OUT. No spawn, no chunks, no funny business

---

## 🔐 Features

### 👤 Player Protection
- Automatic IP binding on first join
- Zero setup for players
- Login blocked instantly on mismatch

### 📡 Discord Alerts (cool embeds, not ugly spam)
Sends a clean embed with:
- Player username
- Stored IP
- Attempted IP
- Timestamp
- Server name  

So you can watch account theft attempts like a Netflix series.

### 🔄 Automatic Update Checking
- Checks for updates on startup from GitHub
- Sends a console message if a new version is available

### ⚡ Performance
- **Async disk I/O**
- **Async webhook requests**
- Never blocks the main thread
- Safe on restarts and reloads

---

## 🧾 Commands

### Player Commands

#### /ipstatus
- Shows if your IP is bound
- Shows **when** it was bound
- Does NOT leak your IP (privacy W)

---

### Admin Commands (OP only, no funny business)

#### /ipreset <player>
- Resets the player’s IP binding
- Instantly kicks the player
- Next login = new IP bound
#### /ipinfo <player>
- Shows stored IP
- Shows last bind/login time
#### /ipforce <player>
- Removes current IP binding and uses the ip from next join
- Does NOT kick the player
#### /ipreload
- Reloads config + Discord webhook
- No restart needed because we’re civilized

---

## ⚙️ Configuration

`plugins/KatIPAuth/config.yml`

```yaml
discord:
  webhook: "YOUR_DISCORD_WEBHOOK_URL"

privacy:
  mask-ips: false  # If true, last two octets of IPs will be replaced with x (e.g. 192.168.xx.xx)

messages:
  blocked: "&cLogin blocked: IP mismatch. Contact staff if this is wrong."
```
---

## ❓ FAQ

### Q: Can VPNs bypass this?
- A: VPN users get blocked unless they reset IP. That’s the point.

### Q: Can two people share one IP?
- A: Yes. Same IP ≠ same username. This is IP → username, not the other way around.

### Q: Is this better than AuthMe?
- A: Different goal. AuthMe = passwords.
- KatIPAuth = identity locking using yo ip.
- Use both if you’re paranoid lmao.

### Q: Does this plugin check for updates?
- A: Yes, it automatically checks GitHub for new versions on startup and logs a message if an update is available, directing you to Modrinth for downloads. This can be disabled in the config.
