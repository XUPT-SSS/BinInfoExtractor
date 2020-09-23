package utils;

public class CGEdge
{
	public Function srcFun;
	public Function dstFun;
	public int callBlkID;
	public int callInsAddr;
	
	public CGEdge(Function srcFun, Function dstFun) {
		this.srcFun=srcFun;
		this.dstFun=dstFun;
	}
	
	public CGEdge(Function srcFun, Function dstFun, int callBlkID, int callInsAddr) {
		this.srcFun=srcFun;
		this.dstFun=dstFun;
		this.callBlkID=callBlkID;
		this.callInsAddr=callInsAddr;
	}
	
	@Override
	public String toString() {
		return srcFun.toString()+"-->"+dstFun.toString();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(this ==obj){
            return true;            
        } 
        if (obj!=null && obj instanceof CGEdge) {   
            CGEdge u = (CGEdge) obj;   
            return this.srcFun.equals(u.srcFun)&&this.dstFun.equals(u.dstFun)
            		&&this.callBlkID==u.callBlkID&&this.callInsAddr==u.callInsAddr;   
        }else{
            return false;
        }
	}
	
	@Override
	public int hashCode() {
		return this.toString().hashCode();
	}

}

