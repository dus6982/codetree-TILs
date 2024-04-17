import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.StringTokenizer;

public class Main {
	
	static int K, M, sum;
	static int[] ruin; //유물 조각 리스트
	static PriorityQueue<Point> pq;
	static int[][] map = new int[5][5]; //5*5
	static boolean[][] del = new boolean[5][5];
	static int[] result; //턴마다의 유물 가치 총합
	
	static int[] dr = {-1, 1, 0, 0};
	static int[] dc = {0, 0, -1, 1};

	public static void main(String[] args) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		StringTokenizer st = new StringTokenizer(br.readLine());
		
		K = Integer.parseInt(st.nextToken()); //턴 수
		M = Integer.parseInt(st.nextToken()); //유물 개수
		
		//맵 입력
		for(int r=0; r<5; r++) {
			st = new StringTokenizer(br.readLine());
			for(int c=0; c<5; c++) {
				map[r][c] = Integer.parseInt(st.nextToken());
			}
		}
		
		//유물 입력
		st = new StringTokenizer(br.readLine());
		ruin = new int[M];
		for(int i=0; i<M; i++) {
			ruin[i] = Integer.parseInt(st.nextToken());
		}

		result = new int[K];
		
		for(int k=0; k<K; k++) {
			//1. 탐사 진행
			//3*3구역마다 90도, 180도, 270도 해서 유물 가치 최대인 구역 표시해주기
			pq = new PriorityQueue<>();
			
			for(int r=1; r<=3; r++) {
				for(int c=1; c<=3; c++) {
					findSection(r,c);
				}
			}

			sum = 0;
			
			//2. 유물 획득
			//pg 맨 앞에 맞게 원본 배열을 회전 시켜준 뒤
			//만들어 둔 bfs로 3개 이상으로 연결된 조각  표시 해주기
			Point p = pq.peek();
			
			//회전 다 해본 결과 최대 유물 가치가 3개가 넘는 것이 없다면 회전할 수 있는 경우가 없단 것
			//즉시 중단
			if(p.value<3) break; 

			getRuin(p, true);
			result[k]+= sum;
			
			//표시 된 구역 없애주고 (0으로)
			if(sum>0) {
				removeSection();
				//빈 칸 벽면에서 유물 채워주기
				addRuin();
				
				sum = 0;
				del = new boolean[5][5];
			}
			
			while(true) {
				//3. 유물 연쇄 획득
				//만들어 둔 bfs로 3개 이상으로 연결된 조각  표시 해주기
				getRuin(p, false);
				result[k]+= sum;
				
				//표시 된 구역 없애주고 (0으로)
				if(sum>0) {
					removeSection();
					//빈 칸 벽면에서 유물 채워주기
					addRuin();
					
					sum = 0;
					del = new boolean[5][5];
				} else break;
			}
			
		}
		
		for(int k=0; k<K; k++) {
			if(result[k]==0) continue;
			System.out.print(result[k]+" ");
		}
	}
	
	private static void removeSection() {
		for(int r=0; r<5; r++) {
			for(int c=0; c<5; c++) {
				if(del[r][c]) map[r][c] = 0;
			}
		}
	}

	private static void getRuin(Point p, boolean rotate) {
		if(rotate) {
			int[][] tmp = new int[5][5];
			
			for(int i=0; i<=p.rt; i++) {
				tmp = rotate(map, p.r, p.c);
			}
			
			for(int r=0; r<5; r++) {
				for(int c=0; c<5; c++) {
					map[r][c] = tmp[r][c];
				}
			}
		}
		
		boolean[][] visit = new boolean[5][5];
		for(int r=0; r<5; r++) {
			for(int c=0; c<5; c++) {
				if(visit[r][c]) continue;
				
				sum += bfs(r, c, map, visit, true);
			}
		}
	}

	private static void findSection(int r, int c) {
		int[][] tmap = new int[5][5]; //회전 완료된 거 전체
		
		//1. 먼저 90도 하기
		tmap = rotate(map, r, c);
		pq.add(new Point(r, c, 0, calVal(tmap)));
		
		//2. 180도
		tmap = rotate(tmap, r, c);
		pq.add(new Point(r, c, 1, calVal(tmap)));
		
		//3. 270도
		tmap = rotate(tmap, r, c);
		pq.add(new Point(r, c, 2, calVal(tmap)));
	}

	private static int calVal(int[][] tmap) {
		boolean[][] visit = new boolean[5][5];
		int sum = 0;
		
		for(int r=0; r<5; r++) {
			for(int c=0; c<5; c++) {
				if(visit[r][c]) continue;
				
				sum += bfs(r, c, tmap, visit, false);
			}
		}
		
		return sum;
	}

	private static int bfs(int r, int c, int[][] tmap, boolean[][] visit, boolean mark) {
		Queue<int[]> q = new LinkedList<int[]>();
		Queue<int[]> q1 = new LinkedList<int[]>();
		
		q.add(new int[] {r, c});
		q1.add(new int[] {r, c});
		
		visit[r][c] = true;
		
		int idx = tmap[r][c];
		int cnt = 1;
		
		while(!q.isEmpty()) {
			int[] arr = q.poll();
			
			for(int d=0; d<4; d++) {
				int nr = arr[0]+dr[d];
				int nc = arr[1]+dc[d];
				
				if(!check(nr,nc)) continue;
				if(visit[nr][nc]) continue;
				
				if(tmap[nr][nc]==idx) {
					visit[nr][nc] = true;
					q.add(new int[] {nr, nc});
					q1.add(new int[] {nr, nc});
					cnt++;
				}
			}
		}
		
		if(cnt<3) {
			cnt = 0;
		} 
		
		if(cnt>=3 && mark) {
			mark(q1);
		}
		
		return cnt;
	}

	private static void mark(Queue<int[]> q) {
		for(int[] arr : q) {
			del[arr[0]][arr[1]] = true;
		}
	}

	private static boolean check(int nr, int nc) {
		if(nr>=0 && nr<5 && nc>=0 && nc<5) {
			return true;
		}
		
		return false;
	}

	private static int[][] rotate(int[][] map, int sr, int sc) {
		int[][] tmp = new int[5][5];
		
		for(int r=sr-1; r<=sr+1; r++) {
			for(int c=sc-1; c<=sc+1; c++) {
				int r1 = r-(sr-1);
				int c1 = c-(sc-1);
				
				int y = c1;
				int x = 3-r1-1;
				
				tmp[y+(sr-1)][x+(sc-1)] = map[r1+(sr-1)][c1+(sc-1)];
			}
		}
		
		for(int r=0; r<5; r++) {
			for(int c=0; c<5; c++) {
				if(tmp[r][c]==0) {
					tmp[r][c] = map[r][c];
				}
			}
		}
		
		return tmp;
	}

	private static void addRuin() {
//		System.out.println("유물 새로 추가====");
		// 유물 채우기
		//c0-5 , r5-0  맵 보면서 0인 자리에 
		//벽면에 쓰여있는 유물 순서대로 넣어주고 
		//뺀 유물은 유물 리스트에 0으로 바꿔주기 
		for(int c=0; c<5; c++) {
			for(int r=4; r>=0; r--) {
				if(map[r][c]==0) {
					map[r][c] = findRuin();
				}
			}
		}
		
	}

	private static int findRuin() {
		int idx = 0;
		
		for(int i=0; i<M; i++) {
			if(ruin[i]!=0) {
				idx = ruin[i];
				ruin[i] = 0;
				break;
			}
		}
		
		return idx;
	}

	static class Point implements Comparable<Point> {
		int r;
		int c;
		int rt;
		int value;
		
		public Point(int r, int c, int rt, int value) {
			super();
			this.r = r;
			this.c = c;
			this.rt = rt;
			this.value = value;
		}

		@Override
		public int compareTo(Point o) {
			//유물 획득 가치 최대
			if(this.value != o.value) return o.value - this.value;
			//회전 각도 가장 작은
			if(this.rt != o.rt) return this.rt - o.rt;
			//회전 중심 열 좌표 가장 작은
			if(this.r != o.r) return this.r - o.r;
			//회전 중심 행 좌표 가장 작은
			return this.c - o.c;
		}
	}
}