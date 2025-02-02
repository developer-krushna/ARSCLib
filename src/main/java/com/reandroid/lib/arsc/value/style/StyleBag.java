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
package com.reandroid.lib.arsc.value.style;

import com.reandroid.lib.arsc.item.SpecString;
import com.reandroid.lib.arsc.item.TypeString;
import com.reandroid.lib.arsc.value.EntryBlock;
import com.reandroid.lib.arsc.value.ResValueBag;
import com.reandroid.lib.arsc.value.array.ArrayBagItem;
import com.reandroid.lib.arsc.value.attribute.AttributeBag;

public class StyleBag {
    private final StyleBagItem[] mBagItems;
    private StyleBag(StyleBagItem[] bagItems){
        this.mBagItems=bagItems;
    }
    public StyleBagItem[] getBagItems() {
        return mBagItems;
    }
    public String getName(){
        EntryBlock entryBlock=getEntryBlock();
        if(entryBlock==null){
            return null;
        }
        SpecString spec = entryBlock.getSpecString();
        if(spec==null){
            return null;
        }
        return spec.get();
    }
    public String getParentResourceName(){
        int id=getParentId();
        if(id==0){
            return null;
        }
        EntryBlock entryBlock=getEntryBlock();
        if(entryBlock==null){
            return null;
        }
        return entryBlock.buildResourceName(id, '@', true);
    }
    public int getParentId(){
        ResValueBag resValueBag=getBagItems()[0].getBagItem().getParentBag();
        if(resValueBag==null){
            return 0;
        }
        return resValueBag.getParentId();
    }
    public int getResourceId(){
        EntryBlock entryBlock=getEntryBlock();
        if(entryBlock==null){
            return 0;
        }
        return entryBlock.getResourceId();
    }
    public String getTypeName(){
        EntryBlock entryBlock=getBagItems()[0].getBagItem().getEntryBlock();
        if(entryBlock==null){
            return null;
        }
        TypeString typeString = entryBlock.getTypeString();
        if(typeString==null){
            return null;
        }
        return typeString.get();
    }
    private EntryBlock getEntryBlock(){
        return getBagItems()[0].getBagItem().getEntryBlock();
    }
    @Override
    public String toString() {
        StringBuilder builder=new StringBuilder();
        builder.append("<");
        String type=getTypeName();
        builder.append(type);
        builder.append(" name=\"");
        builder.append(getName());
        builder.append("\"");
        String parent=getParentResourceName();
        if(parent!=null){
            builder.append(" parent=\"");
            builder.append(parent);
            builder.append("\"");
        }
        builder.append("\">");
        StyleBagItem[] allItems = getBagItems();
        for(int i=0;i<allItems.length;i++){
            builder.append("\n    ");
            builder.append(allItems[i].toString());
        }
        builder.append("\n</");
        builder.append(type);
        builder.append(">");
        return builder.toString();
    }

    /** TODO: find another method to check instead of checking type name (plurals),
     * just like {@link AttributeBag} **/
    public static boolean isStyle(ResValueBag resValueBag){
        if(resValueBag==null){
            return false;
        }
        EntryBlock entryBlock = resValueBag.getEntryBlock();
        if(entryBlock==null){
            return false;
        }
        String type = entryBlock.getTypeName();
        if(type==null){
            return false;
        }
        if(!type.startsWith(NAME)){
            return false;
        }
        return ArrayBagItem.create(resValueBag.getBagItems()) != null;
    }

    public static StyleBag create(ResValueBag resValueBag){
        if(resValueBag==null){
            return null;
        }
        StyleBagItem[] bagItems=StyleBagItem.create(resValueBag.getBagItems());
        if(bagItems==null){
            return null;
        }
        return new StyleBag(bagItems);
    }
    public static final String NAME="style";
}
