package de.fraunhofer.fokus.OpenMobileNetworkToolkit;

import java.io.BufferedReader;
import java.io.InputStreamReader;


@SuppressWarnings({"UnusedReturnValue", "unused"})
public final class LogReader {


    public interface LoggerSR {
        void onLogGrabbed(String msg);
    }

    private static final String processId = Integer.toString(android.os.Process.myPid());


    public static StringBuilder getLog(LoggerSR logger) {
        StringBuilder builder = new StringBuilder();
        try {
//            String command = "logcat --pid=" + pid + " -d";
//            Process process = Runtime.getRuntime().exec(command);
            String[] command = new String[]{"logcat", "-d", "-v", "threadtime"};
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while (null != (line = bufferedReader.readLine())) {
                if (line.contains(processId)) {
                    builder.append(line);
                    logger.onLogGrabbed(builder.toString());
                }
            }
        } catch (Throwable ex) {
            SRLog.e("LogReader", "getLog failed", ex);
        }
        return builder;
    }


}
