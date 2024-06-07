# Set Subscription
OMNT needs to attach it self to a so called Telephony Manager. Each Telephony Manager instance is tied to an active subscription (SIM card).
Device with support for multiple subscriptions e.g. eUICCs and UICCs allow the usage of multiple subscriptions at the same time. Default OMNT will try to use the
first active one which is most of the time the subscription configured for calls in the Android settings. 
By setting a different subscription e.g. a SIM card with carrier permissions enabling hashes deployed can be selected while connecting with a second subscription to a
mobile network. Some feature will be disabled / not work for subscription without the permissions.


[Settings](settings.md) | [Home](../OpenMobileNetworkToolkit.md)