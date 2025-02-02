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


 import com.reandroid.lib.arsc.base.Block;
 import com.reandroid.lib.arsc.pool.TypeStringPool;

 public class TypeString extends StringItem {
    public TypeString(boolean utf8) {
        super(utf8);
    }
    public byte getId(){
        TypeStringPool stringPool=getTypeStringPool();
        if(stringPool!=null){
            return stringPool.idOf(this);
        }
        // Should not reach here , this means it not added to string pool
        return (byte) (getIndex()+1);
    }
    @Override
    public StyleItem getStyle(){
        // Type don't have style unless to obfuscate/confuse other decompilers
        return null;
    }
    private TypeStringPool getTypeStringPool(){
        Block parent=this;
        while (parent!=null){
            if(parent instanceof TypeStringPool){
                return (TypeStringPool) parent;
            }
            parent=parent.getParent();
        }
        return null;
    }
}
