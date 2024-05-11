# alpha-codegen

There are currently two versions of AlphaZ:
* old version (alphaz)
* new version (alpha-language)
This repository is a temporary solution for generating demand driven code in the new version.
Currently, the new version has a demand driven code generator but it only generates the system file, not the makefile and the wrapper.

The new and old versions can not be loaded into and called from the same package due to conflicting dependencies. For this reason, there are two separate packages:
* alpha.glue.v1 
* alpha.glue.v2 
