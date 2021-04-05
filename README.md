# Leviathan
Kotlin based service locator for easy DI implementation

##Advantages:

Code generation - NOPE, there is nothing to generate

Runtime crushes - NOPE (unless you want it), you provide a builders so it is impossible to get runtime class cast exception

Usage of 'instanceOf', 'is', 'as' - A bit... for tagged instance to check if you trying to cheat and put 2 diff classes for same tag

Slowdown build - NOPE, no code generation means no need additional time to compile nor build time checks 

Slowdown performance - NOPE, there is no checks for tables, searching by class or so. It means it works FAST

A lot of annotations - NOPE, there is no annotations needed

Multi module - YES, hey, it is just a classes, you can inherit em or make em nested, make em delegate, do what tou want

Transparency - YES YES YES YES, you are the owner of code, do modules and provider as you want, you are the king, nothing is hidden

Simple - ALMOST, you need to know how delegated work, BUT, it is pretty easy.