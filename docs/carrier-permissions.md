# SIM UICC configuration for Carrier Permissions
Carrier specific settings can only be accessed by apps running with so-called carrier privileges. This was added in android 6 and is mostly relevant for 4G and 5G networks. (https://source.android.com/devices/tech/config/carrier). These permissions also allow OMNT to talk to APIs restricted to system apps (apps that are shipped with your phone usually not removable). OMNT works without these permissions, but some features will be disabled.

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

---

#### Alternative Solution

The `aram_store_ref_ar_do` command may return the following error:
```
EXCEPTION of type 'ValueError' occurred with message: Dict [{'ref_ar_do': [{'ref_do': [{'aid_ref_do': 'FFFFFFFFFFFF'}, {'dev_app_id_ref_do': 'E46872F28B350B7E1F140DE535C2A8D5804F0BE3'}]}, {'ar_do': [{'apdu_ar_do': {'generic_access_rule': 'always'}}, {'perm_ar_do': {'permissions': '0000000000000001'}}]}]}] doesn't contain expected key command_store_ref_ar_do
```

In this case, the GP provided by the [CoIMS_Wiki](https://github.com/herlesupreeth/CoIMS_Wiki) project can be used as an alternative solution by following the steps below. To implement this solution, the KIC1, KID1 and KIK1 keys of the SIM card are required.
```bash
git clone https://github.com/herlesupreeth/CoIMS_Wiki
cd CoIMS_Wiki
alias gp="java -jar $PWD/gp.jar"
gp --key-enc <KIC1> --key-mac <KID1> --key-dek <KIK1> -lvi
# Unlock the SIM card for easier installation of applet
gp --key-enc <KIC1> --key-mac <KID1> --key-dek <KIK1> --unlock
# Install ARA-M Java Card applets on USIM/ISIM
gp --install applet.cap
# Push the SHA-1 certificate of the OMNT app onto ARA-M in USIM/ISIM
gp -a 00A4040009A00000015141434C0000 -a 80E2900033F031E22FE11E4F06FFFFFFFFFFFFC114E849B63A7B96E571F788FC5845C4AA3C520D18E8E30DD00101DB080000000000000001
# Check the list of installed certificates
gp --acr-list-aram
```


[Home](OpenMobileNetworkToolkit.md)