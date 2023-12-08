import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Queue;
import java.util.StringTokenizer;

public class Main {
	//색깔 폭탄
	static int n, m;
	static final int red = 0, stone = -1, blank = -2;
	static int[][] map;
	static boolean[][] visit;
	static int cnt, score;
	static ArrayList<Color> list;
	static int[] dr = {-1, 1, 0, 0};
	static int[] dc = {0, 0, -1, 1};
	
	public static void main(String[] args) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		StringTokenizer st = new StringTokenizer(br.readLine());
		
		n = Integer.parseInt(st.nextToken());
		m = Integer.parseInt(st.nextToken()); //m개의 색깔
		
		map = new int[n][n];
		list = new ArrayList<>();
		
		//맵 입력
		for(int r=0; r<n; r++) {
			st = new StringTokenizer(br.readLine());
			for(int c=0; c<n; c++) {
				map[r][c] = Integer.parseInt(st.nextToken());
			}
		}
		
		while(true) {
			//폭탄 묶음이 없을 경우에는 종료
			if(!existBomb()) break;
			
			//1. 가장 큰 폭탄 묶음 찾기
			Color big = findMaxBomb();
			
			//2. 해당 폭탄 제거하기
			cnt = 0;
			removeBomb(big);
			
			//3. 중력 = (중력-90도 회전-중력)
			gravity();
			
			//4. 점수 반영
			score += cnt*cnt;
			
			//터질 폭탄 묶음 리스트 초기화
			list.clear();
		}
		
		System.out.println(score);
	}

	private static boolean existBomb() {
		//각 색깔의 기준점과 폭탄 개수 구하기
		visit = new boolean[n][n];
		boolean[] exist = new boolean[m+1];
		
		for(int r=0; r<n; r++) {
			for(int c=0; c<n; c++) {
				if(visit[r][c]) continue;
				if(map[r][c]==stone || map[r][c]==red || map[r][c] == blank) {
					visit[r][c] = true;
					continue;
				}

				//묶음인 경우에만 넣어주기
				Color clr = bundle(map[r][c], r, c);
				if(clr.cnt>1) exist[clr.idx] = true;
			}
		}
		
		for(int i=1; i<=m; i++) {
			if(exist[i]) return true; //하나의 묶음이라도 있다면 가능
		}
		
		return false;
	}

	private static void gravity() {
		fallDown();
		rotate();
		fallDown();
	}

	private static void rotate() {
		//반시계 방향으로 90도 회전
		int[][] arr = new int[n][n];
        for(int i=0; i<n; i++){
            for(int j=0; j<n; j++){
                arr[i][j] = map[j][n-i-1];
            }
        }

        //다시 복사
        for(int i=0; i<n; i++) {
        	for(int j=0; j<n; j++) {
        		map[i][j] = arr[i][j];
        	}
        }
	}

	private static void fallDown() {
		int[] arr = new int[n];
		
		//빈 칸의 개수 구하기
		for(int c=0; c<n; c++) {
			for(int r=0; r<n; r++) {
				if(map[r][c]==blank) arr[c]++;
			}
		}
		
		for(int c=0; c<n; c++) {
			if(arr[c]==0) continue; //내릴 필요 없음
			
			while(arr[c]>0) {
				for(int r=n-1; r>0; r--) {
					if(map[r][c] == blank) { //현재 칸이 빈 칸이면 위에꺼 내려줘야 함
						if(map[r+dr[0]][c] >= red) { //색깔 있으면 아래로 내려주고 위칸은 빈칸으로
							//위 칸을 아래 칸으로 이동
							map[r][c] = map[r+dr[0]][c];
							map[r+dr[0]][c] = blank;
						} 
					} 
				}
				arr[c]--; //한 번 내려 줬으니 --
			}
		}
	}

	private static void removeBomb(Color clr) {
		visit = new boolean[n][n];
		boolean[][] rm = new boolean[n][n];
		Queue<int[]> q = new LinkedList<int[]>();
		visit[clr.r][clr.c] = true; rm[clr.r][clr.c] = true;
		q.add(new int[] {clr.r,clr.c});
		
		while(!q.isEmpty()) {
			int[] now = q.poll();
			int cr = now[0]; int cc = now[1];
			
			for(int d=0; d<4; d++) {
				int nr = cr+dr[d];
				int nc = cc+dc[d];
				
				if(!check(nr, nc) || visit[nr][nc] || rm[nr][nc]) continue;
				
				visit[nr][nc] = true;
				
				if(map[nr][nc] == clr.idx || map[nr][nc] == red) {
					q.add(new int[] {nr, nc});
					rm[nr][nc] = true;
				}
			}
		}
		remove(rm);
	}

	private static void remove(boolean[][] rm) {
		for(int r=0; r<n; r++) {
			for(int c=0; c<n; c++) {
				if(rm[r][c]) {
					map[r][c] = -2; //폭탄 제거
					cnt++;
				}
			}
		}
	}

	private static Color findMaxBomb() {
		int idx = 0;
		
		//각 색깔의 기준점과 폭탄 개수 구하기
		visit = new boolean[n][n];
		for(int r=0; r<n; r++) {
			for(int c=0; c<n; c++) {
				if(visit[r][c]) continue;
				if(map[r][c]==stone || map[r][c]==red || map[r][c]==blank) {
					visit[r][c] = true;
					continue;
				}
				
				//묶음인 경우에만 넣어주기
				Color clr = bundle(map[r][c], r, c); 
				if(clr.cnt>1) list.add(clr);
			}
		}
		
		//list에서 크기가 가장 큰 폭탄 선택
		Collections.sort(list);

		return list.get(0);
	}

	private static Color bundle(int idx, int r, int c) {
		boolean[][] r_visit = new boolean[n][n];
		Queue<int[]> q = new LinkedList<int[]>();
		visit[r][c] = true;
		q.add(new int[] {r,c});
		
		int cnt = 1; //현재 자기 자신 포함
		int r_cnt = 0; //빨간 폭탄 개수
		int stand_r = r; int stand_c = c; //기준점
		
		while(!q.isEmpty()) {
			int[] now = q.poll();
			int cr = now[0]; int cc = now[1];
			
			for(int d=0; d<4; d++) {
				int nr = cr+dr[d];
				int nc = cc+dc[d];
				
				//범위 벗어나면 x 돌이면 x
				if(!check(nr, nc) || map[nr][nc] == stone) continue;
				
				//빨간색이면 
				if(map[nr][nc] == red && !r_visit[nr][nc]) {
					q.add(new int[] {nr,nc});
					r_visit[nr][nc] = true;
					cnt++; r_cnt++;
				} else {
					//방문 했으면 x
					if(visit[nr][nc]) continue;
					//자기 자신이 아니면 x
					if(map[nr][nc] != idx) continue;
					
					//자기 자신이거나 빨간색 폭탄인 경우
					q.add(new int[] {nr,nc});
					visit[nr][nc] = true;
					cnt++;

					//기준점
					if(nr>stand_r || (nr==stand_r && nc<stand_c)) {
						//행이 가장 크거나 행이 같은 때는 열이 가장 작은 것이 기준점
						stand_r = nr; stand_c = nc;
					}
				}
			}
		}
		
		return new Color(idx, stand_r, stand_c, cnt, r_cnt);
	}

	private static boolean check(int nr, int nc) {
		if(nr>=0 && nr<n && nc>=0 && nc<n) {
			return true;
		}
		return false;
	}

	static class Color implements Comparable<Color> {
		int idx;
		int r;
		int c;
		int cnt;
		int r_cnt;
		
		public Color(int idx, int r, int c, int cnt, int r_cnt) {
			super();
			this.idx = idx;
			this.r = r;
			this.c = c;
			this.cnt = cnt;
			this.r_cnt = r_cnt;
		}

		@Override
		public int compareTo(Color o) {
			//크기가 똑같으면 빨간색이 제일 작은거
			if(o.cnt == this.cnt) {
				//빨간색의 개수도 똑같으면 행이 큰 거
				if(o.r_cnt == this.r_cnt) {
					//행이 똑같으면 열이 제일 작은거
					if(o.r == this.r) {
						return this.c - o.c; //열이 젤 작은 애
					}
					else return o.r - this.r; //행이 젤 큰 애
				} 
				else return this.r_cnt - o.r_cnt; //빨간색이 젤 적은애
			}
			else return o.cnt - this.cnt; //크기가 제일 큰 애
		}
	}

}