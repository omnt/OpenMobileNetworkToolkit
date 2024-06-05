# Quick start guide

This guide aims to bring you quickly to a working setup with InfluxDB and Grafana by using the cloud accounts. You will be able to store your measurements in an InfluxDB instance and visualize the data with our example dashboard in Grafana. This guide further assumes you have an Android phone with a compatible Android Version and you either already know how to build OMNT yourself or a release APK.

Note: If you run your own Grafana / InfluxDB you can jump ahead to #TODO

# Accounts
This guide assumes you will use the free cloud accounts of InfluxDB and Grafana. If you don't have accounts for both services:

  * https://cloud2.influxdata.com/signup
  * https://grafana.com/products/cloud/

both services can be used for free to get a first impression, more advanced usage will require a paid subscription or a private / on prem instance.

# Installation
The simplest way to install the app is by downloading it on the phone, if ADB is at hand installing via adb more convenient.

    adb install release.apk

or use android studio to build and install OMNT. Follow #TODO for more information on building the app.

# Setup InfluxDB
Log into your influxDB account and hower over the top symbol in the right menue bar (below the influx logo), in now visible menu select ```buckets```. Click on the ```Create bucket``` button in the top right of the screen. You can name the bucket what ever you want e.g. ```omnt```.

Next click on the ```api tocken``` tab and create an API tocken. Again naming is up to you. 
For simplicity create an ```all access``` token. 
Make sure to copy the token somewhere you find it later, it will not be shown again.

On the top of the window you see you organization, most likely something like ```dev```, write this down to.

The last information you need from influx is the url to your instance. It depence on the cloud zone you choose and will look e.g. like this ```https://eu-central-1-1.aws.cloud2.influxdata.com/```

# Setup Grafana
Log into your Grafna account and add a new datasource by selecting ```Connections``` and than ```add new datasource```. Now choose ```InfluxDB``` and click on ```add datasource```.
Name the datasource what ever you like. As our sample dashboards using Flux as querry language select ```flux``` from the dropdown menu. If you later develop you own dashboards you can also use InfluxQL or SQL on the same data.

Scroll down to ```InfluxDB Details``` and enter the information written down during the InfluxDB setup. 
Click on ```Save & Test```

# Setup OMNT
Now its time to open OMNT and configure its logging component. Touch the three dots in the right top corner to open the app menue. Select ```Settings```

<img align="right" src="images/influx_settings_credentials.png">

