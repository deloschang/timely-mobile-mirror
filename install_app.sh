#!/bin/bash
ant clean; ant debug; adb shell pm uninstall dartmouth.timely;adb -d install bin/timely-debug.apk 
