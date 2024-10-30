# cordova-plugin-mock-location

[![npm](https://img.shields.io/github/package-json/v/cesarcervantesb/cordova-plugin-mock-location
)](https://github.com/cesarcervantesb/cordova-plugin-mock-location/)
![License](https://img.shields.io/github/license/cesarcervantesb/cordova-plugin-mock-location?color=orange)

This plugin allows checking the status from your current device location and detect if it is a spoof location.

## Installation

Install plugin from npm:

```
npm i @ccervantesb/cordova-plugin-mock-location
```

Or install the latest master version from GitHub:

```
cordova plugin add https://github.com/cesarcervantesb/cordova-plugin-mock-location
```

## Supported Platforms

- Android
- iOS

## Usage

The plugin creates the object cordova.plugins.mockLocation and is accessible after the deviceready event has been fired.

```js
document.addEventListener('deviceready', function () {
    // cordova.plugins.mockLocation is now available
}, false);
```

## Available methods

- `checkMockLocation` - check if current location is mock.
```js
mockLocation.checkMockLocation()
```
