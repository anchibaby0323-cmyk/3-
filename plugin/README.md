# MobilePilot ChatGPT 外掛

此外掛讓 ChatGPT 對話呼叫 `/v1/commands`，再由背景推送把指令送到 Android App。使用 OAuth／Google 帳號識別，不要求使用者複製固定金鑰。

目前原始碼故意不包含任何 Firebase、Google 或 Cloudflare 私密憑證。設定完成前，端點會回傳 `configuration_required`，避免看起來成功但實際沒有控制手機。
