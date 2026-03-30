# 🛡️ KatIPAuth (KIA)

[![Download on Modrinth](https://img.shields.io/modrinth/dt/katipauth?logo=modrinth)](https://modrinth.com/plugin/katipauth) [![GitHub](https://img.shields.io/github/v/release/oiupoyt/katIPAuth)](https://github.com/oiupoyt/katIPAuth)

**KatIPAuth** is an optimized plugin for **Minecraft 1.19 to 1.21.x** that locks accounts to IPs. 
It provides account security for cracked servers without the need for complex authentication plugins.  
No passwords. No sessions. Just **IP says yes or go home**.

## 🚀 Key Features

### 👤 UUID-IP Identity Locking
- **Dual Binding**: Binds both player UUID and IP on first join for maximum security.
- **Bidirectional Verification**: UUID can only use its bound IP, and each IP can only be used by its bound UUID.
- **Multi-Account Support**: Optional multi-account mode allows multiple UUIDs on the same IP with configurable limits.
- **Login Block**: Automatically blocks access from mismatched UUID-IP combinations.
- **Specific Error Messages**: Clear, actionable kick messages that explain why access was denied.

### 📶 Subnet Locking (BETA)
- **Dynamic IP Support**: Solving dynamic IP issues by allowing connections within the same subnet.
- **Security Balance**: Provides flexibility for players whose IPs change within their ISP's range while still blocking unauthorized access from elsewhere.

### 📡 Discord Alerts & Logging
- **Real-time Discord Notifications**: Sends detailed embeds to your configured webhook with player info, stored ID, attempt details, and timestamps.
- **Console Logging**: All login attempts (success and failure) are logged to console and files for audit trails.
- **Complete Audit Trail**: Local log files with timestamps for compliance and troubleshooting.

### 🛠️ Maintenance & Utilities
- **Auto Config Update**: Automatically adds missing settings and comments to your `config.yml` on plugin updates.
- **H2 Database**: Persistent, reliable storage with automatic migrations from legacy formats.
- **Formatted Data**: View binding times in standard readable formats (`yyyy-MM-dd HH:mm:ss`).

---

## 🧾 Commands

### Player Commands
#### /ipstatus
- Shows if your currently used IP is bound.
- Shows exactly **when** it was bound in a readable format.

### Admin Commands (Permission: `ipauth.admin`)
#### /ipinfo <player>
- Displays the stored UUID, IP address, and original binding time for any player.
#### /ipreset <player>
- Resets a player's UUID-IP binding and kicks them instantly. Shows which UUID and IP were removed. The next IP they join with will be their new bound IP.
#### /ipforce <player>
- Removes a player's current UUID-IP binding. Unlike `/ipreset`, it does NOT kick the player; the next time they join, a new UUID-IP pair will be bound.
#### /ipreload
- Reloads the `config.yml` and re-initializes all settings (including Discord webhooks).

---

## ⚙️ Configuration

This is a sample of the `config.yml` with explanations for each section:

```yaml
# Discord Webhook URL for logging IP changes (optional)
discord:
  webhook: "PUT_WEBHOOK_URL_HERE"

# Privacy settings
privacy:
  mask-ips: false # If true, last two octets of IPs will be replaced with x (e.g. 192.168.xx.xx)

# Debug settings
debug:
  verbose: false # If true, print debug info in console for player joins, blocks, IP changes, etc.

# Security settings
security:
  allow-multiple-accounts: true # If true, multiple UUIDs can bind to the same IP.
  max-accounts-per-ip: 2 # Maximum number of accounts allowed per IP. Need allow-multiple-accounts=true for this to work.

# Customizable messages
messages:
  blocked: "&cLogin blocked. If you believe this is a mistake contact the owner" # Default fallback message
  uuid-ip-mismatch: "&cLogin blocked: UUID bound to different IP. Contact the owner if this is a mistake"
  ip-uuid-mismatch: "&cLogin blocked: IP already bound to another account"
  max-accounts: "&cLogin blocked: Maximum number of accounts reached for this IP"

# Beta features (Use with caution)
beta:
  subnet-locking: false # If true, players can join as long as they are in the same /24 subnet (e.g. 192.168.1.*)
```

### Security Settings Explained
- **allow-multiple-accounts**: When `true`, multiple players can use the same IP (useful for local networks). When `false`, each IP is locked to a single UUID.
- **max-accounts-per-ip**: When multi-account is enabled, limits the number of different UUIDs that can use the same IP.

---

## ❓ FAQ

### Q: How does the UUID-IP binding work?
- A: On first join, a player's UUID and IP are bound together. To access the server, a player must use both the same UUID (account) AND the same IP. This prevents account sharing across different IPs and IP sharing across different accounts (unless multi-account is enabled).

### Q: Can I allow multiple players on the same IP?
- A: Yes. Enable `security.allow-multiple-accounts: true` and set `security.max-accounts-per-ip` to your desired limit (default: 2). This is useful for local networks or shared internet connections.

### Q: Can dynamic IP players use this?
- A: Yes. If their IP changes within the same subnet, you can enable **Subnet Locking** in the beta settings.
*Reminder: Subnet Locking is a beta feature and may not be fully stable.*

### Q: Can VPNs bypass this?
- A: No. VPN users get blocked unless they join from an IP that matches their binding or the subnet.

### Q: Is this compatible with other auth plugins?
- A: Yes. It works independently before players even enter the server, making it a great second layer of security alongside plugins like AuthMe.

### Q: How do I see what's blocked and why?
- A: Check the console logs (filtered by [KatIPAuth]) or your local log files in `plugins/KatIPAuth/LOGS/`. Enable `debug.verbose: true` for detailed information. Discord webhook (if configured) will also send alerts.

### Q: How do I reset a player's binding?
- A: Ask an admin to use `/ipreset <player>` to reset their UUID-IP binding and kick them. They can then rejoin to bind new credentials. The reset command shows which UUID and IP were removed.

### Q: Where are logs stored?
- A: Local logs are stored in `plugins/KatIPAuth/LOGS/` with daily files named `yyyy-MM-dd.log`. Discord webhooks (if configured) provide real-time alerts.
