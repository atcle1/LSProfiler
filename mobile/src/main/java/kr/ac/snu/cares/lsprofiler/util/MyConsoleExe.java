package kr.ac.snu.cares.lsprofiler.util;

import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MyConsoleExe {
	Runtime runtime;
	Process process;
	DataOutputStream outputStream;
	BufferedReader reader;
	InputStream stdout;

	public MyConsoleExe() {
		runtime = Runtime.getRuntime();
	}

	public String exec(String comm, Boolean bSu) {
		StringBuilder result = new StringBuilder();
		exec(comm, result, bSu);
		return result.toString();
	}
	
	public int exec2(String comm, Boolean bSu) {
		return exec(comm, null, bSu);
	}

    public int execSu(String comm[]) {
        int exitCode = -1;
        String line = "";
        StringBuffer output = new StringBuffer();
        try{
            Process process = Runtime.getRuntime().exec("su");
            DataOutputStream outputStream = new DataOutputStream(process.getOutputStream());
            for (int i = 0; i < comm.length; i++) {
                outputStream.writeBytes(comm[i]+"\n");
                outputStream.flush();
            }

            outputStream.writeBytes("exit\n");
            outputStream.flush();
            //process = runtime.exec(comm);
            reader = new BufferedReader(new InputStreamReader(
                    process.getInputStream()));

            while ((line = reader.readLine()) != null) {
                output.append(line + "\n");
                //System.out.println(line);
            }
            exitCode = process.waitFor();
            System.out.println("exe : "+comm + " return : "+exitCode);

            process.waitFor();
        }catch(IOException e){
            e.printStackTrace();
        }catch(InterruptedException e){
            e.printStackTrace();
        }
        Log.i("EXE", "stdout " + output.toString());
        // System.out.println(result);
        return exitCode;
    }

	public int exec(String comm, StringBuilder result, Boolean bSu) {
		int exitCode = -1;
		String line = "";
		StringBuffer output = new StringBuffer();
        Log.i("EXE", "su:" + bSu + " " + comm);
		try {
            if (bSu)
			    process = runtime.exec(new String[]{"su", "-c", "\'" + comm + "\'"});
            else {
                //process = runtime.exec(new String[]{"su", "-c", "/system/bin/sh -c '/data/local/sprofiler 6'"});
                //process = runtime.exec("/system/bin/sh -c '/data/local/sprofiler 6'");
                //process = runtime.exec("/data/local/sprofiler 6");
                process = runtime.exec(comm);
            }

			//process = runtime.exec(comm);
			reader = new BufferedReader(new InputStreamReader(
					process.getInputStream()));

			while ((line = reader.readLine()) != null) {
				output.append(line + "\n");
				//System.out.println(line);
			}
			exitCode = process.waitFor();
			System.out.println("exe : "+comm + " return : "+exitCode);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (result != null)
			result.append(output.toString());
		// System.out.println(result);
		return exitCode;
	}
}