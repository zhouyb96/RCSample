# HEQ RemoteController SDK API

This document is for app developers who integrate `heq.v1.impl.remotecontroller` as an AAR.

## 1. Module Overview

`sdk-impl-remotecontroller` provides:

- `SceptreRemoteControllerTransport`
  - RemoteController + AirLink transport implementation
  - websocket protocol endpoint: `ws://127.0.0.1:8765`
- `RemoteControllerSdkBootstrap`
  - standalone installer entry for external apps
  - installs:
    - `ComponentType.REMOTE_CONTROLLER`
    - `ComponentType.AIRLINK`

## 2. Build AAR

Run in repository root:

```bash
./gradlew :sdk-core:assembleRelease :sdk-impl-remotecontroller:assembleRelease
```

Windows PowerShell:

```powershell
.\gradlew.bat :sdk-core:assembleRelease :sdk-impl-remotecontroller:assembleRelease
```

Expected outputs:

- `sdk-core/build/outputs/aar/sdk-core-release.aar`
- `sdk-impl-remotecontroller/build/outputs/aar/sdk-impl-remotecontroller-release.aar`

External apps need both AARs.

## 3. External App Integration

### 3.1 Add AAR

Place the 2 AAR files into consumer app local libs path, for example:

- `app/libs/sdk-core-release.aar`
- `app/libs/sdk-impl-remotecontroller-release.aar`

Gradle example:

```kotlin
dependencies {
    implementation(files("libs/sdk-core-release.aar"))
    implementation(files("libs/sdk-impl-remotecontroller-release.aar"))
}
```

### 3.2 SDK init + bootstrap

```kotlin
import heq.v1.impl.remotecontroller.RemoteControllerSdkBootstrap

// after SDKManager init/register
RemoteControllerSdkBootstrap.installStandalone(productId = 20_001)
```

## 4. Implemented Keys

The implementation is strict capability-based:
- keys not listed below return `KEY_NOT_SUPPORTED`.

### 4.1 RemoteController keys

- `RemoteControllerKey.KeyConnection` (GET)
- `RemoteControllerKey.KeyControlMode` (GET/SET)
- `RemoteControllerKey.KeyFirmwareVersion` (GET)
- `RemoteControllerKey.KeyPairingStatus` (GET)
- `RemoteControllerKey.KeyRequestPairing` (ACTION)
- `RemoteControllerKey.KeyStopPairing` (ACTION)

### 4.2 AirLink keys

- `AirLinkKey.KeyConnection` (GET)
- `AirLinkKey.KeySignalQuality` (GET/LISTEN)
- `AirLinkKey.KeyFrequencyBandRange` (GET)
- `AirLinkKey.KeyFrequencyBand` (GET/SET)
- `AirLinkKey.KeyChannelSelectionMode` (GET/SET)
- `AirLinkKey.KeyFrequencyPointRange` (GET)
- `AirLinkKey.KeyFrequencyPoint` (GET/SET)

## 5. Usage Examples

All examples below use HEQ SDK V1 wrapper (`heq.v1.et`) with DJI V5-style call patterns:

```kotlin
import heq.v1.et.create
```

### 5.1 Listen AirLink signal quality

```kotlin
AirLinkKey.KeySignalQuality.create().listen(this, getOnce = true) { quality ->
    val v = quality ?: 0
    // update UI
}
```

### 5.2 Request pairing

```kotlin
RemoteControllerKey.KeyRequestPairing.create().action(
    onSuccess = { /* request sent */ },
    onFailure = { e -> /* handle error */ },
)
```

### 5.3 Stop pairing

```kotlin
RemoteControllerKey.KeyStopPairing.create().action(
    onSuccess = { /* stop sent */ },
    onFailure = { e -> /* handle error */ },
)
```

### 5.4 Set/get AirLink band

```kotlin
AirLinkKey.KeyFrequencyBand.create().set(
    heq.sdk.keyvalue.value.airlink.FrequencyBand.BAND_2_DOT_4_GHZ,
    onSuccess = { },
    onFailure = { e -> },
)

val band = AirLinkKey.KeyFrequencyBand.create().get()
```

## 6. WebSocket Protocol Notes

Current transport expects websocket server on:

- `ws://127.0.0.1:8765`

Main command mappings:

- pairing:
  - send: `START_PAIR`, `STOP_PAIR`
  - recv: `PAIR_SUCCESS`, `PAIR_TIMEOUT`
- RC status:
  - recv: `RC_STATUS_CALLBACK`
- AirLink:
  - recv: `BAND_INFO_CALLBACK`, `CHANNEL_INFO_CALLBACK`
  - send: `SET_BAND`, `SET_CHAN_MODE`, `SET_CHAN`
- identity:
  - recv: `VERSION_NOTIFY`

Payload fields are mapped with `@SerializedName` (snake_case + nested fields).

## 7. Timeout Behavior

- pairing timeout: 60s
- normal command ack timeout: 3s

Timeout returns SDK error code `TIMEOUT`.

## 8. Limitations

- No simulated fallback data; only real websocket-driven values.
- Unsupported keys fail fast with `KEY_NOT_SUPPORTED`.
- Transport currently assumes local websocket endpoint and protocol contract.

