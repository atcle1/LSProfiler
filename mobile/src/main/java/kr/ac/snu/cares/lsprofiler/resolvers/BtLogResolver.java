package kr.ac.snu.cares.lsprofiler.resolvers;

import java.io.File;

import kr.ac.snu.cares.lsprofiler.util.FileLogWritter;

/**
 * Created by summer on 15. 6. 21.
 */
public class BtLogResolver {
    final static String btEnableDir = "/sdcard/bt/";
    final static String btEnableFile = "btEnabled";
    public static void enableBtLog() {

        File f = new File(btEnableDir + btEnableFile);
        if (f.exists()) return;
        try {
            File dir = new File(btEnableDir);
            dir.mkdirs();
            f.createNewFile();
        } catch(Exception ex) {
            FileLogWritter.writeException(ex);
        }
    }
    public static void disableBtLog() {
        File f = new File(btEnableDir + btEnableFile);
        try {
            if (f.exists()) {
                f.delete();
            }
        } catch (Exception ex) {
            FileLogWritter.writeException(ex);
        }
    }
}
