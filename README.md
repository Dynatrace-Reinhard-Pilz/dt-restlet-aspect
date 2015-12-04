# dt-restlet-aspect
A solution to provide end to end coverage for HTTP calls served by the Restlet API within dynaTrace

## Objective
The [Restlet Framework](http://restlet.com/projects/restlet-framework/) offers a rich API for publishing RESTful Web Services - either embedded into a Serlvet Engine or within a standalone application.
In the latter case there is unfortunately no out of the box visibility (yet) when monitoring your application with [Dynatrace](http://www.dynatrace.com/en/free-apm-tools.html#dt-free-trial).

This solution utilizes the Dynatrace ADK for Java in order to change that.

## Build Instructions
* This solution represents an Eclipse Project created for [Eclipse Mars](https://eclipse.org/mars/)
* Your Eclipse installation needs to include the [AspectJ Development Tools](http://www.eclipse.org/ajdt/)
  - AJDT Update Site - http://download.eclipse.org/tools/ajdt/44/dev/update
* Import the existing Project into your Eclipse Workspace
* In order to compile and link the ```dt-restlet-aspect.jar``` right click on ```<PROJECT>/dt-restlet-aspect.jardesc``` within the Eclipse Package Explorer and select ```Create JAR (AspectJ)```
* A precompiled version of ```dt-restlet-aspect.jar``` is available within [this repository](https://github.com/Dynatrace-Reinhard-Pilz/dt-restlet-aspect/blob/master/dt-restlet-aspect.jar?raw=true) for download and should also be contained within your Eclipse Project

## Installation Instructions
* Install AspectJ
  - Download [AspectJ 1.8](http://www.eclipse.org/downloads/download.php?file=/tools/aspectj/aspectj-1.8.7.jar) and extract it.
  - The following instructions assume that a folder ```/opt/aspectj1.8``` is the installation folder of AspectJ.
* Rebuild ```dt-restlet-aspect.jar``` or download it from [GitHub](https://github.com/Dynatrace-Reinhard-Pilz/dt-restlet-aspect/blob/master/dt-restlet-aspect.jar?raw=true)
  - The following instructions assume that a folder ```/opt/dt-restlet-aspect``` contains ```dt-restlet-aspect.jar```
* Download [org.restlet-2.2.3.jar](http://maven.forgerock.org/repo/repo/org/restlet/jee/org.restlet/2.2.3/org.restlet-2.2.3.jar)
  - This file is also available for download within [this repository]https://github.com/Dynatrace-Reinhard-Pilz/dt-restlet-aspect/blob/master/lib/org.restlet-2.2.3.jar?raw=true) in case the link above does not work
  - The following instructions assume that a folder ```/opt/dt-restlet-aspect``` contains ```org.restlet-2.2.3.jar```
* Download [dynatrace-adk-6.2.0.1147.jar](https://github.com/Dynatrace-Reinhard-Pilz/dt-restlet-aspect/blob/master/lib/dynatrace-adk-6.2.0.1147.jar?raw=true) which is included in this repository
  - The following instructions assume that a folder ```/opt/dt-restlet-aspect``` contains ```dynatrace-adk-6.2.0.1147.jar```
* Download [javax.servlet-api-3.0.1.jar](http://central.maven.org/maven2/javax/servlet/javax.servlet-api/3.0.1/javax.servlet-api-3.0.1.jar) or copy it from your local maven repository cache
  - The following instructions assume that a folder ```/opt/dt-restlet-aspect``` contains ```javax.servlet-api-3.0.1.jar```
* Ensure by modifying the ```CLASSPATH``` variable or by specifying the ```-cp``` or ```-classpath``` JVM argument that these files are part of the JVMs classpath
  - ```/opt/dt-restlet-aspect/dynatrace-restlet-aspect.jar```, ```/opt/dt-restlet-aspect/dynatrace-adk-6.2.0.1147.jar```, ```/opt/dt-restlet-aspect/javax.servlet-api-3.0.1.jar``` and ```/opt/aspectj1.8/lib/aspectjrt.jar```
  - In case your application is based on Java Servlets (e.g. because it is a J2EE Web Application) there is no need to include ```javax.servlet-api-3.0.1.jar```
  - Example for a bash start script in Linux: ```CLASSPATH=$CLASSPATH:/opt/dt-restlet-aspect/dynatrace-restlet-aspect.jar:/opt/dt-restlet-aspect/dynatrace-adk-6.2.0.1147.jar:/opt/aspectj1.8/lib/aspectjrt.jar:/opt/dt-restlet-aspect/javax.servlet-api-3.0.1.jar```
* The AspectJ Weaver Agent needs to be specified via JVM Arguments *before* the ```–agentpath``` Argument for the dynaTrace Agent
  - Example for Linux: ```java -javaagent:/opt/aspectj1.8/lib/aspectjweaver.jar -agentpath:/opt/dynatrace-6.2/agent/lib64/libdtagent.so=name=<agentname>```
