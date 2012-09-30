; This installer script is an attempt to create a universal installer for BrailleBlaster

!include "LogicLib.nsh"
!include "MUI2.nsh"
!include "X64.nsh"
!include "WordFunc.nsh"

; Language translations
LangString DESC_BBInstall ${LANG_ENLISH} "BrailleBlaster Application"
LangString CreateDesktopShortCutMsg ${LANG_ENGLISH} "Create desktop shortcut"
; End language translations

; Some basic information

Name "BrailleBlaster"
!define APPVERSION "2012.2"
OutFile "BrailleBlaster-${APPVERSION}-installer.exe"

SetCompressor /SOLID LZMA
ShowInstDetails nevershow
ShowUninstDetails nevershow

InstallDir "$PROGRAMFILES\brailleblaster"
InstallDirRegKey HKLM "Software\BrailleBlaster" "installdir"

RequestExecutionLevel admin

Var StartMenuFolder

!define MinJVMVersion "1.6"
Var JVMHome
Var JVMVersion
Var JVMX64

; Interface settings
!define MUI_ABORTWARNING

; The pages to show
; First the customisation of the finish page
Var Checkbox
Function BBFinishShow
  ${NSD_CreateCheckbox} 120u 110u 100% 10u "$(CreateDesktopShortCutMsg)"
  pop $Checkbox
  SetCtlColors $Checkbox "ffffff"
  ${NSD_SetState} $Checkbox 1
FunctionEnd
Function BBFinishLeave
  ${NSD_GetState} $Checkbox $0
  ${If} $0 <> 0
    CreateShortCut "$DESKTOP\BrailleBlaster.lnk" "$INSTDIR\brailleblasterw.lnk"
  ${EndIf}
FunctionEnd

!insertmacro MUI_PAGE_WELCOME
!insertmacro MUI_PAGE_LICENSE "..\..\LICENSE.txt"
!insertmacro MUI_PAGE_COMPONENTS
!insertmacro MUI_PAGE_DIRECTORY
!define MUI_STARTMENUPAGE_REGISTRY_ROOT "HKLM"
!define MUI_STARTMENUPAGE_REGISTRY_KEY "SOFTWARE\BrailleBlaster"
!define MUI_STARTMENUPAGE_REGISTRY_VALUENAME "Start Menu Folder"
!insertmacro MUI_PAGE_STARTMENU BrailleBlaster $StartMenuFolder
!insertmacro MUI_PAGE_INSTFILES
!define MUI_FINISHPAGE_NOAUTOCLOSE
!define MUI_FINISHPAGE_RUN
!define MUI_FINISHPAGE_RUN_TEXT "Run BrailleBlaster"
!define MUI_FINISHPAGE_RUN_FUNCTION "LaunchLink"
;!define MUI_FINISHPAGE_SHOWREADME "$INSTDIR\readme.txt"
!define MUI_PAGE_CUSTOMFUNCTION_SHOW "BBFinishShow"
!define MUI_PAGE_CUSTOMFUNCTION_LEAVE "BBFinishLeave"
!insertmacro MUI_PAGE_FINISH

!insertmacro MUI_UNPAGE_WELCOME
!insertmacro MUI_UNPAGE_CONFIRM
!insertmacro MUI_UNPAGE_INSTFILES
!insertmacro MUI_UNPAGE_FINISH

; The languages which are supported
!insertmacro MUI_LANGUAGE "English"

Section "BrailleBlaster" SecBBInstall
  SetOutPath "$INSTDIR"
  ;Call JVMDetect
  ${IfNot} "$JVMHome" == ""
    ${If} "$JVMX64" == "64"
      ;MessageBox MB_OK "64-bit JVM detected"
      File /r /x "README" "x64\*"
      CreateShortCut "$INSTDIR\brailleblasterw.lnk" "$\"$JVMHome\bin\javaw.exe$\"" "-jar $\"$INSTDIR\brailleblaster.jar$\""
    ${Else}
      ;MessageBox MB_OK "32-bit JVM detected"
      File /r /x "README" "x86\*"
      CreateShortCut "$INSTDIR\brailleblasterw.lnk" "$\"$JVMHome\bin\javaw.exe$\"" "-jar $\"$INSTDIR\brailleblaster.jar$\""
    ${EndIf}
  ;${Else}
    ;MessageBox MB_OK "No JVM detected"
  ${EndIf}
  ; Add the application files
  File "..\..\dist\brailleblaster.jar"
  File /r /x "README" "..\..\dist\programData"
  File /r /x "README"  /x "swt.jar" "..\..\dist\lib"
  File /r "..\..\dist\helpDocs"
  File "..\..\dist\contributors-credits.txt"
  File "..\..\dist\COPYRIGHT.txt"
  File "..\..\dist\LICENSE.txt"
  
  ; Store the install directory
  WriteRegStr HKLM "Software\BrailleBlaster" "installdir" $INSTDIR
  ; Add the add/remove programs information
  WriteRegStr HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\BrailleBlaster" "DisplayName" "BrailleBlaster"
  WriteRegStr HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\BrailleBlaster" "DisplayVersion" "${APPVERSION}"
  WriteRegStr HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\BrailleBlaster" "Publisher" "BrailleBlaster Project"
  WriteRegStr HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\BrailleBlaster" "URLInfoAbout" "http://www.brailleblaster.org"
  WriteRegStr HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\BrailleBlaster" "UninstallString" "$\"$INSTDIR\Uninstall.exe$\""
  WriteRegDWORD HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\BrailleBlaster" "NoModify" 1
  WriteRegDWORD HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\BrailleBlaster" "NoRepair" 1

  ; Create uninstaller
  WriteUninstaller "$INSTDIR\Uninstall.exe"
  ;The start menu group
  !insertmacro MUI_STARTMENU_WRITE_BEGIN BrailleBlaster
    CreateDirectory "$SMPROGRAMS\$StartMenuFolder"
    ; Clean up old links (eg. if we are upgrading)
    Delete "$SMPROGRAMS\$StartMenuFolder\Uninstall.lnk"
    Delete "$SMPROGRAMS\$StartMenuFolder\brailleblaster.lnk"
    ; Now create the new links
    CreateShortCut "$SMPROGRAMS\$StartMenuFolder\BrailleBlaster.lnk" "$instdir\brailleblasterw.lnk"
    CreateShortCut "$SMPROGRAMS\$StartMenuFolder\Uninstall BrailleBlaster.lnk" "$INSTDIR\Uninstall.exe"
    CreateShortCut "$SMPROGRAMS\$StartMenuFolder\BrailleBlaster website.lnk" "http://www.brailleblaster.org/"
  !insertmacro MUI_STARTMENU_WRITE_END
  ;CreateShortCut "$DESKTOP\BrailleBlaster.lnk" "$INSTDIR\brailleblasterw.lnk"
SectionEnd


; The Uninstaller section
Section "Uninstall"
  Delete "$INSTDIR\Uninstall.exe"
  Delete "$DESKTOP\BrailleBlaster.lnk"
  RMDir /r "$INSTDIR"
  !insertmacro MUI_STARTMENU_GETFOLDER BrailleBlaster $StartMenuFolder
  Delete "$SMPROGRAMS\$StartMenuFolder\brailleblaster.lnk"
  Delete "$SMPROGRAMS\$StartMenuFolder\Uninstall BrailleBlaster.lnk"
  Delete "$SMPROGRAMS\$StartMenuFolder\BrailleBlaster website.lnk"
  RMDir "$SMPROGRAMS\$StartMenuFolder"
  DeleteRegKey HKLM "SOFTWARE\Microsoft\Windows\CurrentVersion\Uninstall\BrailleBlaster"
  DeleteRegKey HKLM "Software\BrailleBlaster"
SectionEnd

function .onInit
  Call JVMDetect
  ${If} $JVMHome == ""
    MessageBox MB_OK "You need Java installed to be able to use BrailleBlaster. Please install Java and retry installing. Installer will now exit."
    Abort
  ${Else}
    ${VersionCompare} $JVMVersion ${MinJVMVersion} $0
    ${If} $0 == 2
      MessageBox MB_OK "Your Java version is $JVMVersion, but BrailleBlaster requires at least Java ${MinJVMVersion}. Please upgrade to at least Java 1.6 and then try installing. The installer will now exit"
      Abort
    ${EndIf}
    ; If we are using a 64-bit JVM then use the correct Program Files directory
    ; But We must not have a install directory from the registry
    ReadRegStr $0 HKLM "SOFTWARE\BrailleBlaster" "installdir"
    ${If} "$JVMX64" == "64"
    ${AndIf} $0 == ""
      StrCpy $INSTDIR "$PROGRAMFILES64\brailleblaster"
    ${EndIf}
  ${EndIf}
FunctionEnd

function JVMDetect
  ;Var /GLOBAL JVMVersion
  StrCpy $JVMVersion ""
  ;Var /GLOBAL JVMHome
  StrCpy $JVMHome ""
  ;Var /GLOBAL JVMX64
  StrCpy $JVMX64 ""
  ; If running on X64 then the user may have either 64-bit or 32-bit JVM installed
  ${If} ${RunningX64}
    SetRegView 64
    ReadRegStr $JVMVersion HKLM "SOFTWARE\JavaSoft\Java Runtime Environment" "CurrentVersion"
    ReadRegSTr $JVMHome HKLM "SOFTWARE\JavaSoft\Java Runtime Environment\$JVMVersion" "JavaHome"
    ; Better check the javaw.exe actually exists here
    ${IfNot} $JVMHome == ""
      IfFileExists "$JVMHome\bin\javaw.exe" +2 0
        StrCpy $JVMHome ""
    ${EndIF}
    ; Still valid to run if the user has the JDK
    ${If} $JVMHome == ""
      ReadRegStr $JVMVersion HKLM "SOFTWARE\JavaSoft\Java Development Kit" "CurrentVersion"
      ReadRegStr $JVMHome HKLM "SOFTWARE\Java Development Kit\$JVMVersion" "JavaHome"
      ; Check that the JDK actually exists
      ${IfNot} $JVMHome == ""
        IfFileExists "$JVMHome\bin\javaw.exe" +2 0
          StrCpy $JVMHome ""
      ${EndIf}
    ${EndIf}
    ; If a JVM has been detected by now then we better say its an X64 JVM
    ${IfNot} $JVMHome == ""
      StrCpy $JVMX64 "64"
    ${EndIf}
    SetRegView 32
  ${EndIf}
  ; Only check for a 32 bit JVM if none has been found yet
  ; This should always be the case for 32-bit systems
  ${If} $JVMHome == ""
    ReadRegStr $JVMVersion HKLM 'SOFTWARE\JavaSoft\Java Runtime Environment' "CurrentVersion"
    ReadRegStr $JVMHome HKLM "SOFTWARE\JavaSoft\Java Runtime Environment\$JVMVersion" "JavaHome"
    ${IfNot} $JVMHome == ""
      IfFileExists "$JVMHome\bin\javaw.exe" +2 0
        StrCpy $JVMHome ""
    ${EndIf}
    ; Still valid to run if the user has the JDK
    ${If} $JVMHome == ""
      ReadRegStr $JVMVersion HKLM "SOFTWARE\JavaSoft\Java Development Kit" "CurrentVersion"
      ReadRegStr $JVMHome HKLM "SOFTWARE\Java Development Kit\$JVMVersion" "JavaHome"
      ${IfNot} $JVMHome == ""
        IfFileExists "$JVMHome\bin\javaw.exe" +2 0
          StrCpy $JVMHome ""
      ${EndIf}
    ${EndIf}
  ${EndIf}
FunctionEnd

Function "LaunchLink"
  ExecShell "" "$INSTDIR\brailleblasterw.lnk"
FunctionEnd

; Set the descriptions for the sections
!insertmacro MUI_FUNCTION_DESCRIPTION_BEGIN
  !insertmacro MUI_DESCRIPTION_TEXT ${SecBBInstall} $(DESC_BBInstall)
!insertmacro MUI_FUNCTION_DESCRIPTION_END
