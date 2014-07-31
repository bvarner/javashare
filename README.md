javashare
=========

JavaShare is a BeShare compatible chat &amp; local file sharing application.

This was one of the first (larger?) java programs I'd ever written (started while I was still in school) and as such is dragging along legacy code / baggage from java in the heady days of Java 1.1.8 + swing.

Eventually I started moving things along, but the structure of the object model, event handling, etc. has been pretty much unaltered and untouched in all the intervening years. Please excuse the poor quality of this code.

I found a CD with an archive of the codebase (from a few releases prior to the last 'public' release I made years ago) and figured I'd update some of it and toss it out here on github (I was actually asked to do this, and thought it might be fun).

The code herein has a rather nasty NullPointerException during startup, which is a consequence of a terrible object model which passing object references around during constructor invocations. (I did mention this was code produced *very early* in my OOP days, right?

At some point, I put some time into fixing that, but I cannot find that code (yet), so here's what I have.

I recall having this compile / run on MacOS Classic, OS/2, BeOS (yes, with the unfinished 1.4 Java port), Windows, Linux, and a few other obscure OS's back in the day.

Enjoy!
