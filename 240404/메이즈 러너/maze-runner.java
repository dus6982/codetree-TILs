import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.StringTokenizer;

public class Main {
	static int N, M, K, cnt;
	static int exit_r, exit_c; //출구 좌표
	static ArrayList<Integer>[][] pmap; //한 좌표에 2명 이상 존재할 수 있음
	static Person[] plist; //참가자의 정보
	static int[][] map; //빈칸:0, 벽:1~9, 출구:-1, 사람:-2
	static final int exit = -1, person = -2;
	
	static int[] dr = {-1, 1, 0, 0}; //상하좌우
	static int[] dc = {0, 0, -1, 1};

	public static void main(String[] args) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		StringTokenizer st = new StringTokenizer(br.readLine());
		
		N = Integer.parseInt(st.nextToken()); //N*N
		M = Integer.parseInt(st.nextToken()); //참가자 수
		K = Integer.parseInt(st.nextToken()); //턴 수
		
		//미로 : 빈 칸, 벽, 참가자 표시
		map = new int[N][N];
		for(int r=0; r<N; r++) {
			st = new StringTokenizer(br.readLine());
			for(int c=0; c<N; c++) {
				map[r][c] = Integer.parseInt(st.nextToken());
			}
		}
		
		//한 칸에 2명 이상 존재 가능하기 때문에
		//참가자 중복 표시
		pmap = new ArrayList[N][N];
		for(int r=0; r<N; r++) {
			for(int c=0; c<N; c++) {
				pmap[r][c] = new ArrayList<>();
			}
		}
		
		//참가자 좌표 저장
		cnt = M;
		plist = new Person[M+1];
		for(int i=1; i<=M; i++) {
			st = new StringTokenizer(br.readLine());
			//좌상단 1,1이라서 빼주기
			int r = Integer.parseInt(st.nextToken())-1;
			int c = Integer.parseInt(st.nextToken())-1;
			
			plist[i] = new Person(r, c, 0, false);
			pmap[r][c].add(i); //해당 칸에 참가자 번호 넣어주기
			map[r][c] = person; //미로에 참가자 표시
		}
		
		//출구 좌표 저장
		st = new StringTokenizer(br.readLine());
		exit_r = Integer.parseInt(st.nextToken())-1;
		exit_c = Integer.parseInt(st.nextToken())-1;
		map[exit_r][exit_c] = exit; //미로에 출구 표시
		
		//K번 진행
		while(K-- > 0) {
			if(cnt==0) break;

			move();

			if(cnt==0) break;
			
			//2. 미로 회전 & 벽 내구도 감소
			smulate();
		}
		
		int result = 0;
		for(int i=1; i<=M; i++) {
			result += plist[i].dist;
		}
		System.out.println(result);
		System.out.println((exit_r+1)+" "+(exit_c+1));
	}

	private static void smulate() {
		//1. 범위 구하기
		for(int side=2; side<=N; side++) {
			for(int r=0; r+side<=N; r++) {
				for(int c=0; c+side<=N; c++) {
					//해당 영역에 사람, 출구가 있는지
					if(isPossible(r, c, side)) {
						//2. 회전
						rotation(r, c, side);
						return;
					}
				}
			}
		}
	}

	private static boolean isPossible(int s_r, int s_c, int side) {
		int e_r = s_r+side; int e_c = s_c+side;
		boolean p = false, e = false;
		
		for(int r=s_r; r<e_r; r++) {
			for(int c=s_c; c<e_c; c++) {
				if(map[r][c]==person) p = true;
				else if(map[r][c]==exit) e = true;
			}
		}
		
		return p&&e;
	}

	private static void rotation(int s_r, int s_c, int side) {
		//회전 담아둘 임시 배열
		int[][] tmp = new int[N][N];
		
		////이동 시켜줘야 할 사람
		Queue<int[]> q = new LinkedList<int[]>();
		
		//회전
		int e_r = s_r+side;
		int e_c = s_c+side;

		for(int r=s_r; r<e_r; r++) {
			for(int c=s_c; c<e_c; c++) {
				int r1 = r - s_r, c1 = c - s_c; //[0][0]으로 바꾸기
				int r2 = c1, c2 = side - r1 -1; //[j][N-i-1]
				tmp[r2+s_r][c2+s_c] = map[r][c];
				
				//참가자가 이동했어
				//이동한 참가자를 목록에 넣어주기
				if(map[r][c]==person) {
					for(int n : pmap[r][c]) {
						//n번의 참가자 좌표가
						//i->j, j->e_r-i-1로 이동
						q.add(new int[] {n, r2+s_r, c2+s_c});
					}
					
					pmap[r][c].clear();
				} else if(map[r][c]==exit) {
					//출구 좌표가 바뀜
					exit_r = r2+s_r; exit_c = c2+s_c;
				}
			}
		}
		
		//반영
		for(int r=s_r; r<e_r; r++) {
			for(int c=s_c; c<e_c; c++) {
				//내구도 깎아주기
				if(tmp[r][c]>0) tmp[r][c]--;
				
				map[r][c] = tmp[r][c];
			}
		}		
		
		//참가자 이동시키기
		while(!q.isEmpty()) {
			int[] arr = q.poll();
			
			pmap[arr[1]][arr[2]].add(arr[0]);
			
			plist[arr[0]].r = arr[1];
			plist[arr[0]].c = arr[2];
		}
	}

	private static void print(int[][] map) {
		for(int r=0; r<N; r++) {
			for(int c=0; c<N; c++) {
				System.out.print(map[r][c]+" ");
			}
			System.out.println();
		}
	}

	private static void move() {
		for(int i=1; i<=M; i++) {
			Person p = plist[i];
			
			//탈출한 사람 넘어가
			if(p.out) continue;
			
			//현재 자신 좌표와 출구 좌표 최단거리 구하기
			int min_dist = Math.abs(exit_r-p.r)+Math.abs(exit_c-p.c);
			
			//상하좌우 가장 가까운 칸 찾기
			int move_r = -1, move_c = -1;
			
			for(int d=0; d<4; d++) {
				int nr = p.r+dr[d];
				int nc = p.c+dc[d];
				
				//범위 체크
				if(!check(nr,nc)) continue;
				
				//벽이면 이동 불가
				if(map[nr][nc]>0) continue;
				
				//출구면 탈출
				if(map[nr][nc]==exit) {
					cnt--; //참가자 수--
					p.dist++; //거리++
					p.out = true; //탈출 표시
					deletePerson(p.r, p.c, i); //해당 칸에서 해당 번호 참가자 삭제
					if(pmap[p.r][p.c].size()==0) map[p.r][p.c] = 0; //size가 0이면 빈 칸 됨
					
					break; //탈출하면 다른 동작 필요 없음
				}
				
				int dist = Math.abs(exit_r-nr)+Math.abs(exit_c-nc);
				
				if(min_dist > dist) {
					min_dist = dist;
					move_r = nr; move_c = nc;
				}
			}
			
			//이동 가능하면 좌표 수정해주기
			if(!p.out && move_r != -1 && move_c != -1) {
				//이동했더니 전 칸에 사람이 아예 사라지게 되었으면 빈칸으로 수정
				deletePerson(p.r, p.c, i);
				if(pmap[p.r][p.c].size()==0) map[p.r][p.c] = 0;
				
				//이동한 칸에 사람 반경
				pmap[move_r][move_c].add(i);
				map[move_r][move_c] = person;
				
				//정보 수정
				p.r = move_r; p.c = move_c;
				p.dist++;
			}
		}
	}

	private static void deletePerson(int r, int c, int idx) {
		for(int i=0; i<pmap[r][c].size(); i++) {
			if(pmap[r][c].get(i) == idx) {
				pmap[r][c].remove(i);
			}
		}
	}

	private static boolean check(int nr, int nc) {
		if(nr>=0 && nr<N && nc>=0 && nc<N) {
			return true;
		}
		
		return false;
	}

	static class Person{
		int r;
		int c;
		int dist; //이동 횟수
		boolean out; //탈출여부
		
		public Person(int r, int c, int dist, boolean out) {
			super();
			this.r = r;
			this.c = c;
			this.dist = dist;
			this.out = out;
		}

//		@Override
//		public int compareTo(Person o) {
//			if(this.r == o.r) return this.c - o.c;
//			return this.r - o.r;
//		}
	}
}