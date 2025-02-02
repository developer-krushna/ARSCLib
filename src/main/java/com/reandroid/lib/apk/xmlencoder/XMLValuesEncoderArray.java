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
package com.reandroid.lib.apk.xmlencoder;

import com.reandroid.lib.arsc.array.ResValueBagItemArray;
import com.reandroid.lib.arsc.decoder.ValueDecoder;
import com.reandroid.lib.arsc.value.ResValueBag;
import com.reandroid.lib.arsc.value.ResValueBagItem;
import com.reandroid.lib.arsc.value.ValueType;
import com.reandroid.xml.XMLElement;

class XMLValuesEncoderArray extends XMLValuesEncoderBag{
    XMLValuesEncoderArray(EncodeMaterials materials) {
        super(materials);
    }
    @Override
    void encodeChildes(XMLElement parentElement, ResValueBag resValueBag){
        int count = parentElement.getChildesCount();
        boolean tag_string="string-array".equals(parentElement.getTagName());
        ResValueBagItemArray itemArray = resValueBag.getResValueBagItemArray();
        for(int i=0;i<count;i++){
            XMLElement child=parentElement.getChildAt(i);
            ResValueBagItem bagItem = itemArray.get(i);
            bagItem.setIdHigh((short) 0x0100);
            bagItem.setIdLow((short) (i+1));

            String valueText=child.getTextContent();

            if(ValueDecoder.isReference(valueText)){
                bagItem.setTypeAndData(ValueType.REFERENCE,
                        getMaterials().resolveReference(valueText));
            }else if(EncodeUtil.isEmpty(valueText)) {
                bagItem.setTypeAndData(ValueType.NULL, 0);
            }else if(!tag_string){
                ValueDecoder.EncodeResult encodeResult =
                        ValueDecoder.encodeGuessAny(valueText);
                if(encodeResult!=null){
                    bagItem.setTypeAndData(encodeResult.valueType,
                            encodeResult.value);
                }else {
                    bagItem.setValueAsString(ValueDecoder
                            .unEscapeSpecialCharacter(valueText));
                }
            }else {
                bagItem.setValueAsString(ValueDecoder
                        .unEscapeSpecialCharacter(valueText));
            }
        }
    }
}
