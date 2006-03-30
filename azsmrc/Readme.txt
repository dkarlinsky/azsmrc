AzMultiUser Readme
------------------
Index
1.  What is AzMultiUser
2.  System Requirements
3.  Installation
4.  Setup
5.  Manually starting the remote (FireFrog)
------------------

1.  What is AzMultiUser?

AzMultiUser is a two part remote control and multi-user system for
the BitTorrent client Azureus.  With AzMultiUser, you can have
several people using a single installation of Azureus at one time.
Even if you do not want multiple users for Azureus, AzMultiUser
is the most complete remote control system for Azureus to date.


2. System Requirements

	Supported Operating Systems include any that both java and swt
	support (Windows, Linux, MacOSX, etc.)

	Server system:
	1. A running version of Azureus 2.4.0.0 or greater
	2. Java 1.5 or greater
	3. SWT version 3.1 or greater
		Note:  A fresh installation of Azureus 2.4.0.0 will provide
		this automatically
	4. Functioning internet connection with at least one port
		available for communication to the server
		(default port is 49009)


	Remote system:
	1.  Java 1.5 or greater
	2.  SWT version 3.1 or greater (3.2M4 does not work well on Windows, if
			you are going to use the 3.2 branch, be sure to use at least
			version 3.2M5a or greater)
	3.  Functioning internet connection with at least one port
		available for communication to the server
		(default port is 49009)



3.  Installation

Installation of AzMultiUser comes in two parts:

(1) Installing the plugin into Azureus
(2) Installing the remote control program (FireFrog)

3.1  Installing the plugin into Azureus

	To install the plugin into Azurues, first Stop and Exit from
	Azureus. Then, open the plugin folder for Azureus in your
	file manager

	For Windows, this is usually:
	C:\Program Files\Azureus\plugins

	For Linux, this is usually:
	~\.azureus\plugins

	Next, make a new directory in the plugins folder called 'azmultiuser'
	Go into this directory and copy the following items into there:

		1.  azmultiuser_x.x.x.jar
		2.  jdom_1.0.jar
		3.  commons-codec_1.3.jar

	jdom and commons-codec are contained in the lib.zip file.  Be sure when
	uncompressing them, to keep the version number as Azureus will not load
	them if they do not have them.

	Finally, restart Azureus

3.2  Installing the remote control program (FireFrog)

		1.  Create a directory of your choice and extract the
			contents of the FireFrog.zip as well as lib.zip there
		2.  If you did not download the file with the SWT libraries
			in it, then you will need to manually obtain those and
			place them in your chosen folder yourself.

			To download visit here:
			http://download.eclipse.org/eclipse/downloads/index.php


4.  Setup

Setting up AzMultiUser can be accomplished in two ways.  The easiest
is to use the GUI provided with AzMultiUser in Azureus.  When you
open the GUI you are presented with a login screen.

	The default login is:
		username:  admin
		password:  azmultiuser


Once you have logged into the plugin, add a new user via the green
plus sign.  Be sure to fill in all of the details there, and click
ok.

The connection port for the remote can be changed by going to
Tools->Options->Plugins->AzMultiUser

Another way to setup the server is to use the remote itself.
Simply start the remote and log in to the server with the above
username and password and the default port 49009.  This way, you can
run Azureus in 'headless' mode and access it exclusevily via the
remote

Once there, click on the User Managment tools and add / edit users
from there

5.  Manually starting the remote (FireFrog) client

	For Windows:

	From the install directory, execute this command

javaw -classpath launcher.jar lbms.tools.launcher.Launcher

	or if that does not work on Windows, then try the java command

java -classpath launcher.jar lbms.tools.launcher.Launcher

	For Linux:

	java -classpath launcher.jar  -Djava.library.path=. lbms.tools.launcher.Launcher
