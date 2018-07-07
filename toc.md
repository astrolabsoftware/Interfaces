
# toc.py: a TOC generator

This little python tool generates a "Table Of Contents" (TOC) section within your README.md file.

## Principle

**toc.py**

- operates only onto the README.md file
- detects all header lines (ie. lines starting with the RE pattern: "[#]+[ ]")
- detect a section within the README.md file enclosed within: ```<!-- toc -->``` and ```<!-- endtoc -->```
- includes the generated TOC inside this section if it is exists
- saves a copy of the README.md into README.md.bck
- creates a new version of README.md

