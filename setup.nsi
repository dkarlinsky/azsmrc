# Auto-generated by EclipseNSIS Script Wizard
# 19.07.2006 12:44:26

Name AzSMRC
# Defines
!define REGKEY "SOFTWARE\$(^Name)"

# MUI defines
!define MUI_ICON "${NSISDIR}\Contrib\Graphics\Icons\modern-install.ico"
!define MUI_FINISHPAGE_NOAUTOCLOSE
!define MUI_STARTMENUPAGE_REGISTRY_ROOT HKLM
!define MUI_STARTMENUPAGE_NODISABLE
!define MUI_STARTMENUPAGE_REGISTRY_KEY Software\AzSMRC
!define MUI_STARTMENUPAGE_REGISTRY_VALUENAME StartMenuGroup
!define MUI_STARTMENUPAGE_DEFAULT_FOLDER AzSMRC
!define MUI_UNICON "${NSISDIR}\Contrib\Graphics\Icons\modern-uninstall.ico"
!define MUI_UNFINISHPAGE_NOAUTOCLOSE
!define MUI_FINISHPAGE_RUN $INSTDIR\AzSMRC.exe
!define MUI_FINISHPAGE_SHOWREADME $INSTDIR\Readme.txt
!define MUI_FINISHPAGE_SHOWREADME_TEXT "Show Readme File"
!define MUI_FINISHPAGE_LINK "Visit our Website"
!define MUI_FINISHPAGE_LINK_LOCATION "http://azsmrc.sourceforge.net"

# Included files
!include Sections.nsh
!include MUI.nsh

# Reserved Files

# Variables
Var StartMenuGroup

# Installer pages
!insertmacro MUI_PAGE_WELCOME
!insertmacro MUI_PAGE_LICENSE license.txt
!insertmacro MUI_PAGE_COMPONENTS
!insertmacro MUI_PAGE_DIRECTORY
!insertmacro MUI_PAGE_STARTMENU Application $StartMenuGroup
!insertmacro MUI_PAGE_INSTFILES
!insertmacro MUI_PAGE_FINISH
!insertmacro MUI_UNPAGE_CONFIRM
!insertmacro MUI_UNPAGE_COMPONENTS
!insertmacro MUI_UNPAGE_INSTFILES

# Installer languages
!insertmacro MUI_LANGUAGE English

# Installer attributes
!ifdef SWT
OutFile "dist\${JVERSION}\AzSMRC_${VERSION}.exe"
!endif
!ifndef SWT
OutFile "dist\${JVERSION}\AzSMRC_${VERSION}_NoSWT.exe"
!endif

InstallDir $PROGRAMFILES\AzSMRC
CRCCheck on
XPStyle on
ShowInstDetails show
InstallDirRegKey HKLM "${REGKEY}" Path
ShowUninstDetails show

# Installer sections
Section -Main SEC0000
	SetOutPath $INSTDIR
	SetOverwrite on
	File "dist\${JVERSION}\launcher.jar"
	File "dist\${JVERSION}\AzSMRC_${VERSION}.jar"
	File "dist\${JVERSION}\AzSMRC.exe"
	File license.txt
	File Readme.txt
	File AzSMRCupdate.xml.gz
	File launch.properties
	File AzSMRC.exe.manifest
	File lbms\azsmrc\libs\jdom_1.1.jar
	File lbms\azsmrc\libs\commons-codec_1.3.jar
	File lbms\azsmrc\libs\log4j_1.2.13.jar
	WriteRegStr HKLM "${REGKEY}\Components" Main 1
SectionEnd

!ifdef SWT #only compile if swt is defined
Section -SWT SEC0001
	SetOutPath $INSTDIR
	SetOverwrite on
	File "${SWTDIR}\swt.jar"
SectionEnd
!endif

SectionGroup Shortcuts SECG0001
Section "Add QuickLaunch Shortcut" SEC0003
	CreateShortcut "$QUICKLAUNCH\AzSMRC.lnk" $INSTDIR\AzSMRC.exe
SectionEnd

Section "Add Desktop Shortcut" SEC0004
	CreateShortcut "$DESKTOP\AzSMRC.lnk" $INSTDIR\AzSMRC.exe
SectionEnd
SectionGroupEnd

Section "Associate with .torrent files" SEC0005
 ; back up old value of .opt
!define Index "Line${__LINE__}"
	ReadRegStr $1 HKCR ".torrent" ""
	StrCmp $1 "" "${Index}-NoBackup"
	StrCmp $1 "AzSMRC.Torrent" "${Index}-NoBackup"
	WriteRegStr HKCR ".torrent" "backup_val" $1
"${Index}-NoBackup:"
	WriteRegStr HKCR ".torrent" "" "AzSMRC.Torrent"
	ReadRegStr $0 HKCR "OptionsFile" ""
	StrCmp $0 "" 0 "${Index}-Skip"
	WriteRegStr HKCR "AzSMRC.Torrent" "" "AzSMRC Torrent"
	WriteRegStr HKCR "AzSMRC.Torrent\shell" "" "open"
	WriteRegStr HKCR "AzSMRC.Torrent\DefaultIcon" "" "$INSTDIR\AzSMRC.exe,0"
"${Index}-Skip:"
	WriteRegStr HKCR "AzSMRC.Torrent\shell\open\command" "" \
		'$INSTDIR\AzSMRC.exe "%1"'
	WriteRegStr HKCR "AzSMRC.Torrent\shell\scrape" "" "Scrape Torrent"
	WriteRegStr HKCR "AzSMRC.Torrent\shell\scrape\command" "" \
	   '$INSTDIR\AzSMRC.exe --scrape "%1"'

	System::Call 'Shell32::SHChangeNotify(i 0x8000000, i 0, i 0, i 0)'
!undef Index
  ; Rest of script 
SectionEnd

Section -post SEC0002
	WriteRegStr HKLM "${REGKEY}" Path $INSTDIR
	WriteUninstaller $INSTDIR\uninstall.exe
	!insertmacro MUI_STARTMENU_WRITE_BEGIN Application
	SetOutPath $SMPROGRAMS\$StartMenuGroup
	CreateShortcut "$SMPROGRAMS\$StartMenuGroup\AzSMRC.lnk" $INSTDIR\AzSMRC.exe
	CreateShortcut "$SMPROGRAMS\$StartMenuGroup\Readme.lnk" $INSTDIR\Readme.txt
	CreateShortcut "$SMPROGRAMS\$StartMenuGroup\Uninstall $(^Name).lnk" $INSTDIR\uninstall.exe
	!insertmacro MUI_STARTMENU_WRITE_END
	WriteRegStr HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\$(^Name)" DisplayName "$(^Name)"
	WriteRegStr HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\$(^Name)" DisplayIcon $INSTDIR\uninstall.exe
	WriteRegStr HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\$(^Name)" UninstallString $INSTDIR\uninstall.exe
	WriteRegDWORD HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\$(^Name)" NoModify 1
	WriteRegDWORD HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\$(^Name)" NoRepair 1
SectionEnd

# Macro for selecting uninstaller sections
!macro SELECT_UNSECTION SECTION_NAME UNSECTION_ID
	Push $R0
	ReadRegStr $R0 HKLM "${REGKEY}\Components" "${SECTION_NAME}"
	StrCmp $R0 1 0 next${UNSECTION_ID}
	!insertmacro SelectSection "${UNSECTION_ID}"
	GoTo done${UNSECTION_ID}
next${UNSECTION_ID}:
	!insertmacro UnselectSection "${UNSECTION_ID}"
done${UNSECTION_ID}:
	Pop $R0
!macroend

# Uninstaller sections

Section -un.Main UNSEC0000
	Delete /REBOOTOK $INSTDIR\commons-codec_1.3.jar
	Delete /REBOOTOK $INSTDIR\jdom_1.0.jar
	Delete /REBOOTOK "$INSTDIR\AzSMRC_${VERSION}.jar"
	Delete /REBOOTOK $INSTDIR\AzSMRC.exe.manifest
	Delete /REBOOTOK $INSTDIR\AzSMRC.exe
	Delete /REBOOTOK $INSTDIR\launcher.jar
	Delete /REBOOTOK $INSTDIR\license.txt
	Delete /REBOOTOK $INSTDIR\Readme.txt
	Delete /REBOOTOK $INSTDIR\AzSMRCupdate.xml.gz
	Delete /REBOOTOK $INSTDIR\log4j_1.2.13.jar
	Delete /REBOOTOK $INSTDIR\launch.properties
	Delete /REBOOTOK $INSTDIR\.certs

	#SWT
	Delete /REBOOTOK $INSTDIR\swt.jar

	DeleteRegValue HKLM "${REGKEY}\Components" Main

	;start of restore script
!define Index "Line${__LINE__}"
	ReadRegStr $1 HKCR ".torrent" ""
	StrCmp $1 "AzSMRC.Torrent" 0 "${Index}-NoOwn" ; only do this if we own it
	ReadRegStr $1 HKCR ".torrent" "backup_val"
	StrCmp $1 "" 0 "${Index}-Restore" ; if backup="" then delete the whole key
	DeleteRegKey HKCR ".torrent"
	Goto "${Index}-NoOwn"
"${Index}-Restore:"
	WriteRegStr HKCR ".torrent" "" $1
	DeleteRegValue HKCR ".torrent" "backup_val"

	DeleteRegKey HKCR "AzSMRC.Torrent" ;Delete key with association settings

	System::Call 'Shell32::SHChangeNotify(i 0x8000000, i 0, i 0, i 0)'
"${Index}-NoOwn:"
!undef Index
  ;rest of script 
SectionEnd

Section "un.Delete Config" UNSEC0003
	Delete /REBOOTOK $INSTDIR\config.cfg
SectionEnd

Section "un.Delete Directory" UNSEC0004
	RMDir /r $INSTDIR
SectionEnd

Section -un.post UNSEC0002
	DeleteRegKey HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\$(^Name)"
	Delete /REBOOTOK "$SMPROGRAMS\$StartMenuGroup\Uninstall $(^Name).lnk"
	Delete /REBOOTOK "$SMPROGRAMS\$StartMenuGroup\AzSMRC.lnk"
	Delete /REBOOTOK "$SMPROGRAMS\$StartMenuGroup\Readme.lnk"
	Delete /REBOOTOK "$QUICKLAUNCH\AzSMRC.lnk"
	Delete /REBOOTOK "$DESKTOP\AzSMRC.lnk"
	Delete /REBOOTOK $INSTDIR\uninstall.exe
	DeleteRegValue HKLM "${REGKEY}" StartMenuGroup
	DeleteRegValue HKLM "${REGKEY}" Path
	DeleteRegKey /IfEmpty HKLM "${REGKEY}\Components"
	DeleteRegKey /IfEmpty HKLM "${REGKEY}"
	RmDir /REBOOTOK $SMPROGRAMS\$StartMenuGroup
	RmDir /REBOOTOK $INSTDIR
SectionEnd

# Installer functions
Function .onInit
	InitPluginsDir
FunctionEnd

# Uninstaller functions
Function un.onInit
	ReadRegStr $INSTDIR HKLM "${REGKEY}" Path
	ReadRegStr $StartMenuGroup HKLM "${REGKEY}" StartMenuGroup
	!insertmacro SELECT_UNSECTION Main ${UNSEC0000}
FunctionEnd