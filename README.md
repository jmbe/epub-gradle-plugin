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

Create `build.gradle` with the following contents:

    buildscript {
        repositories {
            mavenLocal()
            maven { url "https://jitpack.io" }
        }
    
        dependencies {
            classpath "com.github.jmbe:epub-gradle-plugin:0.6"
        }
    }

    plugins {
        id "base"
    }
    
    subprojects {
        apply plugin: "se.intem.epub"
    }

Add names of books to `settings.gradle`:

    include "book1"
    include "book2"
    ...


## Using

| | Command |
| --- | --- |
| Build once | `gradle epub` | 
| Watch mode | `gradle epub --continuous` |
| Create ncx file | `gradle ncx` |
| Rebuild | `gradle clean epub` |

## Configuration

Configuration options can be passed to the plugin:

    subprojects {
        apply plugin: "se.intem.epub"
        
        ncx {
            ncxFile = file("EPUB/toc.ncx")
        }
    
        epub {
            validate true
            failOnWarnings true
            failOnErrors true
        }
    }

### Epub task

| Option | Description | Default |
| --- | --- | --- |
| validate | Validate EPUB using [EpubCheck](https://github.com/IDPF/epubcheck/) | true |
| failOnWarnings | Fail build on validation warnings | true |
| failOnErrors | Fail build on validation errors | true |

### Ncx task

| Option | Description | Default |
| --- | --- | --- |
| ncxFile | Ncx output file | EPUB/toc.ncx |
| navFile | Navigation document input file | EPUB/nav.xhtml |

These files will be determined from package document if *not* provided as parameter (recommended).
