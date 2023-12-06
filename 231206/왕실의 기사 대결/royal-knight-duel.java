import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.StringTokenizer;

public class Main {
	static int L, N, Q;
	static int[][] map; //함정과 벽 표시 맵
	static int[][] kmap; //기사 표시 맵
	static Knight[] knights; //각 기사들의 위치, 체력 
	static int[] dmg; //각 기사가 받은 데미지
	static int[] dr = {-1, 0, 1, 0}; //상우하좌
	static int[] dc = {0, 1, 0,-1};
	static int lastIdx;
	static boolean[] move; // 한 턴에 움직여줘야 되는 기사 번호

	public static void main(String[] args) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		StringTokenizer st = new StringTokenizer(br.readLine());
		
		L = Integer.parseInt(st.nextToken()); //L*L
		N = Integer.parseInt(st.nextToken()); //기사 수
		Q = Integer.parseInt(st.nextToken()); //왕의 명령
		
		//맵 정보 
		map = new int[L][L];
		for(int r=0; r<L; r++) {
			st = new StringTokenizer(br.readLine());
			for(int c=0; c<L; c++) {
				map[r][c] = Integer.parseInt(st.nextToken());
			}
		}

		//기사 맵 정보
		kmap = new int[L][L];
		knights = new Knight[N+1];
		dmg = new int[N+1];
		for(int n=1; n<=N; n++) {
			st = new StringTokenizer(br.readLine());
			int r = Integer.parseInt(st.nextToken())-1;
			int c = Integer.parseInt(st.nextToken())-1;
			int h = Integer.parseInt(st.nextToken());
			int w = Integer.parseInt(st.nextToken());
			int k = Integer.parseInt(st.nextToken()); //초기 체력
			
			knights[n] = new Knight(r, c, h, w, k);
			
			for(int i=r; i<r+h; i++) {
				for(int j=c; j<c+w; j++) {
					kmap[i][j] = n;
				}
			}
		}

		//Q개의 명령 진행
		while(Q-- > 0) {
			st = new StringTokenizer(br.readLine());
			int idx = Integer.parseInt(st.nextToken());
			int d = Integer.parseInt(st.nextToken());
			
			//죽은 친구는 그냥 넘어가
			if(knights[idx].hp<=0) continue;
			
			//1. 왕의 명령 받은 기사 이동
			move = new boolean[N+1];
			moveKnight(idx, d);
		}
		
		
		//총 받은 데미지 합 출력
		int result = 0;
		for(int i=1; i<=N; i++) {
			if(knights[i].hp>0) result += dmg[i];
		}
		System.out.println(result);
	}

	private static void moveKnight(int idx, int d) {
		//움직이는 기사의 정보를 일단 가져오자
		move[idx] = true;
		Knight knight = knights[idx];
		int sr = knight.r; int sc = knight.c; 
		int er = sr+knight.h; int ec = sc+knight.w;
		
		boolean flag = false;
		
		//d로 한 칸 이동
		int cnt = 0;
		if(d==0 || d==2) cnt = knight.w;
		else cnt = knight.h;
		
		for(int r=sr; r<er; r++) {
			for(int c=sc; c<ec; c++) {
				int nr = r+dr[d];
				int nc = c+dc[d];
				
				if(!check(nr, nc)) {
					return;
				}
				
				//다음 칸이 자기 자신이면 넘어가기
				if(kmap[nr][nc]==idx) continue;
				
				//벽인지 확인하기 - 벽이면 이동 불가
				if(map[nr][nc]==2) {
					flag = true;
					break;
				}
				
				//연쇄 없이 이동 가능
				if(kmap[nr][nc]==0) {
					cnt--;
					continue;
				} else if(kmap[nr][nc]!=0) { //바로 이동하지 못하고 누가 존재해서 밀어조야함
					//전부 이동할 수 있어야 움직이는 것이다
						if(movePossible(kmap[nr][nc], d)) {
							cnt--;
							continue;
						} else { //연쇄 받는 기사가 한 명이라도 움직이지 못한다면 이동 못함
							flag = true;
							break;
						}
					
				}
			}
		}
		
		if(flag || cnt!=0) {
			move = new boolean[N+1];
		}
		
		if(cnt==0) { //모든 기사 이동 가능해짐
			moveAll(d, idx);
		}
	}
	
	private static boolean movePossible(int idx, int d) {
		//움직이는 기사의 정보를 일단 가져오자
		move[idx] = true;
		Knight knight = knights[idx];
		int sr = knight.r; int sc = knight.c; 
		int er = sr+knight.h; int ec = sc+knight.w;
		
		int cnt = 0;
		if(d==0 || d==2) cnt = knight.w;
		else cnt = knight.h;
		
		for(int r=sr; r<er; r++) {
			for(int c=sc; c<ec; c++) {
				int nr = r+dr[d];
				int nc = c+dc[d];
				
				if(!check(nr, nc)) {
					return false;
				}
				
				//다음 칸이 자기 자신이면 넘어가기
				if(kmap[nr][nc]==idx) continue;
				
				//연쇄 없이 이동 가능(누가 없고 벽도 아니면)
				if(kmap[nr][nc]==0 && map[nr][nc]!=2) {
					cnt--;
					continue;
				}
				
				//벽이면 이동 불가
				if(map[nr][nc]==2) {
					return false;
				}
				
				if(kmap[nr][nc]!=idx) {
					if(movePossible(kmap[nr][nc], d)) {
						cnt--;
					}
				}
			}
		}
		
		//모든 칸이 이동가능하다 ~~
		if(cnt==0) {
			return true;
		} else {
			return false;
		}
	}

	private static void moveAll(int d, int idx) {
		kmap = new int[L][L];
		
		for(int i=1; i<move.length; i++) {
			Knight kn = knights[i];
			
			if(!move[i]) {
				for(int r=kn.r; r<kn.r+kn.h; r++) {
					for(int c=kn.c; c<kn.c+kn.w; c++) {
						kmap[r][c] = i;
					}
				}
			} else {
				for(int r=kn.r; r<kn.r+kn.h; r++) {
					for(int c=kn.c; c<kn.c+kn.w; c++) {
						kmap[r+dr[d]][c+dc[d]] = i;
						
						if(i==idx) continue;
						if(map[r+dr[d]][c+dc[d]]==1) dmg[i]++;
					}
				}
				
				//이동된 좌표 반영해주고 데미지
				updateInfo(kn, d, i);
			}
		}
	}

	private static void updateInfo(Knight kn, int d, int idx) {
		kn.r = kn.r+dr[d];
		kn.c = kn.c+dc[d];
		kn.hp -= dmg[idx];
		
		//죽었으면 맵에서 없애주기
		if(kn.hp<=0) {
			for(int r=kn.r; r<kn.r+kn.h; r++) {
				for(int c=kn.c; c<kn.c+kn.w; c++) {
					kmap[r][c] = 0;
				}
			}
		}
	}

	private static boolean check(int nr, int nc) {
		if(nr>=0 && nr<L && nc>=0 && nc<L) {
			return true;
		}
		return false;
	}

	private static void print(int[][] map) {
		for(int r=0; r<map.length; r++) {
			for(int c=0; c<map[r].length; c++) {
				System.out.print(map[r][c]+" ");
			}
			System.out.println();
		}
	}
	
	static class Knight {
		int r; 
		int c;
		int h;
		int w;
		int hp;
		
		public Knight(int r, int c, int h, int w, int hp) {
			super();
			this.r = r;
			this.c = c;
			this.h = h;
			this.w = w;
			this.hp = hp;
		}

		@Override
		public String toString() {
			return "Knight [r=" + r + ", c=" + c + ", h=" + h + ", w=" + w + ", hp=" + hp + "]";
		}
	}
}