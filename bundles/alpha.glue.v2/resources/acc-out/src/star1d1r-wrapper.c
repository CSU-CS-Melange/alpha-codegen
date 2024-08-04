// star1d1r-wrapper.c

#include<stdio.h>
#include<stdlib.h>
#include<math.h>
#include<sys/time.h>

#define max(x, y)   ((x)>(y) ? (x) : (y))
#define min(x, y)   ((x)>(y) ? (y) : (x))
#define ceild(n,d)  (int)ceil(((double)(n))/((double)(d)))
#define floord(n,d) (int)floor(((double)(n))/((double)(d)))
#define mallocCheck(v,s,d) if ((v) == NULL) { printf("Failed to allocate memory for %s : size=%lu\n", "sizeof(d)*(s)", sizeof(d)*(s)); exit(-1); }

// External system declarations
void star1d1r(long T, long N, float *X, float *Y);
void star1d1r_abft_v1_10_500(long T, long N, float *X, float *Y);
void star1d1r_abft_v2_10_500(long T, long N, float *X, float *Y);

// Memory mapped targets
#define mem_X(i) X[((-10 + T >= 0 && N - i >= 0 && -30 + N >= 0 && -1 + i >= 0) ? (i) : 0)]
#define mem_Y(t,i) Y[((-10 + T >= 0 && -30 + N >= 0 && -1 + t >= 0 && T - t >= 0 && -1 + i >= 0 && N - i >= 0) ? (((1 + N) * t + i)) : (i == 0 && -10 + T >= 0 && -30 + N >= 0 && -1 + t >= 0 && T - t >= 0) ? ((1 + N) * t) : (t == 0 && -10 + T >= 0 && -30 + N >= 0 && N - i >= 0 && -1 + i >= 0) ? (i) : 0)]

// Memory access functions
#define X(i) mem_X(((i)))
#define Y(t,i) mem_Y(((t) - 2*floord((t), 2)),((i)))



int main(int argc, char **argv) 
{
	if (argc < 3) {
		printf("usage: %s T N\n", argv[0]);
		return 1;
	}
	
	// Parse parameter sizes
	long T = atol(argv[1]);
	long N = atol(argv[2]);
	float threshold = atof(argv[2]);
	// Local memory allocation
	float *X = (float*)(calloc(((-10 + T >= 0 && -30 + N >= 0) ? ((1 + N)) : 0),sizeof(float)));
	float *Y = (float*)(calloc(((-10 + T >= 0 && -30 + N >= 0) ? ((2 + 2 * N)) : 0),sizeof(float)));
	
	srand(0);
	
	// X initialization
	#define SX(i) X(i) = rand() % 100 + 1
	for (int i = 0; i <= N; i += 1) {
	  SX(i);
	}
	#undef SX
	// Y initialization
	#define SY(t,i) Y(t,i) = rand() % 100 + 1
	for (int t = 0; t <= T; t += 1) {
	  for (int i = 0; i <= N; i += 1) {
	    SY(t, i);
	  }
	}
	#undef SY
	
	// Baseline
	star1d1r(T, N, X, Y);
	// ABFTv1
	star1d1r_abft_v1_10_500(T, N, X, Y);
	// ABFTv2
	star1d1r_abft_v2_10_500(T, N, X, Y);
	
	return 0;
}

