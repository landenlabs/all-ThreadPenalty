
# LanDen Labs - Thread Locality Penalty 
Android UI Test  [Home website](http://landenlabs.com/android/index.html)  
26-Jan-2020
  
Measure Thread Locality Penalty which is the extra time wasted when multiple threads access memory close together.  Concurrent memory modification can cause CPU memory caches to get invalidated inflicting extra time on the 2nd thread accessing the cached memory location.


Using C++ Threads, access memory from multiple threads and measure access time. Plot graph of access time based on distance between memory locations accessed by concurrent threads.

[Article on Thread Locality Penalty](http://landenlabs.com/code/locality.html)

## NOTES on App

The app is based off of template app I use and has support for multiple fragment pages and menus.

In this app only the main fragment page is populated, the rest is left as placeholders to add features and functionality or just delete it. 

The *share* button works to send a sample screen shot. 

* Program is written in Java and C++.
* Android SDK compile and target 28
* MinSDK 23
* Using AndroidX libraries

## Screen shots

Starting test in Portrait mode - shows silly clock threads running and scroll thread messages below graph

![Test running](https://raw.githubusercontent.com/landenlabs/all-ThreadPenalty/master/screenshots/test-portrait-running-1.jpg)

![Test running](https://raw.githubusercontent.com/landenlabs/all-ThreadPenalty/master/screenshots/test-portrait-running-2.jpg)

![Test running](https://raw.githubusercontent.com/landenlabs/all-ThreadPenalty/master/screenshots/test-portrait-running-3.jpg)


Landscape mode hides most of the useless data, leaving the graph

![Test running](https://raw.githubusercontent.com/landenlabs/all-ThreadPenalty/master/screenshots/test-landscape-running-1.jpg)

![Test running](https://raw.githubusercontent.com/landenlabs/all-ThreadPenalty/master/screenshots/test-landscape-running-2.jpg)

![Test running](https://raw.githubusercontent.com/landenlabs/all-ThreadPenalty/master/screenshots/test-landscape-running-3.jpg)

![Test running](https://raw.githubusercontent.com/landenlabs/all-ThreadPenalty/master/screenshots/test-landscape-done.jpg)

## Web Page  
  
[Home web page](http://landenlabs.com/android/index.html) for more information.  
  
## License  
  
```  
Copyright 2019 Dennis Lang  
  
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

