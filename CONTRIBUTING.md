# How to contribute

First, thanks for participating!
When submitting a pull request please follow the existing formatting and guidelines.
This project loosely follows the
[Google Java Style](https://google.github.io/styleguide/javaguide.html) with the
exception of 4 spaces per indentation instead of 2.

## How to build

It should be as simple as `mvn install`

## How to release

The release process is automated with the maven release plugin.
However, we do not use maven to publish.
So, to create a new release:
```
mvn release:prepare -Dresume=false
git push --follow-tags
jarsigner pyramidio-cli/target/pyramidio-cli-<version>.jar pyramidio -tsa http://tsa.startssl.com/rfc3161
```
The last command expects the existence of a pyramidio alias in the keystore.
See http://introcs.cs.princeton.edu/java/85application/jar/sign.html for details on how to create one.


Once signed, the jar should be uploaded on the GitHub release page.
