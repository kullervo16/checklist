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
        
        function showTemplate(id) {
            $window.location.href = './checklist.html?id='+id+"&mode=template";    
        }
        
        function showStats(id) {
            $http.get('rest/template/stats?id='+id)
                .success(function (data,status,headers,config) {
                    $scope.stats = data;        
                    $('#stats').modal('show'); 
                }).error(function (data,status,headers,config) {
                    console.log('Error getting rest/template/list');
                });  
            
        }
                
        
        function setFile(files) {
            $scope.files = files;
        }
        
        function setTemplateName(tn) {
            $scope.templateName = tn.value;
        }
        
        function hideModal() {
            $('#myModal').modal('hide');
            $('body').removeClass('modal-open');
            $('.modal-backdrop').remove();
        }
        
        function showModal() {
            $('#myModal').modal('show');
        }
        
        function uploadFile() {
                hideModal();
                var fd = new FormData();
                //Take the first selected file
                fd.append("file", $scope.files[0]);
                

                $http.post('rest/template/upload?name='+$scope.templateName, fd, {
                    withCredentials: true,
                    headers: {'Content-Type': undefined },
                    transformRequest: angular.identity
                }).success( function (data,status,headers,config) {
                                $scope.uploadValidationData = data;   
                                if(data.length > 0) {                                    
                                    showModal();
                                } else {
                                    hideModal();
                                    init();
                                }
                            }
                          ).error( console.log("Error uploading file") );

            };
            
        function getClassForStep(step) {
            if(step.successRate === 100) {
                return "success";
            } else if (step.successRate > 90) {
                return "warning";
            } else if (step.successRate > 80) {
                return "info";
            } else {
                return "danger";
            }
        }
        
        $scope.createChecklist = createChecklist;
        $scope.getClassForMilestone = getClassForMilestone;
        $scope.uploadFile = uploadFile;
        $scope.setTemplateName = setTemplateName;
        $scope.setFile = setFile;
        $scope.hideModal = hideModal;
        $scope.showModal = showModal;
        $scope.showTemplate = showTemplate;
        $scope.showStats = showStats;      
        $scope.getClassForStep = getClassForStep;
        init();
    }
    );
    
    
    app.controller('checklistController', function($scope,$http,$window,$location,$anchorScroll,$interval) {
        // =================================================
        // init stuff... get data from backend     
        // =================================================                                            
            $scope.mode = $location.search().mode;
            if($scope.mode === 'template') {
                // if mode is template, we show a template in the checklist view (but in readonly)
                $http.get('rest/template/get?id='+$location.search().id)
                    .success(function (data,status,headers,config) {
                        console.log("Data loaded");
                        $scope.data = data;                    
                    }).error(function (data,status,headers,config) {
                        console.log('Error getting rest/checklist/get');
                    });
            } else {
                $scope.mode = 'checklist';
                $http.get('rest/checklist/get?id='+$location.search().id)
                    .success(function (data,status,headers,config) {
                        console.log("Data loaded");
                        $scope.data = data;                    
                    }).error(function (data,status,headers,config) {
                        console.log('Error getting rest/checklist/get');
                    });
            }
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
            return (step.state === 'EXECUTION_FAILED_NO_COMMENT' || step.state === 'CHECK_FAILED_NO_COMMENT') && $scope.mode !== 'template';
        }
        
        function showActionDetails(step) {
            return step.state === 'UNKNOWN' || step.state === 'EXECUTION_FAILED' || $scope.mode === 'template';
        }
        
        function showActionButtons(step) {
            return showActionDetails(step) && step.action && $scope.mode !== 'template';
        }
        
        function showChecks(step) {
            return step.state === 'EXECUTED' || $scope.mode === 'template';
        }
        
        function showCheckButtons(step,check) {
            if($scope.mode === 'template') {
                return false;
            }
            if (! (step.id in $scope.checkResults)) {
                return true; // no results yet, so definitely show
            }
            return ! (check in $scope.checkResults[step.id]); // only when no result yet for that check
        }
        
        function showSubchecklist(step) {
            return showActionDetails(step) && step.subChecklist;
        }
        
        function getSubchecklistClass() {
            return $scope.mode === 'template'  ? "btn btn-default disabled" : "btn btn-default";          
        }
        
        function showMainBody() {
            return ($scope.data !== undefined && $scope.data.specificTagSet) || $scope.mode === 'template';
        }
        
        function showProgressBar() {
            return $scope.mode !== 'template';
        }
        
        function showOptions(step) {
            return step.options && ($scope.mode === 'template' || !step.selectedOption) ;
        }             
        
        function reposition() {
            var activeStep = undefined;
            // for some odd reason, the links don't work for the last 3 steps... go to complete then
            for(var i=0;i< $scope.data.steps.length-3;i++) {
                var step = $scope.data.steps[i];
                if(!step.complete) {
                    activeStep = step;
                    break;
                }
            }
            
            if(activeStep === undefined) {
                $window.location.hash = 'complete';     
                console.log('Start new repositioning cycle');
                $interval(function(){console.log('repos');$anchorScroll();},500,3);               
            } else {
                $window.location.hash = activeStep.id;     
                console.log('Start new repositioning cycle');
                $interval(function(){console.log('repos');$anchorScroll();},500,3);
            }
            
            
            
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
                    reposition();
                }).error(function (data,status,headers,config) {
                    console.log('Error updating step '+step.id);
                });  
        }
        
        function addErrorAction(step, error) {
            $http.post('rest/checklist/addErrorToStep?id='+$location.search().id+"&step="+step.id, error)
                .success(function (data,status,headers,config) {
                    $scope.data = data;   
                    reposition();
                }).error(function (data,status,headers,config) {
                    console.log('Error updating step '+step.id);
                });  
        }
        
        function addTag(tag) {
            $http.post('rest/checklist/addTag?id='+$location.search().id+"&tag="+tag)
                .success(function (data,status,headers,config) {
                    $scope.data = data;  
                    reposition();
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
                    reposition();
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
        // =================================================
        // overview operations
        // =================================================
        function getChecklists() {
            $http.get('rest/checklist/list')
                .success(function (data,status,headers,config) {
                    $scope.checklists = data;                
                }).error(function (data,status,headers,config) {
                    console.log('Error listing checklists');
                }); 
        }
        
        function getClassForChecklist(checklist) {
            if(checklist.complete) {
                return "list-group-item list-group-item-success";
            } else {
                return "list-group-item list-group-item-info";
            }
        }
        // =================================================
        // reload function
        // =================================================
        function toggleRefresh(state) {          
          if(state) {
              $scope.refresh = $interval(function () {
                  $http.get('rest/checklist/get?id='+$location.search().id)
                .success(function (data,status,headers,config) {
                    console.log("Data loaded");
                    $scope.data = data;                    
                }).error(function (data,status,headers,config) {
                    console.log('Error getting rest/checklist/get');
                });
              },5000);
          } else {
              $interval.cancel($scope.refresh);
          }
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
        $scope.showMainBody      = showMainBody;
        $scope.showProgressBar   = showProgressBar;        
        $scope.getSubchecklistClass = getSubchecklistClass;
        
        $scope.updateAction   = updateAction;
        $scope.addErrorAction = addErrorAction;
        $scope.setCheckResult = setCheckResult;
        $scope.addTag         = addTag;
        $scope.setStepOption  = setStepOption;
        $scope.launchSubChecklist = launchSubChecklist;
        
        $scope.getChecklists = getChecklists;
        $scope.getClassForChecklist = getClassForChecklist;       
        
        $scope.toggleRefresh = toggleRefresh;     
    }
    );

   
})();