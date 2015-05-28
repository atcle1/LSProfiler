package kr.ac.snu.cares.lsprofiler.resolvers;

import java.io.File;

import kr.ac.snu.cares.lsprofiler.LSPLog;
import kr.ac.snu.cares.lsprofiler.util.FileLogWritter;
import kr.ac.snu.cares.lsprofiler.util.Su;

/**
 * Created by summer on 15. 5. 27.
 */
public class DumpsysResolver {
    private static final String dumpscripPath = "/data/local/dump.sh ";
    private int timeOut = 1000 * 20;

    private DumpsysThread dumpThread;
    private boolean bStarted = false;

    public static void doWriteDump(String fileName, int timeMilis) {
        try {
            if (Su.isRooted()) {
                Su.executeSuOnce(dumpscripPath + fileName, 1000 * 100);
            } else {
                Su.executeCommandLine(dumpscripPath + fileName, 1000 * 100);
            }
        } catch (Exception ex) {
            LSPLog.onException(ex);
        }
    }

    public void doWriteDumpAsync(String fileName) {
        if (bStarted == false) {
            dumpThread = new DumpsysThread();
            dumpThread.fileName = fileName;
            bStarted = true;
            dumpThread.start();
        }
    }

    public void joinDumpAsync(int timeMilis) {
        if (bStarted == true) {
            try {
                dumpThread.join(timeMilis);
                if (dumpThread.isAlive()) {
                    dumpThread.interrupt();
                }
            }catch(Exception ex) {
                LSPLog.onException(ex);
            }
        }
    }

    private class DumpsysThread extends Thread {
        public String fileName = "";
        @Override
        public void run() {
            super.run();
            try {
                doWriteDump(fileName, timeOut);
            }catch (Exception ex) {
                LSPLog.onException(ex);
            }
        }
    }

}
