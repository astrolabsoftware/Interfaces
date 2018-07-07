
# toc.py: a TOC generator for the *README.md* file

This little python tool generates a "Table Of Contents" (TOC) section within your *README.md* file.

## Principle

**toc.py**

- operates only onto the *README.md* file
- detects all header lines (ie. lines starting with the RE pattern: ```[#]+[ ]```)
- detect a section within the *README.md* file enclosed within: ```<!-- toc -->``` and ```<!-- endtoc -->```
- includes the generated TOC inside this section if it is exists
- saves a copy of the *README.md* into *README.md.bck*
- creates a new version of *README.md*

## Usage

```
> cd   (where README.md is)
> vi README.md
...
<!-- toc -->
<!-- endtoc -->
...
>
> python3 toc.py
```

Note that, applying again toc.py will update the TOC according the changes made to README.md

