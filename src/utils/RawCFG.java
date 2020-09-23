package utils;

import java.util.ArrayList;

import Config.Config;


/**
 * The original control flow graph obtained with IDA
 * 
 * @author Administrator
 *
 */
public class RawCFG {

	public ArrayList<BasicBlock> nodes;
	public ArrayList<CFGEdg> edges;
	// public int[][] matrix;

	private static int INF = Integer.MAX_VALUE;

	public RawCFG() {
		this.nodes = new ArrayList<>();
		this.edges = new ArrayList<>();
	}

	/**
	 * Add the basic block as one node in the CFG
	 */
	public void addNodes(BasicBlock node) {
		this.nodes.add(node);
	}

	/**
	 * Add to the edges of the CFG
	 * 
	 * @param edge
	 */
	public void addEdges(CFGEdg edge) {
		this.edges.add(edge);
	}

	
	/**
	 * Call only after all nodes and edges have been added to the CFG
	 * @return the adjacent matrix representation of CFG
	 */
	public int[][] getAdjMatrix() {
		int size = nodes.size();
		int[][] matrix = new int[size][size];
		for (int i = 0; i < size; i++) { // initialize the matrix
			for (int j = 0; j < size; j++) {
				if (Config.FLOYDALG) {
					matrix[i][j] = INF;
				} else {
					matrix[i][j] = INF / 100;
				}
			}
		}
		for (CFGEdg edg : edges) {
			int p1 = getPosition(edg.srcBlk);
			int p2 = getPosition(edg.dstBlk);
			matrix[p1][p2] = 1;
		}
		return matrix;
	}

	/**
	 * Traverse the CFG
	 */
	// public void cfgTraverse(){
	// for(int[] i:matrix){
	// for(int j:i){
	// System.out.print(j+" ");
	// }
	// System.out.println();
	// }
	// }

	// 根据顶点名称获取对应的矩阵下标
	/**
	 * Obtain the index in the adjacent matrix of the node according to its
	 * corresponding basic block's address
	 * 
	 * @param bb
	 * @return
	 */
	public int getPosition(BasicBlock bb) {
		for (int i = 0; i < nodes.size(); i++)
			if (nodes.get(i).bbAddr == bb.bbAddr)
				return i;
		return -1;
	}

	/**
	 * 
	 * Get the entry basicblock of the cfg
	 * In the case that there is a loop also to the entry node, then just return the first node
	 * 
	 * @return null if the cfg contains no node
	 */
	public BasicBlock getEntryNode() {
		if (nodes.isEmpty()) {
			return null;
		} else {
			for (BasicBlock bb : nodes) {
				if (bb.inBlocks.size() == 0) {
					return bb;
				}
			}
			// System.err.println("This case should not happen!! There should be at least one root node in the CFG");
			// in the case that there is a loop to the entry node, then return the first node
			return nodes.get(0);
		}
	}

	/**
	 * Get the exit basic blocks of the cfg
	 * 
	 * @return null if the cfg contains no node
	 */
	public ArrayList<BasicBlock> getExitNodes() {
		ArrayList<BasicBlock> exitNodes = new ArrayList<>();
		if (nodes.isEmpty()) {
			return null;
		} else {
			for (BasicBlock bb : nodes) {
				if (bb.outBlocks.size() == 0) {
					exitNodes.add(bb);
				}
			}
			if (exitNodes.isEmpty()) {
				System.err.println("This case should not happen!!! There should be at least one exit block");
			}
			return exitNodes;
		}
	}
}

