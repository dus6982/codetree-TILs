import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

public class Main {
	static int[] dr = {-1, 0, 1, 0}; //상우하좌
	static int[] dc = {0, 1, 0, -1};
	
	static int L, N, Q;
	static int[][] map; //벽, 함정
	static int[][] kmap; //기사 표시
	static Knight[] knights; //기사 정보
	static boolean[] move; //움직이는 기사 번호
	static int[] damage; // 총 받은 데미지 합
	
	static final int trap = 1, wall = 2;

	public static void main(String[] args) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		StringTokenizer st = new StringTokenizer(br.readLine());
		
		L = Integer.parseInt(st.nextToken()); //크기
		N = Integer.parseInt(st.nextToken()); //기사 수
		Q = Integer.parseInt(st.nextToken()); //명령 수
		
		map = new int[L][L];
		kmap = new int[L][L];
		
		for(int r=0; r<L; r++) {
			st = new StringTokenizer(br.readLine());
			for(int c=0; c<L; c++) {
				map[r][c] = Integer.parseInt(st.nextToken());
			}
		}

		knights = new Knight[N+1];
		damage = new int[N+1];
		
		for(int i=1; i<=N; i++) {
			st = new StringTokenizer(br.readLine());
			int r = Integer.parseInt(st.nextToken())-1;
			int c = Integer.parseInt(st.nextToken())-1;
			int h = Integer.parseInt(st.nextToken());
			int w = Integer.parseInt(st.nextToken());
			int k = Integer.parseInt(st.nextToken());
			
			knights[i] = new Knight(r, c, h, w, k, false, 0);
			
			//kmap에 표시해주기
			mark(i,r,c,h,w);
		}

		for(int q=1; q<=Q; q++) {
			st = new StringTokenizer(br.readLine());
			int idx = Integer.parseInt(st.nextToken());
			int direct = Integer.parseInt(st.nextToken());
			
			move = new boolean[N+1];
			
			//명령 받은 기사가 죽었으면 그냥 넘어가
			if(knights[idx].die) continue;
			
			//1. 모든 기사가 이동 가능한지 확인
			if(possible(idx, direct)) {
				//2. 모든 기사가 이동 가능하면 move 배열 속 번호들 이동 시켜주기
				moveAll(direct);
				
				//3. 이동한 기사 함정 w*h로 확인하고 데미지 입히기
				//명령을 받은 기사는 데미지 안 받음
				move[idx] = false;
				trap();
			}
		}

		int result = 0;
		for(int i=1; i<=N; i++) {
			if(!knights[i].die) result += damage[i];
		}
		System.out.println(result);
	}
	
	private static void trap() {
		for(int idx=1; idx<=N; idx++) {
			if(move[idx]) {
				Knight knight = knights[idx];
				
				int dmg = 0;
				for(int i=knight.r; i<knight.r+knight.h; i++) {
					for(int j=knight.c; j<knight.c+knight.w; j++) {
						//자기가 위치한 자리가 함정 자리면 데미지 입음
						if(map[i][j]==trap) {
							dmg++;
						}
					}
				}
				
				//데미지 반영해주기
				knight.k -= dmg;
				damage[idx] += dmg;
				
				//체력이 0 이하가 되면 죽음
				if(knight.k <= 0) {
					knight.die = true; //죽음 여부 표시
					remove(idx); //kmap에서 삭제 시켜주기
				}
			}
		}
	}

	private static void remove(int idx) {
		Knight knight = knights[idx];
		
		for(int i=knight.r; i<knight.r+knight.h; i++) {
			for(int j=knight.c; j<knight.c+knight.w; j++) {
				kmap[i][j] = 0;
			}
		}
	}

	private static void moveAll(int direct) {
		for(int idx=1; idx<=N; idx++) {
			if(move[idx]) {
				Knight knight = knights[idx];
				knight.r += dr[direct];
				knight.c += dc[direct];
			}
		}
		
		kmap = new int[L][L];
		for(int idx=1; idx<=N; idx++) {
			mark(idx);
		}
	}

	private static boolean possible(int idx, int direct) {
		Knight knight = knights[idx];
		int cnt = 0;
		
		for(int i=knight.r; i<knight.r+knight.h; i++) {
			for(int j=knight.c; j<knight.c+knight.w; j++) {
				int nr = i+dr[direct];
				int nc = j+dc[direct];
				
				if(!check(nr,nc) || map[nr][nc]==wall) continue; //범위 or 벽
				
				//자기 자신이거나 0이거나
				if(kmap[nr][nc] == idx || kmap[nr][nc] == 0) cnt++;
				//다른 기사가 있다면 해당 기사도 이동 가능한지 확인해주기
				else if(kmap[nr][nc] > 0){
					if(series(kmap[nr][nc], direct)) cnt++;
				}
			}
		}
		
		//모든 칸이 이동 가능하면 
		if(cnt==knight.cnt) {
			move[idx] = true; //이동하는 기사
			return true;
		} else return false;
	}

	private static boolean series(int idx, int direct) {
		//이미 움직일 수 있다고 표시 되었으면 다시 봐줄 필요 x
		if(move[idx]) return true;
		
		Knight knight = knights[idx];
		int cnt = 0;
		
		for(int i=knight.r; i<knight.r+knight.h; i++) {
			for(int j=knight.c; j<knight.c+knight.w; j++) {
				int nr = i+dr[direct];
				int nc = j+dc[direct];
				
				if(!check(nr,nc) || map[nr][nc]==wall) continue; //범위 
				
				//자기 자신이거나 0이거나
				if(kmap[nr][nc] == idx || kmap[nr][nc] == 0) cnt++;
				//다른 기사가 있다면 연쇄 확인해주기
				else if(kmap[nr][nc] > 0){
					if(series(kmap[nr][nc], direct)) cnt++;
				}
				
			}
		}
		
		//모든 칸이 이동 가능하면 
		if(cnt==knight.cnt) {
			move[idx] = true; //이동하는 기사
			return true;
		} else return false;
	}

	private static void mark(int idx, int r, int c, int h, int w) {
		int cnt = 0;
		
		for(int i=r; i<r+h; i++) {
			for(int j=c; j<c+w; j++) {
				kmap[i][j] = idx;
				cnt++;
			}
		}
		
		knights[idx].cnt = cnt;
	}
	
	private static void mark(int idx) {
		Knight knight = knights[idx];
		
		for(int i=knight.r; i<knight.r+knight.h; i++) {
			for(int j=knight.c; j<knight.c+knight.w; j++) {
				kmap[i][j] = idx;
			}
		}
	}
	
	private static boolean check(int nr, int nc) {
		if(nr>=0 && nr<L && nc>=0 && nc<L) {
			return true;
		}
		return false;
	}

	static class Knight {
		int r; int c; //좌측 상단 좌표
		int h; int w; //세로 가로
		int k; boolean die; //체력, 죽음 여부
		int cnt; //칸 수


		public Knight(int r, int c, int h, int w, int k, boolean die, int cnt) {
			super();
			this.r = r;
			this.c = c;
			this.h = h;
			this.w = w;
			this.k = k;
			this.die = die;
			this.cnt = cnt;
		}
	}

}