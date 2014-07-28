# Kidozen SDK for Android Devices
The KidoZen SDK for Android provides libraries for developers to build connected mobile applications using KidoZen. This guide walks through the steps for setting up the SDK and running the code samples.

## About the SDK
The KidoZen SDK for Android includes:
- Source code
- Class libraries: Build Android applications on top of class libraries that hide much of the lower-level plumbing of the web service interface, including authentication, caching, and error handling.

## Get Set Up
To get Your Credentials Register at [KidoZen]("http://kidozen.com/")

## Requirements
- API Level 17 or higher. For more information on the Android SDK, [see]("http://developer.android.com/index.html">http://developer.android.com/index.html)
- To run the code examples, you also need the Android Studio 0.8.1 or above

## How to Include the KidoZen SDK for Android in an Existing Application
The SDK now is packaged as a Gradle project, you can either include the full code or inculde a jar file
## Adding client library in Android Studio

- Create a new Android Project
- Create a new 'library' folder in your project and copy the SDK folder inside it
- Open the file settings.gradle and add the following line of code: 'include ':library:client' This set ups the client as a new module
- Select your application module settings and add the client depency
- Rebuild your project to update all dependencies

## Building the SDK as .jar file
You can also create the jars file using gradle by typing the following in your terminal:

		./gradlew jarRelease androidJavadocsJar androidSourcesJar

This will generate two files in the 'builds/libs' folder

- client.jar : the SDK as .jar file
- client-javadoc.jar : javadocs
- client-sources.jar: sources

## Execute integration tests
The integration tests code can help you to understand how to use some features of the SDK. To run the tests you should :

- Navigate to the folder: 'client/src/test/java' and update the file 'AppSettings.java' with your KidoZen environment details.
- Run the tests using gradle by running the following command:

		./gradlew tests

##Getting started with the code
One instance of the Application object has one or many instances of each of the services that you can find in the Kidozen platform (Storage, Queue, etc.) SDK API is callback based on all its interfaces, so it will never block the UI. The callback signature is the same for all the methods:

- StatusCode is the http status code response that the service invocation has returned
- Response the response body of the call. It could be an string with the description of the operation such as “Created” or “Internal server error” or a JSON Object with the results of one operation
- Exception the Exception object is there was one

###Initialization
During initialization the SDK pulls the application configuration from the cloud services

		KZApplication app = new KZApplication(TENANT, APP_NAME, APP_KEY, new ServiceEventListener() {
		    @Override
		    public void onFinish(ServiceEvent e) {
		    }
		});

Getting application key

This key is used for secure authentication in the KidoZen SDK’s, you must use this key in order to send crash logs to KidoZen platform. To get it browse the application marketplace and select the target application, you can find it below the application icon.

###Authentication
You must provide the identity provider that you will use the username and the password (Kidozen, Google, etc.). The SDK hides all the calls needed to authenticate the user against the selected identity provider and to create a security context to execute all the services call.

    app.Authenticate(PROVIDER, USER, PASS, new ServiceEventListener() {
        @Override
        public void onFinish(ServiceEvent e) {
        }
    });

For more information please check the [KidoZen documentation](http://docs.kidozen.com/)

## Samples
You wil find samples about Social Authentication, Push notifications and other services inside the 'Samples' folder.

###Breaking changes

- The constructor now requires an Application Id. You must get this ID from your marketplace
- The 'ByPassSSL' parameter changed his name and default value. Now its called 'StrictSSL' and its default value is TRUE instead of FALSE
- The 'OnSessionExpirationRunnable' method has been remove from the KZApplication object. You can create your own expiration handler using GetExpirationInMiliseconds() method of the KidoZen user instance

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
