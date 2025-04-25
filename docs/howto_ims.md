# How-to Enable IMS on 999xx PLMNs

It is possible to activate IMS on PLMN IDs such as 999xx using OMNT. To achieve this, the following steps should be followed:

1. In addition to the **internet** APN, create another APN value for IMS on the smartphone.

* Go to `Settings/Network and Internet/SIMs/<Name of SIM card>`
* Add the APN in `Access point names`. (Name: *ims*, APN: *ims*, APN type: *ims*, APN protocol: *IPv4*, APN roaming protocol: *IPv4*). After that, save it and leave the **internet** APN selected.

2. Disable SQN checking on SIM cards. (See the *Disabling / Enabling SQN validation* section of the sysmoISIM user manuals)

**WARNING:** This step disables a major security feature of the 3G/4G/5G authentication and key agreement. It is necessary when using separate HSS instances for CN and IMS (e.g., [Open5GS-HSS](https://github.com/open5gs/open5gs/tree/main/src/hss) for CN and [PyHSS](https://github.com/nickvsnetworking/pyhss) for IMS). Without SQN check, there is no protection against authentication replay attacks. Only use if you really know what you’re doing, and only in a lab. It is provided here because it is recommended in [Step 23 of Open5GS's VoLTE tutorial](https://open5gs.org/open5gs/docs/tutorial/02-VoLTE-setup/).

```bash
# Add '--pcsc-shared' flag if the card status includes 'Shared Mode'
# p: Card reader number in the 'pcsc_scan' output
./pySim-shell.py --pcsc-shared -p 1
pySIM-shell > verify_adm <your-admin-key>
pySIM-shell > select ADF.USIM/EF.USIM_SQN
pySIM-shell > read_binary_decoded # Check the "sqn_check" value
pySIM-shell > update_binary_decoded --json-path $.flag1.sqn_check false
pySIM-shell > read_binary_decoded # Check again if the "sqn_check" value is false
```

3. IMS activation by overriding the IMS settings may be required if the PLMN ID of the SIM card is not in the [carrier list](https://android.googlesource.com/platform/packages/providers/TelephonyProvider/+/refs/heads/main/assets/sdk34_carrier_id/carrier_list.textpb). SysmoISIM cards can connect to IMS without needing extra SIM card configuration if the PLMN ID is **00101**, but for some PLMN IDs such as **999xx**, the following steps should be applied:

* The carrier permissions should be activated. (See [SIM Card Setup for Carrier Permissions](./carrier-permissions.md))

* Open the OMNT app and enable `CARRIER_VOLTE_AVAILABLE_BOOL` and `CARRIER_VOLTE_PROVISIONED_BOOL` in `Settings/Mobile Network/Android 10 API 29 (Q)`. Then, press `Apply carrier settings now`.