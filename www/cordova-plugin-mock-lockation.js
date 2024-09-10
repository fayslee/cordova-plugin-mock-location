var exec = require('cordova/exec');

exports.checkMockLocation = function (success, error) {
    exec(success, error, 'PluginMockLocation', 'checkMockLocation', []);
};
