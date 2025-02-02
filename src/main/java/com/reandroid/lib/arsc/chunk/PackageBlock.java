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
package com.reandroid.lib.arsc.chunk;

 import com.reandroid.lib.arsc.BuildInfo;
 import com.reandroid.lib.arsc.array.LibraryInfoArray;
 import com.reandroid.lib.arsc.array.SpecTypePairArray;
 import com.reandroid.lib.arsc.base.Block;
 import com.reandroid.lib.arsc.container.BlockList;
 import com.reandroid.lib.arsc.container.PackageBody;
 import com.reandroid.lib.arsc.container.SingleBlockContainer;
 import com.reandroid.lib.arsc.container.SpecTypePair;
 import com.reandroid.lib.arsc.group.EntryGroup;
 import com.reandroid.lib.arsc.io.BlockLoad;
 import com.reandroid.lib.arsc.io.BlockReader;
 import com.reandroid.lib.arsc.item.FixedLengthString;
 import com.reandroid.lib.arsc.item.IntegerItem;
 import com.reandroid.lib.arsc.item.ReferenceItem;
 import com.reandroid.lib.arsc.pool.SpecStringPool;
 import com.reandroid.lib.arsc.pool.TableStringPool;
 import com.reandroid.lib.arsc.pool.TypeStringPool;
 import com.reandroid.lib.arsc.value.EntryBlock;
 import com.reandroid.lib.arsc.value.LibraryInfo;
 import com.reandroid.lib.arsc.value.StagedAliasEntry;
 import com.reandroid.lib.json.JSONConvert;
 import com.reandroid.lib.json.JSONObject;

 import java.io.IOException;
 import java.util.*;


 public class PackageBlock extends BaseChunk
        implements BlockLoad, JSONConvert<JSONObject>, Comparable<PackageBlock> {
    private final IntegerItem mPackageId;
    private final FixedLengthString mPackageName;

    private final IntegerItem mTypeStringPoolOffset;
    private final IntegerItem mTypeStringPoolCount;
    private final IntegerItem mSpecStringPoolOffset;
    private final IntegerItem mSpecStringPoolCount;
    private final SingleBlockContainer<IntegerItem> mTypeIdOffsetContainer;
    private final IntegerItem mTypeIdOffset;

    private final TypeStringPool mTypeStringPool;
    private final SpecStringPool mSpecStringPool;

    private final PackageBody mBody;

    private final Map<Integer, EntryGroup> mEntriesGroup;

    public PackageBlock() {
        super(ChunkType.PACKAGE, 3);
        this.mPackageId=new IntegerItem();
        this.mPackageName = new FixedLengthString(256);

        this.mTypeStringPoolOffset = new IntegerItem();
        this.mTypeStringPoolCount = new IntegerItem();
        this.mSpecStringPoolOffset = new IntegerItem();
        this.mSpecStringPoolCount = new IntegerItem();

        this.mTypeIdOffsetContainer = new SingleBlockContainer<>();
        this.mTypeIdOffset = new IntegerItem();
        this.mTypeIdOffsetContainer.setItem(mTypeIdOffset);


        this.mTypeStringPool=new TypeStringPool(false, mTypeIdOffset);
        this.mSpecStringPool=new SpecStringPool(true);

        this.mBody = new PackageBody();

        this.mEntriesGroup=new HashMap<>();

        mPackageId.setBlockLoad(this);

        addToHeader(mPackageId);
        addToHeader(mPackageName);
        addToHeader(mTypeStringPoolOffset);
        addToHeader(mTypeStringPoolCount);
        addToHeader(mSpecStringPoolOffset);
        addToHeader(mSpecStringPoolCount);
        addToHeader(mTypeIdOffsetContainer);

        addChild(mTypeStringPool);
        addChild(mSpecStringPool);

        addChild(mBody);

    }
    public BlockList<UnknownChunk> getUnknownChunkList(){
         return mBody.getUnknownChunkList();
    }

    public StagedAliasEntry searchByStagedResId(int stagedResId){
        for(StagedAlias stagedAlias:getStagedAliasList().getChildes()){
            StagedAliasEntry entry=stagedAlias.getStagedAliasEntryArray()
                    .searchByStagedResId(stagedResId);
            if(entry!=null){
                return entry;
            }
        }
        return null;
    }
    public BlockList<StagedAlias> getStagedAliasList(){
        return mBody.getStagedAliasList();
    }
    public BlockList<Overlayable> getOverlayableList(){
        return mBody.getOverlayableList();
    }
    public BlockList<OverlayablePolicy> getOverlayablePolicyList(){
        return mBody.getOverlayablePolicyList();
    }
    public void sortTypes(){
        getSpecTypePairArray().sort();
    }
    @Override
    public void onBlockLoaded(BlockReader reader, Block sender) throws IOException {
        if(sender==mPackageId){
            int headerSize=getHeaderBlock().getHeaderSize();
            if(headerSize!=288){
                mTypeIdOffset.set(0);
                mTypeIdOffsetContainer.setItem(null);
            }
        }
    }
    public void removeEmpty(){
        getSpecTypePairArray().removeEmptyPairs();
    }
    public boolean isEmpty(){
        return getSpecTypePairArray().isEmpty();
    }
    public int getId(){
        return mPackageId.get();
    }
    public void setId(byte id){
         setId(0xff & id);
     }
    public void setId(int id){
        mPackageId.set(id);
    }
    public String getName(){
        return mPackageName.get();
    }
    public void setName(String name){
        mPackageName.set(name);
    }
    public TableBlock getTableBlock(){
        Block parent=getParent();
        while(parent!=null){
            if(parent instanceof TableBlock){
                return (TableBlock)parent;
            }
            parent=parent.getParent();
        }
        return null;
    }
    public TypeStringPool getTypeStringPool(){
        return mTypeStringPool;
    }
    public SpecStringPool getSpecStringPool(){
        return mSpecStringPool;
    }
    public SpecTypePairArray getSpecTypePairArray(){
        return mBody.getSpecTypePairArray();
    }
    public Collection<LibraryInfo> listLibraryInfo(){
        return getLibraryBlock().listLibraryInfo();
    }

    public void addLibrary(LibraryBlock libraryBlock){
        if(libraryBlock==null){
            return;
        }
        for(LibraryInfo info:libraryBlock.getLibraryInfoArray().listItems()){
            addLibraryInfo(info);
        }
    }
    public void addLibraryInfo(LibraryInfo info){
        getLibraryBlock().addLibraryInfo(info);
    }
    private LibraryBlock getLibraryBlock(){
        return mBody.getLibraryBlock();
    }
    public Set<Integer> listResourceIds(){
        return mEntriesGroup.keySet();
    }
    public EntryBlock getOrCreateEntry(byte typeId, short entryId, String qualifiers){
        return getSpecTypePairArray().getOrCreateEntry(typeId, entryId, qualifiers);
    }
    public EntryBlock getEntry(byte typeId, short entryId, String qualifiers){
        return getSpecTypePairArray().getEntry(typeId, entryId, qualifiers);
    }
    public TypeBlock getOrCreateTypeBlock(byte typeId, String qualifiers){
        return getSpecTypePairArray().getOrCreateTypeBlock(typeId, qualifiers);
    }
    public TypeBlock getTypeBlock(byte typeId, String qualifiers){
        return getSpecTypePairArray().getTypeBlock(typeId, qualifiers);
    }
    public Map<Integer, EntryGroup> getEntriesGroupMap(){
        return mEntriesGroup;
    }
    public Collection<EntryGroup> listEntryGroup(){
        return getEntriesGroupMap().values();
    }

    /**
     * Searches entries by resource id from local map, then if not find
     * search by alias resource id
     * */
    public EntryGroup getEntryGroup(int resourceId){
        if(resourceId==0){
            return null;
        }
        EntryGroup entryGroup=getEntriesGroupMap().get(resourceId);
        if(entryGroup!=null){
            return entryGroup;
        }
        StagedAliasEntry stagedAliasEntry = searchByStagedResId(resourceId);
        if(stagedAliasEntry!=null){
            return getEntriesGroupMap()
                    .get(stagedAliasEntry.getFinalizedResId());
        }
        return null;
    }
    public void updateEntry(EntryBlock entryBlock){
        if(entryBlock==null||entryBlock.isNull()){
            return;
        }
        updateEntryGroup(entryBlock);
        updateEntryTableReferences(entryBlock);
    }
    public void removeEntryGroup(EntryBlock entryBlock){
        if(entryBlock==null){
            return;
        }
        int id=entryBlock.getResourceId();
        EntryGroup group=getEntriesGroupMap().get(id);
        if(group==null){
            return;
        }
        group.remove(entryBlock);
        if(group.size()==0){
            getEntriesGroupMap().remove(id);
        }
    }
    private void updateEntryTableReferences(EntryBlock entryBlock){
        TableBlock tableBlock=getTableBlock();
        if(tableBlock==null){
            return;
        }
        List<ReferenceItem> tableReferences=entryBlock.getTableStringReferences();
        TableStringPool tableStringPool=tableBlock.getTableStringPool();
        tableStringPool.addReferences(tableReferences);
    }
    private void updateEntryGroup(EntryBlock entryBlock){
        int resId=entryBlock.getResourceId();
        EntryGroup group=getEntriesGroupMap().get(resId);
        if(group==null){
            group=new EntryGroup(resId);
            getEntriesGroupMap().put(resId, group);
        }
        group.add(entryBlock);
    }

    public List<EntryBlock> listEntries(byte typeId, int entryId){
        List<EntryBlock> results=new ArrayList<>();
        for(SpecTypePair pair:listSpecTypePair(typeId)){
            results.addAll(pair.listEntries(entryId));
        }
        return results;
    }
    public List<SpecTypePair> listSpecTypePair(byte typeId){
        List<SpecTypePair> results=new ArrayList<>();
        for(SpecTypePair pair:listAllSpecTypePair()){
            if(typeId==pair.getTypeId()){
                results.add(pair);
            }
        }
        return results;
    }
    public Collection<SpecTypePair> listAllSpecTypePair(){
        return getSpecTypePairArray().listItems();
    }

    private void refreshTypeStringPoolOffset(){
        int pos=countUpTo(mTypeStringPool);
        mTypeStringPoolOffset.set(pos);
    }
    private void refreshTypeStringPoolCount(){
        mTypeStringPoolCount.set(mTypeStringPool.countStrings());
    }
    private void refreshSpecStringPoolOffset(){
        int pos=countUpTo(mSpecStringPool);
        mSpecStringPoolOffset.set(pos);
    }
    private void refreshSpecStringCount(){
        mSpecStringPoolCount.set(mSpecStringPool.countStrings());
    }
    private void refreshTypeIdOffset(){
        // TODO: find solution
        //int largest=getSpecTypePairArray().getHighestTypeId();
        //int count=getTypeStringPool().countStrings();
        //mTypeIdOffset.set(count-largest);
        mTypeIdOffset.set(0);
    }
    public void onEntryAdded(EntryBlock entryBlock){
        updateEntry(entryBlock);
    }
    @Override
    public void onChunkLoaded() {
    }

    @Override
    protected void onChunkRefreshed() {
        refreshTypeStringPoolOffset();
        refreshTypeStringPoolCount();
        refreshSpecStringPoolOffset();
        refreshSpecStringCount();
        refreshTypeIdOffset();
    }

    @Override
    public JSONObject toJson() {
        JSONObject jsonObject=new JSONObject();

        jsonObject.put(BuildInfo.NAME_arsc_lib_version, BuildInfo.getVersion());

        jsonObject.put(NAME_package_id, getId());
        jsonObject.put(NAME_package_name, getName());
        jsonObject.put(NAME_specs, getSpecTypePairArray().toJson());
        LibraryInfoArray libraryInfoArray = getLibraryBlock().getLibraryInfoArray();
        if(libraryInfoArray.childesCount()>0){
            jsonObject.put(NAME_libraries,libraryInfoArray.toJson());
        }
        StagedAlias stagedAlias =
                StagedAlias.mergeAll(getStagedAliasList().getChildes());
        if(stagedAlias!=null){
            jsonObject.put(NAME_staged_aliases,
                    stagedAlias.getStagedAliasEntryArray().toJson());
        }
        return jsonObject;
    }
    @Override
    public void fromJson(JSONObject json) {
        setId(json.getInt(NAME_package_id));
        setName(json.getString(NAME_package_name));
        getSpecTypePairArray().fromJson(json.getJSONArray(NAME_specs));
        LibraryInfoArray libraryInfoArray = getLibraryBlock().getLibraryInfoArray();
        libraryInfoArray.fromJson(json.optJSONArray(NAME_libraries));
        if(json.has(NAME_staged_aliases)){
            StagedAlias stagedAlias=new StagedAlias();
            stagedAlias.getStagedAliasEntryArray()
                    .fromJson(json.getJSONArray(NAME_staged_aliases));
            getStagedAliasList().add(stagedAlias);
        }
    }
    public void merge(PackageBlock packageBlock){
        if(packageBlock==null||packageBlock==this){
            return;
        }
        if(getId()!=packageBlock.getId()){
            throw new IllegalArgumentException("Can not merge different id packages: "
                    +getId()+"!="+packageBlock.getId());
        }
        setName(packageBlock.getName());
        getLibraryBlock().merge(packageBlock.getLibraryBlock());
        getSpecTypePairArray().merge(packageBlock.getSpecTypePairArray());
    }
    /**
     * It is allowed to have duplicate type name therefore it is not recommend to use this.
     * Lets depreciate to warn developer
     */
    @Deprecated
    public SpecTypePair searchByTypeName(String typeName){
        return getSpecTypePairArray().searchByTypeName(typeName);
    }
    @Override
    public int compareTo(PackageBlock pkg) {
        return Integer.compare(getId(), pkg.getId());
    }
    @Override
    public String toString(){
        StringBuilder builder=new StringBuilder();
        builder.append(super.toString());
        builder.append(", id=");
        builder.append(String.format("0x%02x", getId()));
        builder.append(", name=");
        builder.append(getName());
        int libCount=getLibraryBlock().getLibraryCount();
        if(libCount>0){
            builder.append(", libraries=");
            builder.append(libCount);
        }
        return builder.toString();
    }

    public static final String NAME_package_id = "package_id";
    public static final String NAME_package_name = "package_name";
    public static final String JSON_FILE_NAME = "package.json";
    private static final String NAME_specs="specs";
    private static final String NAME_libraries="libraries";
    public static final String NAME_staged_aliases="staged_aliases";
}
