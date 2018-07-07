#!/usr/bin/env python
# -*- coding: utf-8 -*-

import re

def mylen(s):
    return(len(s))


INDOC = False

def read(file_name):
    global INDOC

    toc = []

    with open(file_name, 'rb') as f:
        for line in f:
            line = line.strip().decode('utf-8')
            if re.match("[#]+[ ].*", line):
                m = re.match("(?P<level>[#]+)[ ]+(?P<title>[^<]*)([<]a[ ]name[=])*", line)
                head = m.group("level")
                level = mylen(head)
                title = m.group('title').strip()
                url = re.sub(" ", "-", title)
                ##print('{} {} <a name="{}"> </a>'.format(head, title, url))
                toc.append((title, level, url))
            else:
                pass
                # print(line)

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
                url = re.sub(" ", "-", title)
                print('{} {} <a name="{}"> </a>'.format(head, title, url))
            elif line == "<!-- toc -->":
                print(line)
                for item in toc:
                    title = item[0]
                    level = item[1] - 1
                    url = item[2]

                    print("  "*level, "1. [{}]({})".format(title, url))
                INDOC = True
            else:
                print(line)

    return toc

if __name__ == '__main__':
    toc = read("README.md")


