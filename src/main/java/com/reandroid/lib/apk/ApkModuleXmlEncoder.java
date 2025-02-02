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

 import com.reandroid.archive.APKArchive;
 import com.reandroid.archive.FileInputSource;
 import com.reandroid.lib.apk.xmlencoder.RESEncoder;
 import com.reandroid.lib.arsc.chunk.TableBlock;
 import com.reandroid.xml.XMLException;

 import java.io.File;
 import java.io.IOException;
 import java.util.List;

 public class ApkModuleXmlEncoder {
     private final RESEncoder resEncoder;
     public ApkModuleXmlEncoder(){
         this.resEncoder = new RESEncoder();
     }
     public ApkModuleXmlEncoder(ApkModule module, TableBlock tableBlock){
         this.resEncoder = new RESEncoder(module, tableBlock);
     }
     public void scanDirectory(File mainDirectory) throws IOException, XMLException {
         resEncoder.scanDirectory(mainDirectory);
         File rootDir=new File(mainDirectory, "root");
         scanRootDir(rootDir);
     }
     public ApkModule getApkModule(){
         return resEncoder.getApkModule();
     }

     private void scanRootDir(File rootDir){
         APKArchive archive=getApkModule().getApkArchive();
         List<File> rootFileList=ApkUtil.recursiveFiles(rootDir);
         for(File file:rootFileList){
             String path=ApkUtil.toArchivePath(rootDir, file);
             FileInputSource inputSource=new FileInputSource(file, path);
             archive.add(inputSource);
         }
     }
     public void setApkLogger(APKLogger apkLogger) {
         this.resEncoder.setAPKLogger(apkLogger);
     }
 }
