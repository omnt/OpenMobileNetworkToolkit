# Signing the app
This can be done with android studio.  

To generated a signed apk:

1) Go to ```build``` -> ```Generate signed Bundle/APK```
2) You will be ask to generate a new key or import an existing one
3) Click through the dialogs until you end up with a signed APK

To sign the debug apks used for development

1) Go to ```file``` -> Project Structure -> Modules
2) Select the ```app```
3) select the ```Signing config``` tab.
4) create a new signing config referencing you key file

You can also manually re-sign the downloaded .apk file using [apksigner](https://developer.android.com/tools/).
```shell
apksigner sign --ks my.keystore OpenMobileNetworkToolkit.apk
```
where my.keystore is your java keystore. 

[Home](OpenMobileNetworkToolkit.md)