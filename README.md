# MonikaBot

A Discord bot that does... Whatever things I want ~~her~~ it to do.

## Getting Started

These instructions will set the project up and run the application on your local machine.

### Prerequisites

- [Java SE Development Kit 8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)

You will also need a [Discord](https://discordapp.com/) account, and a [Discord Application](https://discordapp.com/developers/applications/me) already setup.

### Compiling and Running

This step assumes that you have already downloaded a copy of MonikaBot's source code.

In `src/main/resources/properties`, add a file named `source.properties`.

```sh
cd <project_root>
touch src/main/resources/properties/source.properties
```

Add the following information into the file.

```sh
adminId=<id_of_your_account>
privateKey=<private_key_of_bot>
```

Additionally, you may add `suId` property to allow other users to become a superuser, and `debugChannelId` to specify a channel to dump debug information.

Finally, run Gradle to get started (use `gradle.bat` if on Windows).

```sh
./gradle run
```

### Distributing the Binary

Currently there are no mechanisms to distribute the binary.

## Usage

For an exhaustive list of command, send the following command to Monika:
```
help
```

Note that if the channel is a server channel, you must mention Monika's Discord tag:
```
@MonikaBot help
```

## Versioning

[SemVer](http://semver.org/) is used for all releases. For all releases, see 
[tags on this repository](https://github.com/Derppening/monikabot/releases).

## Authors

* **David Mak** - [Derppening](https://github.com/Derppening)

## License

This project is licensed under GPLv3 license - see [COPYING](LICENSE) for details.

This project uses the [Discord4J](https://github.com/austinv11/Discord4J) interface, which is licensed under 
[LGPLv3](https://github.com/austinv11/Discord4J/blob/master/LICENSE.txt) license.
