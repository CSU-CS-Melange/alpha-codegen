# alpha-codegen

This repo contains scripts that can be used to generate and compile simple demand driven C code from input Alpha programs.
There are currently two versions of AlphaZ ("alphaz" [v1](https://github.com/CSU-CS-Melange/AlphaZ) and "alpha-language" [v2](https://github.com/CSU-CS-Melange/alpha-language)) included here as submodules.
This repository contains two bundles, one for each, which wrap and expose the respective codegen entry points.

## Alpha to C Compiler (acc)

The `acc` utility glues together the codegen functionality from both versions and has the following usage,
```
$ acc
usage: acc alpha_file [output_directory]
parameters:
     alpha_file       :  input Alpha source file
     output_directory :  directory in which to place the output files, if the path
                         does not exist then it will be created (default: .)
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
Alternatively, you can follow the steps in the following sections to build everything from source.


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
1. Right click the `alpha.glue.v1` package > Export ... > Java > Runnable JAR File > specify the "Launch configuration" from step 5, specify the "Export destination" as `artifact/bin/alpha.glue.v1.jar`, and select the option to extract required libraries into generated JAR.
1. Right click the `alpha.glue.v2` package > Export ... > Java > Runnable JAR File > specify the "Launch configuration" from step 6, specify the "Export destination" as `artifact/bin/alpha.glue.v2.jar`, and select the option to extract required libraries into generated JAR.

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

## Generate code

Given a single-system Alpha program such as the following,
```
// test.alpha
affine prefix_sum [N] -> {: 10<N}
	inputs X : [N]
	outputs	Y : [N]
	let	Y = reduce(+, (i,j->i), {: 0<=j<=i} : X[j]);
.
```

Run the `acc` script as follows to generate and compile everything in a new directory called `out`,
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

The generated files, in the `out` directory, will look like this,
```
out/
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
$ ./out/prefix_sum 30
Execution time : 0.000008 sec.

$ ./out/prefix_sum.verify-rand 30
Execution time : 0.000011 sec.
TEST for Y PASSED
```

The verification targets use the old WriteC.
This can be used to bug check the new code generator (assuming the same bug is not also present in the old one).
If verification reports "TEST ... PASSED", then it confirms that the new code generator produces a program that computes the same answer as the old code generator.
