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
package com.reandroid.lib.arsc.value.attribute;

import com.reandroid.lib.arsc.decoder.ValueDecoder;
import com.reandroid.lib.arsc.value.EntryBlock;
import com.reandroid.lib.arsc.value.ResValueBag;
import com.reandroid.lib.arsc.value.ValueType;
import com.reandroid.lib.common.EntryStore;


public class AttributeBag {
    private final AttributeBagItem[] mBagItems;
    public AttributeBag(AttributeBagItem[] bagItems){
        this.mBagItems=bagItems;
    }

    public boolean contains(AttributeValueType valueType){
        return getFormat().contains(valueType);
    }
    public boolean isEqualType(AttributeValueType valueType){
        return getFormat().isEqualType(valueType);
    }
    public ValueDecoder.EncodeResult encodeEnumOrFlagValue(String valueString){
        if(valueString==null || !isEnumOrFlag()){
            return null;
        }
        int value=0;
        boolean foundOnce=false;
        String[] names=valueString.split("[\\s|]+");
        for(String name:names){
            AttributeBagItem item=searchByName(name);
            if(item==null){
                continue;
            }
            value|=item.getBagItem().getData();
            foundOnce=true;
        }
        if(!foundOnce){
            return null;
        }
        ValueType valueType = isFlag()?ValueType.INT_HEX:ValueType.INT_DEC;
        return new ValueDecoder.EncodeResult(valueType, value);
    }
    public String decodeAttributeValue(EntryStore entryStore, int attrValue){
        AttributeBagItem[] bagItems=searchValue(attrValue);
        return AttributeBagItem.toString(entryStore, bagItems);
    }
    public String decodeValueType(){
        return AttributeValueType.toString(getValueTypes());
    }
    public AttributeBagItem searchByName(String entryName){
        AttributeBagItem[] bagItems= getBagItems();
        for(AttributeBagItem item:bagItems){
            if(item.isType()){
                continue;
            }
            if(entryName.equals(item.getNameOrHex())){
                return item;
            }
        }
        return null;
    }
    public AttributeBagItem[] searchValue(int attrValue){
        if(isFlag()){
            return searchFlagValue(attrValue);
        }
        AttributeBagItem item=searchEnumValue(attrValue);
        if(item!=null){
            return new AttributeBagItem[]{item};
        }
        return null;
    }
    private AttributeBagItem searchEnumValue(int attrValue){
        AttributeBagItem[] bagItems= getBagItems();
        for(AttributeBagItem item:bagItems){
            if(item.isType()){
                continue;
            }
            int data=item.getData();
            if(attrValue==data){
                return item;
            }
        }
        return null;
    }

    private AttributeBagItem[] searchFlagValue(int attrValue){
        AttributeBagItem[] bagItems= getBagItems();
        int len=bagItems.length;
        AttributeBagItem[] foundBags = new AttributeBagItem[len];
        for(int i=0;i<len;i++){
            AttributeBagItem item=bagItems[i];
            if(item.isType()){
                continue;
            }
            int data=item.getData();
            if ((attrValue & data) != data) {
                continue;
            }
            if(attrValue==data){
                return new AttributeBagItem[]{item};
            }
            int index=indexOf(foundBags, data);
            if (index>=0) {
                foundBags[index] = item;
            }
        }
        return removeNull(foundBags);
    }

    private int indexOf(AttributeBagItem[] foundFlag, int data) {
        for (int i = 0; i < foundFlag.length; i++) {
            AttributeBagItem item=foundFlag[i];
            if(item==null){
                return i;
            }
            int flag=item.getData();
            if(flag==0){
                return i;
            }
            if ((flag & data) == data) {
                return -1;
            }
            if ((flag & data) == flag) {
                return i;
            }
        }
        return -1;
    }

    private AttributeBagItem[] removeNull(AttributeBagItem[] bagItems){
        int count=countNonNull(bagItems);
        if(count==0){
            return null;
        }
        AttributeBagItem[] results=new AttributeBagItem[count];
        int index=0;
        int len=bagItems.length;
        for(int i=0;i<len;i++){
            AttributeBagItem item=bagItems[i];
            if(item!=null){
                results[index]=item;
                index++;
            }
        }
        return results;
    }
    private int countNonNull(AttributeBagItem[] bagItems) {
        if(bagItems==null){
            return 0;
        }
        int result=0;
        int len=bagItems.length;
        for (int i = 0; i < len; i++) {
            if(bagItems[i]!=null){
                result++;
            }
        }
        return result;
    }
    public AttributeBagItem[] getBagItems(){
        return mBagItems;
    }
    public AttributeValueType[] getValueTypes(){
        AttributeBagItem format = getFormat();
        return format.getValueTypes();
    }
    public AttributeBagItem getFormat(){
        AttributeBagItem item = find(AttributeItemType.FORMAT);
        if(item==null){
            item= getBagItems()[0];
        }
        return item;
    }
    public AttributeBagItem getMin(){
        return find(AttributeItemType.MIN);
    }
    public AttributeBagItem getMax(){
        return find(AttributeItemType.MAX);
    }
    public AttributeBagItem getL10N(){
        return find(AttributeItemType.L10N);
    }
    private AttributeBagItem find(AttributeItemType itemType){
        AttributeBagItem[] bagItems = getBagItems();
        for(int i=0;i<bagItems.length;i++){
            AttributeBagItem item=bagItems[i];
            if(itemType==item.getItemType()){
                return item;
            }
        }
        return null;
    }
    private boolean isEnumOrFlag(){
        return isFlag() || isEnum();
    }
    public boolean isFlag(){
        return getFormat().isFlag();
    }
    public boolean isEnum(){
        return getFormat().isEnum();
    }

    @Override
    public String toString(){
        AttributeBagItem[] bagItems= getBagItems();
        StringBuilder builder=new StringBuilder();
        AttributeBagItem format=getFormat();
        EntryBlock entryBlock=format.getBagItem().getEntryBlock();
        if(entryBlock!=null){
            builder.append(entryBlock.getSpecString());
        }
        int len=bagItems.length;
        builder.append(", childes=").append(len);
        for(int i=0;i<len;i++){
            AttributeBagItem item=bagItems[i];
            builder.append("\n    [").append((i+1)).append("]  ");
            builder.append(item.toString());
        }
        return builder.toString();
    }

    public static AttributeBag create(ResValueBag resValueBag){
        if(resValueBag==null){
            return null;
        }
        AttributeBagItem[] bagItems=AttributeBagItem.create(resValueBag.getBagItems());
        if(bagItems==null){
            return null;
        }
        return new AttributeBag(bagItems);
    }
    public static boolean isAttribute(ResValueBag resValueBag){
        if(resValueBag==null){
            return false;
        }
        AttributeBagItem[] bagItems=AttributeBagItem.create(resValueBag.getBagItems());
        return bagItems!=null;
    }

    public static final short TYPE_ENUM = 0x0001;
    public static final short TYPE_FLAG = 0x0002;
}
