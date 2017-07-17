# AeroGear Android Store

[![circle-ci](https://img.shields.io/circleci/project/github/aerogear/aerogear-android-store/master.svg)](https://circleci.com/gh/aerogear/aerogear-android-store)
[![License](https://img.shields.io/badge/-Apache%202.0-blue.svg)](https://opensource.org/s/Apache-2.0)
[![Maven Central](https://img.shields.io/maven-central/v/org.jboss.aerogear/aerogear-android-store.svg)](http://search.maven.org/#search%7Cga%7C1%7Caerogear-android-store)
[![Javadocs](http://www.javadoc.io/badge/org.jboss.aerogear/aerogear-android-store.svg?color=blue)](http://www.javadoc.io/doc/org.jboss.aerogear/aerogear-android-store)

## Store

AeroGear Android Store is a simple data storage API. This API is useful for caching responses, sharing data among different systems, or providing some form of limited offline support.

|                 | Project Info  |
| --------------- | ------------- |
| License:        | Apache License, Version 2.0  |
| Build:          | Maven  |
| Documentation:  | https://aerogear.org/android/  |
| Issue tracker:  | https://issues.jboss.org/browse/AGDROID  |
| Mailing lists:  | [aerogear-users](http://aerogear-users.1116366.n5.nabble.com/) ([subscribe](https://lists.jboss.org/mailman/listinfo/aerogear-users))  |
|                 | [aerogear-dev](http://aerogear-dev.1069024.n5.nabble.com/) ([subscribe](https://lists.jboss.org/mailman/listinfo/aerogear-dev)) 

## Usage

There are two supported ways of developing apps using AeroGear for Android: Android Studio and Maven.

### Android Studio

Add to your application's `build.gradle` file

```groovy
dependencies {
  compile 'org.jboss.aerogear:aerogear-android-store:3.1.0'
}
```

### Maven

Include the following dependencies in your project's `pom.xml`

```xml
<dependency>
  <groupId>org.jboss.aerogear</groupId>
  <artifactId>aerogear-android-store</artifactId>
  <version>3.1.0</version>
  <type>aar</type>
</dependency>
```

## Documentation

For more details about that please consult [our documentation](https://aerogear.org/android/).

## Demo apps

Take a look in our demo apps

* [CarStore](https://github.com/aerogear/aerogear-android-cookbook/blob/master/CarStore)
* [AeroDoc](https://github.com/aerogear/aerogear-android-cookbook/blob/master/AeroDoc)
* [PasswordManager](https://github.com/aerogear/aerogear-android-cookbook/tree/master/PasswordManager)

## Development

If you would like to help develop AeroGear you can join our [developer's mailing list](https://lists.jboss.org/mailman/listinfo/aerogear-dev), join #aerogear on Freenode, or shout at us on Twitter @aerogears.

Also takes some time and skim the [contributor guide](http://aerogear.org/docs/guides/Contributing/)

## Questions?

Join our [user mailing list](https://lists.jboss.org/mailman/listinfo/aerogear-users) for any questions or help! We really hope you enjoy app development with AeroGear!

## Found a bug?

If you found a bug please create a ticket for us on [Jira](https://issues.jboss.org/browse/AGDROID) with some steps to reproduce it.

