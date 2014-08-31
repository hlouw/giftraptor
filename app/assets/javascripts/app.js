(function(){
    var santaList = [
        { name: 'Azurite', description: "That's a strange name for a Secret Santa." },
        { name: 'St. Stealth', description: "Santa is coming."},
        { name: 'Santa I', description: "Santa is coming."},
        { name: 'Santa II', description: "Santa is coming."},
        { name: 'Santa III', description: "Santa is coming."}
    ];

    var app = angular.module('santaApp', []);

    app.controller('ProfileController', ['$http', function($http) {
        var profile = this;
        profile.santas = [];

        $http.get('http://localhost:9000/user/hlouw/santas').success(function(data) {
            profile.santas = data;
        });
    }]);

})();