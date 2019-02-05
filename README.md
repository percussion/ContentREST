**Content Rest Service**

Provides a restful interface for import content into the CMS through import templates that are defined in Velocity Templates.

This version has been updated for the 7.3.2 [732_20180928] Patch level.

**Installation**
`
Download the release version and unzip it on the Percussion Server.

Run the Install.bat or install.sh script from a command prompt while the Percussion Service is in a stopped state.  

Windows Example  (Percussion is installed in the C:\Rhythmyx folder):

c:\downloads\psotoolkit\Install.bat C:\Rhythmyx

Linux Example: Percussion is installed in the /opt/Percussion folder.

$ ./install.sh /opt/Percussion

**Contributing**
The project is configured with Apache Maven and currently requires maven 3 or greater. 

After cloning the project.

mvn clean install

Will build the distribution zip and execute tests. To skip tests, pass the -DskipTests=true parameter. 


