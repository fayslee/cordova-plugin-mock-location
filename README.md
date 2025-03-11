# cordova-plugin-mock-location

[![npm](https://img.shields.io/github/package-json/v/cesarcervantesb/cordova-plugin-mock-location
)](https://github.com/cesarcervantesb/cordova-plugin-mock-location/)
![License](https://img.shields.io/github/license/cesarcervantesb/cordova-plugin-mock-location?color=orange)

This plugin allows checking the status from your current device location and detect if it is a spoof location. And create tests location (Only for Android)

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

The plugin creates the object `cordova.plugins.mockLocation` and is accessible after the deviceready event has been fired.

```js
document.addEventListener('deviceready', function () {
    // cordova.plugins.mockLocation is now available
}, false);
```

# Available methods

## `async checkMockLocation` 
check if current location is mock.

This function can receive one param `getInfo` by default is `false`
If it's `true`. Return two additional objects with related information about the real location and mock location.


Returns a `JSON Object`:

- `isMockLocation`

If send param `getInfo` as true and the current location was detected as mock. Returns in same JSON Object two additionals `gpsLocation` and `mockLocation`. 
With the next attributes:

- `Latittude`
- `Longitude`
- `Accuracy`
- `Altitude`
- `Provider`

### Examples

```js
cordova.plugins.mockLocation().then( (location) => {
    // Success callback
    console.log("isMockLocation: " + location.isMockLocation);
}).catch( (error) => {
    // Error callback
    console.log("Error: " + error.message);
});
```

```js
const pluginResult = await mockLocation.checkMockLocation(true);
if (pluginResult.error) {
    console.log("Error: " + pluginResult.error.message);
}
else {
    console.log("isMockLocation: " + pluginResult.isMockLocation);
}
```
---

## `async setMockLocation` (Only for Android)

This function create a custom spoof location given a `JSON object` with the params:

- `provider` (`String`) - Name of the test provider.
- `latitude` (`float`) - Value of custom latitude.
- `longitude` (`float`) - Value of custom longitude. 

Return a `JSON Object` with info about setted mock location.

### Examples

```js
var opts = {
    provider: "test",
    lalitude: 52.520007,
    longitude: 13.404954
};
cordova.plugins.setMockLocation(opts).then( (location) => {
    // Success callback
    console.log("isMockLocation: " + location.isMockLocation);
}).catch( (error) => {
    // Error callback
    console.log("Error: " + error.message);
});
```

```js
var opts = {
    provider: "test",
    lalitude: 52.520007,
    longitude: 13.404954
};
const pluginResult = await mockLocation.setMockLocation(opts);
if (pluginResult.error) {
    console.log("Error: " + pluginResult.error.message);
}
else {
    console.log("isMockLocation: " + pluginResult.isMockLocation);
}
```

---

## `async disableMockLocation` (Only for Android)

This function disable a custom mock location given a `provider`

- `provider` (`String`) - Name of the test provider to disable mock lockation.


Return a `JSON Object` with info about location.

### Examples

```js
var provider = "test";
cordova.plugins.disableMockLocation(provider).then( (location) => {
    // Success callback
    console.log("isMockLocation: " + location.isMockLocation);
}).catch( (error) => {
    // Error callback
    console.log("Error: " + error.error.message);
});
```

```js
var provider = "test";
const pluginResult = await mockLocation.disableMockLocation(provider);
if (pluginResult.error) {
    console.log("Error: " + pluginResult.error.message);
}
else {
    console.log("isMockLocation: " + pluginResult.isMockLocation);
}
```

---

## `async isAvailable` (Only for Android)

This function check if device can create tests location.

Return a `JSON Object` with `isAvailable` as `true` if device can create tests location, `false` otherwise.

### Examples

```js
cordova.plugins.isAvailable().then( (mockLocation) => {
    // Success callback
    console.log("isAvailable: " + mockLocation.isAvailable);
}).catch( (error) => {
    // Error callback
    console.log("Error: " + error.message);
});
```

```js
const pluginResult = await mockLocation.isAvailable();
if (pluginResult.error) {
    console.log("Error: " + pluginResult.error.message);
}
else {
    console.log("isAvailable: " + pluginResult.isAvailable);
}
```