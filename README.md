# OpenMobileNetworkToolkit

OMNT provides tooling to researchers and developers of mobile communication networks like 3GPP 2/3/4/5G.
The main objective of OMNT is the collection of measurement data on the mobile network like RSSI, RSRQ, RSRP, GNSS position, Cell ID, PLNM and much more.
On top of those passive measurements on the radio environment and network parameters, OMNT can also run a iPerf3 server / client for bandwidth, latency and jitter evaluation as well as
round-trip-time and jitter evaluation via ICMP (Ping). Measurement data can be stored locally and / or send to an InfluxDB 2.x server.
Measurement results can be visualized e.g. via provided Graphana Dashboards or be further processed e.g. with python.
Besides its measurement capabilities, OMNT provides a deep insight in the state of the phone e.g. software versions, connectivity states, SIM card content and much more.
Network related Carrier Settings can be configured (if the app is granted the corresponding privileges).
Also, OMNT provides access to different "secret" settings in Android phones.

The current state of the app can be described as "research software", it fits our needs but does not aim to be complete or bug free.
Use the app at your own risk. If you find it useful for your research, please cite the app in publications.


* [Quick Start HowTo](docs/quick-start.md)
* [User Manual](docs/OpenMobileNetworkToolkit.md)
* [Signing HowTo](docs/signing.md)

## Why use this app

  * As apps like OMNT can can access a lot of private information. It is important for users to be able to make sure that those data is not
send somewhere else.
  * The app builds on top of official APIs, so it should be stable across devices and android version.
  * The app does not need root privileges and can run on unmodified phones.
  * The app is developed with researches in mind and provides multiple options to export the data in a way that it can be further analyzed.
  * No advertisements or other annoying anti-features

## Motivation

Our motivation to start the development of OMNT was the need to modify carrier settings on Android UEs to integrated them
with our testbeds. Later on, we needed to provide measurements on our testbed deployments for which we had no satisfying tooling
at that point in time.
Since at least a year, we used OMNT to collect measurement for research projects and extended the app with different functions we needed in the projects.

## Permissions

Android provides a strict permission system to access APIs e.g. for information that make a phone identifiable or its location known to an app.
Also, there are APIs meant for carrier to set network specific settings to ensure compatibility with the network. 
Depending on the phone vendor and the android version, the required permission differ as well as if the user is able to allow an app to gain
specific privileges.

The following permissions are requested:

  * Location: Collecting location and network information
  * Storage: Writing measurement log files to the phones memory
  * Read Phone State: Cell Information, Connectivity information
  * Receive Boot complete: Start measurements on boot
  * Foreground Service: Logging 
  * Carrier Permission: Use Carrier APIs

If the app can't gain Carrier Privileges, some values can't be accessed and some features are disabled. See below.


## SPDX-SBOM
The SPDX formatted ```Software Bill Of Material``` is a machine readable list of all software components used in the app. To generate a new file run
```shell
 ./gradlew app:spdxSbomForRelease
```