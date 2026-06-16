# HEQ RemoteController SDK API

This document is for app developers who integrate `heq.v1.impl.remotecontroller` as an AAR.
### LastVersion 0.2.0
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


## 2. External App Integration

### 2.1 Add AAR

Place the 2 AAR files into consumer app local libs path, for example:

- `app/libs/sdk-core-release.aar`
- `app/libs/sdk-impl-remotecontroller-release.aar`

Gradle example:

```kotlin
dependencies {
    implementation(files("libs/sdk-core-${last-version}.aar"))
    implementation(files("libs/sdk-impl-remotecontroller-${last-version}.aar"))
    
    implementation("com.google.code.gson:gson:2.13.1")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("io.netty:netty-all:4.1.112.Final")
}
```

### 2.2 SDK init + bootstrap

```kotlin
import heq.v1.impl.remotecontroller.RemoteControllerSdkBootstrap

//确保初始化完成再注册相关组件和调用Key
SDKManager.getInstance().init(
    this,
    object : SDKManagerCallback {
        override fun onInitProcess(event: HEQSDKInitEvent, totalProcess: Int) {}
        override fun onRegisterSuccess() {
            RemoteControllerSdkBootstrap.installStandalone()
            //you can start listening 
            //you can start fetch value
        }
        override fun onRegisterFailure(error: IHEQError) {
        }
        override fun onProductDisconnect(productId: Int) {}
        override fun onProductConnect(productId: Int) {}
        override fun onProductChanged(productId: Int) {}
        override fun onDatabaseDownloadProgress(current: Long, total: Long) {}
    },
)
SDKManager.getInstance().registerApp()
```

## 3. Implemented Keys

The implementation is strict capability-based:
- keys not listed below return `KEY_NOT_SUPPORTED`.

```kotlin
val REMOTE_CONTROLLER_SUPPORTED_KEYS: List<HEQKey<*>> = listOf(
        RemoteControllerKey.KeyConnection,
        RemoteControllerKey.KeyControlMode,
        RemoteControllerKey.KeyFirmwareVersion,
        RemoteControllerKey.KeyPairingStatus,
        RemoteControllerKey.KeyRequestPairing,
        RemoteControllerKey.KeyStopPairing,
    )

val AIRLINK_SUPPORTED_KEYS: List<HEQKey<*>> = listOf(
    AirLinkKey.KeyConnection,
    AirLinkKey.KeySignalQuality,
    AirLinkKey.KeyFrequencyBandMode,
    AirLinkKey.KeyFrequencyBandRange,
    AirLinkKey.KeyFrequencyBand,
    AirLinkKey.KeyChannelSelectionMode,
    AirLinkKey.KeyFrequencyPointRange,
    AirLinkKey.KeyFrequencyPoint,
    AirLinkKey.KeyRSSI,
    AirLinkKey.KeyBandwidth,
    AirLinkKey.KeyTxBandwidth,
)

val RC_FETCH_SUPPORTED_IDS: Set<String> = setOf(
    RemoteControllerKey.KeyConnection.identifier,
    RemoteControllerKey.KeyControlMode.identifier,
    RemoteControllerKey.KeyFirmwareVersion.identifier,
    RemoteControllerKey.KeyPairingStatus.identifier,
)

val RC_SET_SUPPORTED_IDS: Set<String> = setOf(
    RemoteControllerKey.KeyControlMode.identifier,
)

val RC_ACTION_SUPPORTED_IDS: Set<String> = setOf(
    RemoteControllerKey.KeyRequestPairing.identifier,
    RemoteControllerKey.KeyStopPairing.identifier,
)

val AIRLINK_FETCH_SUPPORTED_IDS: Set<String> = setOf(
    AirLinkKey.KeyConnection.identifier,
    AirLinkKey.KeySignalQuality.identifier,
    AirLinkKey.KeyFrequencyBandRange.identifier,
    AirLinkKey.KeyFrequencyBand.identifier,
    AirLinkKey.KeyChannelSelectionMode.identifier,
    AirLinkKey.KeyFrequencyPointRange.identifier,
    AirLinkKey.KeyFrequencyPoint.identifier,
    AirLinkKey.KeyBandwidth.identifier,
    AirLinkKey.KeyTxBandwidth.identifier,
)

val AIRLINK_SET_SUPPORTED_IDS: Set<String> = setOf(
    AirLinkKey.KeyFrequencyBand.identifier,
    AirLinkKey.KeyFrequencyBandMode.identifier,
    AirLinkKey.KeyChannelSelectionMode.identifier,
    AirLinkKey.KeyFrequencyPoint.identifier,
    AirLinkKey.KeyBandwidth.identifier,
    AirLinkKey.KeyTxBandwidth.identifier,
)
```

## 4. Usage Examples

All examples below use HEQ SDK V1 wrapperwith DJI V5-style call patterns:


### 4.1 Listen AirLink signal quality

```kotlin
AirLinkKey.KeySignalQuality.create().listen(this, getOnce = true) { quality ->
    val v = quality ?: 0
    // update UI
}
```

### 4.2 Request pairing

```kotlin
RemoteControllerKey.KeyRequestPairing.create().action(
    onSuccess = { /* request sent */ },
    onFailure = { e -> /* handle error */ },
)
```

### 4.3 Stop pairing

```kotlin
RemoteControllerKey.KeyStopPairing.create().action(
    onSuccess = { /* stop sent */ },
    onFailure = { e -> /* handle error */ },
)
```

### 4.4 Set/get AirLink band

```kotlin
AirLinkKey.KeyFrequencyBand.create().set(
    heq.sdk.keyvalue.value.airlink.FrequencyBand.BAND_2_DOT_4_GHZ,
    onSuccess = { },
    onFailure = { e -> },
)

val band = AirLinkKey.KeyFrequencyBand.create().get()
```

## 5. WebSocket Protocol Notes

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

## 6. Timeout Behavior

- pairing timeout: 60s
- normal command ack timeout: 3s

Timeout returns SDK error code `TIMEOUT`.

## 7. Limitations

- No simulated fallback data; only real websocket-driven values.
- Unsupported keys fail fast with `KEY_NOT_SUPPORTED`.
- Transport currently assumes local websocket endpoint and protocol contract.

