#!/usr/bin/env python
# -*- coding: utf-8 -*-

import re
import os

def mylen(s):
    return(len(s))

def makeurl(title):
    url = re.sub(" ", "-", title)
    url = re.sub("[(]", "-", url)
    url = re.sub("[)]", "-", url)
    return url

INDOC = False

def read(file_name):
    global INDOC

    old_file_name = file_name + ".bck"
    new_file_name = file_name + ".new"

    toc = []

    with open(file_name, 'rb') as f:
        for line in f:
            line = line.strip().decode('utf-8')
            if re.match("[#]+[ ].*", line):
                m = re.match("(?P<level>[#]+)[ ]+(?P<title>[^<]*)([<]a[ ]name[=])*", line)
                head = m.group("level")
                level = mylen(head)
                title = m.group('title').strip()
                url = makeurl(title)
                ##print('{} {} <a name="{}"> </a>'.format(head, title, url))
                toc.append((title, level, url))
            else:
                pass
                # print(line)

    with open(new_file_name, "w") as g:
        with open(file_name, 'rb') as f:
            for line in f:
                line = line.strip().decode('utf-8')
                if INDOC:
                    if line == "<!-- endtoc -->":
                        INDOC = False
                    else:
                        continue

                if re.match("[#]+[ ].*", line):
                    m = re.match("(?P<level>[#]+)[ ]+(?P<title>[^<]*)([<]a[ ]name[=])*", line)
                    head = m.group("level")
                    level = mylen(head)
                    title = m.group('title').strip()
                    url = makeurl(title)
                    g.write('{} {} <a name="{}"> </a>\n'.format(head, title, url))
                elif line == "<!-- toc -->":
                    g.write(line + '\n')
                    for item in toc:
                        title = item[0]
                        level = item[1] - 1
                        url = item[2]

                        g.write("{}1. [{}]({})\n".format("   "*level, title, url))
                    INDOC = True
                else:
                    g.write(line + '\n')
    os.rename(file_name, old_file_name)
    os.rename(new_file_name, file_name)

if __name__ == '__main__':
    toc = read("README.md")


