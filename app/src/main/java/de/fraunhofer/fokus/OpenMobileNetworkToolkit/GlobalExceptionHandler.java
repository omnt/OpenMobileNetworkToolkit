package de.fraunhofer.fokus.OpenMobileNetworkToolkit;

import android.icu.text.SimpleDateFormat;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Locale;

public class GlobalExceptionHandler implements Thread.UncaughtExceptionHandler {
    private static final String TAG = "GlobalExceptionHandler";
    private Thread.UncaughtExceptionHandler uncaughtExceptionHandler;
    public GlobalExceptionHandler() {
        this.uncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
    }

    @Override
    public void uncaughtException(@NonNull Thread t, @NonNull Throwable e) {
        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath() + "/omnt/debug/";
        try {
            Files.createDirectories(Paths.get(path));
        } catch (IOException ioException) {
            throw new RuntimeException(ioException);
        }

        // create the log file
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.US);
        Date now = new Date();
        String filename = path + formatter.format(now) + ".txt";
        Log.d(TAG, "logfile: " + filename);
        File logfile = new File(filename);
        try {
            logfile.createNewFile();
        } catch (IOException ioException) {
            throw new RuntimeException(ioException);
        }
        FileOutputStream stream = null;
        // get an output stream
        final Writer stringBuffSync = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(stringBuffSync);
        e.printStackTrace(printWriter);
        String stacktrace = stringBuffSync.toString();
        printWriter.close();

        try {
            stream = new FileOutputStream(logfile);
            stream.write(stacktrace.getBytes());
            stream.close();
        } catch (IOException fileNotFoundException) {
            fileNotFoundException.printStackTrace();
        }
        uncaughtExceptionHandler.uncaughtException(t, e);
    }
}
