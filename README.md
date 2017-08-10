# walk-detector
## An Android app that notifies you when you start walking by sending you a notification and playing a sound.

I made this app because I always forget to turn on the [Charity Miles](http://www.charitymiles.org/) app when I go for a walk.
And also I wanted to try the play-services-fitness API.
The minimum Android version required is API level 21  5.0 (LOLLIPOP) 

## In the project I use:
 - [Fitness History API](https://developers.google.com/fit/android/history) to query for step_count.delta in the checked period of time
 - [ReactiveX/RxAndroid](https://github.com/ReactiveX/RxAndroid) to describe my interval based logic in more readable way
 - [Service](https://developer.android.com/guide/components/services.html) which is defined as indestructible and handles the detection
 - [Alarm manager](https://developer.android.com/training/scheduling/alarms.html) to send auto start and stop Intents to the Service
 - [Lambda expressions](https://docs.oracle.com/javase/tutorial/java/javaOO/lambdaexpressions.html)
 - [Butterknife](http://jakewharton.github.io/butterknife/) field and method binding for Android views
 - [Adaptive icons](https://developer.android.com/preview/features/adaptive-icons.html) just to test them
 - [AnimatedVectorDrawable](https://developer.android.com/reference/android/graphics/drawable/AnimatedVectorDrawable.html) to animate the FAB icon on the Home screen

## How to run the project:

 - [Get an OAuth 2.0 Client ID](https://developers.google.com/fit/android/get-api-key) this is needed for developer authentication for the Fit App. The link describes how to generate it. Once generated it is not used anywhere. The servers know that the build is made on your machine because of the Certificate fingerprints you give them through the generation process.
 - Open the project in Android Studio
 - You will need [Java 8](https://developer.android.com/studio/write/java8-support.html). Update the Android plugin to 3.0.0-alpha1 (or higher) or add [Retrolambda](https://github.com/evant/gradle-retrolambda) to the gradle.
 - In your gradle.properties file you need to add:
```
    RELEASE_STORE_FILE=<path_to_your_jks>
    RELEASE_STORE_PASSWORD=<password>
    RELEASE_KEY_ALIAS=<key_alias>
    RELEASE_KEY_PASSWORD=<password>
```
 - In order for the certificate to work you may need to Build signed APK from Build > Generate Signed APK > Create new / Choose existing



Please feel free to contact me if you have any questions. :)
