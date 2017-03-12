# CS454Group6_Gomoku
A group project for CS454 to create an Android game of Gomoku.
===========================================

Team members: 
Thaddeus Sundin
tsundin@pdx.edu

Charlie Juszczak
cbj3@pdx.edu

Roopa Satyanarayan
rs7@pdx.edu

Scott Fabini
sfabini@pdx.edu

===========================================

<h2>Contents</h2>

* **app**. Represents the Gomoku game app. A simple single or multiplayer game that can be played as a single player against the AI player, multiplayer with the same device, multiplayer using a local Bluetooth connection, or multiplayer with Google Play Game Services over the internet. Each game can be played with multiple options including board sizes (10x10, 15x15, 20x20) and modes (standard and freestyle).


new generation in modern button-clicking excitement. A simple multiplayer game sample that shows how to set up the Google Play real-time multiplayer API, invite friends, automatch, accept invitations, use the waiting room UI, send and receive messages and other multiplayer topics.

* **BaseGameUtils**. Utilities used on all samples, which you can use in your projects too. This is not a stand-alone sample, it's a library project.

<h3>Building using Android Studio...</h3>

1. Open Android Studio and launch the Android SDK manager from it (Tools | Android | SDK Manager)
1. Check that these two components are installed and updated to the latest version. Install or upgrade
them if necessary.
1. *Android SDK Platform Tools*
2. *Android Support Library*
2. *Google Play Services*
3. *Google Repository*
1. Return to Android Studio and select *Import Project*
1. Select the **CS454Group6_Gomoku_** directory
1. Select "Import from existing model - Gradle"

<h3>Modify IDs, compile and run</h3>

To set up a sample:

1. Change the application id in the build.gradle file to your own package name
(the same one you registered in Developer Console!).  You will have to update
the build.gradle file for each sample you want to run.  There is no need to
edit the AndroidManifest.xml file.
2. Modify res/values/ids.xml and place your Google Play IDs there, as given by the
Developer Console. In the Developer console, select a resource type
turn-based multiplayer match and click "Get Resources".  Copy the
contents from the console and replace the contents of res/values/ids.xml.
3. Compile and run.

IMPORTANT: make sure to sign your apk with the same certificate
as the one whose fingerprint you configured on Developer Console, otherwise
you will see errors.

IMPORTANT: if you are testing an unpublished game, make sure that the account you intend
to sign in with (the account on the test device) is listed as a tester in the
project on your Developer Console setup (check the list in the "Testing"
section), otherwise the server will act as though your project did not exist and
return errors.

<h3>If you're using another build system...</h3>

<h3>Building</h3>
To build the samples after you have applied the changes above, you can use the build/run option in
Eclipse or Android Studio.
