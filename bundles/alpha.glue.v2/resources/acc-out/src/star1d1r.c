	/* star1d1r.c
	 * 
	 * Code generation of the following Alpha system:
	 *   affine star1d1r [T, N] -> {  : T >= 10 and N >= 30 }
	 *   	inputs
	 *   		X : {[i]: 0 <= i <= N }
	 *   	outputs
	 *   		Y : {[t, i]: 0 <= t <= T and 0 <= i <= N }
	 *   	when {  : T >= 10 and N >= 30 } let
	 *   		Y[t,i] = case  {
	 *   			{: 0 <= t <= 1 } : X[i];
	 *   			{: i = N and t >= 2  or  i = 0 and t >= 2 } : Y[t-1,i];
	 *   			{: t >= 2 and 0 < i < N } : (((0.3332[] * Y[t-1,i-1]) + (0.3333[] * Y[t-1,i])) + (0.3[] * Y[t-1,i+1]));
	 *   		};
	 *   .
	 *
	 * Uses the memory map:
	 *   Y(t,i) -> Y(t - 2*floord(t, 2),i)
	 *
	 * Implements the schedule:
	 *   child:
	 *     sequence:
	 *     - filter: "{ SY[t,i] }"
	 *       child:
	 *         schedule: "[T,N]->[\
	 *         	{ SY[t,i]->[t] } \
	 *         ]"
	 *
	 */
	#include<stdio.h>
	#include<stdlib.h>
	#include<math.h>
	#include<time.h>
	
	#define max(x, y)   ((x)>(y) ? (x) : (y))
	#define min(x, y)   ((x)>(y) ? (y) : (x))
	#define ceild(n,d)  (int)ceil(((double)(n))/((double)(d)))
	#define floord(n,d) (int)floor(((double)(n))/((double)(d)))
	#define mallocCheck(v,s,d) if ((v) == NULL) { printf("Failed to allocate memory for %s : size=%lu\n", "sizeof(d)*(s)", sizeof(d)*(s)); exit(-1); }
	
	void initialize_timer();
	void reset_timer();
	void start_timer();
	void stop_timer();
	double elapsed_time();
	
	// Memory mapped targets
	#define mem_X(i) X[((-10 + T >= 0 && N - i >= 0 && -30 + N >= 0 && -1 + i >= 0) ? (i) : 0)]
	#define mem_Y(t,i) Y[((-1 + t == 0 && -10 + T >= 0 && -30 + N >= 0 && -1 + i >= 0 && N - i >= 0) ? (((1 + N) + i)) : (i == 0 && -1 + t == 0 && -10 + T >= 0 && -30 + N >= 0) ? ((1 + N)) : (t == 0 && -10 + T >= 0 && -30 + N >= 0 && -1 + i >= 0 && N - i >= 0) ? (i) : 0)]
	
	// Memory access functions
	#define X(i) mem_X(((i)))
	#define Y(t,i) mem_Y(((t) - 2*floord((t), 2)),((i)))
	
	void star1d1r(long T, long N, float *X, float *Y)
	{

	  #define SY(t,i) Y(t,i) = (((((-10 + T) >= (0)) && ((-30 + N) >= (0))) && ((t) >= (0))) && ((1 - t) >= (0))) ? (X(((i)))) : (((((((-N + i) == (0)) && ((-10 + T) >= (0))) && ((-30 + N) >= (0))) && ((-2 + t) >= (0))) || (((((i) == (0)) && ((-10 + T) >= (0))) && ((-30 + N) >= (0))) && ((-2 + t) >= (0)))) ? (Y((-1 + (t)),((i)))) : ((((0.3332) * (Y((-1 + (t)),(-1 + (i))))) + ((0.3333) * (Y((-1 + (t)),((i)))))) + ((0.3) * (Y((-1 + (t)),(1 + (i)))))))

	  // Timers
	  double execution_time;
	  initialize_timer();
	  start_timer();

	  /*
	   * domain: "[T, N] -> { SY[t, i] : T >= 10 and N >= 30 and 0 <= t <= T and 0 <= i <= N }"
	   * child:
	   *   sequence:
	   *   - filter: "{ SY[t, i] }"
	   *     child:
	   *       schedule: "[T, N] -> [{ SY[t, i] -> [(t)] }]"
	   */
	  for (int c0 = 0; c0 <= T; c0 += 1) {
	    for (int c2 = 0; c2 <= N; c2 += 1) {
	      SY(c0, c2);
	    }
	  }

	  stop_timer();
	  execution_time = elapsed_time();

	  #undef SY

	  
	  printf("star1d1r: %lf sec\n", execution_time);
	
	}
