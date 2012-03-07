How to build
============

## Requirement

Compile and install [ideacolorschemes-commons](https://github.com/iron9light/ideacolorschemes-commons) to your local maven repository.

## From IntelliJ IDEA

* git clone

* Open Project by IntelliJ. It will be opened as a Java project with Maven support.

* `git reset HEAD --hard`

* Reload project and Ignore maven module

* Set project SDK to your special IDEA SDK

* Build -> Make Project

## From Maven

* Change properties **idea.version** and **idea.home** to your special value.

* `mvn clean pachage` to generate Color-*.zip (under __target__ folder).