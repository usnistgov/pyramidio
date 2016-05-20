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
All release components are kept in a `release` profile, so remember to use maven with the `-P=release` option.
```
mvn -P=release release:clean release:prepare
#Automatic push is disabled, so do it manually
git push --follow-tags
mvn -P=release release:perform
```
The last command will release the new version on maven central.
For convenience, the `pyramidio-cli` jar should be uploaded to the GitHub release page as well.
