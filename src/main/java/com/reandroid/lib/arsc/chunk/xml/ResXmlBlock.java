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
package com.reandroid.lib.arsc.chunk.xml;

 import com.reandroid.lib.arsc.chunk.BaseChunk;
 import com.reandroid.lib.arsc.chunk.ChunkType;
 import com.reandroid.lib.arsc.container.SingleBlockContainer;
 import com.reandroid.lib.arsc.header.HeaderBlock;
 import com.reandroid.lib.arsc.io.BlockReader;
 import com.reandroid.lib.arsc.pool.ResXmlStringPool;
 import com.reandroid.lib.arsc.value.ValueType;
 import com.reandroid.lib.common.EntryStore;
 import com.reandroid.lib.json.JSONArray;
 import com.reandroid.lib.json.JSONConvert;
 import com.reandroid.lib.json.JSONObject;
 import com.reandroid.xml.XMLDocument;
 import com.reandroid.xml.XMLElement;
 import com.reandroid.xml.XMLException;

 import java.io.*;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;

 public class ResXmlBlock extends BaseChunk implements JSONConvert<JSONObject> {
    private final ResXmlStringPool mResXmlStringPool;
    private final ResXmlIDMap mResXmlIDMap;
    private ResXmlElement mResXmlElement;
    private final SingleBlockContainer<ResXmlElement> mResXmlElementContainer;
    public ResXmlBlock() {
        super(ChunkType.XML,3);
        this.mResXmlStringPool=new ResXmlStringPool(true);
        this.mResXmlIDMap=new ResXmlIDMap();
        this.mResXmlElement=new ResXmlElement();
        this.mResXmlElementContainer=new SingleBlockContainer<>();
        this.mResXmlElementContainer.setItem(mResXmlElement);
        addChild(mResXmlStringPool);
        addChild(mResXmlIDMap);
        addChild(mResXmlElementContainer);
    }
    public ResXmlElement createRootElement(String tag){
        int lineNo=1;
        ResXmlElement resXmlElement=new ResXmlElement();
        resXmlElement.newStartElement(lineNo);

        setResXmlElement(resXmlElement);

        if(tag!=null){
            resXmlElement.setTag(tag);
        }
        return resXmlElement;
    }
    void linkStringReferences(){
        ResXmlElement element=getResXmlElement();
        if(element!=null){
            element.linkStringReferences();
        }
    }
    /*
     * method Block.addBytes is inefficient for large size byte array
     * so let's override here because this block is the largest
     */
    @Override
    public byte[] getBytes(){
        ByteArrayOutputStream os=new ByteArrayOutputStream();
        try {
            writeBytes(os);
            os.close();
        } catch (IOException ignored) {
        }
        return os.toByteArray();
    }
    @Override
    public void onReadBytes(BlockReader reader) throws IOException {
        HeaderBlock headerBlock=reader.readHeaderBlock();
        if(headerBlock==null){
            return;
        }
        ChunkType chunkType=headerBlock.getChunkType();
        if(chunkType!=ChunkType.XML){
            throw new IOException("Not ResXmlBlock: "+headerBlock);
        }
        BlockReader chunkReader=reader.create(reader.getPosition(), headerBlock.getChunkSize());
        getHeaderBlock().readBytes(chunkReader);
        while (chunkReader.isAvailable()){
            boolean readOk=readNext(chunkReader);
            if(!readOk){
                break;
            }
        }
        reader.offset(headerBlock.getChunkSize());
        chunkReader.close();
        onChunkLoaded();
    }
    @Override
    public void onChunkLoaded(){
        super.onChunkLoaded();
        linkStringReferences();
    }
    private boolean readNext(BlockReader reader) throws IOException {
        if(!reader.isAvailable()){
            return false;
        }
        int position=reader.getPosition();
        HeaderBlock headerBlock=reader.readHeaderBlock();
        if(headerBlock==null){
            return false;
        }
        ChunkType chunkType=headerBlock.getChunkType();
        if(chunkType==ChunkType.STRING){
            mResXmlStringPool.readBytes(reader);
        }else if(chunkType==ChunkType.XML_RESOURCE_MAP){
            mResXmlIDMap.readBytes(reader);
        }else if(isElementChunk(chunkType)){
            mResXmlElementContainer.readBytes(reader);
            return reader.isAvailable();
        }else {
            throw new IOException("Unexpected chunk "+headerBlock);
        }
        return reader.isAvailable() && position!=reader.getPosition();
    }
    private boolean isElementChunk(ChunkType chunkType){
        if(chunkType==ChunkType.XML_START_ELEMENT){
            return true;
        }
        if(chunkType==ChunkType.XML_END_ELEMENT){
            return true;
        }
        if(chunkType==ChunkType.XML_START_NAMESPACE){
            return true;
        }
        if(chunkType==ChunkType.XML_END_NAMESPACE){
            return true;
        }
        if(chunkType==ChunkType.XML_CDATA){
            return true;
        }
        if(chunkType==ChunkType.XML_LAST_CHUNK){
            return true;
        }
        return false;
    }
    public ResXmlStringPool getStringPool(){
        return mResXmlStringPool;
    }
    public ResXmlIDMap getResXmlIDMap(){
        return mResXmlIDMap;
    }
    public ResXmlElement getResXmlElement(){
        return mResXmlElement;
    }
    public void setResXmlElement(ResXmlElement resXmlElement){
        this.mResXmlElement=resXmlElement;
        this.mResXmlElementContainer.setItem(resXmlElement);
    }
    @Override
    protected void onChunkRefreshed() {

    }
    public void readBytes(File file) throws IOException{
        BlockReader reader=new BlockReader(file);
        super.readBytes(reader);
    }
    public void readBytes(InputStream inputStream) throws IOException{
        BlockReader reader=new BlockReader(inputStream);
        super.readBytes(reader);
    }
    public final int writeBytes(File file) throws IOException{
        if(isNull()){
            throw new IOException("Can NOT save null block");
        }
        File dir=file.getParentFile();
        if(dir!=null && !dir.exists()){
            dir.mkdirs();
        }
        OutputStream outputStream=new FileOutputStream(file);
        int length = super.writeBytes(outputStream);
        outputStream.close();
        return length;
    }
    @Override
    public JSONObject toJson() {
        JSONObject jsonObject=new JSONObject();
        jsonObject.put(ResXmlBlock.NAME_element, getResXmlElement().toJson());
        JSONArray pool = getStringPool().toJson();
        if(pool!=null){
            jsonObject.put(ResXmlBlock.NAME_styled_strings, pool);
        }
        return jsonObject;
    }
    @Override
    public void fromJson(JSONObject json) {
        onFromJson(json);
        ResXmlElement xmlElement=getResXmlElement();
        xmlElement.fromJson(json.optJSONObject(ResXmlBlock.NAME_element));
        refresh();
    }
    public XMLDocument decodeToXml(EntryStore entryStore, int currentPackageId) throws XMLException {
        XMLDocument xmlDocument = new XMLDocument();
        XMLElement xmlElement = getResXmlElement()
                .decodeToXml(entryStore, currentPackageId);
        xmlDocument.setDocumentElement(xmlElement);
        return xmlDocument;
    }
    private void onFromJson(JSONObject json){
        List<JSONObject> attributeList=recursiveAttributes(json.optJSONObject(ResXmlBlock.NAME_element));
        buildResourceIds(attributeList);
        Set<String> allStrings=recursiveStrings(json.optJSONObject(ResXmlBlock.NAME_element));
        ResXmlStringPool stringPool = getStringPool();
        stringPool.addStrings(allStrings);
        stringPool.refresh();
    }
    private void buildResourceIds(List<JSONObject> attributeList){
        ResIdBuilder builder=new ResIdBuilder();
        for(JSONObject attribute:attributeList){
            int id=attribute.getInt(ResXmlAttribute.NAME_id);
            if(id==0){
                continue;
            }
            String name=attribute.getString(ResXmlAttribute.NAME_name);
            builder.add(id, name);
        }
        builder.buildTo(getResXmlIDMap());
    }
    private List<JSONObject> recursiveAttributes(JSONObject elementJson){
        List<JSONObject> results = new ArrayList<>();
        if(elementJson==null){
            return results;
        }
        JSONArray attributes = elementJson.optJSONArray(ResXmlElement.NAME_attributes);
        if(attributes != null){
            int length = attributes.length();
            for(int i=0; i<length; i++){
                JSONObject attr=attributes.optJSONObject(i);
                if(attr!=null){
                    results.add(attr);
                }
            }
        }
        JSONArray childElements = elementJson.optJSONArray(ResXmlElement.NAME_childes);
        if(childElements!=null){
            int length=childElements.length();
            for(int i=0;i<length;i++){
                JSONObject child=childElements.getJSONObject(i);
                results.addAll(recursiveAttributes(child));
            }
        }
        return results;
    }
    private Set<String> recursiveStrings(JSONObject elementJson){
        Set<String> results = new HashSet<>();
        if(elementJson==null){
            return results;
        }
        results.add(elementJson.optString(ResXmlElement.NAME_namespace_uri));
        results.add(elementJson.optString(ResXmlElement.NAME_name));
        JSONArray namespaces=elementJson.optJSONArray(ResXmlElement.NAME_namespaces);
        if(namespaces != null){
            int length = namespaces.length();
            for(int i=0; i<length; i++){
                JSONObject nsObject=namespaces.getJSONObject(i);
                results.add(nsObject.getString(ResXmlElement.NAME_namespace_uri));
                results.add(nsObject.getString(ResXmlElement.NAME_namespace_prefix));
            }
        }
        JSONArray attributes = elementJson.optJSONArray(ResXmlElement.NAME_attributes);
        if(attributes != null){
            int length = attributes.length();
            for(int i=0; i<length; i++){
                JSONObject attr=attributes.optJSONObject(i);
                if(attr==null){
                    continue;
                }
                results.add(attr.getString(ResXmlAttribute.NAME_name));
                ValueType valueType=ValueType.fromName(attr.getString(ResXmlAttribute.NAME_value_type));
                if(valueType==ValueType.STRING){
                    results.add(attr.getString(ResXmlAttribute.NAME_data));
                }
            }
        }
        JSONArray childElements = elementJson.optJSONArray(ResXmlElement.NAME_childes);
        if(childElements!=null){
            int length=childElements.length();
            for(int i=0;i<length;i++){
                JSONObject child=childElements.getJSONObject(i);
                results.addAll(recursiveStrings(child));
            }
        }
        return results;
    }

    public static boolean isResXmlBlock(File file){
        if(file==null){
            return false;
        }
        try {
            InputStream inputStream=new FileInputStream(file);
            boolean result = isResXmlBlock(inputStream);
            inputStream.close();
            return result;
        } catch (IOException ignored) {
            return false;
        }
    }
    public static boolean isResXmlBlock(InputStream inputStream){
        try {
            HeaderBlock headerBlock=BlockReader.readHeaderBlock(inputStream);
            return isResXmlBlock(headerBlock);
        } catch (IOException ignored) {
            return false;
        }
    }
    public static boolean isResXmlBlock(BlockReader blockReader){
        if(blockReader==null){
            return false;
        }
        try {
            HeaderBlock headerBlock = blockReader.readHeaderBlock();
            return isResXmlBlock(headerBlock);
        } catch (IOException ignored) {
            return false;
        }
    }
    public static boolean isResXmlBlock(HeaderBlock headerBlock){
        if(headerBlock==null){
            return false;
        }
        ChunkType chunkType=headerBlock.getChunkType();
        return chunkType==ChunkType.XML;
    }
    private static final String NAME_element ="element";
    private static final String NAME_styled_strings="styled_strings";
}
