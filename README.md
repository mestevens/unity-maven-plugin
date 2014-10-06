# unity-maven-plugin

## Latest Version
0.1

## Description

A maven plugin that will be used to build unity projects

## Pom.xml information

In order to use this plugin, add the following to your pom.xml

```
<plugin>
	<groupId>ca.mestevens.unity</groupId>
	<artifactId>unity-maven-plugin</artifactId>
	<version>${xcode.maven.plugin.version}</version>
	<extensions>true</extensions>
</plugin>
```

where `${xcode.maven.plugin}` is the version of the plugin you want to use.

## Goals

### unity-android-build

Will build your unity project as a google android studio project in your target directory, as well as create a pom with any jar or aar dependencies you had in the unity pom.