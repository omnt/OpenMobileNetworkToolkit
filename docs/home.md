# Home Screen 
When starting the OMNT or touching the OMNT logo in the top left corner the ```home screen``` is displayed.
The ```home screen``` provides two tabs, the ```quick view``` and the ```detail view```
The ```quick view``` presents the most relevant network parameters and is automatically updated.
On the other hand the ```detail view``` is only  updated on load or refresh to make it more convenient to read.
Here are all information the OMNT can collect displayed. Different cards grouped the data into categories.
The data displayed is updates on load and swipe down. This way the data of the time of refresh can be analyzed.
The same grouping is applied to the data send to the InfluxDB.

>[!NOTE] 
>All information listed below are passive measurements. This means these are information provided by the Android framework collected from 
>different source e.g. the mobile base band. E.g. bandwidth in the cell information card is not active measured value but reported by the api.
>For active measurements see [Iperf3](iperf3.md) and [Ping](ping.md).

## Cell Information
This card displays information from the Android [CellInfo API](https://developer.android.com/reference/android/telephony/CellInfo). The cell information are on the cell the phone is camping on or registered to. If neighbor cells are present / announced and display of neighbor cells is enabled in the settings they are displayed to. Information  on neighbor cells are usually limited.

## Signal Strength Information
This card displays information from the Android [Signal Strength API](https://developer.android.com/reference/android/telephony/SignalStrength). Signal information have less properties than the Cell info API but on some devices they seem to be more accurate / more often updated. 

## Network Information
This card presents the current network state of the phone. This includes the current mobile network registration as well as the default route, currently used access network and DNS.

## Device Information
This card displays information on the phone itself. This information only changes on e.g. firmware updates. It is often helpful to e.g. compare baseband versions between to phones to spot the cause for different behavior.

## Device Features
This card displays the availability of some relevant APIs on the phone.

## APP Permissions
This card displays the permissions granted to the OMNT.

## Network Interfaces
This card lists all network interfaces of the UE and there current IP addresses.

## Location
This card shows the current location information provided to OMNT by Android.

[Home](OpenMobileNetworkToolkit.md)