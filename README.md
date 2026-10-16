# SpacemiT OEMConfig

The EMM‑facing app. It carries the managed‑configuration schema an EMM reads to
build its admin UI, and it applies the values the EMM pushes by calling the
SpacemiT policy service through the SDK.

## How it works

1. `res/xml/app_restrictions.xml` is the schema (declared via the
   `android.content.APP_RESTRICTIONS` meta‑data). An EMM reads it, the admin sets
   values, and the device owner pushes them with
   `DevicePolicyManager.setApplicationRestrictions("com.spacemit.oemconfig", …)`.
2. `RestrictionsReceiver` fires on `APPLICATION_RESTRICTIONS_CHANGED` and
   `BOOT_COMPLETED` (both protected system broadcasts, so the receiver is
   `exported="false"`).
3. `OemConfigApplyService` reads the config via `RestrictionsManager`, binds the
   policy service, applies each key, then unbinds.

## Scope of the schema

Only **SpacemiT/board‑specific** keys live here (`kiosk_mode`, `fake_camera`,
`adb_enabled`). Standard toggles (camera, mic, Bluetooth, NFC, USB data) are set
by the device owner directly through `DevicePolicyManager` user restrictions, so
they are intentionally absent.

## Build

Opt‑in with `SPACEMIT_DEVICE_POLICY=true`. Platform‑signed + privileged, so it
holds `MANAGE_DEVICE_POLICY` by signature. Installs to
`/system/priv-app/SpacemitOemConfig`.

## Try it with a test DPC

```
adb install TestDPC.apk
adb shell dpm set-device-owner com.afwsamples.testdpc/.DeviceAdminReceiver
# In TestDPC: Manage app restrictions → com.spacemit.oemconfig → set kiosk_mode = true
adb shell settings get global spacemit_policy_kiosk_mode   # -> 1
```
