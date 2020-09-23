package APIUsageExamples.Utils;


import java.util.ArrayList;
import java.util.HashSet;

public class DijkstraII {  
	public static int INF=Integer.MAX_VALUE/100;
	public static int M=10000;
    public static void main(String[] args) {  
  
//        //邻接矩阵  
        int[][] matrix = {  
                {0,3,2000,7,M},  
                {3,0,4,2,M},  
                {M,4,0,5,4},  
                {7,2,5,0,6},  
                {M,M,4,6,0}  
        };  
        
        int[][] matrix2={  
                {INF,1,INF,INF,INF,INF,INF},  
                {INF,INF,1,1,INF,INF,INF},  
                {INF,INF,INF,1,INF,INF,INF},  
                {INF,INF,INF,INF,1,1,INF},  
                {INF,INF,INF,INF,INF,INF,INF},
                {INF,INF,INF,INF,INF,INF,1},
                {INF,INF,1,INF,INF,INF,INF}
        };
  
        int start=0;  
        HashSet<ArrayList<Integer>> paths = Dijsktra(matrix2,start);  
        for (ArrayList<Integer> path : paths) {
			System.out.println(path.toString());
		}
    }  
  
    //接受一个有向图的权重矩阵，和一个起点编号start（从0编号，顶点存在数组中）  
    public static HashSet<ArrayList<Integer>> Dijsktra(int[][] weight,int start){  
  
        int n = weight.length;  
        //存放从start到其他各点的最短路径  
//        int[] shortPath = new int[n];  
        //存放从start到其他各点的最短路径的字符串表示
        HashSet<ArrayList<Integer>> paths=new HashSet<>();
        
        String[] path=new String[n];  
        for(int i=0;i<n;i++)  
        {
            path[i] = start + "-->" + i;  
        }  
        //标记当前该顶点的最短路径是否已经求出,1表示已求出  
        int[] visited = new int[n];  
  
//        shortPath[start] = 0;  
        visited[start] = 1;  
        for(int count = 1;count <= n - 1;count++)  
        {  
            //选出一个距离初始顶点start最近的未标记顶点  
            int k = -1;  
            int dmin = 1000;  
            for(int i = 0;i < n;i++)  
            {  
                if(visited[i] == 0 && weight[start][i] < dmin)  
                {  
                    dmin = weight[start][i];  
                    k = i;  
                }  
            }  
            //将新选出的顶点标记为已求出最短路径，且到start的最短路径就是dmin  
//            shortPath[k] = dmin;
/*            if (k>=n||k<0) {
				System.out.println(k);
			}
            visited[k] = 1; */ 
            //以k为中间点，修正从start到未访问各点的距离  
            for(int i = 0;i < n;i++)  
            {
                if(visited[i] == 0 && weight[start][k] + weight[k][i] < weight[start][i])  
                {  
                    weight[start][i] = weight[start][k] + weight[k][i];  
                    path[i]=path[k]+"-->"+i;  
                }  
            }  
        }
        for(int i=0;i<n;i++)  
        {
        	if (start==i) {
				continue;
			}
        	String[] nodes=path[i].split("-->");
        	ArrayList<Integer> tmpPath=new ArrayList<>(nodes.length);
        	for (String str : nodes) {
				tmpPath.add(Integer.parseInt(str));
			}
        	paths.add(tmpPath);
//            System.out.println("从"+start+"出发到"+i+"的最短路径为："+path[i]);  
        }  
        return paths;  
    }  
}