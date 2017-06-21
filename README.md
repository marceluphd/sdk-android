[![Build Status](https://api.travis-ci.org/kuzzleio/sdk-android.svg?branch=master)](https://travis-ci.org/kuzzleio/sdk-android) [![codecov.io](http://codecov.io/github/kuzzleio/sdk-android/coverage.svg?branch=master)](http://codecov.io/github/kuzzleio/sdk-android?branch=master)
[ ![Download](https://api.bintray.com/packages/kblondel/maven/kuzzle-sdk-android/images/download.svg) ](https://bintray.com/kblondel/maven/kuzzle-sdk-android/_latestVersion)
[![Join the chat at https://gitter.im/kuzzleio/kuzzle](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/kuzzleio/kuzzle?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

Official Kuzzle Android SDK
======

This SDK version requires Kuzzle v1.0.0-RC9 or higher.

## About Kuzzle

For UI and linked objects developers, Kuzzle is an open-source solution that handles all the data management (CRUD, real-time storage, search, high-level features, etc).

You can access the Kuzzle repository on [Github](https://github.com/kuzzleio/kuzzle)


## SDK Documentation

The complete SDK documentation is available [here](http://kuzzle.io/sdk-documentation)

## Report an issue

Use following meta repository to report issues on SDK:

https://github.com/kuzzleio/kuzzle-sdk/issues

## Installation

You can configure your android project to get the Kuzzle's android SDK from jcenter in your build.gradle:

    buildscript {
        repositories {
            maven {
                url  "http://dl.bintray.com/kuzzle/maven"
            }
            jcenter()
        }

    }

    dependencies {
        compile 'io.kuzzle:sdk-android:2.2.0'
    }


## License

[Apache 2](LICENSE.md)
