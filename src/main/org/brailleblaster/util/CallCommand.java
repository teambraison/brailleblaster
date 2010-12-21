package org.brailleblaster.util;

import org.apache.commons.exec.*;

public class CallCommand
{
public CallCommand (String command, String[args], int returnValue)
{
CommandLine cmdLine = new CommandLine(command);
for (int i = 0; i < args.length; i++)
cmdLine.addArgument(args[i]);
//HashMap map = new HashMap();
//map.put("file", new File("invoice.pdf"));
//commandLine.setSubstitutionMap(map);
DefaultExecuteResultHandler resultHandler = new DefaultExecuteResultHandler();
ExecuteWatchdog watchdog = new ExecuteWatchdog(60*1000);
Executor executor = new DefaultExecutor();
executor.setExitValue(returnValue);
executor.setWatchdog(watchdog);
executor.execute(cmdLine, resultHandler);
int exitValue = resultHandler.waitFor();
}

}

