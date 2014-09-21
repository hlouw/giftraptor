(function(){
    var app = angular.module('santaApp', ['ngCookies']);

    app.controller('AppController', ['$scope', '$cookies', function($scope, $cookies) {
        $scope.code = '';
        $cookies.secret = undefined;

        $scope.setSessionCode = function() {
            // Add code to cookies
            $cookies.secret = $scope.code.trim();
            $scope.code = '';
        };
    }]);

    app.controller('ProfileController', ['$http', function($http) {
        var profile = this;
        profile.santas = [];

        $http.get('/santas').success(function(data) {
            profile.santas = data;
        });

    }]);

    app.controller('SantaViewController', ['$http', '$scope', function($http, $scope) {
        var santa = this;
        santa.giftee = '';

        $http.get('/santa/' + $scope.santa._id + '/giftee').success(function(data) {
            santa.giftee = data;
        });
    }]);

})();