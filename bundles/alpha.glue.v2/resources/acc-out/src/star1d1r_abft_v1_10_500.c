	/* star1d1r_abft_v1_10_500.c
	 * 
	 * Code generation of the following Alpha system:
	 *   affine star1d1r_abft_v1_10_500 [T, N] -> {  : T >= 10 and N >= 30 }
	 *   	inputs
	 *   		X : {[i]: 0 <= i <= N }
	 *   	outputs
	 *   		Y : {[t, i]: 0 <= t <= T and 0 <= i <= N }
	 *   	locals
	 *   		W : {[i]: -1 <= i <= 1 }
	 *   		C1 : {[tt, ti]: tt >= 2 and 10tt <= -10 + T and ti > 0 and 500ti <= -501 + N }
	 *   		C2 : {[tt, ti]: tt >= 2 and 10tt <= -10 + T and ti > 0 and 500ti <= -501 + N }
	 *   		I : {[tt, ti]: tt >= 2 and 10tt <= -10 + T and ti > 0 and 500ti <= -501 + N }
	 *   		patch : {[w, i]: 0 <= w <= 10 and -1 - w <= i <= 500 + w }
	 *   		patch_NR : {[w, i]: 0 < w <= 10 and -1 - w <= i <= 500 + w }
	 *   	when {  : T >= 10 and N >= 30 } let
	 *   		Y[t,i] = case  {
	 *   			{: 0 <= t <= 1 } : X[i];
	 *   			{: i = N and t >= 2  or  i = 0 and t >= 2 } : Y[t-1,i];
	 *   			{: t >= 2 and 0 < i < N } : (((0.3332[] * Y[t-1,i-1]) + (0.3333[] * Y[t-1,i])) + (0.3[] * Y[t-1,i+1]));
	 *   		};
	 *   		
	 *   		W[i] = case  {
	 *   			{: i = 1 } : 0.33[];
	 *   			{: i = -1 } : 0.3332[];
	 *   			{: i = 0 } : 0.3333[];
	 *   		};
	 *   		
	 *   		patch[w,i] = case  {
	 *   			{: w > 0 } : patch_NR;
	 *   			{: w = 0 and 0 <= i <= 499 } : 1[];
	 *   			auto : 0[];
	 *   		};
	 *   		
	 *   		C1[tt,ti] = reduce(+, (tt,ti,i->tt,ti), {: 500ti <= i <= 499 + 500ti } : Y[10tt,i]);
	 *   		
	 *   		C2[tt,ti] = reduce(+, (tt,ti,p->tt,ti), (patch[10,p] * Y[10tt-10,500ti+p]));
	 *   		
	 *   		I[tt,ti] = ((C2 - C1) / C1);
	 *   		
	 *   		patch_NR[w,i] = reduce(+, (w,i,p->w,i), (patch[w-1,i-p] * W[p]));
	 *   .
	 *
	 * Uses the memory map:
	 *   Y(t,i) -> Y(t - 2*floord(t, 2),i)
	 *
	 * Implements the schedule:
	 *   child:
	 *     sequence:
	 *     - filter: "{ SW[i] }"
	 *     - filter: "{ Spatch[w,i]; Spatch_NR[w,i,p] }"
	 *       child:
	 *         schedule: "[T,N]->[\
	 *           { Spatch[w,i]->[w]; Spatch_NR[w,i,p]->[w]} \
	 *         ]"
	 *     - filter: "{ SI[tt,ti]; SC1[tt,ti,i]; SC2[tt,ti,p]; SY[t,i] }"
	 *       child:
	 *         schedule: "[T,N]->[\
	 *         	{ SC1[tt,ti,i]->[tt]; SC2[tt,ti,p]->[tt-1]; SI[tt,ti]->[tt]; SY[t,i]->[t/10] }, \
	 *         	{ SC1[tt,ti,i]->[10tt]; SC2[tt,ti,p]->[10tt-10]; SI[tt,ti]->[10tt]; SY[t,i]->[t] } \
	 *         ]"
	 *         child:
	 *           sequence:
	 *           - filter: "{ SY[t,i] }"
	 *           - filter: "{ SC1[tt,ti,i] }"
	 *           - filter: "{ SC2[tt,ti,p] }"
	 *           - filter: "{ SI[tt,ti] }"
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
	#define mem_W(i) W[((i >= 0 && 1 - i >= 0) ? ((1 + i)) : 0)]
	#define mem_C1(tt,ti) C1[((-3 + tt >= 0 && -10 + T - 10*tt >= 0 && -2 + ti >= 0 && -501 + N - 500*ti >= 0) ? ((((3 - 2 * tt) + ti) + (-2 + tt) * floord(499 + N, 500))) : (-1 + ti == 0 && -3 + tt >= 0 && -10 + T - 10*tt >= 0 && -1001 + N >= 0) ? (((4 - 2 * tt) + (-2 + tt) * floord(499 + N, 500))) : (-2 + tt == 0 && -501 + N - 500*ti >= 0 && -30 + T >= 0 && -2 + ti >= 0) ? ((-1 + ti)) : 0)]
	#define mem_C2(tt,ti) C2[((-3 + tt >= 0 && -10 + T - 10*tt >= 0 && -2 + ti >= 0 && -501 + N - 500*ti >= 0) ? ((((3 - 2 * tt) + ti) + (-2 + tt) * floord(499 + N, 500))) : (-1 + ti == 0 && -3 + tt >= 0 && -10 + T - 10*tt >= 0 && -1001 + N >= 0) ? (((4 - 2 * tt) + (-2 + tt) * floord(499 + N, 500))) : (-2 + tt == 0 && -501 + N - 500*ti >= 0 && -30 + T >= 0 && -2 + ti >= 0) ? ((-1 + ti)) : 0)]
	#define mem_I(tt,ti) I[((-3 + tt >= 0 && -10 + T - 10*tt >= 0 && -2 + ti >= 0 && -501 + N - 500*ti >= 0) ? ((((3 - 2 * tt) + ti) + (-2 + tt) * floord(499 + N, 500))) : (-1 + ti == 0 && -3 + tt >= 0 && -10 + T - 10*tt >= 0 && -1001 + N >= 0) ? (((4 - 2 * tt) + (-2 + tt) * floord(499 + N, 500))) : (-2 + tt == 0 && -501 + N - 500*ti >= 0 && -30 + T >= 0 && -2 + ti >= 0) ? ((-1 + ti)) : 0)]
	#define mem_patch(w,i) patch[((-1 + w >= 0 && 10 - w >= 0 && w + i >= 0 && 500 + w - i >= 0) ? (((1 + 502 * w + w*w) + i)) : (1 + w + i == 0 && -1 + w >= 0 && 10 - w >= 0) ? ((501 * w + w*w)) : (w == 0 && i >= 0 && 500 - i >= 0) ? (((1 + w) + i)) : 0)]
	#define mem_patch_NR(w,i) patch_NR[((-10 + T >= 0 && -30 + N >= 0 && -2 + w >= 0 && 10 - w >= 0 && w + i >= 0 && 500 + w - i >= 0) ? (((-501 + 502 * w + w*w) + i)) : (1 + w + i == 0 && -10 + T >= 0 && -30 + N >= 0 && -2 + w >= 0 && 10 - w >= 0) ? ((-502 + 501 * w + w*w)) : (-1 + w == 0 && -10 + T >= 0 && -30 + N >= 0 && 1 + i >= 0 && 501 - i >= 0) ? (((1 + w) + i)) : 0)]
	
	// Memory access functions
	#define X(i) mem_X(((i)))
	#define Y(t,i) mem_Y(((t) - 2*floord((t), 2)),((i)))
	#define W(i) mem_W(((i)))
	#define C1(tt,ti) mem_C1(((tt)),((ti)))
	#define C2(tt,ti) mem_C2(((tt)),((ti)))
	#define I(tt,ti) mem_I(((tt)),((ti)))
	#define patch(w,i) mem_patch(((w)),((i)))
	#define patch_NR(w,i) mem_patch_NR(((w)),((i)))
	
	void star1d1r_abft_v1_10_500(long T, long N, float *X, float *Y)
	{
	  // Local memory allocation
	  float *patch = (float*)(calloc((5632),sizeof(float)));
	  float *W = (float*)(calloc((3),sizeof(float)));
	  float *I = (float*)(calloc(((-30 + T >= 0 && -1001 + N >= 0) ? (((4 - 2 * floord(T, 10)) + (-2 + floord(T, 10)) * floord(499 + N, 500))) : 0),sizeof(float)));
	  float *patch_NR = (float*)(calloc(((-30 + N >= 0 && -10 + T >= 0) ? (5130) : 0),sizeof(float)));
	  float *C1 = (float*)(calloc(((-30 + T >= 0 && -1001 + N >= 0) ? (((4 - 2 * floord(T, 10)) + (-2 + floord(T, 10)) * floord(499 + N, 500))) : 0),sizeof(float)));
	  float *C2 = (float*)(calloc(((-30 + T >= 0 && -1001 + N >= 0) ? (((4 - 2 * floord(T, 10)) + (-2 + floord(T, 10)) * floord(499 + N, 500))) : 0),sizeof(float)));

	  #define SC1(tt,ti,i) C1(tt,ti) += Y((10*(tt)),((i)))
	  #define SC2(tt,ti,p) C2(tt,ti) += (patch((10),((p)))) * (Y((-10 + 10*(tt)),(500*(ti) + (p))))
	  #define SI(tt,ti) I(tt,ti) = ((C2(tt,ti)) - (C1(tt,ti))) / (C1(tt,ti))
	  #define SW(i) W(i) = ((-1 + i) == (0)) ? (0.33) : (((1 + i) == (0)) ? (0.3332) : (0.3333))
	  #define SY(t,i) Y(t,i) = (((((-10 + T) >= (0)) && ((-30 + N) >= (0))) && ((t) >= (0))) && ((1 - t) >= (0))) ? (X(((i)))) : (((((((-N + i) == (0)) && ((-10 + T) >= (0))) && ((-30 + N) >= (0))) && ((-2 + t) >= (0))) || (((((i) == (0)) && ((-10 + T) >= (0))) && ((-30 + N) >= (0))) && ((-2 + t) >= (0)))) ? (Y((-1 + (t)),((i)))) : ((((0.3332) * (Y((-1 + (t)),(-1 + (i))))) + ((0.3333) * (Y((-1 + (t)),((i)))))) + ((0.3) * (Y((-1 + (t)),(1 + (i)))))))
	  #define Spatch(w,i) patch(w,i) = ((-1 + w) >= (0)) ? (patch_NR(w,i)) : (((((w) == (0)) && ((i) >= (0))) && ((499 - i) >= (0))) ? (1) : (0))
	  #define Spatch_NR(w,i,p) patch_NR(w,i) += (patch((-1 + (w)),((i) - (p)))) * (W(((p))))

	  // Timers
	  double execution_time;
	  initialize_timer();
	  start_timer();

	  /*
	   * domain: "[T, N] -> { SI[tt, ti] : tt >= 2 and 10tt <= -10 + T and ti > 0 and 500ti <= -501 + N; SC1[tt, ti, i] : tt >= 2 and 10tt <= -10 + T and ti > 0 and 500ti <= -501 + N and 500ti <= i <= 499 + 500ti; SC2[tt, ti, p] : tt >= 2 and 10tt <= -10 + T and ti > 0 and 500ti <= -501 + N and -11 <= p <= 510 and p <= N - 500ti; SY[t, i] : T >= 10 and N >= 30 and 0 <= t <= T and 0 <= i <= N; Spatch_NR[w, i, p] : T >= 10 and N >= 30 and 0 < w <= 10 and p >= -1 and -499 - w + i <= p <= 1 and p <= w + i; Spatch[w, i] : T >= 10 and N >= 30 and 0 <= w <= 10 and -1 - w <= i <= 500 + w; SW[i] : T >= 10 and N >= 30 and -1 <= i <= 1 }"
	   * child:
	   *   sequence:
	   *   - filter: "{ SW[i] }"
	   *   - filter: "{ Spatch[w, i]; Spatch_NR[w, i, p] }"
	   *     child:
	   *       schedule: "[T, N] -> [{ Spatch_NR[w, i, p] -> [(w)]; Spatch[w, i] -> [(w)] }]"
	   *   - filter: "{ SI[tt, ti]; SY[t, i]; SC2[tt, ti, p]; SC1[tt, ti, i] }"
	   *     child:
	   *       schedule: "[T, N] -> [{ SI[tt, ti] -> [(tt)]; SC1[tt, ti, i] -> [(tt)]; SC2[tt, ti, p] -> [(-1 + tt)]; SY[t, i] -> [(floor((t)/10))] }, { SI[tt, ti] -> [(10tt)]; SC1[tt, ti, i] -> [(10tt)]; SC2[tt, ti, p] -> [(-10 + 10tt)]; SY[t, i] -> [(t)] }]"
	   *       child:
	   *         sequence:
	   *         - filter: "{ SY[t, i] }"
	   *         - filter: "{ SC1[tt, ti, i] }"
	   *         - filter: "{ SC2[tt, ti, p] }"
	   *         - filter: "{ SI[tt, ti] }"
	   */
	  for (int c0 = -1; c0 <= 1; c0 += 1) {
	    SW(c0);
	  }
	  for (int c0 = 0; c0 <= 10; c0 += 1) {
	    if (c0 >= 1) {
	      for (int c2 = -c0 - 1; c2 <= c0 + 500; c2 += 1) {
	        for (int c3 = max(-1, -c0 + c2 - 499); c3 <= min(1, c0 + c2); c3 += 1) {
	          Spatch_NR(c0, c2, c3);
	        }
	      }
	    }
	    for (int c2 = -c0 - 1; c2 <= c0 + 500; c2 += 1) {
	      Spatch(c0, c2);
	    }
	  }
	  for (int c0 = 0; c0 <= T / 10; c0 += 1) {
	    for (int c1 = 10 * c0; c1 <= min(T, 10 * c0 + 9); c1 += 1) {
	      for (int c3 = 0; c3 <= N; c3 += 1) {
	        SY(c1, c3);
	      }
	      if (c0 >= 2 && T >= 10 * c0 + 10 && c1 == 10 * c0) {
	        for (int c3 = 1; c3 < (N - 1) / 500; c3 += 1) {
	          for (int c4 = 500 * c3; c4 <= 500 * c3 + 499; c4 += 1) {
	            SC1(c0, c3, c4);
	          }
	        }
	      }
	      if (c0 >= 1 && T >= 10 * c0 + 20 && c1 == 10 * c0) {
	        for (int c3 = 1; c3 < (N - 1) / 500; c3 += 1) {
	          for (int c4 = -11; c4 <= min(510, N - 500 * c3); c4 += 1) {
	            SC2(c0 + 1, c3, c4);
	          }
	        }
	      }
	      if (c0 >= 2 && T >= 10 * c0 + 10 && c1 == 10 * c0) {
	        for (int c3 = 1; c3 < (N - 1) / 500; c3 += 1) {
	          SI(c0, c3);
	        }
	      }
	    }
	  }

	  stop_timer();
	  execution_time = elapsed_time();

	  #undef SY
	  #undef SW
	  #undef Spatch
	  #undef SC1
	  #undef SC2
	  #undef SI
	  #undef Spatch_NR

	  // Print I values
	  
	  #define SI(tt,ti) if (fabs(I(tt,ti))>=threshold) printf("star1d1r_abft_v1_10_500.I(%d,%d) = %E\n",tt,ti, I(tt,ti))
	  
	  float threshold = 0;
	  const char* env_threshold = getenv("THRESHOLD");
	  if (env_threshold != NULL) {
	    threshold = atof(env_threshold);
	  }
	  for (int tt = 2; tt < T / 10; tt += 1) {
	    for (int ti = 1; ti < (N - 1) / 500; ti += 1) {
	      SI(tt, ti);
	    }
	  }
	  
	  #undef SI
	  
	  printf("star1d1r_abft_v1_10_500: %lf sec\n", execution_time);
	
	  free(patch);
	  free(W);
	  free(I);
	  free(patch_NR);
	  free(C1);
	  free(C2);
	}
