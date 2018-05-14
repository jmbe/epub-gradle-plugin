# EPUB plugin for Gradle

Assembles and validates EPUB books.

## Setup

### Directory structure

It is assumed that books are held in sub-directories of the root project.

    ├── book1
    │   ├── EPUB
    │   ├── META-INF
    │   └── mimetype
    ├── book2
    │   ├── EPUB
    │   ├── META-INF
    │   └── mimetype
    ├── ...
    ├── build.gradle
    └── settings.gradle

Create build.gradle with the following contents:

    buildscript {
        repositories {
            mavenLocal()
            maven { url "https://jitpack.io" }
        }
    
        dependencies {
            classpath "com.github.jmbe:epub-gradle-plugin:master"
        }
    }
    
    subprojects {
        apply plugin: "se.intem.epub"
    }

Add names of books to settings.gradle:

    include "book1"
    include "book2"
    ...


## Running

| | Command |
| --- | --- |
| Build once | **gradle epub** | 
| Watch mode | **gradle epub --continuous** |

## Configuration

Configuration options can be passed to the plugin:

    subprojects {
        apply plugin: "se.intem.epub"
    
        epub {
            validate true
            failOnWarnings true
            failOnErrors true
        }
    }

| Option | Description | Default |
| --- | --- | --- |
| validate | Validate EPUB using EpubCheck | true |
| failOnWarnings | Fail build on validation warnings | true |
| failOnErrors | Fail build on validation errors | true |
