# 電池資訊 (BatteryLens)

POCO X8 Pro 上的一鍵電池資訊讀取 App，繁體中文介面。Shizuku UserService 僅讀取 `/sys/class/power_supply/battery` 與 `bms` 中列明的節點，不執行修改指令、不需 Root、不連網。Android 一般 API 提供電量、溫度、電壓及有提供時的循環數。

**健康度顯示原則：**只在驅動回報 1–110 的 `soh` / `state_of_health` 時顯示百分比；缺值顯示「未提供有效 SOH」。FCC、設計容量、Qmax 原值分開顯示，絕不以 `charge_full / charge_full_design` 冒充健康度。

## 手機安裝

1. 到 GitHub Actions 的「Build BatteryLens APK」工作流程下載 `BatteryLens-debug` 成品，解壓縮取得 APK。
2. 在手機啟動 Shizuku；打開「電池資訊」，按「重新讀取」，授予 Shizuku 權限。
3. 如果顯示「未提供有效 SOH」，代表這台韌體沒有把可信的 SOH 節點開放給 ADB shell，無法由 Shizuku 憑空測得精確數字。

專案以 Android Gradle Plugin 8.9.2、JDK 17、Android SDK 35 編譯。`cd battery-health && gradle :app:assembleDebug`。
