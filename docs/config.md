## Configuration via Config File

You can export the current settings of the App via the Config Menu.
Navigate to `Settings` and click on `Config`.
Here, you'll find options to export, import, and reset the Config (note: resetting cannot be undone!).

The current configuration of the App will also be displayed.

Please note that there is currently no sanity check for the settings after importing them.
If the app crashes after importing an invalid Config, reinstall the app.

## Config File Example

The config file should ideally be placed under `/sdcard0/Documents/omnt/configs`, but it can be stored anywhere on the device.
When importing the config file, you can also select specific parts of the file to import.
An example of a config file is shown below:
```json
{
    "default_sp": {
        "select_subscription": "1",
        "show_neighbour_cells": true
    },
    "mobile_network_sp": {
        "select_network_type": "CDMA2000",
        "select_subscription": "3"
    },
    "logging_sp": {
        "influx_ip_address_data": false,
        "influx_token": "",
        "enable_notification_update": false,
        "enable_local_file_log": false,
        "start_logging_on_boot": false,
        "log_neighbour_cells": false,
        "tags": "device=",
        "influx_URL": "",
        "influx_bucket": "",
        "log_signal_data": false,
        "influx_throughput_data": false,
        "measurement_name": "",
        "logging_interval": "1000",
        "log_wifi_data": false,
        "enable_influx": false,
        "fake_location": false,
        "influx_battery_data": false,
        "enable_logging": false,
        "influx_network_data": false,
        "influx_org": "",
        "influx_cell_data": false
    },
    "iperf3_sp": {
        "iperf3Streams": "",
        "iperf3Interval": "",
        "iperf3Port": "",
        "iperf3OneOff": false,
        "iperf3Bytes": "",
        "iperf3Duration": "",
        "iperf3Bandwidth": "",
        "iperf3IP": "",
        "iperf3cport": "",
        "iperf3Reverse": false,
        "iperf3BiDir": false
    },
    "carrier_sp": {
        "switch_KEY_WORLD_MODE_ENABLED_BOOL": true,
        "list_KEY_CARRIER_DEFAULT_WFC_IMS_MODE_INT": "1",
        "switch_KEY_CARRIER_SETTINGS_ENABLE_BOOL": true,
        "switch_KEY_CARRIER_RCS_PROVISIONING_REQUIRED_BOOL": false,
        "switch_KEY_CARRIER_VOLTE_PROVISIONING_REQUIRED_BOOL": false,
        "switch_KEY_DISPLAY_HD_AUDIO_PROPERTY_BOOL": false,
        "switch_KEY_AUTO_RETRY_ENABLED_BOOL": false,
        "select_network_type": "CDMA2000",
        "switch_KEY_REQUIRE_ENTITLEMENT_CHECKS_BOOL": false,
        "switch_KEY_SUPPORT_PAUSE_IMS_VIDEO_CALLS_BOOL": false,
        "list_KEY_VOLTE_REPLACEMENT_RAT_INT9": "18",
        "switch_KEY_HIDE_LTE_PLUS_DATA_ICON_BOOL": false,
        "switch_KEY_ALLOW_EMERGENCY_VIDEO_CALLS_BOOL": false,
        "switch_KEY_CARRIER_VOLTE_PROVISIONED_BOOL": false,
        "switch_KEY_FORCE_HOME_NETWORK_BOOL": false,
        "switch_KEY_CARRIER_UT_PROVISIONING_REQUIRED_BOOL": false,
        "switch_KEY_HIDE_TTY_HCO_VCO_WITH_RTT_BOOL": false,
        "switch_KEY_HIDE_ENHANCED_4G_LTE_BOOL": false,
        "switch_KEY_CARRIER_WFC_SUPPORTS_WIFI_ONLY_BOOL": false,
        "switch_KEY_CARRIER_VOLTE_TTY_SUPPORTED_BOOL": false,
        "edit_text_KEY_APN_SETTINGS_DEFAULT_APN_TYPES_STRING_ARRAY": "",
        "switch_KEY_ALLOW_ADDING_APNS_BOOL": true,
        "switch_KEY_SUPPORT_EMERGENCY_SMS_OVER_IMS_BOOL": false,
        "switch_KEY_CARRIER_ALLOW_DEFLECT_IMS_CALL_BOOL": true,
        "switch_KEY_CARRIER_ALLOW_TURNOFF_IMS_BOOL": true,
        "switch_KEY_EDITABLE_WFC_MODE_BOOL": true,
        "switch_KEY_PREFER_2G_BOOL": false,
        "switch_KEY_CARRIER_DEFAULT_WFC_IMS_ENABLED_BOOL": false,
        "switch_KEY_EDITABLE_WFC_ROAMING_MODE_BOOL": true,
        "switch_KEY_RTT_UPGRADE_SUPPORTED_FOR_DOWNGRADED_VT_CALL_BOOL": true,
        "switch_KEY_HIDE_PRESET_APN_DETAILS_BOOL": false,
        "switch_KEY_CARRIER_VOLTE_AVAILABLE_BOOL": true,
        "switch_KEY_CARRIER_USE_IMS_FIRST_FOR_EMERGENCY_BOOL": false,
        "switch_KEY_HIDE_ENABLE_2G": false,
        "switch_KEY_WORLD_PHONE_BOOL": true,
        "switch_KEY_ALLOW_VIDEO_CALLING_FALLBACK_BOOL": true,
        "switch_KEY_CARRIER_VT_AVAILABLE_BOOL": false,
        "switch_KEY_HIDE_IMS_APN_BOOL": false,
        "switch_KEY_CARRIER_IMS_GBA_REQUIRED_BOOL": false,
        "switch_KEY_CARRIER_SUPPORTS_SS_OVER_UT_BOOL": false,
        "switch_KEY_HIDE_PREFERRED_NETWORK_TYPE_BOOL": false,
        "switch_KEY_SHOW_IMS_REGISTRATION_STATUS_BOOL": true,
        "switch_KEY_ENHANCED_4G_LTE_ON_BY_DEFAULT_BOOL": false,
        "switch_KEY_EDITABLE_ENHANCED_4G_LTE_BOOL": true,
        "edit_text_KEY_READ_ONLY_APN_FIELDS_STRING_ARRAY": "",
        "switch_KEY_CARRIER_WFC_IMS_AVAILABLE_BOOL": true,
        "switch_KEY_APN_EXPAND_BOOL": true,
        "list_KEY_CARRIER_DEFAULT_WFC_IMS_ROAMING_MODE_INT": "1",
        "switch_KEY_HIDE_CARRIER_NETWORK_SETTINGS_BOOL": false,
        "switch_KEY_HIDE_SIM_LOCK_SETTINGS_BOOL": false
    },
    "ping_sp": {
        "ping_input": "-w 5 8.8.8.8",
        "ping": false
    },
    "BuildInformation": {
        "BuildType": "debug",
        "VersionCode": 3,
        "VersionName": "0.3",
        "ApplicationId": "de.fraunhofer.fokus.OpenMobileNetworkToolkit",
        "Debug": true
    }
}
```