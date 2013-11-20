#Kidozen SDK for Android Devices

In order to use the Android SDK in your android application

- Create a libs directory within your Android project directory if one does not already exist.
- Copy the .jar files from "lib" and "third-party" on the application's directory
- Run the application

The SDK now is packaged as a Maven project, you can download the code and import it into your favorite android IDE:

- Eclipse workspace (by typing mvn:eclipse eclipse in your terminal)
- IntelliJ Project
- Android Studio project 

Please check the following [Prerequisites](https://code.google.com/p/maven-android-plugin/wiki/GettingStarted)



##Getting started with the code

One instance of the Application object has one or many instances of each of the services that you can find in the Kidozen platform (Storage, Queue, etc.) SDK API is callback based on all its interfaces, so it will never block the UI. The callback signature is the same for all the methods: 

- `StatusCode` is the http status code response that the service invocation has returned
- `Response` the response body of the call. It could be an string with the description of the operation such as "Created" or "Internal server error" or a JSON Object with the results of one operation
- `Exception` the Exception object is there was one

Initialize the Application: During initialization the SDK pulls the application configuration from the cloud services for the specified platform
  	
		KZApplication app = new KZApplication(TENANT,APPLICATION, new ServiceEventListener() {
			@Override
			public void onFinish(ServiceEvent e) {
			}
		});

Authenticate: you must provide the identity provider that you will use the username and the password. The SDK hides all the calls needed to authenticate the user against the selected identity provider and to create a security context to execute all the services call. 

		app.Authenticate(PROVIDER, USER, PASS, new ServiceEventListener() {
			@Override
			public void onFinish(ServiceEvent e) {
			}
		});

Once the user is authenticated you can start using all the services:

		tasks = app.Storage("tasks");
		tasks.Create(message, new ServiceEventListener() {
			@Override
			public void onFinish(ServiceEvent arg0) {
				...
			}
		});
		...
		queue = app.Queue("messages");
		queue.Enqueue(message, new ServiceEventListener() {
			@Override
			public void onFinish(ServiceEvent arg0) {
				...
			}
		});
		...

##Create the .jar file to include in your projects

To create the .jar library file run the following command in a terminal that will create the file `kz.client.jar` in the `target` folder

    mvn package -Dmaven.test.skip=true


##Runnig Integration tests

The solution contains Integration test (not unit tests) These tests does not requires an Android Emulator image or device.
Before you can run these tests you must update the the file settings.json:

REMARK: To run this tests be sure you have JAVA 1.7 or above and configure the test host runner to use it.

	{
		/*
		* KidoZen general configuration
		*
		* Replace with the right values
		* */
		"kz_tenant" :  "your tenant url",
		"kz_app" : "your app name",
		"kz_usr" : "your user name",
		
		"kz_pass" : "your password",
		"kz_provider" : "Kidozen",
		
		/*
		* Enterprise services configuration
		*
		* Replace with the right values
		*
		* You must configure the following service in the Global section of your KidoZen Marketplace
		*
		* */
		"kz_sharefile_serviceid" : "sharefile";
		"kz_sharefile_user" : "your sharefile user name";
		"kz_sharefile_pass" : "your sharefile password";
		
		
		/*
		* EMail configuration
		*
		* Replace with the right values
		* */
		"kz_email_to" : "some recipient",
		"kz_email_from" : "another recipiet"
		"kz_email_attach" : "path to a file to attach",
	}

Check your Android sdk path settings. If you have some conflict remember you can specify it in the "android.sdk.path" 
element in the pom.xml file 

To run the integration tests simply run the following command in a terminal

	mvn test -Dsettings="settings.json"

##Create the documentation

To create the documentation in html format run the following command in a terminal and check the  `target` folder

    mvn javadoc:javadoc



#License 

Copyright (c) 2013 KidoZen, inc.

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
