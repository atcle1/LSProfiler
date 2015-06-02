/*
 * Copyright (C) 2012-2014 Jorrit "Chainfire" Jongma
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package kr.ac.snu.cares.lsprofiler.util;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

/**
 * Thread utility class continuously reading from an InputStream
 */
public class StreamGobbler extends Thread {
    public static final String TAG = StreamGobbler.class.getSimpleName();
    private BufferedReader reader = null;
    private List<String> writer = null;
    public Handler handler = null;


    public StreamGobbler(InputStream inputStream, List<String> outputList) {
        reader = new BufferedReader(new InputStreamReader(inputStream));
        writer = outputList;
    }


    public StreamGobbler(InputStream inputStream) {
        reader = new BufferedReader(new InputStreamReader(inputStream));
    }

    @Override
    public void run() {
        // keep reading the InputStream until it ends (or an error occurs)
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                if (writer != null) writer.add(line);
                if (handler != null) reply(line);
                Log.i(TAG, "#> "+line);
            }
        } catch (IOException e) {
            // reader probably closed, expected exit condition
            e.printStackTrace();
        }

        // make sure our stream is closed and resources will be freed
        try {
            reader.close();
        } catch (IOException e) {
            // read already closed
            e.printStackTrace();
        }
        //Log.i(TAG, "stream run() end");
    }

    private void reply(String line) {
        if (handler == null) return;
        Message msg = handler.obtainMessage();
        msg.what = 0;
        msg.obj = line;
        handler.sendMessage(msg);
    }
}
