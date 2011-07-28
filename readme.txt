This is the ContentRest service for Rhythmyx 6.7

Do not install this package on 6.5.2 or 7.x!


If you have the Rhythmyx Patch Toolkit installed, you can use the 
Install.bat or install.sh scripts: 

>Install.bat c:\Rhythmyx
>sh install.sh ~/Rhythmyx

Where the argument is the home directory where Rhythmyx is installed. 

Otherwise, to manually install, you must have Java 1.5 and Apache Ant properly installed. 
The RHYTHMYX_HOME environment variable must point at your Rhythmyx 6.5 installation.  


Type the command: 

ant -f deploy.xml 


To use the patch installer to install on Linux, add these lines to your .profile  

export RHYTHMYX_HOME=$HOME/Rhythmyx  ##or where ever it is installed
export JAVA_HOME=$RHYTHMYX_HOME/JRE/
export ANT_HOME=$RHYTHMYX_HOME/Patch/InstallToolkit/

you can then run Ant: 

$ANT_HOME/bin/ant -f deploy.xml 



