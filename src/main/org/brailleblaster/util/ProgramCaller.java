/* BrailleBlaster Braille Transcription Application
  *
  * Copyright (C) 2010, 2012
  * ViewPlus Technologies, Inc. www.viewplus.com
  * and
  * Abilitiessoft, Inc. www.abilitiessoft.com
  * All rights reserved
  *
  * This file may contain code borrowed from files produced by various 
  * Java development teams. These are gratefully acknoledged.
  *
  * This file is free software; you can redistribute it and/or modify it
  * under the terms of the Apache 2.0 License, as given at
  * http://www.apache.org/licenses/
  *
  * This file is distributed in the hope that it will be useful, but
  * WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE
  * See the Apache 2.0 License for more details.
  *
  * You should have received a copy of the Apache 2.0 License along with 
  * this program; see the file LICENSE.
  * If not, see
  * http://www.apache.org/licenses/
  *
  * Maintained by Keith Creasy <kcreasy@aph.org>, Project Manager
*/

package org.brailleblaster.util;

import org.apache.commons.exec.*;
import java.io.IOException;
import org.brailleblaster.BBIni;

/**
* This class calls any program in a safe way
*/
public class ProgramCaller
{

private CommandLine cmdLine;
private DefaultExecuteResultHandler resultHandler;

public ProgramCaller (String command, String[] args, 
int returnValue)
throws ExecuteException, IOException
{
cmdLine = new CommandLine(command + BBIni.getNativeCommandSuffix());
for (int i = 0; i < args.length; i++)
cmdLine.addArgument(args[i]);
resultHandler = new DefaultExecuteResultHandler();
ExecuteWatchdog watchdog = new ExecuteWatchdog(60*1000);
Executor executor = new DefaultExecutor();
executor.setExitValue(returnValue);
executor.setWatchdog(watchdog);
executor.execute(cmdLine, resultHandler);
}

public boolean doneYet () throws InterruptedException
{
return resultHandler.hasResult();
}

public void waitTillDone () throws InterruptedException
{
resultHandler.waitFor();
}

}

