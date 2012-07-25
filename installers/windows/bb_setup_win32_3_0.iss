  ; Inno Setup script for 
; BrailleBlaster win32 version 1.3.0
; file name: bb_setup_win32_3_0.iss
; last compiled: June 08, 2012
; by TJ McElroy - mcelroy.tj@gmail.com
; Using Inno Setup version 5.5.0 
; latest Inno Setup Compiler as of June 08, 2012
; can be found at:
; http://www.jrsoftware.org/isdl.php

; Last modified: Jun 8, 2012
; added comments to beginning of script
; 
; begin:
#define MyAppName "BrailleBlaster"
#define MyAppVersion "1.3.0_win_32"
#define MyAppPublisher "Tester_1"
#define MyAppURL "http://www.brailleblaster.org/"
#define MyAppExeName "brailleblaster.jar"

[Setup]
; NOTE: The value of AppId uniquely identifies this application.
; Do not use the same AppId value in installers for other applications.
; (To generate a new GUID, click Tools | Generate GUID inside the IDE.)
AppId={{3A313675-BD73-4903-A956-A4C57136AAE7}
AppName={#MyAppName}
AppVersion={#MyAppVersion}
;AppVerName={#MyAppName} {#MyAppVersion}
AppPublisher={#MyAppPublisher}
AppPublisherURL={#MyAppURL}
AppSupportURL={#MyAppURL}
AppUpdatesURL={#MyAppURL}
DefaultDirName={pf}\{#MyAppName}
DefaultGroupName={#MyAppName}
AllowNoIcons=yes
LicenseFile=C:\temp\brailleblaster-1-3-0\LICENSE.txt
InfoBeforeFile=C:\temp\brailleblaster-1-3-0\pre_install.txt
InfoAfterFile=C:\temp\brailleblaster-1-3-0\thank_you.txt
OutputDir=c:\lab\braille_blaster\bb_setup_win32_3_0
OutputBaseFilename=setup
Compression=lzma
SolidCompression=yes

[Languages]
Name: "english"; MessagesFile: "compiler:Default.isl"

[Tasks]
Name: "desktopicon"; Description: "{cm:CreateDesktopIcon}"; GroupDescription: "{cm:AdditionalIcons}"; Flags: unchecked
Name: "quicklaunchicon"; Description: "{cm:CreateQuickLaunchIcon}"; GroupDescription: "{cm:AdditionalIcons}"; Flags: unchecked; OnlyBelowVersion: 0,6.1

[Files]
Source: "C:\temp\brailleblaster-1-3-0\brailleblaster.jar"; DestDir: "{app}"; Flags: ignoreversion
Source: "C:\temp\brailleblaster-1-3-0\*"; DestDir: "{app}"; Flags: ignoreversion recursesubdirs createallsubdirs
; NOTE: Don't use "Flags: ignoreversion" on any shared system files

[Icons]
Name: "{group}\{#MyAppName}"; Filename: "{app}\{#MyAppExeName}"
Name: "{group}\{cm:ProgramOnTheWeb,{#MyAppName}}"; Filename: "{#MyAppURL}"
Name: "{group}\{cm:UninstallProgram,{#MyAppName}}"; Filename: "{uninstallexe}"
Name: "{commondesktop}\{#MyAppName}"; Filename: "{app}\{#MyAppExeName}"; Tasks: desktopicon
Name: "{userappdata}\Microsoft\Internet Explorer\Quick Launch\{#MyAppName}"; Filename: "{app}\{#MyAppExeName}"; Tasks: quicklaunchicon

[Run]
Filename: "{app}\{#MyAppExeName}"; Description: "{cm:LaunchProgram,{#StringChange(MyAppName, '&', '&&')}}"; Flags: shellexec postinstall skipifsilent

