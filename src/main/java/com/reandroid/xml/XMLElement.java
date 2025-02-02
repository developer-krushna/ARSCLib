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
package com.reandroid.xml;


import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.*;

public class XMLElement extends XMLNode{
    static final long DEBUG_TO_STRING=500;
    private String mTagName;
    private XMLTextAttribute mTextAttribute;
    private final List<XMLAttribute> mAttributes = new ArrayList<>();
    private final List<XMLElement> mChildes = new ArrayList<>();
    private List<XMLComment> mComments;
    private final List<XMLText> mTexts = new ArrayList<>();
    private XMLElement mParent;
    private int mIndent;
    private Object mTag;
    private int mResId;
    private float mIndentScale;
    private String mStart;
    private String mStartPrefix;
    private String mEnd;
    private String mEndPrefix;
    private Set<NameSpaceItem> nameSpaceItems;
    public XMLElement(String tagName){
        this();
        setTagName(tagName);
    }
    public XMLElement(){
        setDefaultStartEnd();
    }

    public void addText(XMLText text){
        if(text==null){
            return;
        }
        mTexts.add(text);
        super.addChildNode(text);
    }
    private void appendText(String text){
        if(text==null || text.length()==0){
            return;
        }
        addText(new XMLText(text));
    }
    public String getTagNamePrefix(){
        int i=mTagName.indexOf(":");
        if(i>0){
            return mTagName.substring(0,i);
        }
        return null;
    }
    public String getTagNameWoPrefix(){
        int i=mTagName.indexOf(":");
        if(i>0){
            return mTagName.substring(i+1);
        }
        return mTagName;
    }
    private void setDefaultStartEnd(){
        this.mStart="<";
        this.mEnd=">";
        this.mStartPrefix="/";
        this.mEndPrefix="/";
    }
    public void applyNameSpaceItems(){
        if(nameSpaceItems!=null){
            for(NameSpaceItem nsItem:nameSpaceItems){
                SchemaAttr schemaAttr=nsItem.toSchemaAttribute();
                XMLAttribute exist=getAttribute(schemaAttr.getName());
                if(exist!=null){
                    exist.setValue(schemaAttr.getValue());
                }else {
                    addAttributeNoCheck(schemaAttr);
                }
            }
        }
        if(mParent!=null){
            mParent.applyNameSpaceItems();
        }
    }
    public void addNameSpace(NameSpaceItem nsItem){
        if(nsItem==null){
            return;
        }
        if(mParent!=null){
            mParent.addNameSpace(nsItem);
            return;
        }
        if(nameSpaceItems==null){
            nameSpaceItems=new HashSet<>();
        }
        nameSpaceItems.add(nsItem);
    }
    public NameSpaceItem getNameSpaceItemForUri(String uri){
        if(nameSpaceItems!=null){
            for(NameSpaceItem ns:nameSpaceItems){
                if(ns.isUriEqual(uri)){
                    return ns;
                }
            }
        }
        if(mParent!=null){
            return mParent.getNameSpaceItemForUri(uri);
        }
        return null;
    }
    public NameSpaceItem getNameSpaceItemForPrefix(String prefix){
        if(nameSpaceItems!=null){
            for(NameSpaceItem ns:nameSpaceItems){
                if(ns.isPrefixEqual(prefix)){
                    return ns;
                }
            }
        }
        if(mParent!=null){
            return mParent.getNameSpaceItemForPrefix(prefix);
        }
        return null;
    }
    void setStart(String start) {
        this.mStart = start;
    }
    void setEnd(String end) {
        this.mEnd = end;
    }
    void setStartPrefix(String pfx) {
        if(pfx==null){
            pfx="";
        }
        this.mStartPrefix = pfx;
    }
    void setEndPrefix(String pfx) {
        if(pfx==null){
            pfx="";
        }
        this.mEndPrefix = pfx;
    }
    void setIndentScale(float scale){
        mIndentScale=scale;
    }
    private float getIndentScale(){
        XMLElement parent=getParent();
        if(parent==null){
            return mIndentScale;
        }
        return parent.getIndentScale();
    }
    public int getResourceId(){
        return mResId;
    }
    public void setResourceId(int id){
        mResId=id;
    }
    public XMLElement createElement(String tag) {
        XMLElement baseElement=new XMLElement(tag);
        addChildNoCheck(baseElement);
        return baseElement;
    }
    public void addChild(Collection<XMLElement> elements) {
        if(elements==null){
            return;
        }
        for(XMLElement element:elements){
            addChild(element);
        }
    }
    public void addChild(XMLElement child) {
        addChildNoCheck(child);
    }
    private void clearChildElements(){
        mChildes.clear();
    }
    private void clearTexts(){
        mTexts.clear();
    }
    public XMLComment getCommentAt(int index){
        if(mComments==null || index<0){
            return null;
        }
        if(index>=mComments.size()){
            return null;
        }
        return mComments.get(index);
    }
    public void hideComments(boolean recursive, boolean hide){
        hideComments(hide);
        if(recursive){
            for(XMLElement child:mChildes){
                child.hideComments(recursive, hide);
            }
        }
    }
    private void hideComments(boolean hide){
        if(mComments==null){
            return;
        }
        for(XMLComment ce:mComments){
            ce.setHidden(hide);
        }
    }
    public int getCommentsCount(){
        if(mComments==null){
            return 0;
        }
        return mComments.size();
    }
    public void addComments(Collection<XMLComment> commentElements){
        if(commentElements==null){
            return;
        }
        for(XMLComment ce:commentElements){
            addComment(ce);
        }
    }
    public void clearComments(){
        if(mComments==null){
            return;
        }
        mComments.clear();
        mComments=null;
    }
    public void addComment(XMLComment commentElement) {
        if(commentElement==null){
            return;
        }
        if(mComments==null){
            mComments=new ArrayList<>();
        }
        mComments.add(commentElement);
        commentElement.setIndent(getIndent());
        commentElement.setParent(this);
        super.addChildNode(commentElement);
    }
    public void removeChildElements(){
        mChildes.clear();
    }
    public List<XMLAttribute> listAttributes(){
        return mAttributes;
    }
    public int getChildesCount(){
        return mChildes.size();
    }
    public List<XMLElement> listChildElements(){
        return mChildes;
    }
    public XMLElement getChildAt(int index){
        if(index<0 || index>=mChildes.size()){
            return null;
        }
        return mChildes.get(index);
    }
    public int getAttributeCount(){
        return mAttributes.size();
    }
    public XMLAttribute getAttributeAt(int index){
        if(index>=mAttributes.size()){
            return null;
        }
        return mAttributes.get(index);
    }
    public String getAttributeValue(String name){
        XMLAttribute attr=getAttribute(name);
        if (attr==null){
            return null;
        }
        return attr.getValue();
    }
    public int getAttributeValueInt(String name, int def){
        XMLAttribute attr=getAttribute(name);
        if (attr==null){
            return def;
        }
        return attr.getValueInt();
    }
    public int getAttributeValueInt(String name) throws XMLException {
        XMLAttribute attr=getAttribute(name);
        if (attr==null){
            throw new XMLException("Expecting integer for attr <"+name+ "> at '"+toString()+"'");
        }
        try{
            return attr.getValueInt();
        }catch (NumberFormatException ex){
            throw new XMLException(ex.getMessage()+": "+" '"+toString()+"'");
        }
    }
    public boolean getAttributeValueBool(String name, boolean def){
        XMLAttribute attr=getAttribute(name);
        if (attr==null){
            return def;
        }
        if(!attr.isValueBool()){
            return def;
        }
        return attr.getValueBool();
    }
    public boolean getAttributeValueBool(String name) throws XMLException {
        XMLAttribute attr=getAttribute(name);
        if (attr==null || !attr.isValueBool()){
            throw new XMLException("Expecting boolean for attr <"+name+ "> at '"+toString()+"'");
        }
        return attr.getValueBool();
    }
    public XMLAttribute getAttribute(String name){
        if(XMLUtil.isEmpty(name)){
            return null;
        }
        for(XMLAttribute attr:mAttributes){
            if(name.equals(attr.getName())){
                return attr;
            }
        }
        return null;
    }
    public XMLAttribute removeAttribute(String name){
        if(XMLUtil.isEmpty(name)){
            return null;
        }
        XMLAttribute attr=getAttribute(name);
        if(attr==null){
            return null;
        }
        int i=mAttributes.indexOf(attr);
        if(i<0){
            return null;
        }
        mAttributes.remove(i);
        return attr;
    }
    public XMLAttribute setAttribute(String name, int value){
        return setAttribute(name, String.valueOf(value));
    }
    public XMLAttribute setAttribute(String name, boolean value){
        String v=value?"true":"false";
        return setAttribute(name, v);
    }
    public XMLAttribute setAttribute(String name, String value){
        if(XMLUtil.isEmpty(name)){
            return null;
        }
        XMLAttribute attr=getAttribute(name);
        if(attr==null){
            if(SchemaAttr.looksSchema(name, value)){
                attr=new SchemaAttr(name, value);
            }else{
                attr=new XMLAttribute(name,value);
            }
            addAttributeNoCheck(attr);
        }else {
            attr.setValue(value);
        }
        return attr;
    }
    public void addAttributes(Collection<XMLAttribute> attrs){
        if(attrs==null){
            return;
        }
        for(XMLAttribute a:attrs){
            addAttribute(a);
        }
    }
    public void addAttribute(XMLAttribute attr){
        if(attr==null){
            return;
        }
        if(XMLUtil.isEmpty(attr.getName())){
            return;
        }
        XMLAttribute exist=getAttribute(attr.getName());
        if(exist!=null){
            return;
        }
        mAttributes.add(attr);
    }
    private void addAttributeNoCheck(XMLAttribute attr){
        if(attr==null || attr.isEmpty()){
            return;
        }
        mAttributes.add(attr);
    }
    public void sortChildes(Comparator<XMLElement> comparator){
        if(comparator==null){
            return;
        }
        mChildes.sort(comparator);
    }
    public void sortAttributes(Comparator<XMLAttribute> comparator){
        if(comparator==null){
            return;
        }
        mAttributes.sort(comparator);
    }
    public XMLElement getParent(){
        return mParent;
    }
    void setParent(XMLElement baseElement){
        mParent=baseElement;
    }
    private void addChildNoCheck(XMLElement child){
        if(child==null || child == this){
            return;
        }
        child.setParent(this);
        child.setIndent(getChildIndent());
        mChildes.add(child);
        super.addChildNode(child);
    }
    public int getLevel(){
        int rs=0;
        XMLElement parent=getParent();
        if(parent!=null){
            rs=rs+1;
            rs+=parent.getLevel();
        }
        return rs;
    }
    int getIndent(){
        if(hasTextContent()){
            return 0;
        }
        return mIndent;
    }
    int getChildIndent(){
        if(mIndent<=0 || hasTextContent()){
            return 0;
        }
        int rs=mIndent+1;
        String tag= getTagName();
        if(tag!=null){
            int i=tag.length();
            if(i>10){
                i=10;
            }
            rs+=i;
        }
        return rs;
    }
    public void setIndent(int indent){
        mIndent=indent;
        int chIndent=getChildIndent();
        for(XMLElement child:mChildes){
            child.setIndent(chIndent);
        }
        if(mComments!=null){
            for(XMLComment ce:mComments){
                ce.setIndent(indent);
            }
        }
    }
    private boolean appendAttributesIndentText(Writer writer) throws IOException {
        int i=0;
        String tagName=getTagName();
        if(tagName!=null){
            i+=tagName.length();
        }
        i+=2;
        if(i>15){
            i=15;
        }
        i+=getIndentWidth();
        int j=0;
        while (j<i){
            writer.write(' ');
            j++;
        }
        return true;
    }
    boolean appendIndentText(Writer writer) throws IOException {
        int max=getIndentWidth();
        int i=0;
        while (i<max){
            writer.write(' ');
            i++;
        }
        return true;
    }
    private int getIndentWidth(){
        float scale=getIndentScale();
        scale = scale * (float) getIndent();
        int i=(int)scale;
        if(i<=0){
            i=0;
        }
        if(i>40){
            i=40;
        }
        return i;
    }
    private String getIndentText(){
        float scale=getIndentScale();
        scale = scale * (float) getIndent();
        int i=(int)scale;
        if(i<=0){
            return "";
        }
        if(i>40){
            i=40;
        }
        StringBuilder builder=new StringBuilder();
        int max=i;
        i=0;
        while (i<max){
            builder.append(" ");
            i++;
        }
        return builder.toString();
    }
    public String getTagName(){
        return mTagName;
    }
    public void setTagName(String tag){
        mTagName =tag;
    }
    public Object getTag(){
        return mTag;
    }
    public void setTag(Object tag){
        mTag =tag;
    }
    public String getTextContent(){
        return getTextContent(true);
    }
    public String getTextContent(boolean unEscape){
        String text=buildTextContent();
        if(unEscape){
            text=XMLUtil.unEscapeXmlChars(text);
        }
        return text;
    }
    private String buildTextContent(){
        if(!hasTextContent()){
            return null;
        }
        StringWriter writer=new StringWriter();
        for(XMLNode child:getChildNodes()){
            try {
                child.write(writer, false);
            } catch (IOException ignored) {
            }
        }
        writer.flush();
        try {
            writer.close();
        } catch (IOException ignored) {
        }
        return writer.toString();
    }
    XMLTextAttribute getTextAttr(){
        if(mTextAttribute==null){
            mTextAttribute=new XMLTextAttribute();
        }
        return mTextAttribute;
    }
    private void appendTextContent(Writer writer) throws IOException {
        for(XMLNode child:getChildNodes()){
            if(child instanceof XMLElement){
                ((XMLElement)child).setIndent(0);
            }
            child.write(writer, false);
        }
    }
    public boolean hasTextContent() {
        return mTexts.size()>0;
    }
    public void setTextContent(String text){
        setTextContent(text, true);
    }
    public void setTextContent(String text, boolean escape){
        clearChildElements();
        clearTexts();
        if(escape){
            text=XMLUtil.escapeXmlChars(text);
        }
        appendText(text);
    }
    private boolean appendAttributes(Writer writer, boolean newLineAttributes) throws IOException {
        if(mAttributes==null){
            return false;
        }
        boolean addedOnce=false;
        for(XMLAttribute attr:mAttributes){
            if(attr.isEmpty()){
                continue;
            }
            if(addedOnce){
                if(newLineAttributes){
                    writer.write(XMLUtil.NEW_LINE);
                    appendAttributesIndentText(writer);
                }else{
                    writer.write(' ');
                }
            }else {
                writer.write(' ');
            }
            attr.write(writer);
            addedOnce=true;
        }
        return addedOnce;
    }
    boolean isEmpty(){
        if(mTagName!=null){
            return false;
        }
        if(mAttributes.size()>0){
            return false;
        }
        if(mComments!=null && mComments.size()>0){
            return false;
        }
        if(mTexts.size()>0){
            return false;
        }
        return true;
    }
    private boolean canAppendChildes(){
        for(XMLElement child:mChildes){
            if (!child.isEmpty()){
                return true;
            }
        }
        return false;
    }
    boolean appendComments(Writer writer) throws IOException {
        if(mComments==null){
            return false;
        }
        boolean appendPrevious=false;
        boolean addedOnce=false;
        for(XMLComment ce:mComments){
            if(ce.isEmpty()){
                continue;
            }
            if(appendPrevious){
                writer.write(XMLUtil.NEW_LINE);
            }
            appendPrevious=ce.write(writer, false);
            if(appendPrevious){
                addedOnce=true;
            }
        }
        return addedOnce;
    }
    private boolean appendChildes(Writer writer, boolean newLineAttributes) throws IOException {
        boolean appendPrevious=true;
        boolean addedOnce=false;
        for(XMLElement child:mChildes){
            if(stopWriting(writer)){
                break;
            }
            if(child.isEmpty()){
                continue;
            }
            if(appendPrevious){
                writer.write(XMLUtil.NEW_LINE);
            }
            appendPrevious=child.write(writer, newLineAttributes);
            if(!addedOnce && appendPrevious){
                addedOnce=true;
            }
        }
        return addedOnce;
    }
    private boolean stopWriting(Writer writer){
        if(!(writer instanceof ElementWriter)){
            return false;
        }
        ElementWriter elementWriter=(ElementWriter)writer;
        if(elementWriter.isFinished()){
            elementWriter.writeInterrupted();
            return true;
        }
        return false;
    }
    @Override
    public boolean write(Writer writer, boolean newLineAttributes) throws IOException {
        if(isEmpty()){
            return false;
        }
        if(stopWriting(writer)){
            return false;
        }
        boolean appendOnce=appendComments(writer);
        if(appendOnce){
            writer.write(XMLUtil.NEW_LINE);
        }
        appendIndentText(writer);
        writer.write(mStart);
        String tagName=getTagName();
        if(tagName!=null){
            writer.write(tagName);
        }
        appendAttributes(writer, newLineAttributes);
        boolean useEndTag=false;
        boolean hasTextCon=hasTextContent();
        if(hasTextCon){
            writer.write(mEnd);
            appendTextContent(writer);
            useEndTag=true;
        }else if(canAppendChildes()){
            writer.write(mEnd);
            appendChildes(writer, newLineAttributes);
            useEndTag=true;
        }
        if(useEndTag){
            if(!hasTextCon){
                writer.write(XMLUtil.NEW_LINE);
                appendIndentText(writer);
            }
            writer.write(mStart);
            writer.write(mStartPrefix);
            writer.write(getTagName());
        }else {
            writer.write(mEndPrefix);
        }
        writer.write(mEnd);
        return true;
    }
    @Override
    public String toText(int indent, boolean newLineAttributes){
        StringWriter writer=new StringWriter();
        setIndent(indent);
        try {
            write(writer, newLineAttributes);
            writer.flush();
            writer.close();
        } catch (IOException ignored) {
        }
        return writer.toString();
    }
    protected List<XMLNode> listSpannable(){
        List<XMLNode> results = new ArrayList<>();
        for(XMLNode child:getChildNodes()){
            if((child instanceof XMLElement) || (child instanceof XMLText)){
                results.add(child);
            }
        }
        return results;
    }
    protected String getSpannableText() {
        StringBuilder builder = new StringBuilder();
        builder.append(getTagName());
        for(XMLAttribute attribute:listAttributes()){
            builder.append(' ');
            builder.append(attribute.toText(0, false));
        }
        return builder.toString();
    }
    @Override
    public String toString(){
        StringWriter strWriter=new StringWriter();
        ElementWriter writer=new ElementWriter(strWriter, DEBUG_TO_STRING);
        try {
            write(writer, false);
        } catch (IOException e) {
        }
        strWriter.flush();
        return strWriter.toString();
    }

}
