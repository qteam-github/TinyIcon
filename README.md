# **TinyIcon 0.90**

## What is TinyIcon?
TinyIcon is a very small java library that allows to load and decode the Microsoft Windows .ico files.

## Features
- Java 8 compatible
- Supports all image sizes (from 1x1 to 65535x32767 pixels)
- Supports all color depths (1, 4, 8, 24 and 32 bpp)
- Supports compressed icons
- Supports icon sorting
- Supports icon searching
- Reads favicons
- Allows to extract a subset of icons

## Requirements
- Java 8. The library uses lots of new java features such as lambdas, streams, predicates, etc.
- [*Optional*] Netbeans IDE if you want to compile the library itself

## Getting Started
First download the latest TinyIcon library from [here](https://github.com/qteam-github/TinyIcon/releases/download/v090/tinyicon_090.zip).<br/>

Unless you already have them, you will also need:
- The [Java Development Kit](http://www.oracle.com/technetwork/java/javase/downloads/index.html).<br/>
- A Java compatible IDE such as [Netbean IDE](https://netbeans.org/downloads/) (Recommended).<br/>

Then extract the TinyIcon library's .zip file to a folder and add the tinyicon.jar in your project.<br/>
Finally attach the TinyIcon javadoc and source archives to the TinyIcon library (*Optional, but highly recommended*).

## Usage
If you don't care about icon sorting, icon search or image info you can just get the first available image with only a line of code:
```java
final BufferedImage img = new TinyIcon ("pencil.ico").getImage (0);
```

If you want to get the biggest image available you can sort icons by image area, first:
```java
final TinyIcon ti = new TinyIcon ("pencil.ico");
ti.sortIcons (IconSort.BY_RESOLUTION_DESCENDING);

final BufferedImage img = ti.getImage (0);
```

or if you want to get a specific image (*be sure that image exists inside the icon, though*) you can use a condition:
```java
final TinyIcon ti = new TinyIcon ("pencil.ico");

// Get the first image with height >= 128 and bpp = 8
final BufferedImage img = ti.getImage (i -> ((i.getHeight () >= 128) && (i.getBpp () == 8)));
```

For more examples see the javadoc [basic usage](https://rawgit.com/qteam-github/TinyIcon/master/javadoc/org/qteam/tinyicon/TinyIcon.html#basic_usage) description.

## Documentation
Online javadoc is available [here](https://rawgit.com/qteam-github/TinyIcon/master/javadoc/index.html).<br/>
Offline javadoc is already included in the [release zip](https://github.com/qteam-github/TinyIcon/releases/download/v090/tinyicon_090.zip).

## How to compile?
Usually you just need to download the final jar, but if you wish to compile the library itself (for example if you want to contribute with a pull request), you only need the NetBeans IDE and to clone this repository.<br/>

To compile (*Clean and Build* action) just press '*SHIFT + F11*' (No other external libraries are required).<br/>

The only thing to care about is the [tinyicon.release.dir](https://github.com/qteam-github/TinyIcon/blob/master/build.xml#L16) variable in build.xml. This variable is used to copy, with an *ant task*, all release files into a destination directory and its default value is set to ```d:/javalibs/tinyicon```.<br/>
The output directory is automatically created (even if it doesn't exist), but if you don't have a "*D:/*" drive, an error will occur after compiling, so you need to change the value to an existent drive (e.g. "*C:/*") and a *writable* folder.
