package kr.ac.snu.cares.lsprofiler.util;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import kr.ac.snu.cares.lsprofiler.LSPReporter;

/**
 * Created by summer on 4/22/15.
 */
public class ReportItem {
    public Date reportDate;
    public String reportDateString;
    public String backupPath;
    public boolean result;
    public ArrayList<File> fileList;

    public ReportItem() {
        reportDate = new Date();
        // db file name
        SimpleDateFormat transFormat = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        reportDateString = transFormat.format(reportDate);
        transFormat = new SimpleDateFormat("yyyy-MM-dd");
        String dirStr = transFormat.format(reportDate);
        backupPath = LSPReporter.BACKUP_BASE_PATH + dirStr + "/";
        fileList = new ArrayList<File>();
    }
}
