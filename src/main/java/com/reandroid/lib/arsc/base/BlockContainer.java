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
package com.reandroid.lib.arsc.base;

import com.reandroid.lib.arsc.container.BlockList;
import com.reandroid.lib.arsc.io.BlockReader;

import java.io.IOException;
import java.io.OutputStream;

public abstract class BlockContainer<T extends Block> extends Block{
    public BlockContainer(){
        super();
    }

    protected void onPreRefreshRefresh(){

    }
    protected abstract void onRefreshed();
    public final void refresh(){
        if(isNull()){
            return;
        }
        onPreRefreshRefresh();
        refreshChildes();
        onRefreshed();
    }
    protected void refreshChildes(){
        T[] childes=getChildes();
        if(childes!=null){
            int max=childes.length;
            for(int i=0;i<max;i++){
                T item=childes[i];
                if(item instanceof BlockContainer){
                    BlockContainer<?> container=(BlockContainer<?>)item;
                    container.refresh();
                }else if(item instanceof BlockList){
                    BlockList<?> blockList=(BlockList<?>)item;
                    blockList.refresh();
                }
            }
        }
    }
    @Override
    public void onCountUpTo(BlockCounter counter){
        if(counter.FOUND){
            return;
        }
        if(counter.END==this){
            counter.FOUND=true;
            return;
        }
        T[] childes=getChildes();
        if(childes==null){
            return;
        }
        int max=childes.length;
        for(int i=0;i<max;i++){
            if(counter.FOUND){
                return;
            }
            T item=childes[i];
            if(item!=null){
                item.onCountUpTo(counter);
            }
        }
    }
    @Override
    public int countBytes(){
        if(isNull()){
            return 0;
        }
        T[] childes=getChildes();
        if(childes==null){
            return 0;
        }
        int result=0;
        int max=childes.length;
        for(int i=0;i<max;i++){
            T item=childes[i];
            if(item!=null){
                result += item.countBytes();
            }
        }
        return result;
    }
    @Override
    public byte[] getBytes(){
        if(isNull()){
            return null;
        }
        T[] childes=getChildes();
        if(childes==null){
            return null;
        }
        byte[] results=null;
        int max=childes.length;
        for(int i=0;i<max;i++){
            T item=childes[i];
            if(item!=null){
                results = addBytes(results, item.getBytes());
            }
        }
        return results;
    }
    @Override
    public int onWriteBytes(OutputStream stream) throws IOException {
        if(isNull()){
            return 0;
        }
        T[] childes=getChildes();
        if(childes==null){
            return 0;
        }
        int result=0;
        int max=childes.length;
        for(int i=0;i<max;i++){
            T item=childes[i];
            if(item!=null){
                result+=item.writeBytes(stream);
            }
        }
        return result;
    }

    @Override
    public void onReadBytes(BlockReader reader) throws IOException{
        T[] childes=getChildes();
        if(childes==null){
            return;
        }
        int max=childes.length;
        for(int i=0;i<max;i++){
            T item=childes[i];
            if(item!=null){
                item.readBytes(reader);
            }
        }
    }

    public abstract int childesCount();
    public abstract T[] getChildes();
}
