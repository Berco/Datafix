package by.zatta.datafix.model;

public class DataEntry implements Comparable<DataEntry>{
    private String mDataName;
    private String mType;
    private long mYaffsSize;
    private long mDataSize;
    private long mTotalSize;
       
    public DataEntry(String dataName, String dataSize, String yaffsSize, String type) {
    	mDataName = dataName;
    	mType = type;
    	mYaffsSize = Long.valueOf(yaffsSize)*1024;
    	mDataSize = Long.valueOf(dataSize)*1024;
    	mTotalSize = mYaffsSize + mDataSize;
    }
    
    public void setName(String dataName){
    	mDataName = dataName;
    }
    
    public void setSizeYaffs(String yaffs_size){
    	mYaffsSize = Long.valueOf(yaffs_size)*1024;
    }
    
    public void setSizeData(String total_size){
    	mDataSize = Long.valueOf(total_size)*1024;
    }
       
    public String getDataName(){
    	return mDataName;
    }
    
    public String getType(){
    	return mType;
    }
    
    public long getYaffsSize(){
    	return mYaffsSize;
    }
    
    public long getDataSize(){
    	return mDataSize;
    }

        
    @Override public String toString() {
        return mDataName;
    }
    
    @Override
	public int compareTo(DataEntry o) {
		
			if(this.mDataName != null && Long.toString(this.mTotalSize) != null ){
					if (this.mDataName.equals("cache")){
						return -1;
					}
					if (o.mDataName.equals("cache") ){
						return 1;
					}
					else{
						int num = 0;
						if (this.mTotalSize < o.mTotalSize ) num = 1; 
						if (this.mTotalSize == o.mTotalSize ) num =  0;
						if (this.mTotalSize > o.mTotalSize ) num =  -1;
						if (num != 0){
							return num;
						}else{
							return this.mDataName.compareTo(o.getDataName());
						}
					}
			}
			else 
				throw new IllegalArgumentException();
	}
    
//    @Override
//   	public int compareTo(DataEntry o) {
//
//    	if(this.mDataName != null){
//    		return this.mDataName.compareTo(o.getDataName());
//    	}
//    	else throw new IllegalArgumentException();
//    }

}
