AzSMRC Readme
------------------
Index
1.  What is AzSMRC
2.  System Requirements
3.  Installation
4.  Setup
5.  Manually starting the remote
6.  Known issues
7.  Website
8.  Donating
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
	1. A running version of Azureus 2.5.0.0 or greater
	2. Java 1.6 or greater
	3. SWT version 3.4 or greater
		Note:  A fresh installation of Azureus 2.5.0.0 will provide
		this automatically
	4. Functioning internet connection with at least one port
		available for communication to the server
		(default port is 49009)


	Remote system:
	1.  Java 1.6 or greater
	2.  SWT version 3.4
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

	Next, make a new directory in the plugins folder called 'azsmrc'
	Go into this directory and copy the following items into there:

		1.  azsmrcPlugin_x.x.x.jar
		2.  jdom_1.1.jar
		3.  commons-codec_1.3.jar
		4.	commons-io_1.2.jar

	jdom and commons-codec/io are contained in the pluginLibrary.zip file.
	Be sure when uncompressing them, to keep the version number as is
	Azureus will not load them if they do not have them.

	Finally, restart Azureus

3.2  Installing the remote control program

	If you are on Windows you can download the full AzSMRC_x.x.x.exe which
	includes all necessary files.

	If not you have to do following steps:

	1.  Create a directory of your choice and extract the
		contents of the AzSMRC_xxx.zip as well as clientLibrary.zip there
	2.  If you did not download the file with the SWT libraries
		in it, then you will need to manually obtain those and
		place them in your chosen folder yourself.

		To download visit here:
		http://download.eclipse.org/eclipse/downloads/index.php
		
	For OS other than windows you need to download the appropriate SWT.jar
	go to www.eclipse.org/swt and select the stable release for your OS.
	If it is not Windows, Linux,  OS X, then you need to select more and
	scroll all the way down. SWT release are about 3-5mb so don't download
	the whole eclipse by mistake.


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

4.1	SSL

If you want to use SSL encrypted communication you need to create a
SSL certificate first. To do that open Azureus and go to
Tools->Options->Security and create a certificate.
You will need the tools.jar file that is distributed in the Java SDK.
If you have done that, enable SSL in AzSMRC settings and restart Azureus.
Don't forget to enable SSL in the client too.

5.  Manually starting the remote client

	For Windows:

	From the install directory, execute this command

javaw -classpath launcher.jar lbms.tools.launcher.Launcher

	or if that does not work on Windows, then try the java command

java -classpath launcher.jar lbms.tools.launcher.Launcher

	For Linux:

	java -classpath launcher.jar  -Djava.library.path=. lbms.tools.launcher.Launcher


6.  Known issues

- If a user is heavily restricted (does not have normal user access) in Windows, then
the system clipboard monitor feature will not work as that user does not have access
to the 'system clipboard'

- AzSMRC can now run without the tray icon.  If you are using AzSMRC in this way, be
sure to not enable "Exiting the main window will send it to the tray" or "Minimizing
the main window will send it to the tray" in the options as this would hide AzSMRC from
the user so much so that you would have to kill the AzSMRC process manually.


7.	Website

Our ProjectPage is: http://sourceforge.net/projects/azsmrc

Please use the tools there, like the BugTracker or the Feature Requests.
You can find the forum there too.

We have a normal Website too: http://azsmrc.sourceforge.net/

8.  Donations

Donations are always welcome!  We have several methods for donating availble.

These can be viewed directly in the AzSMRC program
(Help-> User Guide or the blue toolbar icon with a 'i')

Also these options are described on our homepage:

http://azsmrc.sourceforge.net/index.php?action=supportUs


a.  PayPal -- Directly give a donation to us through PayPal
b.  Amazon referral system --
		This does not require any actual donation from you, but rather, Amazon
		will donate to us a percentage of your total shopping if you use the
		provided referral links.
		If you are from Germany please use the direct link system as it provides
		the most money back for us from Amazon, so if you plan to do some shopping
		at Amazon, then please do it through this system and support us!