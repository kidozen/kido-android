#Kidozen SDK for Android Devices

In order to use the Android SDK in your android application

- Create a libs directory within your Android project directory if one does not already exist.
- Copy the .jar file in this directory
- Run the application

You can also download the source code and import the project into your Eclipse workspace

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
