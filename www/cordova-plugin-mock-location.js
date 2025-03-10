var exec = require('cordova/exec');

const PLUGIN_NAME = "PluginMockLocation"

exports.checkMockLocation = function (getInfo=false) {
    var args = [getInfo];
    return new Promise((resolve, reject) => {
        exec(resolve, reject, PLUGIN_NAME, 'checkMockLocation', args);
    });
};

exports.setMockLocation = function (opts) {
    var args = [opts];
    return new Promise((resolve, reject) => {
        exec(resolve, reject, PLUGIN_NAME, 'setMockLocation', args);
    });
};

exports.disableMockLocation = function (provider) {
    var args = [provider];
    return new Promise((resolve, reject) => {
        exec(resolve, reject, PLUGIN_NAME, 'disableMockLocation', args);
    });
};

exports.isAvailable = function () {
    var args = [];
    return new Promise((resolve, reject) => {
        exec(resolve, reject, PLUGIN_NAME, 'isAvailable', args);
    });
};