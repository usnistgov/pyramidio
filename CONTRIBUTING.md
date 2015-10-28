# How to contribute

First, thanks for participating!
When submitting a pull request please follow the existing formatting and guidelines.
This project loosely follows the
[Google Java Style](https://google.github.io/styleguide/javaguide.html) with the
exception of 4 spaces per indentation instead of 2.

## How to build

It should be as simple as `mvn install`

## How to release

The release processed is automated with the maven release plugin.
However, we do not use maven to publish.
So, to create a new release:
```
mvn release:prepare -Dresume=false
git push --follow-tags
```
Travis will then take care of uploading the artifacts to the github release page.
