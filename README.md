# Java Simple Fat-jar QuickStart

This quickstarts run in a Java standalone container using the fat-jar style.

This example is implemented using very simple Java code.
The source code is provided in the following java file `src/main/java/io/fabric8/quickstarts/java/simple/fatjar/Main.java`,
which can be viewed from [github](https://github.com/fabric8io/ipaas-quickstarts/blob/master/quickstart/java/simple-fatjar/src/main/java/io/fabric8/quickstarts/java/simple/fatjar/Main.java).

This example is printing *Hello Fabric8! Here's your random string: lRaNR* to the standard output in the infinite loop.


### Building

Navigate to the $IPAAS_QUICKSTART/quickstart/java/simple-fatjar/ folder and the example can be built with

    mvn clean install


### Running the example locally

The example can be run locally using the following Maven goal:

    mvn exec:java


### Running the example in fabric8

   mvn package
   java -jar target/gmaf-mvr-ui-x.y.z.jar

### More details

You can find more details about running this [quickstart](http://fabric8.io/guide/quickstarts/running.html) on the website. This also includes instructions how to change the Docker image user and registry.

