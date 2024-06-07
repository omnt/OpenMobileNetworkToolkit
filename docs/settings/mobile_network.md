# Mobile Network
This section of the OMNT settings combines the mobile network related settings. OMNT needs carrier permissions to apply those settings.
Note that not all devices will silently ignore some of the settings.

## Radio Settings
* Select Access Network Type: Limit the network types the modem will use a specific technology. 
* Set PLMN: Force the modem to only attach to a specific PLMN and ignore other available cells.
* Persist until reboot: Make sure the setting will be applied after e.g. a SIM change until the next reboot

## Carrier Settings
While there are a lot more carrier settings part of a phone configuration not all relevant or even accessible. This section allows to configure the profile 
applied for the selected subscription. OMNT will try to restore them after each network change event which mostly happens on SIM change, modem reboot and similar.
This section reflects the settings desired to be applied and not necessary the current state. To look at the current state please use the [Carrier Settings](../carrier_settings.md) View from the menu.

* Apply carrier settings now: This button will request the phone to apply the settings configured below to be applied.

The different settings are grouped by the android API level on which they where introduced and will be disabled if the phones runs an older version.
Details on the different settings can be found [here](https://developer.android.com/reference/android/telephony/CarrierConfigManager)

[Settings](settings.md) | [Home](../OpenMobileNetworkToolkit.md)