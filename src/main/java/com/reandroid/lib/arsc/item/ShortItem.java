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
package com.reandroid.lib.arsc.item;

public class ShortItem extends BlockItem {
    private short mCache;

    public ShortItem(){
        super(2);
    }
    public ShortItem(short val){
        this();
        set(val);
    }
    public void set(short val){
        if(val==mCache){
            return;
        }
        mCache=val;
        byte[] bts = getBytesInternal();
        bts[1]= (byte) (val >>> 8 & 0xff);
        bts[0]= (byte) (val & 0xff);
    }
    public short get(){
        return mCache;
    }
    public int unsignedInt(){
        return 0xffff & get();
    }
    @Override
    protected void onBytesChanged() {
        // To save cpu usage, better to calculate once only when bytes changed
        mCache=readShortBytes();
    }
    private short readShortBytes(){
        byte[] bts = getBytesInternal();
        return (short) (bts[0] & 0xff | (bts[1] & 0xff) << 8);
    }
    @Override
    public String toString(){
        return String.valueOf(get());
    }
}
