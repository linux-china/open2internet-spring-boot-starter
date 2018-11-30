open2internet-spring-boot-starter
=================================

Expose your local Spring Boot(2.0+) Application to internet, for example testing, demonstration for customers.

### Use cases

* Prototype demonstration for your customers.
* Interaction with your audiences during presentation, and you know you application runs on your laptop.
* Development debug if you develop applications connected with Wechat, facebook etc, and you know HTTP callback required from these platforms.

### How to use

* please add jitpack repository in your pom.xml

```xml
<repositories>
	<repository>
	    <id>jitpack.io</id>
	    <url>https://jitpack.io</url>
	</repository>
</repositories>
```

* Add open2internet dependencies

```xml
<dependency>
  <groupId>com.github.linux-china</groupId>
  <artifactId>open2internet-spring-boot-starter</artifactId>
  <version>1.0.0-SNAPSHOT</version>
</dependency>
```

* Start your local Spring Boot application, and get hint from console.
```
open2internet by @linux_china

Connected Status              online
Management Token              dtie7of5
Internet Web Interface        https://19erktgk.microservices.club
Internet Web QR Code          https://api.qrserver.com/v1/create-qr-code/?size=150x150&data=https%3A%2F%2F19erktgk.microservices.club
Local Web Interface           http://127.0.0.1:58274
Forwarding Rule               https://19erktgk.microservices.club -> http://127.0.0.1:58274
```

* Click url of internet web interface for testing, or copy it to share with other guys.


### Q&A

* Https by default, and supplied by Let's Encrypt. You can use http also.
* You can customize your domain name permanently, for example appName.foobar.com, please contact me, just cname required.
* Tips: please share QR Code to your audiences during presentation.
* Why not ngrok? you know you are developing Spring Boot application, and make life easy. :beer:
* How to disable open2internet feature? Please add following on your application.properties.

```properties
open2internet.disabled=true
```

### Todo

* Management console to replay HTTP requests.
* websocket support.
* Performance: no 127.0.0.1 http request.

### Thanks

I want to say thanks to [RSocket](http://rsocket.io) & [Reactor](https://projectreactor.io/), and I just write little code to implement this features, really true. :smile:

### References

* RSocket:  http://rsocket.io/
* RSocket Java: https://github.com/rsocket/rsocket-java
* ngrok: https://ngrok.com/
* Let's Encrypt: https://letsencrypt.org/
