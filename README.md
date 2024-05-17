# alpha-codegen

This repo contains a utility that can be used to generate and compile simple demand driven C code from input Alpha programs.
Code generation in AlphaZ is currently split across two versions ("alphaz" [v1](https://github.com/CSU-CS-Melange/AlphaZ) and "alpha-language" [v2](https://github.com/CSU-CS-Melange/alpha-language)) referenced here as submodules.
This repo contains two bundles, one for each, which wrap and expose the respective code generation entry points.

## Alpha to C Compiler (acc)

The `acc` utility glues together the codegen functionality from both versions and has the following usage,
```
$ ./artifact/bin/acc --help
usage:
  acc [-m] [-v1 alpha_file] [-v2 alpha_file] [-o out_dir]
      [-s [--target-complexity] [--num-simplifications] [--try-splitting]]
      [ALPHA_FILE]
options:
    -v1, --gen-v1-from         : Input Alpha file (*.alpha) used to generate makefile,
                                 wrapper, and verification files
    -v2, --gen-v2-from         : Input Alpha file (*.alpha) used to generate main system
    -o,  --out-dir             : Directory in which to place the output files, if the
                                 path does not exist then it will be created (default: .)
    -d,  --data-type           : Data type to use for program variables, may be 'int',
                                 'long', 'float', or 'double' (default: 'float')
    -m,  --make                : Run make to build the generated files. Note, this may
                                 fail if both v1 and v2 files have not been generated.
                                 Cannot be used with -s since multiple versions may be
                                 generated. (default: false)
    -s,  --simplify            : Run the simplifying reductions algorithm on v2 input
                                 (default: false)
         --num-simplifications : Stop after a number of simplifications is found, or the
                                 exploration terminates, whichever comes first (default: 1)
         --target-complexity   : Target simplified complexity (default: one less than
                                 the input program's complexity)
         --try-splitting       : Consider splits during simplification (default: false)
    -v,  --verbose             : Emit debug information during simplification exploration
arguments:
    ALPHA_FILE                 : Input Alpha file used to generate main ystem, makefile,
                                 wrapper, and verification files. (required if neither 
                                 -v1 nor -v2 is specified)
```

# Installation

You can install the stand alone utility from the [latest release](https://github.com/CSU-CS-Melange/alpha-codegen/releases/latest).
Download the `acc-installer.sh.tgz` asset, extract it, and run the `acc-installer.sh` script.
```
$ tar -xf acc-installer.sh.tgz
$ ./acc-installer.sh 
Verifying archive integrity...  100%   MD5 checksums are OK. All good.
Uncompressing Alpha to C Compiler (acc)  100%  

Additional steps may be required. The (A)lpha to (C) (C)ompiler has been
installed to the following location:

    $HOME/.acc/bin

You may wish to add this to your path with the following command:

    export PATH=$PATH:$HOME/.acc/bin
```
This will install the `acc` utility in your home directory.
You may optionally wish to add this to your shell's PATH.
Note, this depends on java and your `JAVA_HOME` environment variable must point to a jdk of at least version 11.
Alternatively, you can follow the steps below to build everything from source.


# Usage

Take a single-system Alpha program such as the following,
```
// test.alpha
affine prefix_sum [N] -> {: 10<N}
	inputs  X : [N]
	outputs Y : [N]
	let     Y = reduce(+, (i,j->i), {: 0<=j<=i} : X[j]);
.
```

## Generate demand driven code with no optimization

Run the `acc` script as follows to generate C code for the `test.alpha` program above,
```
$ acc -o out-1 test.alpha 
[acc]: reading 'test.alpha' file
[acc]: created 'out-1/prefix_sum.c' file
[acc]: reading 'test.alpha' file
[acc]: created 'out-1/prefix_sum.ab' file
[acc]: reading 'out-1/prefix_sum.ab' file
[CLooG] INFO: 1 dimensions (over 2) are scalar.
[CLooG] INFO: 1 dimensions (over 3) are scalar.
[CLooG] INFO: 1 dimensions (over 2) are scalar.
[CLooG] INFO: 1 dimensions (over 2) are scalar.
[CLooG] INFO: 1 dimensions (over 2) are scalar.
[acc]: created 'out-1/prefix_sum-wrapper.c' file
[acc]: created 'out-1/prefix_sum_verify.c' file
[acc]: created 'out-1/Makefile' file
```

A new directory called `out-1` will be created and will look like this,
```
out-1
├── Makefile
├── prefix_sum-wrapper.c
├── prefix_sum.ab
├── prefix_sum.c
└── prefix_sum_verify.c
```
Compile with make,
```
$ make -C out-1
cc prefix_sum.c -o prefix_sum.o -O3  -std=c99  -I/usr/include/malloc/ -lm -c
clang: warning: -lm: 'linker' input unused [-Wunused-command-line-argument]
cc prefix_sum-wrapper.c -o prefix_sum prefix_sum.o  -O3  -std=c99  -I/usr/include/malloc/ -lm
cc prefix_sum-wrapper.c -o prefix_sum.check prefix_sum.o  -O3  -std=c99  -I/usr/include/malloc/ -lm -DCHECKING
cc prefix_sum_verify.c -o prefix_sum_verify.o -O3  -std=c99  -I/usr/include/malloc/ -lm -c
clang: warning: -lm: 'linker' input unused [-Wunused-command-line-argument]
cc prefix_sum-wrapper.c -o prefix_sum.verify prefix_sum.o  prefix_sum_verify.o -O3  -std=c99  -I/usr/include/malloc/ -lm -DVERIFY
cc prefix_sum-wrapper.c -o prefix_sum.verify-rand prefix_sum.o  prefix_sum_verify.o -O3  -std=c99  -I/usr/include/malloc/ -lm -DVERIFY -DRANDOM
```
This will produce several executables,
```
out-1
├── Makefile
├── prefix_sum              <-- This is the main binary
├── prefix_sum-wrapper.c
├── prefix_sum.ab
├── prefix_sum.c
├── prefix_sum.check        <-- Allows for manually checking output
├── prefix_sum.o
├── prefix_sum.verify       <-- Asserts v2 and v1 WriteC outputs are equivalent
├── prefix_sum.verify-rand
├── prefix_sum_verify.c
└── prefix_sum_verify.o
```

And the program can be run,
```
./out-1/prefix_sum 100
Execution time : 0.000014 sec.

./out-1/prefix_sum.verify-rand 100
Execution time : 0.000005 sec.
TEST for Y PASSED
```

The verification targets use the old WriteC code generator.
This can be used to bug check the new code generator (assuming the same bug is not also present in the old one).
If verification reports "TEST ... PASSED", then it confirms that the new code generator produces a program that computes the same answer as the old code generator.
This can also be used to sanity check that any optimized versions compute the same answer (i.e., remain semantically equivalent).

## Generate demand driven code with simplification

Take the same prefix sum program above and run the `acc` script but this time ask it to try to simplify the program by passing the `-s` option and ask it to report at most two simplifications with the `--num-simplifications 2` option,
```
$ acc -o out-2 -s --num-simplifications 10 test.alpha
[acc]: reading 'test.alpha' file
[alpha]: found simplification/v0/prefix_sum.alpha
[alpha]: found simplification/v1/prefix_sum.alpha
[acc]: created 'out-2/simplifications/v0/prefix_sum.c' file
[acc]: created 'out-2/simplifications/v0/prefix_sum.c' file
[acc]: reading 'test.alpha' file
[alpha]: found simplification/v0/prefix_sum.alpha
[alpha]: found simplification/v1/prefix_sum.alpha
[acc]: created 'out-2/prefix_sum.ab' file
[acc]: reading 'out-2/prefix_sum.ab' file
[CLooG] INFO: 1 dimensions (over 2) are scalar.
[CLooG] INFO: 1 dimensions (over 3) are scalar.
[CLooG] INFO: 1 dimensions (over 2) are scalar.
[CLooG] INFO: 1 dimensions (over 2) are scalar.
[CLooG] INFO: 1 dimensions (over 2) are scalar.
[acc]: created 'out-2/prefix_sum-wrapper.c' file
[acc]: created 'out-2/prefix_sum_verify.c' file
[acc]: created 'out-2/Makefile' file
```
Now the `out-2` directory looks like this,
```
out-2
├── Makefile
├── prefix_sum-wrapper.c
├── prefix_sum.ab
├── prefix_sum_verify.c
└── simplifications
    ├── v0
    │   ├── prefix_sum.alpha
    │   └── prefix_sum.c
    └── v1
        ├── prefix_sum.alpha
        └── prefix_sum.c
```
We see the two simplifications possible for the prefix sum.
The corresponding Alpha and C files are written to the `simplification` subdirectory of the output directory.

Now you can simply move the simplified `*.c` files to the top level directory, run make, and run the verification target,
```
$ cp out-2/simplifications/v0/prefix_sum.c out-2/
$ make -C out-2
cc prefix_sum.c -o prefix_sum.o -O3  -std=c99  -I/usr/include/malloc/ -lm -c
clang: warning: -lm: 'linker' input unused [-Wunused-command-line-argument]
cc prefix_sum-wrapper.c -o prefix_sum prefix_sum.o  -O3  -std=c99  -I/usr/include/malloc/ -lm
cc prefix_sum-wrapper.c -o prefix_sum.check prefix_sum.o  -O3  -std=c99  -I/usr/include/malloc/ -lm -DCHECKING
cc prefix_sum_verify.c -o prefix_sum_verify.o -O3  -std=c99  -I/usr/include/malloc/ -lm -c
clang: warning: -lm: 'linker' input unused [-Wunused-command-line-argument]
cc prefix_sum-wrapper.c -o prefix_sum.verify prefix_sum.o  prefix_sum_verify.o -O3  -std=c99  -I/usr/include/malloc/ -lm -DVERIFY
cc prefix_sum-wrapper.c -o prefix_sum.verify-rand prefix_sum.o  prefix_sum_verify.o -O3  -std=c99  -I/usr/include/malloc/ -lm -DVERIFY -DRANDOM

$ ./out-2/prefix_sum.verify-rand 100
Execution time : 0.000011 sec.
TEST for Y PASSED
```

The `*.verify-rand` executable compares the output of the simplified alpha program using the new v2 code generator to the output of the input (non-simplified) alpha program using the old v1 code generator.
As expected, they should produce the same answer.


# Build from source

Follow the steps below to build everything from source in a local Eclipse instance.
This script requires that each bundle be exported into its own jar file and that these jars be placed in the same directory as the script.
The following sections describe how to do this.


## Eclipse setup

You will need the relevant plugins installed for both the old and new Alpha versions.
Install Eclipse IDE for Java Developers. In the following, version [2022-06 (4.22)](https://archive.eclipse.org/eclipse/downloads/drops4/R-4.24-202206070700/) is assumed.

1. Launch eclipse and select a fresh workspace
1. ``Help -> Install new software``
   - Select "2022-06 - https://download.eclipse.org/releases/2022-06" as the repository to work with
   - Search for "Xtext" and select ``Xtext Complete SDK`` and install
   - Search for "Eclipse Modeling Framework" and select ``EMF - Eclipse Modeling Framework SDK`` and install
   (you may find two or more, but pick one - it shows up under multiple categories, but they are the same thing)
1. ``Help -> Install new software -> Manage...``
   - Click on ``Add...`` to add a new repository. Create entries for the following 7 locations:
       * Name: ``gecos framework``  
         Location: ``https://gecos.gitlabpages.inria.fr/gecos-framework/artifacts/``
       * Name: ``gecos emf tools``  
         Location: ``https://gecos.gitlabpages.inria.fr/gecos-tools/gecos-tools-emf/artifacts/``
       * Name: ``gecos graph tools``  
         Location: ``https://gecos.gitlabpages.inria.fr/gecos-tools/gecos-tools-graph/artifacts/``
       * Name: ``gecos isl``  
         Location: ``https://gecos.gitlabpages.inria.fr/gecos-tools/gecos-tools-isl/artifacts/``
       * Name: ``gecos jni mapper``  
         Location: ``https://gecos.gitlabpages.inria.fr/gecos-tools/gecos-tools-jnimapper/artifacts/``
       * Name: ``gecos tom mapping``  
         Location: ``https://gecos.gitlabpages.inria.fr/gecos-tools/gecos-tools-tommapping/artifacts/``
       * Name: ``gecos tom sdk``  
         Location: ``https://gecos.gitlabpages.inria.fr/gecos-tools/gecos-tools-tomsdk/artifacts/``
       * Name: ``alphaz``  
         Location: ``https://csu-cs-melange.github.io/AlphaZ/``
       * Name: ``alpha-language``  
         Location: ``https://csu-cs-melange.github.io/alpha-language/``
   - Click on ``Apply and Close``
   - Set "Work with:" to ``--All Available Sites--`` and filter on the string ``gecos`` to populate the list with artifacts from the locations that were just added
   - Select the following:
       * EMF Tools
       * Framework
       * Graph Tools
       * ISL
       * JNI Mapper
       * Tom Mapping
       * Tom SDK
       * Uncategorized (expanding this should show the "JNI Barvinok bindings")
   - Install all of the above 
   - Search for "groovy" and install `Eclipse Groovy Development Tools`
   - Search for "Alpha" and install both `Alpha Language` and `AlphaZ`


## Export JAR files

Follow these steps to create jar files for each of the bundles.

1. Clone this repo and its submodules,
   ```
   $ git clone https://github.com/csu-cs-melange/alpha-codegen && cd alpha-codegen
   $ git submodule init
   $ git submodule update
   ```
1. In Eclipse, import the following projects into the workspace
   - `bundles/alpha.glue.v1`
   - `bundles/alpha.glue.v2`
1. Import the following Alpha v1 projects
   - `alphaz/bundles/edu.csu.melange.alphaz.mde`
   - `alphaz/bundles/edu.csu.melange.jnimap.isl`
   - `alphaz/bundles/org.polymodel`
   - `alphaz/bundles/org.polymodel.isl`
   - `alphaz/bundles/org.polymodel.polyhedralIR`
   - `alphaz/bundles/org.polymodel.polyhedralIR.codegen`
1. Import the following Alpha v2 projects
   - `alpha-language/bundles/alpha.model`
   - `alpha-language/bundles/alpha.codegen`
1. Create a Run Configuration for `alpha.glue.v1.GenerateOldSupportingC`. This can be done implicitly by right clicking the `alpha.glue.v1/src/alpha.glue.v1/GenerateSupportingC.xtend` file > Run As > Java Application.
1. Create another Run Configuration for `alpha.glue.v2.GenerateNewWriteC`.
1. In the `alpha.glue.v1` package, right click the `export.xml` file and select "Run As > Ant Build" (with no "..." at the end).
1. In the `alpha.glue.v2` package, right click the `export.xml` file and select "Run As > Ant Build" (with no "..." at the end).

Eclipse may complain that exporting the jar fails but still creates the jar on the file system.
As long as the jar file exists this can be ignored.
If everything worked, the artifact directory should look like this,
```
artifact
├── bin
│   ├── acc
│   ├── alpha.glue.v1.jar
│   ├── alpha.glue.v2.jar
│   └── install-acc.sh
└── scripts
    └── upload-release-asset.sh
```
and you should be able to run the jars from a terminal and see the default usage messages,
```
$ java -jar artifact/bin/alpha.glue.v2.jar
usage: alpha_v2_file out_dir [choice]

$ java -jar artifact/bin/alpha.glue.v1.jar
usage: alpha_v1_file out_dir
```

