(function(){
    var app = angular.module('santaApp', []);

    app.controller('ProfileController', ['$http', function($http) {
        var profile = this;
        profile.santas = [];

        $http.get('/user/hlouw/santas').success(function(data) {
            profile.santas = data;
        });
    }]);

})();