# LanDen Labs - Thread Locality Penalty 
Android 26-Jan-2020
[Home website](http://landenlabs.com/android/index.html)  
  
Measure Thread Locality Penalty which is the extra time wasted when multiple threads access memory close together.  Concurrent memory modification can cause CPU memory caches to get invalidated inflicting extra time on the 2nd thread accessing the cached memory location.

Using C++ Threads, access memory from multiple threads and measure access time. Plot graph of access time based on distance between memory locations accessed by concurrent threads.

[Article on Thread Locality Penalty](http://landenlabs.com/code/locality.html)

## NOTES on App

The app is based off of template app I use and has support for multiple fragment pages and menus.

In this app only the main fragment page is populated, the rest is left as placeholders to add features and functionality or just delete it. 

The *share* button works to send a sample screen shot. 

* Program is written in **Java** and **C++**.
* Android SDK compile and target 28
* MinSDK 23
* Using **AndroidX** libraries

  
[![Build status](https://travis-ci.org/landenlabs/all-ThreadPenalty.svg?branch=master)](https://travis-ci.org/landenlabs/all-ThreadPenalty)
[![Known Vulnerabilities](https://snyk.io/test/github/landenlabs/all-ThreadPenalty/badge.svg)](https://snyk.io/test/github/landenlabs/all-ThreadPenalty)


## Screen shots

Starting test in Portrait mode - shows graph and thread messages.
<img src="https://raw.githubusercontent.com/landenlabs/all-ThreadPenalty/master/screenshots/test-portrait-running-3.jpg" width="300">


Graphs from several devices in Landscape mode:

### Google Pixel 1 (64 bit)
![Pixel 1](https://raw.githubusercontent.com/landenlabs/all-ThreadPenalty/master/screenshots/pixel1-landscape-done.jpg)

### Samsung Galaxy S10 (64 bit) 
![Galaxy S10](https://raw.githubusercontent.com/landenlabs/all-ThreadPenalty/master/screenshots/galaxy-s10-landscape-done.jpg)

### Samsung Galaxy S5 (32bit)
![Galaxy s5](https://raw.githubusercontent.com/landenlabs/all-ThreadPenalty/master/screenshots/galaxy-s5-landscape-done.jpg)


## Web Page  
  
[Home web page](http://landenlabs.com/android/index.html) for more information.  
  
## License  
  
```  
Copyright 2020 Dennis Lang  
  
Licensed under the Apache License, Version 2.0 (the "License");  
you may not use this file except in compliance with the License.  
You may obtain a copy of the License at  
  
 http://www.apache.org/licenses/LICENSE-2.0  
Unless required by applicable law or agreed to in writing, software  
distributed under the License is distributed on an "AS IS" BASIS,  
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  
See the License for the specific language governing permissions and  
limitations under the License.  
```   
  
[To Top](#table)  
<br>[Home website](http://landenlabs.com/android/index.html)

