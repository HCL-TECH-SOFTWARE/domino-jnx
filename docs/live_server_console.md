---
layout: default
title: Server Console API
nav_order: 008
parent: Overview
---
# Live Server Console Access

The `#openServerConsole` method in `ServerAdmin` allows for live access to server console streams, similar to Administrator. There are some caveats involved, though:

## False XML

The "data" string in the `IConsoleLine` object is not actually XML: if the text portion contains "<" or ">", they are not escaped. Therefore, it has to be parsed by looking for the initial "<" and ">" that wrap the "ct" element, then specifically for "</ct>" followed by "\n"+.

Note: the positions and sequence of line-break characters vary wildly on a single server and between servers. To wit:

* An 11.0.1 server on Win64 separates `<ct>` elements with `\n\r\n`, while a 10.0.1FP2 server splits elements with just `\n`
* In both cases, the data string itself ends with a final `\n`, regardless of whether or not the message is complete (see below)
* On the Windows server, individual lines have no line breaks within the `<ct>` element; on Linux, they contain a `\r\n` before the closing `</ct>` for some reason

One particular implication of this is that it's difficult or impossible to handle the case where the text of the line from the server contains `</ct>` followed by newlines. For example, monitoring a server and then running these commands from another connection...

```
set config foo="</ct>"
show config foo
```

...will result in this (C-escaped) output from a Linux-based server:

```
<ct sq=\"000153D9\" ti=\"007BA55A-8525868F\" ex=\"server\" pi=\"001124\" tr=\"000011-00007F8CFF521700\" co=\"7\">[001124:000011-00007F8CFF521700] FOO=</ct>\r\n</ct>\n
```

...and this from Windows:

```
\r\n<ct sq=\"0000334F\" ti=\"007C1659-8525868F\" ex=\"nserver\" pi=\"1874\" tr=\"0009-125C\" co=\"7\">[1874:0009-125C] FOO=</ct></ct>\n\r\n\n
```

So the safest route appears to be to parse the data by looking for "<ct" followed by "</ct>\n" specifically.

On this point, we can take heart in the fact that Administrator appears to parse the Linux-sent version incorrectly, displaying `FOO=` as the value.

## Object Fragments

The "data" string in the `IConsoleLine` object provided to the handler is an XML string composed of one or more `ct` objects with line information. When the data sent to the handler is too large to fit inside the small buffer, such as showing the results of a `sh ta` command, the string will be sent in multiple sequential calls. There appears to be no signal of this other than the malformed data.

The data value of `IConsoleLine` always ends in a `\n`, which should be chopped off when concatenating such fragments.

## Speed Limit

When I tested running `tell http xsp help` on the server console directly, the console handler stops receiving updates after some time. This is consistent with what happens when connecting to the live console with Administrator, so it's a limitation in the underlying API and not JNX.