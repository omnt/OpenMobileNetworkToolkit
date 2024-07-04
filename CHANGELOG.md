# OpenMobileNetworkToolkit Changelog
## Release [0.3.1]

### Changed
- Fix Bug if DP is not available
- Use [TelephonyCallback](https://developer.android.com/reference/android/telephony/TelephonyCallback) instead of [PhoneStateListener](https://developer.android.com/reference/android/telephony/PhoneStateListener)

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
