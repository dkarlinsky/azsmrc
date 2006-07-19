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
!insertmacro MUI_UNPAGE_INSTFILES

# Installer languages
!insertmacro MUI_LANGUAGE English

# Installer attributes
OutFile dist\AzSMRC_${VERSION}.exe
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
	File dist\launcher.jar
	File "dist\AzSMRC_${VERSION}.jar"
	File AzSMRC.exe
	File license.txt
	File Readme.txt
	File AzSMRCupdate.xml.gz
	File launch.properties
	File javaw.exe.manifest
	File lbms\azsmrc\libs\jdom_1.0.jar
	File lbms\azsmrc\libs\commons-codec_1.3.jar
	WriteRegStr HKLM "${REGKEY}\Components" Main 1
SectionEnd

Section SWT SEC0001
	SetOutPath $INSTDIR
	SetOverwrite on
	File ..\swt-3.2\swt.jar
	File ..\swt-3.2\swt-awt-win32-3232.dll
	File ..\swt-3.2\swt-gdip-win32-3232.dll
	File ..\swt-3.2\swt-wgl-win32-3232.dll
	File ..\swt-3.2\swt-win32-3232.dll
	WriteRegStr HKLM "${REGKEY}\Components" SWT 1
SectionEnd

Section -post SEC0002
	WriteRegStr HKLM "${REGKEY}" Path $INSTDIR
	WriteUninstaller $INSTDIR\uninstall.exe
	!insertmacro MUI_STARTMENU_WRITE_BEGIN Application
	SetOutPath $SMPROGRAMS\$StartMenuGroup
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
Section /o un.SWT UNSEC0001
	Delete /REBOOTOK $INSTDIR\swt-win32-3232.dll
	Delete /REBOOTOK $INSTDIR\swt-wgl-win32-3232.dll
	Delete /REBOOTOK $INSTDIR\swt-gdip-win32-3232.dll
	Delete /REBOOTOK $INSTDIR\swt-awt-win32-3232.dll
	Delete /REBOOTOK $INSTDIR\swt.jar
	DeleteRegValue HKLM "${REGKEY}\Components" SWT
SectionEnd

Section /o un.Main UNSEC0000
	Delete /REBOOTOK $INSTDIR\commons-codec_1.3.jar
	Delete /REBOOTOK $INSTDIR\jdom_1.0.jar
	Delete /REBOOTOK "$INSTDIR\AzSMRC_${VERSION}.jar"
	Delete /REBOOTOK $INSTDIR\javaw.exe.manifest
	Delete /REBOOTOK $INSTDIR\AzSMRC.exe
	Delete /REBOOTOK $INSTDIR\launcher.jar
	Delete /REBOOTOK $INSTDIR\license.txt
	Delete /REBOOTOK $INSTDIR\Readme.txt
	Delete /REBOOTOK $INSTDIR\AzSMRCupdate.xml.gz
	Delete /REBOOTOK $INSTDIR\launch.properties
	DeleteRegValue HKLM "${REGKEY}\Components" Main
SectionEnd

Section un.post UNSEC0002
	DeleteRegKey HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\$(^Name)"
	Delete /REBOOTOK "$SMPROGRAMS\$StartMenuGroup\Uninstall $(^Name).lnk"
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
	!insertmacro SELECT_UNSECTION SWT ${UNSEC0001}
FunctionEnd

