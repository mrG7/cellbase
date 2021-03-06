/*
 * Copyright 2015 OpenCB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opencb.cellbase.app.transform.utils;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Created with IntelliJ IDEA.
 * User: imedina
 * Date: 9/25/13
 * Time: 1:06 PM
 * To change this template use File | Settings | File Templates.
 */
public class FileUtils {


    public static void checkPath(Path path) throws IOException {
        checkPath(path, false);
    }

    public static void checkPath(Path path, boolean writable) throws IOException {
        if(path == null) {
            throw new IOException("Path is null");
        }

        if(!Files.exists(path)) {
            throw new IOException("Path '" + path.toAbsolutePath() + "' does not exist");
        }

        if(!Files.isReadable(path)) {
            throw new IOException("Path '" + path.toAbsolutePath() + "' cannot be read");
        }

        if (writable && !Files.isWritable(path)) {
            throw new IOException("Path '" + path.toAbsolutePath() + "' cannot be written");
        }
    }

    /**
     * This method is able to determine whether a file is GZipped and return a BufferedReader in any case
     * @param path to be read
     * @return BufferedReader object
     * @throws IOException
     */
    public static BufferedReader newBufferedReader(Path path) throws IOException {
        BufferedReader br = null;
        if(path.toFile().getName().endsWith(".gz")) {
            br = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(path.toFile()))));
        }else {
            br = Files.newBufferedReader(path, Charset.defaultCharset());
        }
        return br;
    }

    /**
     * This method is able to determine whether a file is GZipped and return a BufferedReader in any case
     * @param path to be read
     * @return BufferedReader object
     * @throws IOException
     */
    public static BufferedReader newBufferedReader(Path path, Charset charset) throws IOException {
        BufferedReader br = null;
        if(path.toFile().getName().endsWith(".gz")) {
            br = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(path.toFile()))));
        }else {
            br = Files.newBufferedReader(path, charset);
        }
        return br;
    }

    /**
     * This method is able to determine whether a file is GZipped and return a BufferedReader in any case
     * @param path to be read
     * @return BufferedReader object
     * @throws IOException
     */
    public static BufferedWriter newGzipBufferedWriter(Path path) throws IOException {
        return new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(path.toFile()))));
    }
}
