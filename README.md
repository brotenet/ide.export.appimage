# ide.export.appimage
Eclipse plug-in for exporting a Linux AppImage package of a desktop-application project.

Important Note: Currently the pluin generates x64 only AppImages that do not bundle JRE runtimes. In order to execute a generated AppImage, a JRE is required to be installed on the system and the OS architecture must be x64. JRE bundling will come at a later stage. I doubt thoug if 32bit support will be a feature of the plugin since almost all Linux distributions are dropping their 32bit builds.

Features:
* Uses ANT and can also export the build.xml script for the application.

Binary Download:  <a href="ide.export.appimage_1.0.0.201805122053.jar" download>ide.export.appimage_1.0.0.201805122053.jar</a>

Installation: Place the ide.export.appimage_...jar file in Eclipse plugins directory

![img1](readme_resources/img1.png?raw=true "")
![img2](readme_resources/img2.png?raw=true "")
![img3](readme_resources/img3.png?raw=true "")