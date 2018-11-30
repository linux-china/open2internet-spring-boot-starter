Open2internet-spring-boot-starter
=================================

Expose your local Spring Boot Application to internet, for example testing, demonstration for customers.

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


### References

* RSocket:  http://rsocket.io/
* RSocket Java: https://github.com/rsocket/rsocket-java
* ngrok: https://ngrok.com/
