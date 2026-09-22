# Debug signing

The debug APK is intentionally signed with a fixed development key so sideloaded test builds can update one another.

Certificate SHA-256:

`DB:2B:F5:09:CF:41:D8:D2:87:39:0D:B0:1F:6D:FC:9D:14:D5:8D:CF:56:67:E7:50:7F:C0:2F:ED:B1:D6:E0:4F`

This key is for development/testing only. It must never be used to sign a Play Store or other production release. Production must use a separate private release key.
