(function() {
    var app = angular.module('checklist',[]);

    app.controller('templateController', function($scope,$http) {
        $http.get('rest/template/list')
                .success(function (data,status,headers,config) {
                    $scope.items = data;                    
                }).error(function (data,status,headers,config) {
                    console.log('Error getting rest/template/list');
                });   
    }
    );

   
})();