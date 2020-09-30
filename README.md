# Anime Downloader

A graphical application written in Java for download anime from common
anime sites.

#### Why

I've create this application for be able to download video from different sites 
without care about annoying popups, advertisement or similar stuff. 

Moreover this downloader remembers the added video still to download even 
after its closure; this allows to add a "wish list" of video to downloader 
in the future.

Last but not least, just because I had nothing better to do =) 

#### Supported OS

This works for sure on Linux and probably even on Windows and MacOS, but I
do not even want to try those OS, do it on your own ;D

#### Supported sites

* VVVVID (https://www.vvvvid.it)
* Openload (https://openload.co)
* Streamango (https://streamango.com)
* Verystream (https://verystream.com)
* Midrop (https://mixdrop.co)

#### Partially supported sites

* Youtube (https://youtube.com)  
(requires `youtube-dl`, progress and speed not detected)

#### What it looks like

![Main Window](img/anime-downloader-main-window.png)

![Settings](img/anime-downloader-settings.png)

#### Nice, i'm gonna try it!

Just go into github release section and download the .jar file.

Then execute it just like any other java program.

```
java -jar animedownloader.jar
```

Before download video, satisfy the following steps:
* Install google chrome
* Download the right version of [chromedriver](http://chromedriver.chromium.org/downloads) that matches your google chrome version
* Set the path to the chromedriver in the application's settings

---

### Develop


#### Compile

Build a fat jar (bundled with JavaFX 11, which is excluded from JDK 11) via:

```
gradle shadowJar
```

The output will be placed under `build/libs`.