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
package com.reandroid.lib.arsc.pool.builder;

import com.reandroid.lib.arsc.item.StringItem;

import java.util.regex.Pattern;

public class StyleBuilder {
    public static void buildStyle(StringItem stringItem){
    }
    public static boolean hasStyle(StringItem stringItem){
        if(stringItem==null){
            return false;
        }
        return hasStyle(stringItem.getHtml());
    }
    public static boolean hasStyle(String text){
        if(text==null){
            return false;
        }
        int i=text.indexOf('<');
        if(i<0){
            return false;
        }
        i=text.indexOf('>');
        return i>1;
    }
    private static final Pattern PATTERN_STYLE=Pattern.compile("");
}
