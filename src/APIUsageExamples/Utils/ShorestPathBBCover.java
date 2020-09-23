package APIUsageExamples.Utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import Config.Config;


/**
 * Retrieve from a graph (say CFG) all the shortest paths 
 * from a specified node to all other nodes.
 * @author tianz
 *
 */
public class ShorestPathBBCover {
	
	private static int INF=Integer.MAX_VALUE;
	public int[][] matrix;
	private FloydALG graph;
	
	public ShorestPathBBCover(int[][] matrix) {
		this.matrix=matrix;
		if (Config.FLOYDALG) {
			this.graph=new FloydALG(matrix.length);
		}
	}
	
	
	/**
	 * Find all (shortest) paths started at the specified node to every other node with modified Floyd algorithm.
	 * Shorter paths contained in Longer paths will be eliminated
	 * The algorithm ensures all basic blocks are covered at least once.
	 * @param start
	 * @return
	 */
	public HashSet<ArrayList<Integer>> doPathFind_All_Floyd(int start)
	{
		HashSet<ArrayList<Integer>> paths=new HashSet<>();
		for(int i=0;i<matrix.length;++i){
			if (start==i) {
				continue;
			}
			graph.findCheapestPath(start,i, matrix);
			ArrayList<Integer> path=graph.result;
			ArrayList<Integer> pathClone=new ArrayList<>();
			for (Integer ele : path) {
				pathClone.add(new Integer(ele.intValue()));
			}
			
			paths.add(pathClone);
		}
		
		HashSet<ArrayList<Integer>> rdPaths=new HashSet<>();
		for (ArrayList<Integer> path : paths) {
			String s1=path.toString();
			boolean contain=false;
			for (ArrayList<Integer> pathII : paths) {
				String s2=path.toString();
				if (s2.startsWith(s1)&&(!s2.equals(s1))) {
					contain=true;
					break;
				}
			}
			if (!contain) {
				rdPaths.add(path);
			}
		}
		return rdPaths;
		
	}
	
	/**
	 * Find all (shortest) paths started at the specified node and
	 * ended at leaf(or become leaf after node removal or otherwise a randomly selected unvisited node) nodes with modified Floyd algorithm.
	 * Shorter paths contained in Longer paths will be eliminated
	 * The algorithm ensures all basic blocks are covered at least once.
	 * @param start
	 * @return
	 */
	public HashSet<ArrayList<Integer>> doPathFind(int start)
	{
		HashSet<ArrayList<Integer>> paths=new HashSet<>();
		//find the shortest path between the entry node and exit nodes
		HashSet<Integer> visitedNodes=new HashSet<>();
		
		//if there exist unvisited nodes, find paths to them from the entry node
		while(visitedNodes.size()!=matrix.length)
//		HashSet<Integer> exitNodes=findLeafNodes(visitedNodes);
//		while(exitNodes.size()!=0)
		{
			HashSet<Integer> exitNodes=findLeafNodes(visitedNodes);
			for (Integer end : exitNodes) {
				graph.findCheapestPath(start,end, matrix);
				ArrayList<Integer> path=graph.result;
				visitedNodes.addAll(path);
				ArrayList<Integer> pathClone=new ArrayList<>();
				for (Integer ele : path) {
					pathClone.add(new Integer(ele.intValue()));
				}
				
				paths.add(pathClone);
				//System.out.println("Path: "+path.toString());
				//System.out.println("Visited Nodes: "+visitedNodes.toString());
				graph.result.clear();
			}
//			exitNodes=findLeafNodes(visitedNodes);
		}
		
		HashSet<ArrayList<Integer>> rdPaths=new HashSet<>();
		for (ArrayList<Integer> path : paths) {
			String s1=path.toString();
			boolean contain=false;
			for (ArrayList<Integer> pathII : paths) {
				String s2=pathII.toString();
				if (s2.startsWith(s1)&&(!s2.equals(s1))) {
					contain=true;
					break;
				}
			}
			if (!contain) {
				rdPaths.add(path);
			}
		}
		return rdPaths;
//		return paths;
	}
	
	/**
	 * find leaf nodes after removing the visited nodes
	 * @param visited
	 * @return
	 */
	private HashSet<Integer> findLeafNodes(HashSet<Integer> visited){
		int size=matrix.length;
		int[][] updatedMatrix=new int[size][size];
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				if (visited.contains(i)||visited.contains(j)) {
					updatedMatrix[i][j]=INF;
				}else{
					updatedMatrix[i][j]=matrix[i][j];
				}
				
			}
		}
		
		HashSet<Integer> leafNodes=new HashSet<>();
		for (int i = 0; i < size; i++) {
			if (visited.contains(i)) {
				continue;
			}
			boolean isLeaf=true;
			for (int j = 0; j < size; j++) {
				if (updatedMatrix[i][j]!=INF) {
					isLeaf=false;
					break;
				}
			}
			if (isLeaf) {
				leafNodes.add(i);
			}
		}
		if (leafNodes.size()==0) {	//randomly select a node as leaf node
			for (int i = size-1; i >= 0; i--) {
				if (visited.contains(i)) {
					continue;
				}
				leafNodes.add(i);
				break;
			}
		}
		return leafNodes;
	}
	
	
	/**
	 * Find all (shortest) paths started at the specified node to every other node
	 * Shorter paths contained in Longer paths will be eliminated
	 * The algorithm ensures all basic blocks are covered at least once.
	 * @param start
	 * @return
	 */
	public HashSet<ArrayList<Integer>> doPathFind_Dijkstra(int start)
	{
		//find the shortest path between the entry node and every other node		
		Dijkstra dijkstra = new Dijkstra(matrix, start);  
		Map<Integer, List<Integer>> pathMap=dijkstra.pathListMap;
		
		HashSet<ArrayList<Integer>> rdPaths=new HashSet<>();
		for (Integer key : pathMap.keySet()) {
			String s1=pathMap.get(key).toString();
			boolean contain=false;
			for (Integer key2 : pathMap.keySet()) {
				String s2=pathMap.get(key2).toString();
				if (s2.startsWith(s1)&&(!s2.equals(s1))) {
					contain=true;
					break;
				}
			}
			if (!contain) {
				rdPaths.add((ArrayList<Integer>) pathMap.get(key));
			}
		}
		return rdPaths;
	}
	
	/**
	 * Find all (shortest) paths started at the specified node to every other node
	 * Shorter paths contained in Longer paths will be eliminated
	 * The algorithm ensures all basic blocks are covered at least once.
	 * @param start
	 * @return
	 */
	public HashSet<ArrayList<Integer>> doPathFind_DijkstraII(int start)
	{
		//find the shortest path between the entry node and every other node		
		HashSet<ArrayList<Integer>> paths=DijkstraII.Dijsktra(matrix, start);
		
		HashSet<ArrayList<Integer>> rdPaths=new HashSet<>();
		for (ArrayList<Integer> path : paths) {
			String s1=path.toString();
			boolean contain=false;
			for (ArrayList<Integer> path2 : paths) {
				String s2=path2.toString();
				if (s2.startsWith(s1)&&(!s2.equals(s1))) {
					contain=true;
					break;
				}
			}
			if (!contain) {
				rdPaths.add(path);
			}
		}
		return rdPaths;
	}
	
	public static void main(String[] args) {
		int[][] matrix={  
                {INF,1,INF,INF,INF,INF,INF},  
                {INF,INF,1,1,INF,INF,INF},  
                {INF,INF,INF,1,INF,INF,INF},  
                {INF,INF,INF,INF,1,1,INF},  
                {INF,INF,INF,INF,INF,INF,INF},
                {INF,INF,INF,INF,INF,INF,1},
                {INF,INF,1,INF,INF,INF,INF}
        };
		ShorestPathBBCover sPathBBCover=new ShorestPathBBCover(matrix);
		int[] exits={4,6};
		HashSet<ArrayList<Integer>> paths=sPathBBCover.doPathFind(0);
		for (ArrayList<Integer> path : paths) {
			System.out.println(path.toString());
		}
		
	}

}
