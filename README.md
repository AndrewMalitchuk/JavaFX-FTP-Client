# JavaFX FTP Client

JavaFX FTP Client is simple FTP client, which was written in Java, with using [Apache Commons Net](https://commons.apache.org/proper/commons-net/) library.

## Overview

JavaFX FTP Client provides all base FTP operation on remote FTP server. Application's GUI have something in common with Total/Double/Norton Commander, means it has two panels for displaying local and remote file system.

![Demo](https://github.com/AndrewMalitchuk/ftp-client/blob/master/README/1.png "Demo")

**Application demonstration:**

![Demo GIF](https://github.com/AndrewMalitchuk/ftp-client/blob/master/README/1.gif "Demo GIF")

This project was created for improving programming skills and for discovering something new.

## Features

* Uploading files to FTP server.
* Downloading files to local machine.
* Creating folders in both file systems.
* Renaming files in both file systems.
* Deleting files in both file systems.
* Copping files in two directions.
* Sending FTP command.
* Receiving general information about FTP server.

## Installation

**Option 1:**

* Download [JAR file](https://github.com/AndrewMalitchuk/JavaFX-FTP-Client/blob/master/target/JavaFX_FTPClient-1.0.jar) that was complile by me. 
Current JAR-file was compile on machine with ``1.8.0_181`` Java version on a board.

**Option 2:**

* Clone git repository and build it by yourself.
```console
    git clone https://github.com/AndrewMalitchuk/JavaFX-FTP-Client.git
    cd JavaFX-FTP-Client
    mvn install
    cd target
    java -jar JavaFX_FTPClient-1.0.jar
```

## Documenctation

Here is a [JavaDoc link.](https://andrewmalitchuk.github.io/JavaFX-FTP-Client/)

## System requirment

Java version required: ``JDK 8`` and latest.

## Contributing

Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.

Please make sure to update tests as appropriate.

## License

JavaFX FTP Client under the [Apache License, version 2.0](https://github.com/AndrewMalitchuk/JavaFX-FTP-Client/blob/master/LICENSE)

	Copyright (C) 2019 Andrew Malitchuk
	
	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at
	
	    http://www.apache.org/licenses/LICENSE-2.0
	
	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
