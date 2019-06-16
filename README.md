<h1>Energy Management System</>
<h4>by Alexander Karrer</>

This Energy Management System analyzes the energy consumption of your smart home and provides a nice way to look at the data.

<b>It contains 3 Parts:</b>
<ul>
  <li>Energy Management Backend System - Used for getting the raw data from your OpenHAB installation.
  <li>Energy Management REST Service - Used by the Android App to get data from the backend.
  <li>Energy Management Android App - Used for viewing your energy consumption in a nice way.
</ul>
<h2>Installation Guidelines</h2>
<h3>Requirements</h3>
<ul>
  <li>A running OpenHAB installation with Bindings and Items for your energy actors/sensors.
  <li>A server to run the Backend and REST Service on.
  <li>SQLite 3 installed on your server.
  <li>Apache Tomcat 9 installed on your server.
  <li>An Android Phone to use the App.
</ul>
<h3>Energy Management System Backend</h3>
Import the EMS Project into your IDE (I used Eclipse) and the export the project as runnable jar.</br>
On your server place the jar in the "~/EMS/" directory.</br>
To start the Backend Service use: java -jar EMS.jar <IP:Port></br>
If you need any help, refer to the "help" parameter.
<h3>Energy Management REST Service</h3>
To use the REST Service you have to build the .war file from the Maven project and deploy in on your Tomcat.</br>
The used REST URLs are: 
<ul>
  <li>/item/{name}
  <li>/item/{name}/{startDate}
  <li>/item/{name}/{startDate}/{endDate}
</ul>
