# OpenMobileNetworkToolkit Changelog
## Release [0.5] - 38c3

### Added
- Option to set EPDG static address
- build information are now also written to the logging framework
- git hash is no shown and logged

### Changed
- tm instance is now updated in favor of creating a new one if subscription does not match

### Fixed
- crash of the quick view on some phones

## Release [0.4] - Stralsund

### Added
- WiFi Data logging and display on the home screen
- New Quick View on home screen for Cell Information
- Import / Export application settings

### Changed
- code clean up around soon to be deprecated functions
- refactored code that causes warnings
- refactored data model for cell information
- Bump lib dependencies
- Bump gradle version

### Fixed
- Fix crash where no UICC or Cell is available
- Wrong value in Wifi RSSI filed

## Release [0.3.1]

### Added
- Add Issue Template [PR 21](https://github.com/omnt/OpenMobileNetworkToolkit/pull/21)

### Changed
- Use [TelephonyCallback](https://developer.android.com/reference/android/telephony/TelephonyCallback) instead of [PhoneStateListener](https://developer.android.com/reference/android/telephony/PhoneStateListener)
- Bump gradle to 8.5.0
- Bump lib decencies
- disable Radio Settings if Carrier Permissions are not available
- minor documentation update

### Fixed
- Fix Bug if DP is not available, resulting in a crash
- Fix Bug if two Sim Cards are available, see [Issue 22](https://github.com/omnt/OpenMobileNetworkToolkit/issues/22)

## Release [0.3]

### Added
- Github Action Workflow for APK Debug/Release Build and SPDX File [PR 13](https://github.com/omnt/OpenMobileNetworkToolkit/pull/13)
- Documentation [PR 13](https://github.com/omnt/OpenMobileNetworkToolkit/pull/13)
- CHANGELOG.md [PR 13](https://github.com/omnt/OpenMobileNetworkToolkit/pull/13)

### Changed
- Fix possible [InfluxDB Bug](https://github.com/influxdata/influxdb-client-java/issues/731)
- iPerf3 GUI [PR 14](https://github.com/omnt/OpenMobileNetworkToolkit/pull/14)
- Fix Ping Bug, where button is enabled but now Ping is running after app restart [PR 14](https://github.com/omnt/OpenMobileNetworkToolkit/pull/14)
- Update Ping Fragment, now uses the Metric class to display [PR 14](https://github.com/omnt/OpenMobileNetworkToolkit/pull/14)

### Breaking Changes
- see [PR 14](https://github.com/omnt/OpenMobileNetworkToolkit/pull/14)

## Release [0.2]
### Changed
- Improved 4G support. Now all cell parameters of 4G networks should be logged and displayed correctly
- Improved 2G same as 4G
- Logging status indicator in the actionbar
- Filter for Carrier Settings Readout
- Carrier Settings Apply button moved to Settings
- InfluxDB Cloud is now also supported to be used as logging target. This means we can log to Influx 1.x 2.x and 3.x 
- [PR 7](https://github.com/omnt/OpenMobileNetworkToolkit/pull/7)
- [Release 0.2](https://github.com/omnt/OpenMobileNetworkToolkit/releases/tag/0.2
