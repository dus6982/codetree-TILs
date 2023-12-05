import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.StringTokenizer;

public class Main {
	//루돌프의 반란
	static int N, M, P, C, D, live;
	static int rdp_r, rdp_c, rdp_d;
	static int[] score;
	static int[][] map;
	static ArrayList<Santa> santas;
	//		루돌프 8방향 : 좌상, 상, 우상, 우, 우하, 하, 좌하, 좌
	static int[] drR = {-1, -1, -1, 0, 1, 1, 1, 0};
	static int[] dcR = {-1, 0, 1, 1, 1, 0, -1, -1};
	//		산타  4방향 : 상 우 하 좌
	static int[] drS = {-1, 0, 1, 0};
	static int[] dcS = {0, 1, 0, -1};
	
	public static void main(String[] args) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		StringTokenizer st = new StringTokenizer(br.readLine());
		
		N = Integer.parseInt(st.nextToken()); //N*N
		M = Integer.parseInt(st.nextToken()); //게임 턴 수
		P = Integer.parseInt(st.nextToken()); //산타 수
		C = Integer.parseInt(st.nextToken()); //루돌프 힘
		D = Integer.parseInt(st.nextToken()); //산타 힘
		
		map = new int[N][N];
		live = P;

		//루돌프 초기 위치
		st = new StringTokenizer(br.readLine());
		rdp_r = Integer.parseInt(st.nextToken())-1;
		rdp_c = Integer.parseInt(st.nextToken())-1;
		map[rdp_r][rdp_c] = -1;
		
		santas = new ArrayList<>();
		score = new int[P+1];
		for(int i=0; i<P; i++) {
			st = new StringTokenizer(br.readLine());
			int idx = Integer.parseInt(st.nextToken());
			int r = Integer.parseInt(st.nextToken())-1;
			int c = Integer.parseInt(st.nextToken())-1;
			
			map[r][c] = idx;
			santas.add(new Santa(idx, r, c, -1, false, M, false));
		}
		
		while(M-- > 0) {
			//산타가 없으면 종료
			if(live==0) break;
			
			//1. 루돌프 이동
			//루돌프와 가장 가까운 산타 찾기
			Santa near = findNearSanta();
			//거리가 가까운 산타의 좌표와 루돌프가 갈 수 있는 8칸 중 더 가까워지는 자리로 루돌프 이동
			moveRdp(near);
			
			//2. 산타 이동
			moveSanta();
			
			//3. 죽지 않은 산타에게 점수 부여
			for(Santa s : santas) {
				if(!s.out) {
					score[s.idx]++;
				}
			}
		}

		for(int i=1; i<score.length; i++) {
			System.out.print(score[i]+" ");
		}
	}
	
	private static Santa findNearSanta() {
		int min_dist = N*N+M*M; //최대 거리
		Santa near = null;
		
		for(Santa santa : santas) {
			//죽은 산타는 x
			if(santa.out) continue;
			
			int dist = (int) (Math.pow(rdp_r-santa.r,2) + Math.pow(rdp_c-santa.c, 2));
			if(min_dist > dist) {
				min_dist = dist;
				near = santa;
			} else if(min_dist == dist) {
				//r좌표가 커
				//r좌표가 같을 때는 c좌표가 커
				if(santa.r > near.r || (santa.r==near.r && santa.c > near.c)) {
					near = santa;
				}
			}
		}

		return near;
	}

	private static void moveRdp(Santa near) {
		int move_r = 0, move_c = 0;
		int min_dist = (int) (Math.pow(rdp_r-near.r,2) + Math.pow(rdp_c-near.c, 2));
		
		for(int d=0; d<8; d++) {
			int nr = rdp_r+drR[d];
			int nc = rdp_c+dcR[d];
			
			//범위 밖
			if(!check(nr, nc)) continue;
			
			int dist = (int) (Math.pow(nr-near.r,2) + Math.pow(nc-near.c, 2));
			if(min_dist > dist) {
				rdp_d = d;
				min_dist = dist;
				move_r = nr; move_c = nc;
			}
		}
		
		//이동하기 전에 이동할 칸에 누구 있는지 확인
		//누가 있다면 충돌 연쇄 상호 발생
		//그 후에 루돌프 이동 시켜주기
		if(map[move_r][move_c]!=0) {
			crashRdp(move_r, move_c);
		} 
		
		map[rdp_r][rdp_c] = 0;
		map[move_r][move_c] = -1;
		rdp_r = move_r; rdp_c = move_c;
	}

	private static void moveSanta() {
		//번호 순서대로 산타 이동 해야 됨
		//번호로 리스트 정렬하기
		Collections.sort(santas);
		
		for(Santa santa : santas) {
			//죽은 산타는 x
			if(santa.out) continue;
			
			if(santa.faint) {
				if(santa.turn==M) {
					santa.faint = false;
					santa.turn = M;
				} else {
					continue;
				}
			}
			
			//움직일 수 있는 4칸 중 루돌프와 가까워지는 칸으로 1칸 이동			
			int move_r = -1, move_c = -1;
			int min_dist = (int) (Math.pow(rdp_r-santa.r,2) + Math.pow(rdp_c-santa.c, 2));
			
			for(int d=0; d<4; d++) {
				int nr = santa.r+drS[d];
				int nc = santa.c+dcS[d];
				
				//범위 벗어나면 이동불가
				if(!check(nr, nc)) continue;
				
				//루돌프면 충돌 ~~
				if(map[nr][nc]==-1) {
					crashSanta(santa, nr, nc, d);
				}
				
				//누가 있으면 이동불가
				if(map[nr][nc]!=0) continue;
				
				int dist = (int) (Math.pow(nr-rdp_r,2) + Math.pow(nc-rdp_c, 2));
				if(min_dist > dist) {
					santa.direct = d;
					min_dist = dist;
					move_r = nr; move_c = nc;
				}
			}
			
			if(move_r != -1 && move_c != -1) {
				map[santa.r][santa.c] = 0;
				map[move_r][move_c] = santa.idx;
				updateInfoSanta(santa, move_r, move_c, santa.direct, false);
			}
		}
	}
	
	private static void crashRdp(int move_r, int move_c) {
		//루돌프가 이동해서 산타와 충돌
		//해당 칸에 있는 산타는 C점 획득
		int idx = map[move_r][move_c];
		score[idx] += C;
		
		//루돌프의 이동방향대로 C칸 밀려날 거얌
		//움직이게 될 산타의 정보를 가져오자
		Santa santa = findInfoSanta(idx);
		
		int nr = santa.r+(drR[rdp_d]*C);
		int nc = santa.c+(dcR[rdp_d]*C);
		
		if(check(nr, nc)) { //범위 안 벗어나면 이동할 수 있는 것이란돠
			if(map[nr][nc]==0) { 
				//이동할 수 있는 칸에 아무도 없다면 연쇄 없이 이동 가능
				map[nr][nc] = santa.idx;
				map[santa.r][santa.c] = 0;
				//산타 정보 수정
				updateInfoSanta(santa, nr, nc, rdp_d, true);
			} else {
				//루돌프 이동방향대로 1칸씩 연쇄 이동
				int cnt = 1;
				
				int nnr = nr+drR[rdp_d];
				int nnc = nc+dcR[rdp_d];
				
				if(check(nnr, nnc)) {
					while(map[nnr][nnc]!=0) {
						cnt++;
						nnr += drR[rdp_d];
						nnc += dcR[rdp_d];
						
						if(!check(nnr, nnc)) {
							nnr -= drR[rdp_d];
							nnc -= dcR[rdp_d];
							
							int die_idx = map[nnr][nnc];
							dieSanta(die_idx);
							
							break;
						}
					}
					
					for(int i=1; i<=cnt; i++) {
						//앞 칸의 값을 넣어주기
						int move_idx = map[nnr+(drR[rdp_d]*(-1))][nnc+(dcR[rdp_d]*(-1))];
						map[nnr][nnc] = move_idx;
						Santa s = findInfoSanta(move_idx);
						updateInfoSanta(s, nnr, nnc, rdp_d, true);
						
						nnr = nnr+(drR[rdp_d]*(-1));
						nnc = nnc+(dcR[rdp_d]*(-1));
					}
				} else { //범위 밖으로 튕겨나가 죽엉
					int die_idx = map[nr][nc];
					dieSanta(die_idx);
				}
				
				
				//연쇄 끝났으니 첫번째로 치인 산타를 이동시켜주자
				map[nr][nc] = santa.idx;
				updateInfoSanta(santa, nr, nc, rdp_d, true);
			}
		} else { //범위 벗어났으면 산타 die
			dieSanta(santa.idx);
		}
	}

	private static void crashSanta(Santa santa, int r_r, int r_c, int d) {
		//산타가 이동해서 루돌프와 충돌
		//이동하는 산타는 D점 획득
		score[santa.idx] += D;
		
		//자신의 이동방향 반대로(d*-1) D칸 밀려날 거얌
		int nr = r_r+(drS[d]*(-1)*D);
		int nc = r_c+(dcS[d]*(-1)*D);
		
		//원래 내 자리
		if(nr==santa.r && nc==santa.c) return;
		
		if(check(nr, nc)) { //범위 안 벗어나면 이동할 수 있는 것이란돠
			 //범위 안 벗어나면 이동할 수 있는 것이란돠
			if(map[nr][nc]==0) { 
				//이동할 수 있는 칸에 아무도 없다면 연쇄 없이 이동 가능
				map[nr][nc] = santa.idx;
				map[santa.r][santa.c] = 0;
				//산타 정보 수정
				updateInfoSanta(santa, nr, nc, d, true);
			} else {
				//해당 이동방향대로 1칸씩 연쇄 이동
				int cnt = 1;
				
				int nnr = nr+(drS[d]*(-1));
				int nnc = nc+(dcS[d]*(-1));
				
				if(check(nnr, nnc)) {
					while(map[nnr][nnc]!=0) {
						cnt++;
						nnr += drS[d];
						nnc += dcS[d];
						
						if(!check(nnr, nnc)) {
							nnr -= drS[d];
							nnc -= dcS[d];
							
							int die_idx = map[nnr][nnc];
							Santa s =findInfoSanta(die_idx);
							santas.remove(s);
							
							break;
						}
					}
					
					for(int i=1; i<=cnt; i++) {
						//앞 칸의 값을 넣어주기
						int move_idx = map[nnr+drS[d]][nnc+dcS[d]];
						map[nnr][nnc] = move_idx;
						Santa s = findInfoSanta(move_idx);
						updateInfoSanta(s, nnr, nnc, d, false);
						
						nnr = nnr+drS[d];
						nnc = nnc+dcS[d];
					}
				} else { //범위 밖으로 튕겨나가 죽엉
					int die_idx = map[nr][nc];
					dieSanta(die_idx);
				}
				
				
				//연쇄 끝났으니 첫번째로 치인 산타를 이동시켜주자
				map[nr][nc] = santa.idx;
				updateInfoSanta(santa, nr, nc, rdp_d, true);
			}
		
		} else { //범위 밖으로 튕겨나가 죽엉
			dieSanta(santa.idx);
		}
		
	}
	
	private static void dieSanta(int die_idx) {
		int idx = 0;
		for(int i=0; i<santas.size(); i++) {
			if(santas.get(i).idx == die_idx) {
				idx = i;
				break;
			}
		}
		
		Santa s = new Santa(die_idx, 0, 0, 0, true, P, true);
		live--;
		
		santas.set(idx, s);
	}

	private static void updateInfoSanta(Santa santa, int nr, int nc, int d, boolean crash) {
		int idx = 0;
		for(int i=0; i<santas.size(); i++) {
			if(santas.get(i) == santa) {
				idx = i;
				break;
			}
		}
		
		Santa s = null;
		if(crash) {
			s = new Santa(santa.idx, nr, nc, d, true, M-2, false);
		} else {
			s = new Santa(santa.idx, nr, nc, d, false, M, false);
		}
		
		santas.set(idx, s);
	}

	private static boolean check(int nr, int nc) {
		if(nr>=0 && nr<N && nc>=0 && nc<N) return true;
		
		return false;
	}

	private static Santa findInfoSanta(int idx) {
		for(Santa santa : santas) {
			if(santa.idx == idx) {
				return santa;
			}
		}
		
		return null;
	}

	static class Santa implements Comparable<Santa>{
		int idx;
		int r;
		int c;
		int direct; //방향
		boolean faint; //t:기절, f:x-이동가능
		int turn; //T일 시, 움직일 수 있는 턴 숫자
		boolean out;
		
		public Santa(int idx, int r, int c, int direct, boolean faint, int turn, boolean out) {
			super();
			this.idx = idx;
			this.r = r;
			this.c = c;
			this.direct = direct;
			this.faint = faint;
			this.turn = turn;
			this.out = out;
		}

		@Override
		public int compareTo(Santa o) {
			return this.idx - o.idx;
		}
	}
}