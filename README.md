Who-Wants-To-Be-a-Millionaire
=============================

A full-fledged real-life game in Java based on the popular show "Who wants to be a Millionaire" which reads out questions and answer options, offers lifelines, and listens to user speech input for the answer - just like you see on television!

=============================

Installing the required software :

1. Installing CMU's Sphinx4
To install the Sphinx4 software and integrate it into Eclipse, I followed the tutorial video on youtube (whose link is mentioned below).
www.youtube.com/watch?v=Z0f3vvCJWyo
This has the complete installation instructions and gives a fine tutorial on installing and running a sample application.
Include the JSGFDemo.jar in your project : 
1. Right click your project in Eclipse, and go to Build Path -> Configure Build Path
2. Add the following jar files which are present in the sphinx4-1.0beta6-bin\sphinx4- 1.0beta6\lib folder at the location where you have downloaded the software 
	- js.jar, jsapi.jar, sphinx4.jar and tags.jar
3. Add the following demo jar as well. Click on "Add External JARs" in Configure Build Path. Go  to sphinx4-1.0beta6-bin\sphinx4-1.0beta6\bin folder, and select JSGFDemo.jar
2. Installing FreeTTS
To install FreeTTS into eclipse, please follow these steps :
1. Download FreeTTS from www.sourceforge.net/projects/freetts/files/
2. Unzip the freeTTS binary package and check inside the \lib directory for jsapi.exe
3. Run jsapi.exe, say yes to unpack jsapi.jar
4. Find your JRE directory. Copy all the jars (jsapi.jar, freetts.jar, cmu_time_awb.jar, cmu_us_kal.jar etc) to that directory.

=============================

Running the project :

If everything is successfully installed, import the project folder into Eclipse as follows :
1. Open Eclipse
2. From File, select Import -> General -> Existing Projects into Workspace
3. Find the project folder name "trial".
4. The project should now be loaded into the workspace . Run this project.
5. Check in Eclipse, right click the project. Go to Build Path -> Configure Build Path. The jar files you added should be listed there. If they are not, restart and hopefully they'll be there.

=============================

Demo Script :

A sample demo has been shown in the final report. 
