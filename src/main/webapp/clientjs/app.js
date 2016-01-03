(function() {
    var app = angular.module('checklist',['ui.bootstrap']);
    
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
        // =================================================
        // init stuff... get data from backend     
        // =================================================
        $http.get('rest/checklist/get?id='+$location.search().id)
                .success(function (data,status,headers,config) {
                    console.log("Data loaded");
                    $scope.data = data;                    
                }).error(function (data,status,headers,config) {
                    console.log('Error getting rest/checklist/get');
                });   
        $scope.checkResults = {};
               
        // =================================================
        // CSS class calculation
        // =================================================        
        function getClassForMilestone(milestone) {
            if(milestone.reached) {
                return "label label-success";
            } else {
                return "label label-default";
            }           
        }
        function getClassForStep(step) {          
            if(step.state === 'OK') {
                return "ok";
            } else if(step.state === 'EXECUTED') {
                return "executed";
            } else if(step.state === 'ON_HOLD') {
                return "onHold";
            } else if(step.state === 'NOT_APPLICABLE') {
                return "notApplicable";
            } else if(step.state === 'EXECUTION_FAILED' 
                   || step.state === 'CHECK_FAILED' 
                   || step.state === 'CHECK_FAILED_NO_COMMENT' 
                   || step.state === 'EXECUTION_FAILED_NO_COMMENT') {
                return "nok";
            }   
            return "unknown";
        }
        // =================================================
        // Visibility determination
        // =================================================        
        
        function showErrorDialog(step) {
            return step.state === 'EXECUTION_FAILED_NO_COMMENT' || step.state === 'CHECK_FAILED_NO_COMMENT';
        }
        
        function showActionDetails(step) {
            return step.state === 'UNKNOWN' || step.state === 'EXECUTION_FAILED';
        }
        
        function showActionButtons(step) {
            return showActionDetails(step) && step.action;
        }
        
        function showChecks(step) {
            return step.state === 'EXECUTED';
        }
        
        function showCheckButtons(step,check) {
            if (! (step.id in $scope.checkResults)) {
                return true; // no results yet, so definitely show
            }
            return ! (check in $scope.checkResults[step.id]); // only when no result yet for that check
        }
        
        function showSubchecklist(step) {
            return showActionDetails(step) && step.subChecklist;
        }
        
        function showOptions(step) {
            return step.options && !step.selectedOption;
        }
        // =================================================
        // Backend update operations
        // =================================================
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
        
        function addErrorAction(step, error) {
            $http.post('rest/checklist/addErrorToStep?id='+$location.search().id+"&step="+step.id, error)
                .success(function (data,status,headers,config) {
                    $scope.data = data;                      
                }).error(function (data,status,headers,config) {
                    console.log('Error updating step '+step.id);
                });  
        }
        
        function addTag(tag) {
            $http.post('rest/checklist/addTag?id='+$location.search().id+"&tag="+tag)
                .success(function (data,status,headers,config) {
                    $scope.data = data;                      
                }).error(function (data,status,headers,config) {
                    console.log('Error adding a tag '+step.id);
                });              
        }
        
        function setCheckResult(step, check, result) {
            
            if (! (step.id in $scope.checkResults)) {
                $scope.checkResults[step.id] = {};
            }
            
            var stepMap = $scope.checkResults[step.id];            
            stepMap[check] = result;
           
            if ( Object.keys(stepMap).length === step.checks.length) {
                // all results are in... update the backend
                var combinedResult = true;
                for(var key in $scope.checkResults[step.id]) {
                    combinedResult &= $scope.checkResults[step.id][key];
                }
                $http.post('rest/checklist/setCheckResult?id='+$location.search().id+"&step="+step.id+"&result="+(combinedResult === 1))
                .success(function (data,status,headers,config) {
                    $scope.data = data;                      
                }).error(function (data,status,headers,config) {
                    console.log('Error updating step '+step.id);
                });  
            }
        }
        
        function setStepOption(step, choice) {
            $http.post('rest/checklist/setStepOption?id='+$location.search().id+"&step="+step.id+"&choice="+choice)
                .success(function (data,status,headers,config) {
                    $scope.data = data;                      
                }).error(function (data,status,headers,config) {
                    console.log('Error updating step '+step.id);
                });
        }
        
        function launchSubChecklist(step) {
            $http.post('rest/template/createChecklist?id='+step.subChecklist+"&parent="+$location.search().id)
                .success(function (data,status,headers,config) {
                    $window.location.href = './checklist.html?id='+data;                      
                }).error(function (data,status,headers,config) {
                    console.log('Error creating new checklist');
                });  
        }
        
        $scope.getClassForMilestone = getClassForMilestone;
        $scope.getClassForStep      = getClassForStep;
        
        $scope.showActionButtons = showActionButtons;
        $scope.showActionDetails = showActionDetails;
        $scope.showErrorDialog   = showErrorDialog;
        $scope.showChecks        = showChecks;
        $scope.showCheckButtons  = showCheckButtons;
        $scope.showSubchecklist  = showSubchecklist;
        $scope.showOptions       = showOptions;
        
        $scope.updateAction   = updateAction;
        $scope.addErrorAction = addErrorAction;
        $scope.setCheckResult = setCheckResult;
        $scope.addTag         = addTag;
        $scope.setStepOption  = setStepOption;
        $scope.launchSubChecklist = launchSubChecklist;
    }
    );

   
})();