(function(){
    var santaList = [
        { name: 'Azurite', description: "That's a strange name for a Secret Santa." },
        { name: 'St. Stealth', description: "Santa is coming."},
        { name: 'Santa I', description: "Santa is coming."},
        { name: 'Santa II', description: "Santa is coming."},
        { name: 'Santa III', description: "Santa is coming."}
    ];

    var app = angular.module('santaApp', []);

    app.controller('SantaController', function() {
        this.santas = santaList;
    });

})();