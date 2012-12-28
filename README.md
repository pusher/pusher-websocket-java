<h1>pusher-java-client</h1>

Pusher client library for Java targeting general Java and Android.

<h3>Cloning the project</h3>

* If you don't have it already, download Maven and add the ````mvn```` executable to your path.
* Clone the project: ````git clone git@github.com:leggetter/pusher-java-client.git````
* Change to the top level directory for the project: ````cd pusher-java-client````
* Switch to the development branch: ````git checkout public-channel````
* Retrieve the Java-WebSocket library: ````git submodule update --init````

<h3>Develop</h3>

Assuming you are using Eclipse, execute ````mvn eclipse:clean eclipse:eclipse```` to generate the ````.project```` and ````.classpath```` files. Then open Eclipse and import the project as normal.

<h3>Build</h3>

From the top level directory execute ````mvn clean test```` to compile and run the unit tests or ````mvn clean install```` to build the jar. The jar will be output to the ````target```` directory.