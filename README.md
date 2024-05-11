# alpha-codegen

There are currently two versions of AlphaZ.
This repository contains two bundles, one for each.
The `artifact/bin/acc` script glues together the codegen functionality from both versions and has the following usage,
```
$ ./artifact/bin/acc
usage: acc alpha_file [output_directory]
parameters:
     alpha_file       :  input Alpha source file
     output_directory :  directory in which to place the output files, if the path
                         does not exist then it will be created (default: .)
```

This script requires that each bundle be exported into its own jar file and that these jars be placed in the same directory as the script.
This README describes how to do this.
Afterwards, the artifact directory should looke like this,
```
artifact/
├── bin
│   ├── acc
│   ├── alpha.glue.v1.jar
│   └── alpha.glue.v2.jar
└── scripts
    └── patch-v2-jar.sh
```

# Export JAR files

Follow these steps to create jar files for each of the bundles.
Some 

1. In Eclipse, import the existing projects `alpha.glue.v1` and `alpha.glue.v2` from bundles into the workspace.
1. Create a Run Configuration for `alpha.glue.v1.GenerateOldSupportingC`. This can be done implicitly by right clicking the `alpha.glue.v1/src/alpha.glue.v1/GenerateSupportingC.xtend` file > Run As > Java Application.
1. Create another Run Configuration for `alpha.glue.v2.GenerateNewWriteC`.
1. Right click the `alpha.glue.v1` package > Export ... > Java > Runnable JAR File > specify the "Launch configuration" from step 2, specify the "Export destination" as `artifact/bin/alpha.glue.v1.jar`, and select the option to extract required libraries into generated JAR.
1. Right click the `alpha.glue.v2` package > Export ... > Java > Runnable JAR File > specify the "Launch configuration" from step 2, specify the "Export destination" as `artifact/bin/alpha.glue.v2.jar`, and select the option to extract required libraries into generated JAR.
1. From a terminal, change to the `artifact` directory and run the following command to patch the v2 jar: `./scripts/patch-v2-jar.sh bin/alpha.glue.v2.jar`

Eclipse may complain that exporting the jar fails but still creates the jar on the file system, but at long as the jar file exists this can be ignored.
If everything worked, then you should be able to run the jars from a terminal and see the default usage messages,
```
$ java -cp artifact/bin/alpha.glue.v2.jar alpha.glue.v2.GenerateNewWriteC
usage: alpha_v2_file out_dir

$ java -cp artifact/bin/alpha.glue.v1.jar alpha.glue.v1.GenerateOldSupportingC
usage: alpha_v1_file out_dir
```

# Run code generation

Given a simple Alpha file,
```
$ cat tmp/test.alpha
affine prefix_sum [N] -> {: 10<N}
	inputs X : [N]
	outputs	Y : [N]
	let	Y = reduce(+, (i,j->i), {: 0<=j<=i} : X[j]);
.
```

Run the aac script as follows to generate and compile everything in a new directory call "out",
```
$ ./artifact/bin/acc test.alpha out
[acc]: reading 'test.alpha' file
[acc]: created 'out/prefix_sum.ab' file
[acc]: created 'out/prefix_sum.c' file
[acc]: reading 'out/prefix_sum.ab' file
[CLooG] INFO: 1 dimensions (over 2) are scalar.
[CLooG] INFO: 1 dimensions (over 3) are scalar.
[CLooG] INFO: 1 dimensions (over 2) are scalar.
[CLooG] INFO: 1 dimensions (over 2) are scalar.
[CLooG] INFO: 1 dimensions (over 2) are scalar.
[acc]: created 'out/prefix_sum-wrapper.c' file
[acc]: created 'out/prefix_sum_verify.c' file
[acc]: created 'out/Makefile' file
[acc]: building with make
cc prefix_sum.c -o prefix_sum.o -O3  -std=c99  -I/usr/include/malloc/ -lm -c
clang: warning: -lm: 'linker' input unused [-Wunused-command-line-argument]
cc prefix_sum-wrapper.c -o prefix_sum prefix_sum.o  -O3  -std=c99  -I/usr/include/malloc/ -lm
cc prefix_sum-wrapper.c -o prefix_sum.check prefix_sum.o  -O3  -std=c99  -I/usr/include/malloc/ -lm -DCHECKING
cc prefix_sum_verify.c -o prefix_sum_verify.o -O3  -std=c99  -I/usr/include/malloc/ -lm -c
clang: warning: -lm: 'linker' input unused [-Wunused-command-line-argument]
cc prefix_sum-wrapper.c -o prefix_sum.verify prefix_sum.o  prefix_sum_verify.o -O3  -std=c99  -I/usr/include/malloc/ -lm -DVERIFY
cc prefix_sum-wrapper.c -o prefix_sum.verify-rand prefix_sum.o  prefix_sum_verify.o -O3  -std=c99  -I/usr/include/malloc/ -lm -DVERIFY -DRANDOM
```

The generated files, in the "out" directory, will look like this,
```
out/
├── Makefile
├── prefix_sum              <-- This is the main binary
├── prefix_sum-wrapper.c
├── prefix_sum.ab
├── prefix_sum.c
├── prefix_sum.check
├── prefix_sum.o
├── prefix_sum.verify       <-- Checks new and old WriteC outputs
├── prefix_sum.verify-rand  <-- Checks new and old WriteC outputs with random inputs
├── prefix_sum_verify.c
└── prefix_sum_verify.o
```

And the program can be run,
```
$ ./out/prefix_sum 30
Execution time : 0.000008 sec.

$ ./out/prefix_sum.verify-rand 30
Execution time : 0.000011 sec.
TEST for Y PASSED
```
