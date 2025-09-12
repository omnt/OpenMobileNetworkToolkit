# Preferences Documentation

## Logging

### Logging Service

_This configures the logging Service of OMNT._

| Key | Title | Summary | Default Value |
| --- | ----- | ------- | ------------- |
| **enable_logging** | Enable | Start / stop the logging service | `false` |
| **start_logging_on_boot** | Start on boot | Start the logging service on device boot | `false` |
| **logging_interval** | Interval | Logging interval in milliseconds | `1000` |

### Local logging

_This configures a local InfluxDB instance, running on the Device, to log data to. EXPERIMENTAL_

| Key | Title | Summary | Default Value |
| --- | ----- | ------- | ------------- |
| **enable_local_influx_log** | InfluxDB log | Log to a local Influx 2.x database | `fals` |
| **enable_local_file_log** | Log file | Log to a local file | `false` |

### Remote logging

_This configures a remote InfluxDB instance to log data to._

| Key | Title | Summary | Default Value |
| --- | ----- | ------- | ------------- |
| **enable_influx** | InfluxDB log | Log to a remote Influx 2.x database | `false` |
| **influx_URL** | InfluxDB instance URL / IP | Influx URL, it can either be http://IP:8086, https://IP:8086, or any hostname. | `http://IP:8086` |
| **influx_org** | InfluxDB Organization | Influx ORG Name, or ID. | `ORG` |
| **influx_token** | Influx Token | Influx TOKEN | `TOKEN` |
| **influx_bucket** | InfluxDB bucket | Influx Bucket Name or ID. | `BUCKET_NAME` |

### Logging content

| Key | Title | Summary | Default Value |
| --- | ----- | ------- | ------------- |
| **fake_location** | Use fake location | Use a fake location for all measurements for testing / privacy reasons | `false` |
| **measurement_name** | Measurement name |  | `omnt` |
| **tags** | Tags | Comma separated list of tags |  |
| **influx_network_data** | Log network information | This will log NetworkInformation to InfluxDB. Like NetworkOperatorName, SimOperatorName, DataNetworkType. | `false` |
| **log_signal_data** | Log signal data | This will log SignalStrength to InfluxDB. Like RSRP, RSRQ from the SignalStrength API. | `false` |
| **influx_cell_data** | Log cell information | This will log CellInformation to InfluxDB. Like CellId, MCC, MNC, PCI, but also RSRP, RSRQ from the CellInformation API. | `true` |
| **log_neighbour_cells** | Log neighbour cells | This will log neighbour CellInformation data to InfluxDB. Like CellId, MCC, MNC, PCI, but also RSRP, RSRQ from the CellInformation API. | `false` |
| **influx_throughput_data** | Log throughput information | This will log InterfaceThroughput data to InfluxDB. Like download and upload throughput, what the phone thinks what is currently possible. | `false` |
| **log_wifi_data** | Log WiFi information | This will log WifiInformation data to InfluxDB. Like SSID, BSSID, RSSI, Frequency, LinkSpeed, and WifiStandard. | `false` |
| **influx_battery_data** | Log battery information | This will log BatteryInformation data to InfluxDB. Like BatteryLevel and Charging Status. | `false` |
| **influx_ip_address_data** | Log IP addresses | This will log IPAddressInformation data to InfluxDB. Like IPv4 and IPv6 addresses, and the interface name. | `false` |

## Main

### Home screen settings

| Key | Title | Summary | Default Value |
| --- | ----- | ------- | ------------- |
| **show_neighbour_cells** | Show neighbour cells | Shows neighbour cells only when connected to a network and is announced by the network. | `false` |

### Notification settings

| Key | Title | Summary | Default Value |
| --- | ----- | ------- | ------------- |
| **enable_radio_notification** | Enable Cell Notification | Serving Cell Parameter: PCI, RSRP... | `false` |

### App settings

| Key | Title | Summary | Default Value |
| --- | ----- | ------- | ------------- |
| **device_name** | Unique Device Name | Set unique device name. | `OMNT0001` |
| **log_settings** | Logging | Configure the Logging Parameter. |  |
| **mobile_network_settings** | Mobile Network | Configure Mobile Network Settings, only if the App has Carrier Permissions. |  |
| **select_subscription** | Set subscription (SIM) |  | `1` |
| **reset_modem** | Reboot Modem | Reboots the modem, only possible when OMNT has Carrier Permissions. |  |
| **mqtt_settings** | MQTT Settings | Configure MQTT for OMNT. |  |
| **shared_preferences_io** | Config | Import/Export Config of OMNT. |  |

## Mqtt

### MQTT Service

_Enables MQTT Services for OMNT._

| Key | Title | Summary | Default Value |
| --- | ----- | ------- | ------------- |
| **enable_mqtt** | Enable | Enable MQTT | `false` |
| **enable_mqtt_on_boot** | Start on boot | Enable MQTT on boot | `false` |

### MQTT Credentials

_Section to set Credentials for MQTT._

| Key | Title | Summary | Default Value |
| --- | ----- | ------- | ------------- |
| **mqtt_host** | MQTT-Broker Address | MQTT Broker Address | `192.168.213.89:1883` |
| **mqtt_client_username** | MQTT Client Username | MQTT Username | `USERNAME` |
| **mqtt_client_password** | MQTT Client Password | MQTT Client Password. | `PASSWORD` |

## Mobile network

### Radio Settings

| Key | Title | Summary | Default Value |
| --- | ----- | ------- | ------------- |
| **doc** | Click here for Android Doc | Opens Link to Android Doc. |  |
| **select_network_type** | Select Access Network Type |  |  |
| **add_plmn** | Set PLMN |  |  |
| **persist_boot** | Persist until reboot |  |  |

### Carrier Settings

_Applied on SIM / network change_

| Key | Title | Summary | Default Value |
| --- | ----- | ------- | ------------- |
| **apply_cs_settings** | Apply carrier settings now |  |  |

### Android 12 API 31 (S)

| Key | Title | Summary | Default Value |
| --- | ----- | ------- | ------------- |
| **edit_text_EPDG_STATIC_ADDRESS** | EPDG_STATIC_ADDRESS |  |  |
| **multi_select_KEY_CARRIER_NR_AVAILABILITIES_INT_ARRAY** | CARRIER_NR_AVAILABILITIES_INT_ARRAY |  |  |
| **switch_KEY_HIDE_TTY_HCO_VCO_WITH_RTT_BOOL** | HIDE_TTY_HCO_VCO_WITH_RTT_BOOL |  | `false` |
| **switch_KEY_HIDE_ENABLE_2G** | HIDE_ENABLE_2G |  | `false` |
| **switch_KEY_RTT_UPGRADE_SUPPORTED_FOR_DOWNGRADED_VT_CALL_BOOL** | RTT_UPGRADE_SUPPORTED_FOR_DOWNGRADED_VT_CALL_BOOL |  | `true` |

### Android 11 API 30 (R)

| Key | Title | Summary | Default Value |
| --- | ----- | ------- | ------------- |
| **switch_KEY_ALLOW_VIDEO_CALLING_FALLBACK_BOOL** | ALLOW_VIDEO_CALLING_FALLBACK_BOOL |  | `true` |
| **switch_KEY_CARRIER_DEFAULT_WFC_IMS_ENABLED_BOOL** | CARRIER_DEFAULT_WFC_IMS_ENABLED_BOOL |  | `false` |
| **switch_KEY_HIDE_LTE_PLUS_DATA_ICON_BOOL** | HIDE_LTE_PLUS_DATA_ICON_BOOL |  | `false` |
| **switch_KEY_WORLD_MODE_ENABLED_BOOL** | WORLD_MODE_ENABLED_BOOL |  | `true` |
| **switch_KEY_CARRIER_RCS_PROVISIONING_REQUIRED_BOOL** | CARRIER_RCS_PROVISIONING_REQUIRED_BOOL |  | `false` |
| **switch_KEY_SHOW_IMS_REGISTRATION_STATUS_BOOL** | SHOW_IMS_REGISTRATION_STATUS_BOOL |  | `true` |
| **switch_KEY_EDITABLE_WFC_MODE_BOOL** | EDITABLE_WFC_MODE_BOOL |  | `true` |
| **switch_KEY_EDITABLE_WFC_ROAMING_MODE_BOOL** | EDITABLE_WFC_ROAMING_MODE_BOOL |  | `true` |
| **edit_text_KEY_READ_ONLY_APN_FIELDS_STRING_ARRAY** | READ_ONLY_APN_FIELDS_STRING_ARRAY |  |  |
| **edit_text_KEY_APN_SETTINGS_DEFAULT_APN_TYPES_STRING_ARRAY** | APN_SETTINGS_DEFAULT_APN_TYPES_STRING_ARRAY |  |  |
| **switch_KEY_CARRIER_ALLOW_DEFLECT_IMS_CALL_BOOL** | CARRIER_ALLOW_DEFLECT_IMS_CALL_BOOL |  | `true` |

### Android 10 API 29 (Q)

| Key | Title | Summary | Default Value |
| --- | ----- | ------- | ------------- |
| **switch_KEY_FORCE_HOME_NETWORK_BOOL** | FORCE_HOME_NETWORK_BOOL |  | `false` |
| **switch_KEY_PREFER_2G_BOOL** | PREFER_2G_BOOL |  | `false` |
| **switch_KEY_CARRIER_SETTINGS_ENABLE_BOOL** | CARRIER_SETTINGS_ENABLE_BOOL |  | `true` |
| **switch_KEY_CARRIER_ALLOW_TURNOFF_IMS_BOOL** | CARRIER_ALLOW_TURNOFF_IMS_BOOL |  | `true` |
| **switch_KEY_CARRIER_WFC_IMS_AVAILABLE_BOOL** | CARRIER_WFC_IMS_AVAILABLE_BOOL |  | `true` |
| **switch_KEY_EDITABLE_ENHANCED_4G_LTE_BOOL** | EDITABLE_ENHANCED_4G_LTE_BOOL |  | `true` |
| **switch_KEY_CARRIER_VOLTE_AVAILABLE_BOOL** | CARRIER_VOLTE_AVAILABLE_BOOL |  | `true` |
| **switch_KEY_CARRIER_VOLTE_PROVISIONING_REQUIRED_BOOL** | CARRIER_VOLTE_PROVISIONING_REQUIRED_BOOL |  | `false` |
| **switch_KEY_CARRIER_VOLTE_PROVISIONED_BOOL** | CARRIER_VOLTE_PROVISIONED_BOOL |  | `false` |
| **switch_KEY_CARRIER_VT_AVAILABLE_BOOL** | CARRIER_VT_AVAILABLE_BOOL |  | `false` |
| **switch_KEY_CARRIER_VOLTE_TTY_SUPPORTED_BOOL** | CARRIER_VOLTE_TTY_SUPPORTED_BOOL |  | `false` |
| **switch_KEY_HIDE_ENHANCED_4G_LTE_BOOL** | HIDE_ENHANCED_4G_LTE_BOOL |  | `false` |
| **switch_KEY_HIDE_CARRIER_NETWORK_SETTINGS_BOOL** | HIDE_CARRIER_NETWORK_SETTINGS_BOOL |  | `false` |
| **switch_KEY_HIDE_IMS_APN_BOOL** | HIDE_IMS_APN_BOOL |  | `false` |
| **switch_KEY_HIDE_PREFERRED_NETWORK_TYPE_BOOL** | HIDE_PREFERRED_NETWORK_TYPE_BOOL |  | `false` |
| **switch_KEY_HIDE_PRESET_APN_DETAILS_BOOL** | HIDE_PRESET_APN_DETAILS_BOOL |  | `false` |
| **switch_KEY_HIDE_SIM_LOCK_SETTINGS_BOOL** | HIDE_SIM_LOCK_SETTINGS_BOOL |  | `false` |
| **switch_KEY_ALLOW_ADDING_APNS_BOOL** | ALLOW_ADDING_APNS_BOOL |  | `true` |
| **switch_KEY_APN_EXPAND_BOOL** | APN_EXPAND_BOOL |  | `true` |
| **switch_KEY_CARRIER_WFC_SUPPORTS_WIFI_ONLY_BOOL** | CARRIER_WFC_SUPPORTS_WIFI_ONLY_BOOL |  | `false` |
| **switch_KEY_CARRIER_IMS_GBA_REQUIRED_BOOL** | CARRIER_IMS_GBA_REQUIRED_BOOL |  | `false` |
| **switch_KEY_REQUIRE_ENTITLEMENT_CHECKS_BOOL** | REQUIRE_ENTITLEMENT_CHECKS_BOOL |  | `false` |
| **list_KEY_VOLTE_REPLACEMENT_RAT_INT9** | VOLTE_REPLACEMENT_RAT_INT |  | `18` |
| **switch_KEY_CARRIER_USE_IMS_FIRST_FOR_EMERGENCY_BOOL** | CARRIER_USE_IMS_FIRST_FOR_EMERGENCY_BOOL |  | `false` |
| **switch_KEY_AUTO_RETRY_ENABLED_BOOL** | AUTO_RETRY_ENABLED_BOOL |  | `false` |
| **switch_KEY_WORLD_PHONE_BOOL** | WORLD_PHONE_BOOL |  | `true` |
| **switch_KEY_SUPPORT_PAUSE_IMS_VIDEO_CALLS_BOOL** | SUPPORT_PAUSE_IMS_VIDEO_CALLS_BOOL |  | `false` |
| **switch_KEY_CARRIER_UT_PROVISIONING_REQUIRED_BOOL** | CARRIER_UT_PROVISIONING_REQUIRED_BOOL |  | `false` |
| **switch_KEY_CARRIER_SUPPORTS_SS_OVER_UT_BOOL** | CARRIER_SUPPORTS_SS_OVER_UT_BOOL |  | `false` |
| **switch_KEY_ENHANCED_4G_LTE_ON_BY_DEFAULT_BOOL** | ENHANCED_4G_LTE_ON_BY_DEFAULT_BOOL |  | `false` |
| **switch_KEY_SUPPORT_EMERGENCY_SMS_OVER_IMS_BOOL** | SUPPORT_EMERGENCY_SMS_OVER_IMS_BOOL |  | `false` |
| **list_KEY_CARRIER_DEFAULT_WFC_IMS_MODE_INT** | CARRIER_DEFAULT_WFC_IMS_MODE_INT |  | `1` |
| **list_KEY_CARRIER_DEFAULT_WFC_IMS_ROAMING_MODE_INT** | CARRIER_DEFAULT_WFC_IMS_ROAMING_MODE_INT |  | `1` |
| **switch_KEY_ALLOW_EMERGENCY_VIDEO_CALLS_BOOL** | ALLOW_EMERGENCY_VIDEO_CALLS_BOOL |  | `false` |

### Android 8 API 27 (0_MR1)

| Key | Title | Summary | Default Value |
| --- | ----- | ------- | ------------- |
| **switch_KEY_DISPLAY_HD_AUDIO_PROPERTY_BOOL** | DISPLAY_HD_AUDIO_PROPERTY_BOOL |  | `false` |

