  ; Inno Setup script for 
; BrailleBlaster x86/x64 version 2012.1
; file name: bb_setup_x86_2012.1.iss
; Created by TJ McElroy
; last compiled: June 28, 2012
; by Vic Beckley
; Using Inno Setup version 5.5.0 
; latest Inno Setup Compiler as of June 08, 2012
; can be found at:
; http://www.jrsoftware.org/isdl.php

; Last modified: Jun 28, 2012
; Modified comments at beginning of script
; 
; begin:
#define MyAppName "BrailleBlaster"
#define MyAppVersion "2012.1 x86"
#define MyAppPublisher "Jointly ViewPlus Technologies and Abilitiessoft"
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
LicenseFile=..\..\dist\LICENSE.txt
InfoBeforeFile=pre_install.txt
InfoAfterFile=thank_you.txt
OutputDir=..\..\
OutputBaseFilename=BrailleBlaster_2012.1_win_x86
Compression=lzma
SolidCompression=yes

[Languages]
Name: "english"; MessagesFile: "compiler:Default.isl"

[Tasks]
Name: "desktopicon"; Description: "{cm:CreateDesktopIcon}"; GroupDescription: "{cm:AdditionalIcons}"; Flags: unchecked
Name: "quicklaunchicon"; Description: "{cm:CreateQuickLaunchIcon}"; GroupDescription: "{cm:AdditionalIcons}"; Flags: unchecked; OnlyBelowVersion: 0,6.1

[Files]
Source: "..\..\dist\brailleblaster.jar"; DestDir: "{app}"; Flags: ignoreversion
Source: "..\..\dist\*"; DestDir: "{app}"; Flags: ignoreversion recursesubdirs createallsubdirs
; NOTE: Don't use "Flags: ignoreversion" on any shared system files

[Icons]
Name: "{group}\{#MyAppName}"; Filename: "{app}\{#MyAppExeName}"
Name: "{group}\{cm:ProgramOnTheWeb,{#MyAppName}}"; Filename: "{#MyAppURL}"
Name: "{group}\{cm:UninstallProgram,{#MyAppName}}"; Filename: "{uninstallexe}"
Name: "{commondesktop}\{#MyAppName}"; Filename: "{app}\{#MyAppExeName}"; Tasks: desktopicon
Name: "{userappdata}\Microsoft\Internet Explorer\Quick Launch\{#MyAppName}"; Filename: "{app}\{#MyAppExeName}"; Tasks: quicklaunchicon

[Run]
Filename: "{app}\{#MyAppExeName}"; Description: "{cm:LaunchProgram,{#StringChange(MyAppName, '&', '&&')}}"; Flags: shellexec postinstall skipifsilent

