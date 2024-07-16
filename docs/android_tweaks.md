# Android Tweaks
While OMNT should work without changes on the phone settings needed, there are some options useful to set for better measurement results.

## Developer Settings
The following settings can be found in the android developer settings menu. On Vanilla Android devices they are enabled by going to ```Settings -> About the phone``` and pressing there multiple times ```Build number```. If this is not working on your device, a quick web search with your phone name and ```developer settings``` should help. After enabling the developer settings they are usually accessible via ```Settings -> System -> Developer Options```.
* USB debugging: Install and debug apps via ADB
* Mobile data always active: Allows to gather mobile network information even if connected to WiFi
* Force full GNSS measurements: might improve GNSS accuracy while logging

Depending on your phone, there might be more useful options (or less)

[Home](OpenMobileNetworkToolkit.md)