Developer info

create a "build.properties", with the following:
---CUT---
swt.jar=../swt-3.3
azureus.dir=../Azureus/bin
azureus.jar=../Azureus/dist
source.dir=lbms
build.dir=bin
libs.dir=lbms/azsmrc/libs
dist.dir=dist
nsisant.dir=../
nsis.dir=C:/Programme/NSIS
launch4j.dir=../launch4j/
---CUT---

of course you may have to change the path to the appropriate paths on your system

swt.jar = where SWT.jar is located
azureus.dir=../Azureus/bin
azureus.jar = where the Azureus2.jar is located 
source.dir = the source dir of AzSMRC
build.dir = the output dir
libs.dir = the dir where the azsmrc libraries are
dist.dir = the final output fir
###optional: only required for windows exe and setup###
nsisant.dir = directory of the nsisant.jar (http://sourceforge.net/projects/nsisant/)
nsis.dir = path to NSIS http://sourceforge.net/projects/nsis/
launch4j.dir = directory of the launch4j (http://sourceforge.net/projects/launch4j/)


Getting ready to build



How to Build the FlashUI
-------------

1) Download the free flex sdk
available  here: 
http://opensource.adobe.com/flexsdk

2)unpack where you like

3)add to the "build.properties", the following: 

FLEX_HOME=/usr/local/flexsdk
build.img=./img

setting FLEX_HOME to where the flex sdk was unpacked
and build.img to the azsmrc images directory