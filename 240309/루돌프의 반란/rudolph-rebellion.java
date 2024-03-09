import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.StringTokenizer;

public class Main {
	static int[] score; //점수
	static int[][] map; //게임판 ; 루돌프:-1 ; 빈칸:0 ; 산타:번호
	static boolean[] die; //탈락 여부
	
	//	루돌프 8방향 : 좌상, 상, 우상, 우, 우하, 하, 좌하, 좌
	static int[] drR = {-1, -1, -1, 0, 1, 1, 1, 0};
	static int[] dcR = {-1, 0, 1, 1, 1, 0, -1, -1};
	//		산타  4방향 : 상 우 하 좌 => 우선순위
	static int[] drS = {-1, 0, 1, 0};
	static int[] dcS = {0, 1, 0, -1};
	
	static ArrayList<Santa> list;
	
	static int N, M, P, C, D;
	static int live; //살아있는 산타 수
	static int rdpR, rdpC, rdpD; //루돌프 좌표, 방향

	public static void main(String[] args) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		StringTokenizer st = new StringTokenizer(br.readLine());
		
		N = Integer.parseInt(st.nextToken()); //N*N
		M = Integer.parseInt(st.nextToken()); //게임턴 수
		P = Integer.parseInt(st.nextToken()); //산타 수
		C = Integer.parseInt(st.nextToken()); //루돌프 힘
		D = Integer.parseInt(st.nextToken()); //산타 힘
		
		map = new int[N][N];
		live = P; //살아있는 수
		score = new int[P+1]; //점수판
		die = new boolean[P+1];
		
		
		//루돌프 초기 위치
		st = new StringTokenizer(br.readLine());
		rdpR = Integer.parseInt(st.nextToken())-1; 
		rdpC = Integer.parseInt(st.nextToken())-1;
		map[rdpR][rdpC] = -1; 
		
		//산타 정보
		list = new ArrayList<>();
		for(int i=0; i<P; i++) {                                
			st = new StringTokenizer(br.readLine());
			int idx = Integer.parseInt(st.nextToken());
			int r = Integer.parseInt(st.nextToken())-1;
			int c = Integer.parseInt(st.nextToken())-1;
			
			map[r][c] = idx;
			list.add(new Santa(idx, r, c, 0, M, false));
		}
		
		//산타 번호순으로 정렬
		Collections.sort(list);
		
		//M턴 진행
		while(M-- > 0) {
			//산타가 없으면 종료
			if(live==0) {
				break;
			}
			
			//1. 루돌프 이동
			//가까운 산타 찾기 (번호, 좌표 등 정보 필요)
			Santa near = findNearSanta();
			//루돌프가 8방향칸 중 가까운 산타에게 제일 가까워지는 칸으로 이동해야 됨
			rdpMove(near);
			
			//2. 산타 이동
			santaMove();
			
			//3. 탈락하지 않은 산타에게 1점씩 추가
			for(int i=1; i<=P; i++) {
				if(!die[i]) 
					score[i]++;
			}
		}
		
		
		//최종 점수 출력
		for(int i=1; i<=P; i++) {
			System.out.print(score[i]+" ");
		}
	}

	private static void santaMove() {
		for(int i=0; i<list.size(); i++) {
			Santa santa = list.get(i);
			
			//탈락한 산타 움직일 수 없음
			if(die[santa.idx]) continue;
			
			//기절한 산타도 움직일 수 없음
			//근데 기절 턴 수 끝났으면 가능함
			if(santa.faint) {
				if(santa.turn == M) {
					santa.faint = false;
				} else continue;
			}
	
			//4방향칸 중 이동할 수 있는 칸을 찾아보자
			int moveR = -1; int moveC = -1;
			int dist = (int) (Math.pow((rdpR-santa.r), 2)
					+ Math.pow((rdpC-santa.c), 2));
			
			for(int d=0; d<4; d++) {
				int nr = santa.r+drS[d];
				int nc = santa.c+dcS[d];
				
				//범위 벗어나는 곳으로 이동 불가
				if(!check(nr, nc)) continue;
				
				//근데 그 자리에 루돌프가 있네요?
				if(map[nr][nc] == -1) {
					collisionSanta(santa, d);
					break;
				}
				
				//다른 산타 있으면 이동 불가
				if(map[nr][nc] != 0) continue;
				
				int dt = (int) (Math.pow((rdpR-nr), 2)
						+ Math.pow((rdpC-nc), 2));
				
				if(dist > dt) {
					moveR = nr; moveC = nc;
					dist = dt;
					santa.d = d;
				}
			}
			
			//이동할 수 있어요
			if(moveR != -1 && moveC != -1) {
				map[moveR][moveC] = santa.idx;
				map[santa.r][santa.c] = 0;
				santa.r = moveR; santa.c = moveC;
			}
		}
	}

	private static void collisionSanta(Santa santa, int direct) {
		//루돌프가 움직여서 충돌이 일어난 경우
		//산타 D 점수 획득
		score[santa.idx] += D;
		
		//자신이 온 반대방향대로 D칸 밀려남
		//산타는 자신이 이동한 반대 방향으로 D칸 만큼 이동
		int nr = rdpR-drS[direct]*D;
		int nc = rdpC-dcS[direct]*D;
		
		//밀려나는 자리가 원래 있던 자리라면 이동할 필요 없음
		if(nr==santa.r && nc==santa.c) return;
		
		//범위 벗어나면 산타 die
		if(!check(nr, nc)) {
			live--;
			die[santa.idx] = true;
			map[santa.r][santa.c] = 0;
		} else {
			//루돌프와 충돌한 산타 기절
			santa.faint = true;
			santa.turn = M-2;
			
			//충돌 후 착지하게 되는 칸이 빈 칸이면 바로 이동 가능
			if(map[nr][nc] == 0) {
				map[nr][nc] = santa.idx;
				map[santa.r][santa.c] = 0;
				santa.r = nr; santa.c = nc; santa.d = direct;
			} else { //충돌 후 착지하게 되는 칸에 다른 산타가 있다면
				//해당 방향으로 연쇄적으로 1칸씩 밀려남
				chainReaction(nr, nc, direct, false);
				
				//이제 충돌당한 산타를 착지 시켜주자
				map[nr][nc] = santa.idx;
				map[santa.r][santa.c] = 0;
				santa.r = nr; santa.c = nc; santa.d = direct;
			}
		}
	}

	private static void rdpMove(Santa near) {
		//루돌프의 8방향칸 중 제일 가까워지는 칸 찾기
		int moveR = 0; int moveC = 0;
		int dist = (int) (Math.pow((rdpR-near.r), 2)
				+ Math.pow((rdpC-near.c), 2));
		
		//1칸만 이동하는 거라 8칸만 보기
		for(int d=0; d<8; d++) {
			int nr = rdpR+drR[d];
			int nc = rdpC+dcR[d];
			
			if(!check(nr, nc)) continue;
			
			int dt = (int) (Math.pow((nr-near.r), 2)
					+ Math.pow((nc-near.c), 2));
			
			if(dist > dt) {
				dist = dt;
				moveR = nr; moveC = nc; 
				rdpD = d;
			}
		}
		
		//루돌프가 이동할 칸에 산타가 있다면 충돌
		if(map[moveR][moveC] != 0) {
			collisionRdp(moveR, moveC);
		} 
		
		map[rdpR][rdpC] = 0;
		map[moveR][moveC] = -1;
		rdpR = moveR; rdpC = moveC;
	}

	private static boolean check(int nr, int nc) {
		if(nr>=0 && nr<N && nc>=0 && nc<N) return true;
		
		return false;
	}

	private static void collisionRdp(int moveR, int moveC) {
		//루돌프가 움직여서 충돌이 일어난 경우
		//산타 C 점수 획득
		int idx = map[moveR][moveC];
		score[idx] += C;
		
		//루돌프의 방향대로 C칸 밀려남
		//밀려나면 정보를 수정해야하니 움직이게 될 산타의 정보를 가져오자
		Santa santa = findInfoSanta(idx);

		//루돌프와 충돌한 산타 기절
		santa.faint = true;
		santa.turn = M-2;

		//산타는 루돌프가 이동해온 방향으로 C칸 만큼 이동
		int nr = santa.r+drR[rdpD]*C;
		int nc = santa.c+dcR[rdpD]*C;
		
		//범위 벗어나면 산타 die
		if(!check(nr, nc)) {
			live--;
			die[idx] = true;
			map[santa.r][santa.c] = 0;
		} else {
			if(map[nr][nc] != 0) { 
				//충돌 후 착지하게 되는 칸에 다른 산타가 있다면
				//해당 방향으로 연쇄적으로 1칸씩 밀려남
				chainReaction(nr, nc, rdpD, true);
				
//				//이제 충돌당한 산타를 착지 시켜주자
//				map[nr][nc] = idx;
////				map[santa.r][santa.c] = 0;
////				santa.r = nr; santa.c = nc; santa.d = rdpD;
			}
			
			//충돌 후 착지하게 되는 칸이 빈 칸이면 바로 이동 가능
			//원래 위치 없애주고 정보 수정하기
			//이제 충돌당한 산타를 착지 시켜주자
			map[nr][nc] = idx;
			map[santa.r][santa.c] = 0;
			santa.r = nr; santa.c = nc; santa.d = rdpD;
		}
	}

	private static void chainReaction(int r, int c, int d, boolean rdp) {
		int cnt = 1; //일단 1칸은 연쇄가 일어남
		
		int dr = 0; int dc = 0;
		if(rdp) { //루돌프의 방향대로
			dr = drR[d]; dc = dcR[d];
		} else { //산타의 반대방향으로
			dr = -drS[d]; dc = -dcS[d];
		}
			
		int mr = r+dr; int mc = c+dc; 
		
		if(!check(mr, mc)) {
			//연쇄 없이 범위를 벗어나서 죽어
			live--;
			die[map[r][c]] = true;
		} else {
			//범위를 벗어나지 않으니 연쇄 이동을 해보자
			while(map[mr][mc] != 0) { //빈 칸일 때까지 보자
				cnt++;
				mr += dr; mc += dc;
				
				if(!check(mr, mc)) {
					cnt--;
					mr -= dr; mc -= dc;
					live--;
					die[map[mr][mc]] = true;
					break;
				}
			}
			
			//연쇄 수만큼 적용 시켜주기
			for(int i=0; i<cnt; i++) {
				//앞 칸 값 넣어주기
				int idx = map[mr-dr][mc-dc];
				map[mr][mc] = idx;
				
				//이동한 좌표 정보 수정
				updateSantaInfo(idx, mr, mc, d);

				mr -= dr; mc -= dc;
			}
		}
	}

	private static void updateSantaInfo(int idx, int r, int c, int d) {
//		Santa santa = list.get(idx-1);
//		santa.r = r; santa.c = c; santa.d = d;
		for(int i=0; i<list.size(); i++) {
			if(list.get(i).idx == idx) {
				Santa santa = list.get(i);
				santa.r = r; santa.c = c; santa.d = d;
				break;
			}
		}
	}

	private static Santa findInfoSanta(int idx) {
		Santa santa = null;
		
		for(int i=0; i<list.size(); i++) {
			if(list.get(i).idx == idx) {
				santa = list.get(i);
				break;
			}
		}
		
		return santa;
	}

	private static Santa findNearSanta() {
		//산타list에서 현재 루돌프와 가장 가까운 산타를 찾아야 함
		int minD = N*N+N*N; 
		Santa near = null;
		int r = -1; int c = -1;
		
		for(int i=0; i<list.size(); i++) {
			if(die[list.get(i).idx]) continue;
			
			int dist = (int) (Math.pow((rdpR-list.get(i).r), 2)
					+ Math.pow((rdpC-list.get(i).c), 2));
					
			//가깝지 않다면 다음 산타로 넘어가
			if(minD < dist) continue;
			//r좌표가 더 크지 않다면 다음 산타로 넘어가
			if(minD == dist) {
				if(r>list.get(i).r || (r==list.get(i).r && c>list.get(i).c)) continue;
			}
			
			minD = dist;
			r = list.get(i).r; c = list.get(i).c;
			near = list.get(i);
		}
		
		return near;
	}

	static class Santa implements Comparable<Santa>{
		int idx; //번호
		int r; int c; //좌표
		int d; //방향
		int turn; //faint가 true일 시에 확인하기 -> 산타가 이동 가능한 턴 번호
		boolean faint; //기절여부 = true: 기절, false : 기절x 이동가능
		
		public Santa(int idx, int r, int c, int d, int turn, boolean faint) {
			super();
			this.idx = idx;
			this.r = r;
			this.c = c;
			this.d = d;
			this.turn = turn;
			this.faint = faint;
		}

		@Override
		public int compareTo(Santa o) { //번호순 정렬
			return this.idx - o.idx;
		}
	}

}