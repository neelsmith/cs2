# `customcts`


## What's here

In this directory, you can run or build a servlet with your own customized suite of CITE services.

Add any content you like to the `src/main/webapp` directory, and when you use any of the gretty plugin's tasks, the contents of the `cs2` servlet will be folded in with your custom content.  Your custom content has priority, so if you want to redefine something in `cs2`'s standard installation, you can do so.

gretty tasks include:

- `gradle appRun`: runs your servlet
- `gradle assemble`: builds a `.war` file you can add to your own servlet container

**N.b.: It is not `gradle war`, but `gradle assemble` that builds the correct `.war` file!!**



## Configuration

The build system supports filtering content before running or building a `.war` file.
