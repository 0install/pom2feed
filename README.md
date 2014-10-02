#Introduction
pom2feed connects the world of Zero Install with [Apache Maven](http://maven.apache.org/). With this project Zero Install gets access to the huge number of Java projects available at [Maven Central](http://search.maven.org/). This is made possible by two components: the ```pom2feed-service``` and the ```pom2feed-maven-plugin```.

#pom2feed-service
The ```pom2feed-service``` is a [Java Servlet](http://en.wikipedia.org/wiki/Java_Servlet) which transparently maps the [POMs](http://maven.apache.org/pom.html) from Maven Central to [Zero Install feeds](http://0install.net/interface-spec.html).

You can use [Java system properties](http://docs.oracle.com/javase/tutorial/essential/environment/sysprop.html) to configure the service:
* ```pom2feed-service.serviceURL``` (MUST be set to URL where service is hosted)
* ```pom2feed-service.mavenRepository``` (MAY be set to alternative Maven repository)
* ```pom2feed-service.gnuPGKey``` (MUST be set to ID of GnuPG signing key)

An instance of the pom2feed-service is hosted at http://maven.0install.net/. Have a look at the (automatically generated) [Google Guava feed](http://maven.0install.net/com/google/guava/guava/) for an example.

#pom2feed-maven-plugin
To comfortably create Zero Install feeds for your own Maven project you can use the ```pom2feed-maven-plugin```. This will convert your Maven dependencies to Zero Install dependencies (pointing to the ```pom2feed-service```) so you can deploy your application without having to include or host the dependencies yourself. To do this you have two options: include ```pom2feed-maven-plugin``` in your POM or invoke it from the command-line.

##Include it in your POM
Add the following to your project's POM to automatically generate a feed at Maven's package goal.
```xml
<build>
   <plugins>
      ...
      <plugin>
         <groupId>net.zeroinstall.pom2feed</groupId>
         <artifactId>pom2feed-maven-plugin</artifactId>
         <version>1.0.0</version>
         <executions>
            <execution>
               <phase>package</phase>
               <goals>
                  <goal>generate</goal>
               </goals>
            </execution>
         </executions>
      </plugin>
      ...
   </plugins>
</build>
```
After executing ```mvn package``` at your Maven project root you can find the generated feed in the ```target``` folder.

##Generate feed from the command-line
If you don't want to include the plugin in your POM, or you just want to try it out once, you can invoke the plugin from the command-line. Just run ```mvn net.zeroinstall.pom2``` and feed will be created in the ```target``` folder.

An example of a feed generated for the project [Ldif Sort](https://bitbucket.org/nanobyte/ldif-sort) can be found [here](https://bitbucket.org/nanobyte/ldif-sort/src/tip/ldif-sort.xml).
