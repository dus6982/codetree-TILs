import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Queue;
import java.util.StringTokenizer;

public class Main {
	static int N, M, K, turn;
	static int[][] map;
	static ArrayList<Turret> list; //포탑 정보
	static boolean[][] visit; //방문 여부
	static int[][] backR; //지나온 경로 저장하기 
	static int[][] backC;
	static boolean[][] attack; //공격 영향 여부
	static int[][] time;
	//					우 하 좌 상 (4방) / 우상 우하 좌하 좌상 (8방)
	static int[] dr = {0, 1, 0, -1, -1, 1, 1, -1};
	static int[] dc = {1, 0, -1, 0, 1, 1, -1, -1};

	public static void main(String[] args) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		StringTokenizer st = new StringTokenizer(br.readLine());
		
		N = Integer.parseInt(st.nextToken());
		M = Integer.parseInt(st.nextToken());
		K = Integer.parseInt(st.nextToken());
		
		map = new int[N][M];
		time = new int[N][M];
		
		for(int r=0; r<N; r++) {
			st = new StringTokenizer(br.readLine());
			for(int c=0; c<M; c++) {
				map[r][c] = Integer.parseInt(st.nextToken());
			}
		}

		while(K-- > 0) {
			turn++;
			
			//살아있는 포탑 정리
			list = new ArrayList<>();
			for(int r=0; r<N; r++) {
				for(int c=0; c<M; c++) {
					if(map[r][c]>0) 
						list.add(new Turret(r, c, map[r][c], time[r][c]));
				}
			}
			
			//살아있는 포탑이 1개 이하라면 바로 종료
			if(list.size() <= 1) break;
			
			attack = new boolean[N][M];
			
			//공격자 선정하기 위해 정렬해주기
			//0번째가 공격자, list.size()-1번째가 희생자
			//공격자는 힘이 자신의 원래 힘 + (N+M) 됨
			//공격한 턴, 공격 영향 반영
			Collections.sort(list);
			
			map[list.get(0).r][list.get(0).c] += N+M;
			list.get(0).power = map[list.get(0).r][list.get(0).c];

			time[list.get(0).r][list.get(0).c] = turn;
			list.get(0).time = time[list.get(0).r][list.get(0).c];
			
			attack[list.get(0).r][list.get(0).c] = true; //영향 안 받기
			
			//레이저 공격
			boolean isPossible = laserAttack();
			//레이저 공격 못하면 포탄 공격
			if(!isPossible) bombAttack();
			
			//정비 : 공격 관여하지 않은 살아있는 포탑 공격력 +1 해주기
			maintain();
		}
		
		int answer = 0;
		for(int r=0; r<N; r++) {
			for(int c=0; c<M; c++) {
				answer = Math.max(answer, map[r][c]);
			}
		}
		System.out.println(answer);
	}

	private static void maintain() {
		for(int r=0; r<N; r++) {
            for(int c=0; c<M; c++) {
            	//영향을 받았거나 부서진 포탑이면 넘어가
                if(attack[r][c] || map[r][c]==0) 
                    continue;
                
                map[r][c]++;
            }
        }
	}

	private static void bombAttack() {
		Turret attacker = list.get(0); // 가장 약한 공격자
		Turret victim = list.get(list.size()-1); //가장 강한 희생자
		
		//희생자 데미지
		map[victim.r][victim.c] -= attacker.power;
		//포탑이 부서지면 부서짐 반영
		if(map[victim.r][victim.c]<0) 
			map[victim.r][victim.c] = 0;
		attack[victim.r][victim.c] = true; //공격 영향 표시
		
		for(int d=0; d<8; d++) {
			int nr = (victim.r+dr[d]+N)%N;
			int nc = (victim.c+dc[d]+M)%M;
			
			//공격자 자신이면 넘어가기
			if(nr==attacker.r && nc==attacker.c)
				continue;
			
			//부서진 포탑 x
			if(map[nr][nc]==0) continue;
			
			map[nr][nc] -= attacker.power/2;
			//포탑이 부서지면 부서짐 반영
			if(map[nr][nc]<0)
				map[nr][nc] = 0;
			attack[nr][nc] = true; //공격 영향 표시
		}
	}

	private static boolean laserAttack() {
		visit = new boolean[N][M];
		backR = new int[N][M];
		backC = new int[N][M];
		
		Turret attacker = list.get(0);
		Turret victim = list.get(list.size()-1);
		
		Queue<int[]> q = new LinkedList<>();
		q.add(new int[] {attacker.r, attacker.c});
		visit[attacker.r][attacker.c] = true;
		
		boolean possible = false;
		
		while(!q.isEmpty()) {
			int[] arr = q.poll();
			int r = arr[0]; int c = arr[1];
			
			//희생자에게 도달하면 바로 멈추기
			if(r==victim.r && c==victim.c) {
				possible = true;
				break;
			}
			
			for(int d=0; d<4; d++) {
				//경계 넘으면 반대 방향으로
				int nr = (r+dr[d]+N)%N;
				int nc = (c+dc[d]+M)%M;	
				
				//방문 했으면 넘어가 or 부서진 포탑이면 넘어가
				if(visit[nr][nc]) continue;

				if(map[nr][nc]==0) continue;
				
				q.add(new int[] {nr, nc});
				visit[nr][nc] = true;
				
				//전 좌표 저장해주기
				backR[nr][nc] = r;
				backC[nr][nc] = c; 
			}
		}
		
		//최단 경로가 있다면 레이저 공격하기
		if(possible) {
			//희생자에게 공격
			map[victim.r][victim.c] -= attacker.power;
			
			//포탑이 부서지면 부서짐 반영
			if(map[victim.r][victim.c]<0)
				map[victim.r][victim.c] = 0;
			attack[victim.r][victim.c] = true; //공격 영향 표시
			
			//기존 경로 역추적
			//지나온 경로에 있는 포탑은 (공격자힘/2)만큼 데미지 입음
			int cr = backR[victim.r][victim.c];
			int cc = backC[victim.r][victim.c];
			
			while(!(cr==attacker.r && cc==attacker.c)) {
				map[cr][cc] -= attacker.power/2;
				
				//포탑이 부서지면 부서짐 반영
				if(map[cr][cc]<0) 
					map[cr][cc] = 0;
				attack[cr][cc] = true; //공격 영향 표시
				
				//다음 좌표로 이동
				int ncr = backR[cr][cc];
				int ncc = backC[cr][cc];
				
				cr = ncr; cc = ncc;
			}
		}
		
		//레이저 공격 여부 반환
		return possible;
	}

	static class Turret implements Comparable<Turret>{
		int r;
		int c;
		int power;
		int time;
		
		public Turret(int r, int c, int power, int time) {
			super();
			this.r = r;
			this.c = c;
			this.power = power;
			this.time = time;
		}

		@Override
		public int compareTo(Turret o) {
			//공격력 가장 낮은 애
			if(this.power != o.power) return this.power - o.power;
			//가장 최근에 공격한 애 (time의 수가 작은 애)
			if(this.time != o.time) return o.time - this.time;
			//행과 열의 합이 가장 큰 애
			if(this.r+this.c != o.r+o.c) return (o.r+o.c) - (this.r+this.c);
			//열 값이 가장 큰 애
			return o.c - this.c;
		}
	}
}