# About
This is a fork of [AzSMRC](http://azsmrc.sourceforge.net) by Leonard Brünings, Dr. Marc Schaubach and Toni Uebernicke

Main Changes:
* Converted to Gradle build
* Added remote dir history "Select Remote Directory" dialog
* Added support for torrents with 'udp://' announce URLS
* Added "save location" selection to "Add by URL" dialog

# AzSmrc Vuze Plugin

# AzSmrc Remote Control App
A client app with a rich UI for controlling remote a Vuze instance with the plugin installed

## Packaging 

### MacOS

    ./gradlew packageMac
       
The (tar) packaged app will be under:
    
    remote/build/AzSmrc.${version}.app.tar
    
### Windows

    ./gradlew packageWin -Dbits=64 
    
The `-Dbits=[bits]` defaults to 64
The resulting binary is under:

    remote/build/AzSmrc-${version}-x86.exe
    remote/build/AzSmrc-${version}-x86_64.exe
    
    
            
          
