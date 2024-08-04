	/* star1d1r_abft_v2_10_500.c
	 * 
	 * Code generation of the following Alpha system:
	 *   affine star1d1r_abft_v2_10_500 [T, N] -> {  : T >= 10 and N >= 30 }
	 *   	inputs
	 *   		X : {[i]: 0 <= i <= N }
	 *   	outputs
	 *   		Y : {[t, i]: 0 <= t <= T and 0 <= i <= N }
	 *   	locals
	 *   		W : {[i]: -1 <= i <= 1 }
	 *   		C1 : {[tt, ti]: tt >= 2 and 10tt <= -10 + T and ti > 0 and 480ti <= -501 + N }
	 *   		C2 : {[tt, ti, p]: tt >= 2 and 10tt <= -10 + T and ti > 0 and 480ti <= -501 + N and -2 <= p <= 2 }
	 *   		I : {[tt, ti]: tt >= 2 and 10tt <= -10 + T and ti > 0 and 480ti <= -501 + N }
	 *   		WKernel : {[i]: -2 <= i <= 2 }
	 *   		WCombos : {[i]: -2 <= i <= 2 }
	 *   		WAll : {[w]: 0 < w <= 10 }
	 *   		C2_NR : {[tt, ti, p]: tt >= 2 and 10tt <= -10 + T and ti > 0 and 480ti <= -501 + N and -2 <= p < 0 }
	 *   		C2_NR2 : {[tt, ti, p]: tt >= 2 and 10tt <= -10 + T and ti > 0 and 480ti <= -501 + N and 0 < p <= 2 }
	 *   		C2_NR3 : {[tt, ti, p]: p = 0 and tt >= 2 and 10tt <= -10 + T and ti > 0 and 480ti <= -501 + N }
	 *   		I_NR : {[tt, ti]: tt >= 2 and 10tt <= -10 + T and ti > 0 and 480ti <= -501 + N }
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
	 *   		WKernel[i] = case  {
	 *   			{: -1 <= i <= 1 } : 1[];
	 *   			auto : 0[];
	 *   		};
	 *   		
	 *   		WCombos[i] = reduce(+, (i,p->i), (WKernel[i-p] * W[p]));
	 *   		
	 *   		WAll[w] = case  {
	 *   			{: w = 1 } : 1[];
	 *   			{: w >= 2 } : (WAll[w-1] * WCombos[0]);
	 *   		};
	 *   		
	 *   		C1[tt,ti] = reduce(+, (tt,ti,i->tt,ti), {: 480ti <= i <= 499 + 480ti } : Y[10tt,i]);
	 *   		
	 *   		C2[tt,ti,p] = case  {
	 *   			(WCombos[p] * C2_NR);
	 *   			(WCombos[p] * C2_NR2);
	 *   			(WCombos[p] * C2_NR3);
	 *   		};
	 *   		
	 *   		I[tt,ti] = ((I_NR - C1) / C1);
	 *   		
	 *   		C2_NR[tt,ti,p] = reduce(+, (tt,ti,p,w->tt,ti,p), (WAll[w] * Y[10tt-w,480ti+p+w]));
	 *   		
	 *   		C2_NR2[tt,ti,p] = reduce(+, (tt,ti,p,w->tt,ti,p), (WAll[w] * Y[10tt-w,480ti+p-w+499]));
	 *   		
	 *   		C2_NR3[tt,ti,p] = reduce(+, (tt,ti,p,w,i->tt,ti,p), {: w = 10 and 10 + 480ti <= i <= 489 + 480ti } : (WAll[w] * Y[10tt-w,i]));
	 *   		
	 *   		I_NR[tt,ti] = reduce(+, (tt,ti,p->tt,ti), C2);
	 *   .
	 *
	 * Uses the memory map:
	 *   C2_NR(tt,ti,p) -> C2(tt,ti,p)
	 *   C2_NR2(tt,ti,p) -> C2(tt,ti,p)
	 *   C2_NR3(tt,ti,p) -> C2(tt,ti,0)
	 *   Y(t,i) -> Y(t - 2*floord(t, 2),i)
	 *
	 * Implements the schedule:
	 *   child:
	 *     sequence:
	 *     - filter: "{ SW[i] }"
	 *     - filter: "{ SWKernel[i] }"
	 *     - filter: "{ SWCombos[i,p] }"
	 *     - filter: "{ SWAll[w] }"
	 *       child:
	 *         schedule: "[T,N]->[\
	 *           { SWAll[w]->[w] } \
	 *         ]"
	 *     - filter: "{ \
	 *         SY[t,i]; \
	 *         SC1[tt,ti,i]; \
	 *         SC2_NR[tt,ti,p,w]; \
	 *         SC2_NR2[tt,ti,p,w]; \
	 *         SC2_NR3[tt,ti,p,w,i]; \
	 *         SC2[tt,ti,p]; \
	 *         SI_NR[tt,ti,p]; \
	 *         SI[tt,ti] \
	 *       }"
	 *       child:
	 *         schedule: "[T,N]->[\
	 *           { \
	 *             SY[t,i]->[t]; \
	 *             SC1[tt,ti,i]->[10tt]; \
	 *             SC2[tt,ti,p]->[10tt]; \
	 *             SC2_NR[tt,ti,p,w]->[10tt-w]; \
	 *             SC2_NR2[tt,ti,p,w]->[10tt-w]; \
	 *             SC2_NR3[tt,ti,p,w,i]->[10tt-w]; \
	 *             SI[tt,ti]->[10tt]; \
	 *             SI_NR[tt,ti,p]->[10tt] \
	 *           } \
	 *         ]"
	 *         child:
	 *           sequence:
	 *           - filter: "{ SY[t,i] }"
	 *           - filter: "{ SC1[tt,ti,i] }"
	 *           - filter: "{ \
	 *               SC2_NR[tt,ti,p,w]; \
	 *               SC2_NR2[tt,ti,p,w]; \
	 *               SC2_NR3[tt,ti,p,w,i] \
	 *             }"
	 *           - filter: "{ SC2[tt,ti,p] }"
	 *           - filter: "{ SI_NR[tt,ti,p] }"
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
	#define mem_C1(tt,ti) C1[((-3 + tt >= 0 && -10 + T - 10*tt >= 0 && -2 + ti >= 0 && -501 + N - 480*ti >= 0) ? ((((3 - 2 * tt) + ti) + (-2 + tt) * floord(459 + N, 480))) : (-1 + ti == 0 && -3 + tt >= 0 && -10 + T - 10*tt >= 0 && -981 + N >= 0) ? (((4 - 2 * tt) + (-2 + tt) * floord(459 + N, 480))) : (-2 + tt == 0 && -501 + N - 480*ti >= 0 && -30 + T >= 0 && -2 + ti >= 0) ? ((-1 + ti)) : 0)]
	#define mem_C2(tt,ti,p) C2[((-3 + tt >= 0 && -10 + T - 10*tt >= 0 && -2 + ti >= 0 && -501 + N - 480*ti >= 0 && 1 + p >= 0 && 2 - p >= 0) ? (((((17 - 10 * tt) + 5 * ti) + p) + (-10 + 5 * tt) * floord(459 + N, 480))) : (2 + p == 0 && -3 + tt >= 0 && -10 + T - 10*tt >= 0 && -2 + ti >= 0 && -501 + N - 480*ti >= 0) ? ((((15 - 10 * tt) + 5 * ti) + (-10 + 5 * tt) * floord(459 + N, 480))) : (-1 + ti == 0 && -981 + N >= 0 && -3 + tt >= 0 && -10 + T - 10*tt >= 0 && 1 + p >= 0 && 2 - p >= 0) ? ((((22 - 10 * tt) + p) + (-10 + 5 * tt) * floord(459 + N, 480))) : (2 + p == 0 && -1 + ti == 0 && -981 + N >= 0 && -3 + tt >= 0 && -10 + T - 10*tt >= 0) ? (((20 - 10 * tt) + (-10 + 5 * tt) * floord(459 + N, 480))) : (-2 + tt == 0 && -30 + T >= 0 && -2 + ti >= 0 && -501 + N - 480*ti >= 0 && 1 + p >= 0 && 2 - p >= 0) ? (((-3 + 5 * ti) + p)) : (2 + p == 0 && -2 + tt == 0 && -30 + T >= 0 && -2 + ti >= 0 && -501 + N - 480*ti >= 0) ? ((-5 + 5 * ti)) : (-1 + ti == 0 && -2 + tt == 0 && -30 + T >= 0 && 2 - p >= 0 && -981 + N >= 0 && 1 + p >= 0) ? ((2 + p)) : 0)]
	#define mem_I(tt,ti) I[((-3 + tt >= 0 && -10 + T - 10*tt >= 0 && -2 + ti >= 0 && -501 + N - 480*ti >= 0) ? ((((3 - 2 * tt) + ti) + (-2 + tt) * floord(459 + N, 480))) : (-1 + ti == 0 && -3 + tt >= 0 && -10 + T - 10*tt >= 0 && -981 + N >= 0) ? (((4 - 2 * tt) + (-2 + tt) * floord(459 + N, 480))) : (-2 + tt == 0 && -501 + N - 480*ti >= 0 && -30 + T >= 0 && -2 + ti >= 0) ? ((-1 + ti)) : 0)]
	#define mem_WKernel(i) WKernel[((1 + i >= 0 && 2 - i >= 0) ? ((2 + i)) : 0)]
	#define mem_WCombos(i) WCombos[((1 + i >= 0 && 2 - i >= 0) ? ((2 + i)) : 0)]
	#define mem_WAll(w) WAll[((-2 + w >= 0 && 10 - w >= 0) ? ((-1 + w)) : 0)]
	#define mem_I_NR(tt,ti) I_NR[((-3 + tt >= 0 && -10 + T - 10*tt >= 0 && -2 + ti >= 0 && -501 + N - 480*ti >= 0) ? ((((3 - 2 * tt) + ti) + (-2 + tt) * floord(459 + N, 480))) : (-1 + ti == 0 && -3 + tt >= 0 && -10 + T - 10*tt >= 0 && -981 + N >= 0) ? (((4 - 2 * tt) + (-2 + tt) * floord(459 + N, 480))) : (-2 + tt == 0 && -501 + N - 480*ti >= 0 && -30 + T >= 0 && -2 + ti >= 0) ? ((-1 + ti)) : 0)]
	
	// Memory access functions
	#define X(i) mem_X(((i)))
	#define Y(t,i) mem_Y(((t) - 2*floord((t), 2)),((i)))
	#define W(i) mem_W(((i)))
	#define C1(tt,ti) mem_C1(((tt)),((ti)))
	#define C2(tt,ti,p) mem_C2(((tt)),((ti)),((p)))
	#define I(tt,ti) mem_I(((tt)),((ti)))
	#define WKernel(i) mem_WKernel(((i)))
	#define WCombos(i) mem_WCombos(((i)))
	#define WAll(w) mem_WAll(((w)))
	#define C2_NR(tt,ti,p) mem_C2(((tt)),((ti)),((p)))
	#define C2_NR2(tt,ti,p) mem_C2(((tt)),((ti)),((p)))
	#define C2_NR3(tt,ti,p) mem_C2(((tt)),((ti)),(0))
	#define I_NR(tt,ti) mem_I_NR(((tt)),((ti)))
	
	void star1d1r_abft_v2_10_500(long T, long N, float *X, float *Y)
	{
	  // Local memory allocation
	  float *WAll = (float*)(calloc((10),sizeof(float)));
	  float *W = (float*)(calloc((3),sizeof(float)));
	  float *I_NR = (float*)(calloc(((-30 + T >= 0 && -981 + N >= 0) ? (((4 - 2 * floord(T, 10)) + (-2 + floord(T, 10)) * floord(459 + N, 480))) : 0),sizeof(float)));
	  float *I = (float*)(calloc(((-30 + T >= 0 && -981 + N >= 0) ? (((4 - 2 * floord(T, 10)) + (-2 + floord(T, 10)) * floord(459 + N, 480))) : 0),sizeof(float)));
	  float *WKernel = (float*)(calloc((5),sizeof(float)));
	  float *C1 = (float*)(calloc(((-30 + T >= 0 && -981 + N >= 0) ? (((4 - 2 * floord(T, 10)) + (-2 + floord(T, 10)) * floord(459 + N, 480))) : 0),sizeof(float)));
	  float *WCombos = (float*)(calloc((5),sizeof(float)));
	  float *C2 = (float*)(calloc(((-30 + T >= 0 && -981 + N >= 0) ? (((20 - 10 * floord(T, 10)) + (-10 + 5 * floord(T, 10)) * floord(459 + N, 480))) : 0),sizeof(float)));

	  #define SC1(tt,ti,i) C1(tt,ti) += Y((10*(tt)),((i)))
	  #define SC2(tt,ti,p) C2(tt,ti,p) = (((((((-2 + tt) >= (0)) && ((-10 + T - 10*tt) >= (0))) && ((-1 + ti) >= (0))) && ((-501 + N - 480*ti) >= (0))) && ((2 + p) >= (0))) && ((-1 - p) >= (0))) ? ((WCombos(((p)))) * (C2_NR(tt,ti,p))) : ((((((((-2 + tt) >= (0)) && ((-10 + T - 10*tt) >= (0))) && ((-1 + ti) >= (0))) && ((-501 + N - 480*ti) >= (0))) && ((-1 + p) >= (0))) && ((2 - p) >= (0))) ? ((WCombos(((p)))) * (C2_NR2(tt,ti,p))) : ((WCombos(((p)))) * (C2_NR3(tt,ti,p))))
	  #define SC2_NR(tt,ti,p,w) C2_NR(tt,ti,p) += (WAll(((w)))) * (Y((10*(tt) - (w)),(480*(ti) + (p) + (w))))
	  #define SC2_NR2(tt,ti,p,w) C2_NR2(tt,ti,p) += (WAll(((w)))) * (Y((10*(tt) - (w)),(499 + 480*(ti) + (p) - (w))))
	  #define SC2_NR3(tt,ti,p,w,i) C2_NR3(tt,ti,p) += (WAll(((w)))) * (Y((10*(tt) - (w)),((i))))
	  #define SI(tt,ti) I(tt,ti) = ((I_NR(tt,ti)) - (C1(tt,ti))) / (C1(tt,ti))
	  #define SI_NR(tt,ti,p) I_NR(tt,ti) += C2(tt,ti,p)
	  #define SW(i) W(i) = ((-1 + i) == (0)) ? (0.33) : (((1 + i) == (0)) ? (0.3332) : (0.3333))
	  #define SWAll(w) WAll(w) = ((-1 + w) == (0)) ? (1) : ((WAll((-1 + (w)))) * (WCombos((0))))
	  #define SWCombos(i,p) WCombos(i) += (WKernel(((i) - (p)))) * (W(((p))))
	  #define SWKernel(i) WKernel(i) = (((1 + i) >= (0)) && ((1 - i) >= (0))) ? (1) : (0)
	  #define SY(t,i) Y(t,i) = (((((-10 + T) >= (0)) && ((-30 + N) >= (0))) && ((t) >= (0))) && ((1 - t) >= (0))) ? (X(((i)))) : (((((((-N + i) == (0)) && ((-10 + T) >= (0))) && ((-30 + N) >= (0))) && ((-2 + t) >= (0))) || (((((i) == (0)) && ((-10 + T) >= (0))) && ((-30 + N) >= (0))) && ((-2 + t) >= (0)))) ? (Y((-1 + (t)),((i)))) : ((((0.3332) * (Y((-1 + (t)),(-1 + (i))))) + ((0.3333) * (Y((-1 + (t)),((i)))))) + ((0.3) * (Y((-1 + (t)),(1 + (i)))))))

	  // Timers
	  double execution_time;
	  initialize_timer();
	  start_timer();

	  /*
	   * domain: "[T, N] -> { SWKernel[i] : T >= 10 and N >= 30 and -2 <= i <= 2; SI[tt, ti] : tt >= 2 and 10tt <= -10 + T and ti > 0 and 480ti <= -501 + N; SC1[tt, ti, i] : tt >= 2 and 10tt <= -10 + T and ti > 0 and 480ti <= -501 + N and 480ti <= i <= 499 + 480ti; SC2[tt, ti, p] : tt >= 2 and 10tt <= -10 + T and ti > 0 and 480ti <= -501 + N and -2 <= p <= 2; SY[t, i] : T >= 10 and N >= 30 and 0 <= t <= T and 0 <= i <= N; SI_NR[tt, ti, p] : tt >= 2 and 10tt <= -10 + T and ti > 0 and 480ti <= -501 + N and -2 <= p <= 2; SW[i] : T >= 10 and N >= 30 and -1 <= i <= 1; SWCombos[i, p] : T >= 10 and N >= 30 and -2 <= i <= 2 and p >= -2 + i and -1 <= p <= 1 and p <= 2 + i; SC2_NR2[tt, ti, p, w] : tt >= 2 and 10tt <= -10 + T and ti > 0 and 480ti <= -501 + N and 0 < p <= 2 and 0 < w <= 10; SC2_NR3[tt, ti, p = 0, w = 10, i] : tt >= 2 and 10tt <= -10 + T and ti > 0 and 480ti <= -501 + N and 10 + 480ti <= i <= 489 + 480ti; SWAll[w] : T >= 10 and N >= 30 and 0 < w <= 10; SC2_NR[tt, ti, p, w] : tt >= 2 and 10tt <= -10 + T and ti > 0 and 480ti <= -501 + N and -2 <= p < 0 and 0 < w <= 10 }"
	   * child:
	   *   sequence:
	   *   - filter: "{ SW[i] }"
	   *   - filter: "{ SWKernel[i] }"
	   *   - filter: "{ SWCombos[i, p] }"
	   *   - filter: "{ SWAll[w] }"
	   *     child:
	   *       schedule: "[T, N] -> [{ SWAll[w] -> [(w)] }]"
	   *   - filter: "{ SC2_NR2[tt, ti, p, w]; SI[tt, ti]; SC2_NR3[tt, ti, p, w, i]; SI_NR[tt, ti, p]; SY[t, i]; SC2_NR[tt, ti, p, w]; SC2[tt, ti, p]; SC1[tt, ti, i] }"
	   *     child:
	   *       schedule: "[T, N] -> [{ SI[tt, ti] -> [(10tt)]; SC1[tt, ti, i] -> [(10tt)]; SC2[tt, ti, p] -> [(10tt)]; SY[t, i] -> [(t)]; SI_NR[tt, ti, p] -> [(10tt)]; SC2_NR2[tt, ti, p, w] -> [(10tt - w)]; SC2_NR3[tt, ti, p, w, i] -> [(10tt - w)]; SC2_NR[tt, ti, p, w] -> [(10tt - w)] }]"
	   *       child:
	   *         sequence:
	   *         - filter: "{ SY[t, i] }"
	   *         - filter: "{ SC1[tt, ti, i] }"
	   *         - filter: "{ SC2_NR2[tt, ti, p, w]; SC2_NR3[tt, ti, p, w, i]; SC2_NR[tt, ti, p, w] }"
	   *         - filter: "{ SC2[tt, ti, p] }"
	   *         - filter: "{ SI_NR[tt, ti, p] }"
	   *         - filter: "{ SI[tt, ti] }"
	   */
	  for (int c0 = -1; c0 <= 1; c0 += 1) {
	    SW(c0);
	  }
	  for (int c0 = -2; c0 <= 2; c0 += 1) {
	    SWKernel(c0);
	  }
	  for (int c0 = -2; c0 <= 2; c0 += 1) {
	    for (int c1 = max(-1, c0 - 2); c1 <= min(1, c0 + 2); c1 += 1) {
	      SWCombos(c0, c1);
	    }
	  }
	  for (int c0 = 1; c0 <= 10; c0 += 1) {
	    SWAll(c0);
	  }
	  for (int c0 = 0; c0 <= T; c0 += 1) {
	    for (int c2 = 0; c2 <= N; c2 += 1) {
	      SY(c0, c2);
	    }
	    if (c0 >= 20 && T >= c0 + 10 && c0 % 10 == 0) {
	      for (int c2 = 1; c2 < (N - 21) / 480; c2 += 1) {
	        for (int c3 = 480 * c2; c3 <= 480 * c2 + 499; c3 += 1) {
	          SC1(c0 / 10, c2, c3);
	        }
	      }
	    }
	    if (c0 >= 10 && (c0 % 10) + T >= c0 + 20) {
	      for (int c2 = 1; c2 < (N - 21) / 480; c2 += 1) {
	        for (int c3 = 1; c3 <= 2; c3 += 1) {
	          SC2_NR2((c0 + 10) / 10, c2, c3, -(c0 % 10) + 10);
	        }
	      }
	      for (int c2 = 1; c2 < (N - 21) / 480; c2 += 1) {
	        for (int c3 = -2; c3 < 0; c3 += 1) {
	          SC2_NR((c0 + 10) / 10, c2, c3, -(c0 % 10) + 10);
	        }
	      }
	      if (c0 % 10 == 0) {
	        for (int c2 = 1; c2 < (N - 21) / 480; c2 += 1) {
	          for (int c5 = 480 * c2 + 10; c5 <= 480 * c2 + 489; c5 += 1) {
	            SC2_NR3((c0 / 10) + 1, c2, 0, 10, c5);
	          }
	        }
	      }
	    }
	    if (c0 >= 20 && T >= c0 + 10 && c0 % 10 == 0) {
	      for (int c2 = 1; c2 < (N - 21) / 480; c2 += 1) {
	        for (int c3 = -2; c3 <= 2; c3 += 1) {
	          SC2(c0 / 10, c2, c3);
	        }
	      }
	      for (int c2 = 1; c2 < (N - 21) / 480; c2 += 1) {
	        for (int c3 = -2; c3 <= 2; c3 += 1) {
	          SI_NR(c0 / 10, c2, c3);
	        }
	      }
	      for (int c2 = 1; c2 < (N - 21) / 480; c2 += 1) {
	        SI(c0 / 10, c2);
	      }
	    }
	  }

	  stop_timer();
	  execution_time = elapsed_time();

	  #undef SY
	  #undef SW
	  #undef SWKernel
	  #undef SWCombos
	  #undef SWAll
	  #undef SC1
	  #undef SC2
	  #undef SI
	  #undef SC2_NR
	  #undef SC2_NR2
	  #undef SC2_NR3
	  #undef SI_NR

	  // Print I values
	  
	  #define SI(tt,ti) if (fabs(I(tt,ti))>=threshold) printf("star1d1r_abft_v2_10_500.I(%d,%d) = %E\n",tt,ti, I(tt,ti))
	  
	  float threshold = 0;
	  const char* env_threshold = getenv("THRESHOLD");
	  if (env_threshold != NULL) {
	    threshold = atof(env_threshold);
	  }
	  for (int tt = 2; tt < T / 10; tt += 1) {
	    for (int ti = 1; ti < (N - 21) / 480; ti += 1) {
	      SI(tt, ti);
	    }
	  }
	  
	  #undef SI
	  
	  printf("star1d1r_abft_v2_10_500: %lf sec\n", execution_time);
	
	  free(WAll);
	  free(W);
	  free(I_NR);
	  free(I);
	  free(WKernel);
	  free(C1);
	  free(WCombos);
	  free(C2);
	}
