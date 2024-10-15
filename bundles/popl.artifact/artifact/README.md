# max-simplification-artifact

This repository contains code samples that complement the POPL 2025 conference paper titled, "Maximal Simplification of Polyhedral Reductions".

## References

Title: Maximal Simplification of Polyhedral Reductions

Authors: Louis Narmour, Tomofumi Yuki, Sanjay Rajopadhye

[Archived Artifact](https://doi.org/10.5281/zenodo.13909241)

[Artifact Submission Guidelines](https://github.com/mlcommons/ck/blob/master/docs/artifact-evaluation/submission.md)

## Artifact check-list

* **Compilation:** AlphaZ compiler (provided as java JAR files) and make with gcc
* **Binary:** To be produced on target machine
* **Execution:** Automated via provided command line script
* **Experiments:** Asymptotic complexity verification of generated codes
* **How much disk space required (approximately)?:** ~300 MB
* **How much memory required (approximately)?:** ~512 MB
* **How much time is needed to prepare workflow (approximately)?:** <20 min (to install system dependencies if necessary)
* **How much time is needed to complete experiments (approximately)?:** <20min (to generate codes and run execution scripts)
* **Publicly available?:** Yes
* **Code licenses (if publicly available)?:** MIT License

## Description

The (A)lpha to (C) (C)ompiler, `acc`, script can be used to run the simplification algorithm and generate simple demand driven C code for an input Alpha program. It has the following usage:
```
usage:
  acc [-m] [-o out_dir] [-l DOMAIN]
      [-s [--target-complexity] [--num-simplifications] [--try-splitting]]
      [ALPHA_FILE]
options:
    -o,  --out-dir             : Directory in which to place the output files, if the
                                 path does not exist then it will be created (default: .)
    -d,  --data-type           : Data type to use for program variables, may be 'int',
                                 'long', 'float', or 'double' (default: 'float')
    -m,  --make                : Run make to build the generated files. Note, this may
                                 fail if both v1 and v2 files have not been generated.
                                 Cannot be used with -s since multiple versions may be
                                 generated. (default: false)
    -l,  --face-lattice        : Build and show the face lattice of input DOMAIN. Input
                                 domain should be an isl basic set string.
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
                                 wrapper, and verification files.
...
```

## Face Lattice

Given a convex domain expressed as an [isl](https://libisl.sourceforge.io/manual.pdf) basic set, the `-l` option can be used to build the corresponding face lattice.
For example, using 


## Equivalence Classes


## Candidate Splits