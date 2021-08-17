# Leviathan
Kotlin based service locator for easy DI implementation

## Advantages:

### All versions

Runtime crushes - NOPE (unless you want it), you provide a builders so it is impossible to get runtime class cast exception

Usage of 'instanceOf', 'is', 'as' - Nope... just NOPE

Slowdown performance - NOPE, there is no checks for tables, searching by class or so. It means it works FAST

Multi module - YES, hey, it is just a classes, you can inherit em or make em nested, make em delegate, do what tou want

Transparency - YES YES YES YES, you are the owner of code, do modules and provider as you want, you are the king, nothing is hidden

Simple - ALMOST, you need to know how delegated work, BUT, it is pretty easy.

### No code generation version:

Code generation - NOPE, there is nothing to generate

Slowdown build - NOPE, no code generation means no need additional time to compile nor build time checks 

A lot of annotations - NOPE, there is no annotations needed

### Inject version

Code generation - a bit... only Services classes u asked to make... thats it.. no helpers nor strange code

Slowdown build - not more than other libs.. i'd say even less... we only generate necessary and nothing more

A lot of annotations - NOPE... it's only 3!! Literally... 3 !! 
