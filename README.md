#  OpenMobileNetworkToolkit

OMNT provides tooling to researchers and developers of mobile communication networks like 3GPP 4/5G. 

  * Collect and send metrics like RSSI, RSRQ, RSRP, GNSS position, Cell ID, PLNM and much more
  * Run Iperf3 server / client for bandwith, latency and jitter evaluation
  * Store data localy and / or sind it to an InfluxDB 2.x server
  * Configure carrier settings for test and procution networks
  * Provide access to different "secret" settings in Android phones
  * Operate with user, root or carrier privialges (different features are limited depending on the permissions)

## Permissions

Android provides a strict permission system to access APIs e.g. for information that make a phone identifiable or its location known to an app.
Also there are APIs meant for carrier to set network specific settings to ensure compatibility with the network. 
Depending an the phone vendor and the android version the required permission differ as well as if the user is able to allow an app to gain
specific privilages.

The following permissions are requested:

  * Location: Collecting location and network informatin
  * Storage: Writing measurement log files to the phones memory
  * SuperUser: On rooted phones where not carrier priviages are available

### Carrier Permissions
Carrier specific settings can only be accessed by apps running with so called carrier privilages. This was added in android 6 and is mostly relevant 
for 4G and 5G networks. (https://source.android.com/devices/tech/config/carrier)

Carrier or researcher who are able (admin key to the SIM card is required) to programm their own sim cards can store a fingerprint of an signing certificat in an applet on the simcard. 
An app signed with this certificat will get carrier privilages granted by android.

E.g. the [ARAM-Applet by Bertrand Martel](https://github.com/bertrandmartel/aram-applet) for JavaCard basesd SIM cards can be used. 
Some SIM cards like the cards from [Sysmocom](http://shop.sysmocom.de/products/sysmoISIM-SJA2) already come with the applet pre installed. 
To install the applet to a SIM card [GlobalPlatformPro by Martin Paljak](https://github.com/martinpaljak/GlobalPlatformPro) can be used.
Pre-compiled verisons of ARAM and GP can be found in the [CoIMS_Wiki repository by Supreeth Herle](https://github.com/herlesupreeth/CoIMS_Wiki).

To provision the fingerprint to the applet either (pySim)[https://github.com/osmocom/pysim] or GP can be used.  

With pySim it can be done by entering the following commands into pySim-shell:

start pySim shell with an PSCD atteched reader
```
python3 pySim-shell.py -p0
```

Provision the fingerprint. Note that insteat of an fingerprint also an valid android App ID can be used.
```
verify_adm
select ADF.ARA-M
aram_delete_all 
aram_store_ref_ar_do --aid ffffffffffff --device-app-id E849B63A7B96E571F788FC5845C4AA3C520D18E8 --android-permissions 0000000000000001
```
(this assumes the credentials to access the card are stored in the pySim format.)

## Iperf

## Network Slicing

## InfluxDB 2.x

## Vendor secret menues

## References
