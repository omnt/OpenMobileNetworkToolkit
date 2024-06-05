# SIM UICC configuration for Carrier Permissions
Carrier specific settings can only be accessed by apps running with so-called carrier privileges. This was added in android 6 and is mostly relevant for 4G and 5G networks. (https://source.android.com/devices/tech/config/carrier). These permissions also allow OMNT to talk to APIs restricted to system apps (apps that are shipped with your phone usually not removable). OMNT works with out these permissions but some features will be disbaled.

Carrier or researcher who are able (admin key to the SIM card is required) to program their own sim cards can store a fingerprint of an signing certificate in an access rule applet (ARA-M) on the SIM card. 
An app signed with this certificate will get carrier privileges granted by Android. (https://source.android.com/devices/tech/config/uicc)

E.g. the [ARAM-Applet by Bertrand Martel](https://github.com/bertrandmartel/aram-applet) for JavaCard based SIM cards can be used. 
Some SIM cards like the cards from [Sysmocom](http://shop.sysmocom.de/products/sysmoISIM-SJA2) already come with the applet pre installed. 
To install the applet to a SIM card [GlobalPlatformPro by Martin Paljak](https://github.com/martinpaljak/GlobalPlatformPro) can be used.
Pre-compiled versions of ARA-M and GP can be found in the [CoIMS_Wiki repository by Supreeth Herle](https://github.com/herlesupreeth/CoIMS_Wiki).

To provision the fingerprint to the applet, either [pySim](https://github.com/osmocom/pysim) or GP can be used.  

With pySim it can be done by entering the following commands into pySim-shell:

start pySim shell with an PCSCD attached reader
```shell
python3 pySim-shell.py -p0 -a <admin key>
```
or if you have a csv file with ICCID and admin pin:
```shell
python3 pySim-shell.py -p0 --csv card_data.csv 
```

Provision the fingerprint. Note that instead of a fingerprint also a valid android App ID can be used.
If you have a SIM Card with the ARA-M applet from sysmocom you can skip the ```verify_adm``` part as the applet is not write protected.
```shell
verify_adm
select ADF.ARA-M
aram_delete_all 
aram_store_ref_ar_do --aid ffffffffffff --device-app-id E849B63A7B96E571F788FC5845C4AA3C520D18E8 --android-permissions 0000000000000001
```
This assumes the credentials to access the card are stored in the pySim format.
