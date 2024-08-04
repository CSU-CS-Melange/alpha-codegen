all: bin/star1d1r

mkdir:
	mkdir -p bin
	
src/time.o: src/time.c
	gcc -c -o $@ $<

star1d1r.o: src/star1d1r.c mkdir
	gcc -c -o src/$@ $<

star1d1r_abft_v1_10_500.o: src/star1d1r_abft_v1_10_500.c src/time.o mkdir
	gcc -c -o src/$@ $<

star1d1r_abft_v2_10_500.o: src/star1d1r_abft_v2_10_500.c src/time.o mkdir
	gcc -c -o src/$@ $<

bin/star1d1r: src/star1d1r-wrapper.c src/star1d1r.o src/star1d1r_abft_v1_10_500.o src/star1d1r_abft_v2_10_500.o src/time.o mkdir
	gcc -o $@ src/star1d1r-wrapper.c src/star1d1r.o src/star1d1r_abft_v1_10_500.o src/star1d1r_abft_v2_10_500.o src/time.o

clean:
	rm -f src/*.o bin/*
