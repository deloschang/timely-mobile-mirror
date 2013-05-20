Introduction
-----------

Android project for Timely, a Google Now for college students.

Build
-----
Requires ant builder, and the Android Base SDK. All .jar add-ons go in libs/
All dependencies are found in libs/. Change your filepath accordingly. 

Build using
```
ant clean; ant debug; ./install_app.sh 
```

once your Android phone is connected over USB. 

App name: Timely
Package name: dartmouth.timely

Google Maps API 
- 
* For Google Maps API v2, you may need to sign your own debug SHA1 certificate
for the correct API key (~/.android/debug.keystore).


Troubleshooting
-------------
* In Eclipse, if 'Import Existing Project to Workspace' doesn't work, try
'File -> Import -> Existing Android Code to Workspace'


