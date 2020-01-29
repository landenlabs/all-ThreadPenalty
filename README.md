
# LanDen Labs - Thread Locality Penalty 
Android 29-Jan-2020
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

  
[![Known Vulnerabilities](https://snyk.io/test/github/landenlabs/all-ThreadPenalty/badge.svg)](https://snyk.io/test/github/landenlabs/all-ThreadPenalty)


## Screen shots

Starting test in Portrait mode - shows graph and thread messages.
<p>
<img src="https://raw.githubusercontent.com/landenlabs/all-ThreadPenalty/master/screenshots/test-portrait-running-2.jpg" width="300">

<p>
Pressing on Thread Messages expand icon cycles height of Message area:<br>
<img src="https://raw.githubusercontent.com/landenlabs/all-ThreadPenalty/master/screenshots/test-portrait-running-3.jpg" width="300">
<p>
 
**Samples of App User Interface:**
<p>
About page <br>
<img src="https://raw.githubusercontent.com/landenlabs/all-ThreadPenalty/master/screenshots/about-page.jpg" width="300">    
<p>
Side Navigation <br>
<img src="https://raw.githubusercontent.com/landenlabs/all-ThreadPenalty/master/screenshots/side-nav.jpg" width="300"> 
<p>
Side Menu <br>
<img src="https://raw.githubusercontent.com/landenlabs/all-ThreadPenalty/master/screenshots/side-menu.jpg" width="300"> 
  
<p>  
**Graphs from several devices in Landscape mode:**

Google Pixel1 - Graph shows that the concurrent access to similar locations is slower.
![Pixel 1](https://raw.githubusercontent.com/landenlabs/all-ThreadPenalty/master/screenshots/pixel1-landscape-done.jpg)

Google Pixel 2 - Graph shows that the concurrent access to similar locations is slower.
![Pixel 2](https://raw.githubusercontent.com/landenlabs/all-ThreadPenalty/master/screenshots/pixel2-landscape-done.jpg)

Google Nexus 5x - Graph shows very odd timing (need to repeat test)
![Nexus5x](https://raw.githubusercontent.com/landenlabs/all-ThreadPenalty/master/screenshots/nexus5x-landscape-done.jpg)


Samsung Galaxy S10 (64 bit) - Graph shows that the concurrent access to similar locations is slower.
![Galaxy S10](https://raw.githubusercontent.com/landenlabs/all-ThreadPenalty/master/screenshots/galaxy-s10-landscape-done.jpg)

Samsung Galaxy S5 (32bit) - Similar graph, similar location access by threads slower then far apart access. 
![Galaxy s5](https://raw.githubusercontent.com/landenlabs/all-ThreadPenalty/master/screenshots/galaxy-s5-landscape-done.jpg)


## Web Page  
  
[Home web page](http://landenlabs.com/android/index.html) for more information.  

[Discussion about Thread Locality Penalty](http://landenlabs.com/code/locality.html)

[CPU hardware cache ](https://mechanical-sympathy.blogspot.com/2013/02/cpu-cache-flushing-fallacy.html)
 
 [Herb Sutter discussion on Thread Penalty](https://www.drdobbs.com/parallel/maximize-locality-minimize-contention/208200273) 
  
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

