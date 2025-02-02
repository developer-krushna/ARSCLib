 /*
  *  Copyright (C) 2022 github.com/REAndroid
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
package com.reandroid.lib.apk;

import com.reandroid.archive.FileInputSource;
import com.reandroid.archive.InputSource;
import com.reandroid.lib.arsc.base.Block;
import com.reandroid.lib.arsc.chunk.xml.ResXmlBlock;
import com.reandroid.lib.json.JSONException;
import com.reandroid.lib.json.JSONObject;

import java.io.*;

public class JsonXmlInputSource extends InputSource {
    private final InputSource inputSource;
    private APKLogger apkLogger;
    public JsonXmlInputSource(InputSource inputSource) {
        super(inputSource.getAlias());
        this.inputSource=inputSource;
    }
    @Override
    public long write(OutputStream outputStream) throws IOException {
        return getResXmlBlock().writeBytes(outputStream);
    }
    @Override
    public InputStream openStream() throws IOException {
        ResXmlBlock resXmlBlock= getResXmlBlock();
        return new ByteArrayInputStream(resXmlBlock.getBytes());
    }
    @Override
    public long getLength() throws IOException{
        ResXmlBlock resXmlBlock = getResXmlBlock();
        return resXmlBlock.countBytes();
    }
    private ResXmlBlock getResXmlBlock() throws IOException{
        logVerbose("From json: "+getAlias());
        ResXmlBlock resXmlBlock=newInstance();
        InputStream inputStream=inputSource.openStream();
        try{
            JSONObject jsonObject=new JSONObject(inputStream);
            resXmlBlock.fromJson(jsonObject);
        }catch (JSONException ex){
            throw new IOException(inputSource.getAlias()+": "+ex.getMessage());
        }
        return resXmlBlock;
    }
    ResXmlBlock newInstance(){
        return new ResXmlBlock();
    }
    void setAPKLogger(APKLogger logger) {
        this.apkLogger = logger;
    }
    void logMessage(String msg) {
        if(apkLogger!=null){
            apkLogger.logMessage(msg);
        }
    }
    private void logError(String msg, Throwable tr) {
        if(apkLogger!=null){
            apkLogger.logError(msg, tr);
        }
    }
    private void logVerbose(String msg) {
        if(apkLogger!=null){
            apkLogger.logVerbose(msg);
        }
    }

    public static JsonXmlInputSource fromFile(File rootDir, File jsonFile){
        String path=ApkUtil.toArchiveResourcePath(rootDir, jsonFile);
        FileInputSource fileInputSource=new FileInputSource(jsonFile, path);
        return new JsonXmlInputSource(fileInputSource);
    }
}
