# MobilePilot 2.0

Android 16 / POCO X8 Pro 優先的免 Root 手機執行代理原型。

## 第一版已完成

- 本機權限中心：無障礙、Shizuku、麥克風、SAF 資料夾授權
- 本機指令路由：能直接開 App、返回、回首頁、依文字點擊
- 「嘿 Pilot」語音觸發原型（使用 Android SpeechRecognizer）
- 前景服務狀態通知與失敗後自動重啟辨識
- GitHub Actions 一鍵產生 APK
- 移除舊版 Cloudflare Worker、固定配對金鑰與會立即過期的 action token
- 加入與 ChatGPT 外掛相配的 FCM 背景接收服務及防重複執行機制
- 外掛採 OAuth／Google 帳號登入設計，使用者不需複製固定配對金鑰

## 安全設計

- 不上傳或長期保存畫面與對話內容
- 敏感權限必須由使用者在 Android 系統介面明確開啟
- 不嘗試繞過鎖定畫面、家長監護、付款確認或 Android 安全限制
- 網路明文流量停用；此原型不包含遠端控制入口

## 編譯

把整個資料夾推到 GitHub 的 `main` 分支，進入 Actions 執行 **Build MobilePilot APK**，完成後下載 `MobilePilot-2.0-debug`。

## 目前限制

Android 的一般 App 不能保證在所有狀態下永久監聽麥克風；HyperOS 省電、重開機後限制、鎖定畫面及其他 App 佔用麥克風都可能中斷辨識。真正低耗電且可靠的自訂喚醒詞，下一階段應改用裝置端喚醒詞模型。

ChatGPT 雲端無法直接連入手機的私人網路，因此配套外掛包含 Cloudflare Worker API 與 Firebase 推送骨架。部署前仍需建立 Firebase 專案並在 Cloudflare 設定私密服務憑證；任何私密憑證都不可提交到 GitHub。
