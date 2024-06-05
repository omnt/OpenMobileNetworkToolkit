# Home Screen 

When starting the OMNT or touching the OMNT logo in the top left corner the home screen is displayed.
Here are all information the OMNT can collect displayed. Different cards grouped the data into categories.
The data displayed is updates on load and swipe down. This way the data of the time of refreash can be analyzed.
The same grouping is applied to the data send to the InfluxDB.

## Cell Information
This card displays information from the Android [CellInfo API](https://developer.android.com/reference/android/telephony/CellInfo). The cell informations are on the cell the phone is camping on or registered to. If neighbourcells are present / announecd and display of neigbourcells is enabled in the settings they are displayed to. Information  on neighbours cells are usualy limitted.

## Signal Strength Information
This card displays information from the Android [Signal Strength API](https://developer.android.com/reference/android/telephony/SignalStrength). Signal informations have less properties then the Cell info API but on some devices they seem to be more accurat / more often updated. 

## Network Information
This card presents the current network state of the phone. This includes the current mobile network registration as well as the default route, currently used access network and DNS.


## Device Information
This card displays information on the phone it self. This information only change on e.g. firmware updates. It is often helpfull to e.g. compare baseband versions between to phones to spot the cause for different behavior.

## Device Features
This card displays the availablilty of some relevant APIs on the phone.

## APP Permissions
This card displays the permissions granted to the OMNT.

## Network Interfaces
This card lists allnetwork interfaces of the UE and there current IP addresses.

## Location
This card shows the current location information provided to OMNT by Android.