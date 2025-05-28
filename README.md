# Java Simple Fat-jar QuickStart

This quickstarts run in a Java standalone container using the fat-jar style.


### Building

Navigate to the $IPAAS_QUICKSTART/quickstart/java/simple-fatjar/ folder and the example can be built with

    mvn clean install


### Running the example locally

The example can be run locally using the following Maven goal:

    mvn exec:java


### Running the example as jar

    mvn package
    java -jar target/gmaf-mvr-ui-x.y.z-executable.jar

                              
## Usage

- Use conf/gmaf.config for configuration, e.g. collection path.
- The password is also configured in conf/gmaf.config as API-Key - default is 'letmein'.

### More details

You can find more details about running this [quickstart](http://fabric8.io/guide/quickstarts/running.html) on the website. This also includes instructions how to change the Docker image user and registry.

