package org.brailleblaster.util;

import org.apache.commons.exec.*;
import java.io.*;

public class ProgramCaller
{

private CommandLine cmdLine;
private DefaultExecuteResultHandler resultHandler;

public void safeCall (String command, String[] args, 
int 
returnValue)
throws IOException
{
cmdLine = new CommandLine(command);
for (int i = 0; i < args.length; i++)
cmdLine.addArgument(args[i]);
//HashMap map = new HashMap();
//map.put("file", new File("invoice.pdf"));
//commandLine.setSubstitutionMap(map);
resultHandler = new DefaultExecuteResultHandler();
ExecuteWatchdog watchdog = new ExecuteWatchdog(60*1000);
Executor executor = new DefaultExecutor();
executor.setExitValue(returnValue);
executor.setWatchdog(watchdog);
try {
executor.execute(cmdLine, resultHandler);
}
catch (ExecuteException e)
{
return;
}
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

