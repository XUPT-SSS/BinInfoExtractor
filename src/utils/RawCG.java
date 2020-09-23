package utils;

import java.util.ArrayList;
import java.util.HashSet;


/**
 * The original function call graph obtained with IDA
 * @author ZZTian
 *
 */
public class RawCG {
	public HashSet<Function> nodes = new HashSet<>();
	//if called multiple times, there are multiple edges
	public ArrayList<CGEdge> rawEdges=new ArrayList<>();
	//unique edges can be constructed from rawEdges
	public HashSet<CGEdge> edges = new HashSet<>();
	public String moduleName="";	
	//	public int[][] matrix;
	
	public RawCG() {
	}
	
	
	/**
	 * Add the function as one node in the CFG
	 * Redundant nodes will not be added
	 * @param node
	 */
	public void addNodes(Function node) {
//		if (!this.nodes.contains(node)) {
//			this.nodes.add(node);
//		}
		this.nodes.add(node);
	}
	
	
	/**
	 * Add to the edges of the CFG
	 * 
	 * @param edge
	 */
	public void addEdges(CGEdge edge)
	{
		rawEdges.add(edge);
		//do not consider call frequency currently
//		if (!this.edges.contains(edge)) {
//			this.edges.add(edge);
//		}
		this.edges.add(edge);
	}
	
	/**
	 * Call only after all nodes and edges have been added to the CFG
	 */
//	public void getAdjMatrix() {
//		int size = nodes.size();
//		int[][] matrix = new int[size][size];
//		for (CGEdge edg : edges) {
//			int p1 = getPosition(edg.srcFun);
//			int p2 = getPosition(edg.dstFun);
//			matrix[p1][p2] = 1;
//		}
//	}
	
    /**
     * Traverse the CFG
     */
//    public void cfgTraverse(){
//        for(int[] i:matrix){
//            for(int j:i){
//                System.out.print(j+" ");
//            }
//            System.out.println();
//        }
//    }

    /**
     * Obtain the index in the adjacent matrix of the node according to its corresponding basic block's address 
     * @param bb
     * @return
     */
//	private int getPosition(Function fun) {
//		for (int i = 0; i < nodes.size(); i++)
//			if (nodes.get(i).fAddr == fun.fAddr)
//				return i;
//		return -1;
//	}
	
	/**
	 * Get the specified function's calling context
	 * @param f
	 * @return
	 */
	public HashSet<Function> getCallingContext(Function f)
	{
		HashSet<Function> contextList=new HashSet<>();
		contextList.addAll(getParents(f));
		contextList.addAll(getChildren(f));
		return contextList;
		
	}
	
	/**
	 * Get the specified function's parent nodes (functions that call this function) in the SCG
	 * @param f
	 * @return
	 */
	public HashSet<Function> getParents(Function f)
	{
		HashSet<Function> parents=new HashSet<>();
		for (CGEdge edg : this.edges) {
			if (edg.dstFun.equals(f)) {
				parents.add(edg.srcFun);
			}
		}
		return parents;
		
	}
	
	/**
	 * Get the specified function's child nodes (functions that this function calls) in the SCG
	 * @param f
	 * @return
	 */
	public HashSet<Function> getChildren(Function f)
	{
		HashSet<Function> childern=new HashSet<>();
		for (CGEdge edg : this.edges) {
			if (edg.srcFun.equals(f)) {
				childern.add(edg.dstFun);
			}
		}
		return childern;
	}

}

