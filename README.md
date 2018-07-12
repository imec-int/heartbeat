# libHeartbeat

Heartbeat audio signal synthesis tool, written in "pure" Java.
 
Based on from [Ben Holmes](https://github.com/bencholmes)' [MATLAB implementation](https://github.com/bencholmes/heartbeat). Ported to Java (>= v1.7) by [Matthias Stevens](https://github.com/mstevens83) for [IMEC APT](https://github.com/imec-apt).
Like Ben's original this code is released to the public domain ([CC0-v1.0 "license"](https://creativecommons.org/publicdomain/zero/1.0/)), except for the classes in the `be.imec.apt.heartbeat.utils` package (see below).

## libHeartbeat uses:
- [Bernd Porr's iirj library](https://github.com/berndporr/iirj), as an external Maven dependency;
- a small portion of [Christian d'Heureuse's Java DSP collection](http://www.source-code.biz/dsp/java): 2 modified source files in published here (see `be.imec.apt.heartbeat.utils` package), EPLv 1.0 &amp; LGPL v2.1 apply;
 - a small portion of [Phil Burk's Java audio synthesizer library](https://github.com/philburk/jsyn): 1 modified source file published here (see `be.imec.apt.heartbeat.utils` package), Apache License v2.0 applies.
 
## Using this library
The easiest way to use this library in your Java or Android project is to rely on [JitPack.io](https://jitpack.io). In your `build.gradle` file:
- add `maven { url 'https://jitpack.io' }` to the list of `repositories`;
- and add `implementation 'com.github.imec-apt:heartbeat:1.0.0'` to the `dependencies`.

## Special thanks to:
 - [Ben Holmes](https://github.com/bencholmes), for creating of the MATLAB original and helping me port it;
 - and [Joren Six (IPEM, Ghent University)](https://github.com/JorenSix) for advising me.
