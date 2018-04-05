# websms CCMP Library (Android)

SMS2App from websms combines the cost advantages of push messaging and the reliability of SMS texting into one single product. The offered SDK enables our customers to integrate SMS2App into their own application in order to benefit from both worlds – Push and SMS.
 
For more information about SMS2App, visit our [website](https://websms.de/produkte/sms2app)

## Requirements
* Android SDK
* Gradle 3.0.1 or higher
* Android 2.3 or higher

## Demo

The demo application is available [here](https://github.com/websms-com/ccmp-example-android)

## Deployment to local maven repository

It's recommended to deploy the library to your local maven repository.

``` bash
$ cd /path/to/library
$ gradle uploadArchives -PuploadSnapshotRepositoryUri=/home/{username}/.m2/repository
```

## Main methods

The main methods are located at [MessageUtil.java](https://github.com/websms-com/ccmp-lib-android/blob/master/library/src/main/java/net/ut11/ccmp/lib/util/MessageUtil.java)

## Contact

sms.at mobile internet services gmbh<br>
Münzgrabenstraße 92/4<br>
A-8010 Graz

- https://github.com/websms-com
- https://twitter.com/websms_com
- sms2app@websms.com

## License

ccmp-lib-android is available under the MIT license.

Copyright © 2014 sms.at mobile internet services gmbh

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
