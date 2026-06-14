# CHANGELOG - RemoteController SDK

This changelog tracks release-facing updates for:

- `sdk-impl-remotecontroller`
- related RC/AirLink capability behavior in current repository integration

---

## [0.2.0] - 2026-06-07

### Added

- Added standalone bootstrap entry:
  - `heq.v1.impl.remotecontroller.RemoteControllerSdkBootstrap`
  - API: `installStandalone(productId, transport)`
- Added end-user integration guide:
  - `sdk-impl-remotecontroller/API.md`
- Added single-source capability declarations in transport:
  - `REMOTE_CONTROLLER_SUPPORTED_KEYS`
  - `AIRLINK_SUPPORTED_KEYS`
  - internal fetch/set/action whitelist id sets

### Changed

- Switched RC signal source to AirLink:
  - removed `RemoteControllerKey.KeySignalQualityPercent`
  - unified signal quality to `AirLinkKey.KeySignalQuality`
- Converted capability behavior from permissive/skeleton to strict:
  - only explicitly implemented keys are accepted
  - unsupported RC/AirLink keys now fail fast with `KEY_NOT_SUPPORTED`
- WebSocket protocol mapping hardened in `SceptreRemoteControllerTransport`:
  - payload field mapping aligned with `@SerializedName`
  - nested callback payload parsing aligned for:
    - `RC_STATUS_CALLBACK`
    - `BAND_INFO_CALLBACK`
    - `CHANNEL_INFO_CALLBACK`
    - `VERSION_NOTIFY`
- Added timeout protection:
  - command ack timeout: 3s
  - pairing timeout: 60s

### Removed

- Removed simulated RC async execution path in `RemoteControllerAsyncProtocol`:
  - no mock sleep-based success branches
  - no signal simulation thread fallback
- Removed RC signal helper API tied to deleted key:
  - `RemoteControllerKeyPublisher.publishSignalQualityPercent(...)`
- Removed legacy RC state field:
  - `RemoteControllerState.signalQualityPercent`

### Implemented Key Matrix (Current)

#### RemoteController

- `KeyConnection` (GET)
- `KeyControlMode` (GET/SET)
- `KeyFirmwareVersion` (GET)
- `KeyPairingStatus` (GET)
- `KeyRequestPairing` (ACTION)
- `KeyStopPairing` (ACTION)

#### AirLink

- `KeyConnection` (GET)
- `KeySignalQuality` (GET/LISTEN)
- `KeyFrequencyBandRange` (GET)
- `KeyFrequencyBand` (GET/SET)
- `KeyChannelSelectionMode` (GET/SET)
- `KeyFrequencyPointRange` (GET)
- `KeyFrequencyPoint` (GET/SET)

---

## Upgrade Notes

1. If your app was listening to `RemoteControllerKey.KeySignalQualityPercent`, migrate to:
   - `AirLinkKey.KeySignalQuality`
2. Any RC/AirLink key outside implemented matrix will now return `KEY_NOT_SUPPORTED`.
3. External consumers should use:
   - `RemoteControllerSdkBootstrap.installStandalone(...)`

---

## Known Constraints

- Transport expects local websocket endpoint:
  - `ws://127.0.0.1:8765`
- No simulation fallback in current release.
- For AAR delivery, both artifacts are required:
  - `sdk-core-release.aar`
  - `sdk-impl-remotecontroller-release.aar`

