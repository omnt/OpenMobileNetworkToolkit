# Logging Settings

## Logging Service
* Enable: Start / Stop the logging component of OMNT. This effects all data logging targets, iPerf and Ping measurements and the Notification bar.
* Start on boot: Start OMNT and its logging component on device boot.
* Interval: Logging interval in milliseconds
* Enable Notification Update: Show current cell information in the notification area of android. This will wake up the screen and my raise power consumption.

## Local Logging
* InfluxDB log: This is work in progress and should allow the usage of an on-device database
* Log file: Measurements a written to a file on the phones internal memory.

## Remote Logging
This section configured the remote logging function to an InfluxDB. This has been tested with on-prem InfluxDB 2.x installation
as well with InfluxDB 3.x cloud.

* InfluxDb Log: Enable the logging to remote database
* InfluxDB instance URL / IP: Address of the database e.g. ```https://example.com/influxd:443```
* Influx Organization: Either ID or Name of the Org to which the bucket and token belongs
* Influx Token: The InfluxDB API token to be used
* Influx Bucket: Name or ID of the bucket to be used

## Logging Content
Here the content of what data should be logged is configured. Note that this also affects all logging targets. 
* User Fake location: This will replace the GNSS location with a static one. Mostly useful for developers.
* Measurement Name: This name will be used to identifier the measurement later on.
* Tags: This should at least have ```device=YourDevice``` as a tag. Further tags can be added comma separated.
* Log network information: log data displayed in the network information card on the home screen
* Log signal data: log data displayed in the signal info card on the home screen
* Log cell information: log data displayed in the cell information card on the home screen
* Log neighbor cells: include neighbor cells in the log
* Log throughput data: log interface throughput statistics
* Log battery information: include current battery and charging state in the log
* Log IP addresses: Include current IP addresses in the log

[Settings](settings.md) | [Home](../OpenMobileNetworkToolkit.md)