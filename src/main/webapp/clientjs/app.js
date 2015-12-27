(function() {
    var app = angular.module('checklist',[]);
    
    app.config(['$locationProvider', function($locationProvider){
        $locationProvider.html5Mode({
              enabled: true,
              requireBase: false
            });
    }]);

    app.controller('templateController', function($scope,$http,$window) {
        // init stuff... get data from backend
        $http.get('rest/template/list')
                .success(function (data,status,headers,config) {
                    $scope.items = data;                    
                }).error(function (data,status,headers,config) {
                    console.log('Error getting rest/template/list');
                });   
               
        // function definitions
        function createChecklist(templateId) {
            $http.post('rest/template/createChecklist?id='+templateId)
                .success(function (data,status,headers,config) {
                    $window.location.href = 'checklist.html?id='+data;                    
                }).error(function (data,status,headers,config) {
                    console.log('Error creating new checklist');
                });  
           
        }
        
        $scope.createChecklist = createChecklist;
    }
    );
    
    
    app.controller('checklistController', function($scope,$http,$window,$location) {
        // init stuff... get data from backend       
        
        $http.get('rest/checklist/get?id='+$location.search().id)
                .success(function (data,status,headers,config) {
                    $scope.items = data;                    
                }).error(function (data,status,headers,config) {
                    console.log('Error getting rest/checklist/get');
                });   
               
        
    }
    );

   
})();