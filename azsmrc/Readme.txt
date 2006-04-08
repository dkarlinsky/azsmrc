AzSMRC Readme
------------------
Index
1.  What is AzSMRC
2.  System Requirements
3.  Installation
4.  Setup
5.  Manually starting the remote
6.  Website
7.  Donating
------------------

1.  What is AzSMRC?

AzSMRC is a two part remote control and multi-user system for
the BitTorrent client Azureus.  With AzSMRC, you can have
several people using a single installation of Azureus at one time.
Even if you do not want multiple users for Azureus, AzSMRC
is the most complete remote control system for Azureus to date.

AzSMRC is pronounced AzSmirk


2. System Requirements

	Supported Operating Systems include any that both java and swt
	support (Windows, Linux, MacOSX, etc.)

	Server system:
	1. A running version of Azureus 2.4.0.3 (currently CVS only) or greater
	2. Java 1.5 or greater
	3. SWT version 3.1 or greater
		Note:  A fresh installation of Azureus 2.4.0.3 will provide
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

Installation of AzSMRC comes in two parts:

(1) Installing the plugin into Azureus
(2) Installing the remote control program

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

		1.  azsmrcPlugin_x.x.x.jar
		2.  jdom_1.0.jar
		3.  commons-codec_1.3.jar

	jdom and commons-codec are contained in the lib.zip file.  Be sure when
	uncompressing them, to keep the version number as Azureus will not load
	them if they do not have them.

	Finally, restart Azureus

3.2  Installing the remote control program

		1.  Create a directory of your choice and extract the
			contents of the FireFrog.zip as well as lib.zip there
		2.  If you did not download the file with the SWT libraries
			in it, then you will need to manually obtain those and
			place them in your chosen folder yourself.

			To download visit here:
			http://download.eclipse.org/eclipse/downloads/index.php


4.  Setup

Setting up AzSMRC can be accomplished in two ways.  The easiest
is to use the GUI provided with AzSMRC in Azureus.  When you
open the GUI you are presented with a login screen.

	The default login is:
		username:  admin
		password:  azsmrc


Once you have logged into the plugin, add a new user via the green
plus sign.  Be sure to fill in all of the details there, and click
ok.

The connection port for the remote can be changed by going to
Tools->Options->Plugins->AzSMRC

Another way to setup the server is to use the remote itself.
Simply start the remote and log in to the server with the above
username and password and the default port 49009.  This way, you can
run Azureus in 'headless' mode and access it exclusevily via the
remote

Once there, click on the User Managment tools and add / edit users
from there

5.  Manually starting the remote client

	For Windows:

	From the install directory, execute this command

javaw -classpath launcher.jar lbms.tools.launcher.Launcher

	or if that does not work on Windows, then try the java command

java -classpath launcher.jar lbms.tools.launcher.Launcher

	For Linux:

	java -classpath launcher.jar  -Djava.library.path=. lbms.tools.launcher.Launcher

6.	Website

Our ProjectPage is: http://sourceforge.net/projects/azsmrc

Please use the tools there, like the BugTracker or the Feature Requests.
You can find the forum there too.

7.  Donations

Donations are always welcome!  We have several methods for donating availble.

These can be viewed directly in the AzSMRC program
(Help-> User Guide or the blue toolbar icon with a 'i')

Also these options are described on our homepage:

http://azsmrc.sourceforge.net/index.php?action=supportUs


a.  PayPal -- Directly give a donation to us through PayPal
b.  Amazon referral system --
		This does not require any actual donation from you, but rather, Amazon
		will donate to us a percentage of your total shopping if you use the
		provided referral links.  The direct link system provides the most
		money back for us from Amazon, so if you plan to do some shopping at
		Amazon, then please do it through this system and support us!