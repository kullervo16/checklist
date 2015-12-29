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
        var init =  function () {
            $http.get('rest/template/list')
                .success(function (data,status,headers,config) {
                    $scope.items = data;                    
                }).error(function (data,status,headers,config) {
                    console.log('Error getting rest/template/list');
                });  
        };
         
               
        // function definitions
        function createChecklist(templateId) {
            $http.post('rest/template/createChecklist?id='+templateId)
                .success(function (data,status,headers,config) {
                    $window.location.href = './checklist.html?id='+data;                      
                }).error(function (data,status,headers,config) {
                    console.log('Error creating new checklist');
                });  
           
        }
        
        function getClassForMilestone(milestone) {
            if(milestone.reached) {
                return "label label-success";
            } else {
                return "label label-default";
            }           
        }
        
        $scope.createChecklist = createChecklist;
        $scope.getClassForMilestone = getClassForMilestone;
        init();
    }
    );
    
    
    app.controller('checklistController', function($scope,$http,$window,$location) {
        // init stuff... get data from backend               
        $http.get('rest/checklist/get?id='+$location.search().id)
                .success(function (data,status,headers,config) {
                    console.log("Data loaded");
                    $scope.data = data;                    
                }).error(function (data,status,headers,config) {
                    console.log('Error getting rest/checklist/get');
                });   
               
        function getClassForMilestone(milestone) {
            if(milestone.reached) {
                return "label label-success";
            } else {
                return "label label-default";
            }           
        }
        function getClassForStep(step) {
            console.log('get class for '+step.id+" "+step.state);
            if(step.state === 'OK') {
                return "ok";
            } else if(step.state === 'ON_HOLD') {
                return "onHold";
            } else if(step.state === 'NOT_APPLICABLE') {
                return "notApplicable";
            } else if(step.state === 'EXECUTION_FAILED' || step.state === 'CHECK_FAILED') {
                console.log(" -> nok");
                return "nok";
            }   
            console.log(" -> unknown");
            return "unknown";
        }
        function showActionButtons(step) {
            return step.state === 'UNKNOWN';
        }
        /**
         * Action result for a step, send to the backend and reload.
         */
        function updateAction(step, result) {
            $http.post('rest/checklist/setActionResult?id='+$location.search().id+"&step="+step.id+"&result="+result)
                .success(function (data,status,headers,config) {
                    $scope.data = data;                      
                }).error(function (data,status,headers,config) {
                    console.log('Error updating step '+step.id);
                });  
        }
        
        $scope.getClassForMilestone = getClassForMilestone;
        $scope.getClassForStep   = getClassForStep;
        $scope.showActionButtons = showActionButtons;
        $scope.updateAction      = updateAction;
    }
    );

   
})();