# 🛡️ KatIPAuth (KIA)

> cracked server problems require cracked server solutions

**KatIPAuth** is a Paper plugin for **Minecraft 1.21.x** that locks accounts to IPs so little gremlins can’t just change usernames and steal accounts.  
No passwords. No auth plugins. Just **IP says yes or go home**.

Built for cracked servers. Optimized. Async. Doesn’t freeze your server like half the plugins on Spigot.

---

## 🚀 What This Plugin Does (aka why this exists)

Cracked servers have one big issue:

> usernames are free real estate

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

### ⚡ Performance
- **Async disk I/O**
- **Async webhook requests**
- Never blocks the main thread
- Safe on restarts and reloads

---

## 🧾 Commands

### Player Commands

#### /ipreset
- Resets your IP binding
- Instantly kicks you
- Next login = new IP bound
#### /ipstatus
- Shows if your IP is bound
- Shows **when** it was bound
- Does NOT leak your IP (privacy W)

---

### Admin Commands (OP only, no funny business)

#### /ipinfo <player>
- Shows stored IP
- Shows last bind/login time
#### /ipforce <player>
- Removes IP binding
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

messages:
  blocked: "&cLogin blocked: IP mismatch. Contact staff if this is wrong."
```
---

## ❓ FAQ

### Q: Can VPNs bypass this?
### A: VPN users get blocked unless they reset IP. That’s the point.

### Q: Can two people share one IP?
### A: Yes. Same IP ≠ same username. This is IP → username, not the other way around.

### Q: Is this better than AuthMe?
### A: Different goal. AuthMe = passwords.
### KatIPAuth = identity locking.
### Use both if you’re paranoid lmao.
