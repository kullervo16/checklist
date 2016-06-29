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
            $http.get('rest/templates')
                .success(function (data,status,headers,config) {
                    $scope.items = data;                    
                }).error(function (data,status,headers,config) {
                    console.log('Error getting rest/template/list');
                });  
        };
         
               
        // function definitions
        function createChecklist(templateId) {
            $http.post('rest/checklists'+templateId)
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
            $http.get('rest/templates'+id+'/stats')
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
        
        function showModal(nameInputNeeded) {
            if(nameInputNeeded) {
                $('#templateName').removeAttr('disabled');
            } else {
                $('#templateName').attr({
                    'disabled': 'disabled'
                });
            }            
            $('#myModal').modal('show');
        }
        
        function uploadFile() {
                hideModal();
                var fd = new FormData();
                //Take the first selected file
                fd.append("file", $scope.files[0]);
                
                if(!$scope.templateName.startsWith("/")) {
                    $scope.templateName = "/"+$scope.templateName;
                }
                $http.put('rest/templates'+$scope.templateName, fd, {
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
                          ).error( function(data,status,headers,config) {
                                    console.log("Error uploading file") 
                                });

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
        // =================================================
        // upload/download/delete
        // =================================================        
        function uploadTemplate(id) {   
            $scope.templateName = id;    
            $('#templateName').value = id;
            showModal(false);            
        }
        
        function downloadTemplate(id) {
            var hiddenElement = document.createElement('a');

            hiddenElement.href = './rest/templates'+id+'/content';
            hiddenElement.target = '_blank';
            hiddenElement.download = id.substr(id.lastIndexOf('/')+1)+'.yml';
            hiddenElement.click();                                                     
        }
        
        function deleteTemplate(id) {
            $http.delete('rest/templates'+id)
                .success(function (data,status,headers,config) {
                    $http.get('rest/templates')
                        .success(function (data,status,headers,config) {
                            $scope.items = data;                    
                        }).error(function (data,status,headers,config) {
                            console.log('Error getting rest/templates');
                        });                  
                }).error(function (data,status,headers,config) {
                    console.log('Error creating new checklist');
                });  
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
        $scope.uploadTemplate = uploadTemplate;
        $scope.downloadTemplate = downloadTemplate;
        $scope.deleteTemplate = deleteTemplate;
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
                $http.get('rest/templates'+$location.search().id)
                    .success(function (data,status,headers,config) {
                        console.log("Data loaded");
                        $scope.data = data;                    
                    }).error(function (data,status,headers,config) {
                        console.log('Error getting rest/checklist/get');
                    });
            } else {
                $scope.mode = 'checklist';
                $http.get('rest/checklists/'+$location.search().id)
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
            return step.options && ($scope.mode === 'template' || !step.selectedOption) && !step.answerType;
        }           
        
        function showRevalidateButton(step) {
            return step.state === 'CHECK_FAILED';
        }
        
        function showAnswerTextBox(step) {
            return step.answerType === 'text' && step.state === 'UNKNOWN';
        }
        
        function showAnswerRadioButton(step) {
            return step.answerType === 'onlyOne' && step.state === 'UNKNOWN';
        }
        
        function showAnswerChecklists(step) {
            return step.answerType === 'multiple' && step.state === 'UNKNOWN';
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
            $http.put('rest/checklists/'+$location.search().id+"/"+step.id+"/actionresult/"+result)
                .success(function (data,status,headers,config) {
                    $scope.data = data;    
                    reposition();
                }).error(function (data,status,headers,config) {
                    console.log('Error updating step '+step.id);
                });  
        }
        
        function addErrorAction(step, error) {
            $http.post('rest/checklists/'+$location.search().id+"/"+step.id+'/error', error)
                .success(function (data,status,headers,config) {
                    $scope.data = data;   
                    reposition();
                }).error(function (data,status,headers,config) {
                    console.log('Error updating step '+step.id);
                });  
        }
        
        function addAnswer(step, answer) {
            $http.post('rest/checklists/'+$location.search().id+"/"+step.id+'/answer', answer)
                .success(function (data,status,headers,config) {
                    $scope.data = data;   
                    reposition();
                }).error(function (data,status,headers,config) {
                    console.log('Error updating step '+step.id);
                }); 
        }
        
        function addTag(tag) {
            $http.put('rest/checklists/'+$location.search().id+"/tag/"+tag)
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
                $http.put('rest/checklists/'+$location.search().id+"/"+step.id+"/checkresult/"+(combinedResult === 1))
                .success(function (data,status,headers,config) {
                    $scope.data = data;   
                    reposition();
                }).error(function (data,status,headers,config) {
                    console.log('Error updating step '+step.id);
                });  
            }            
        }
        
        function setStepOption(step, choice) {
            $http.put('rest/checklists/'+$location.search().id+"/"+step.id+"/option/"+choice)
                .success(function (data,status,headers,config) {
                    $scope.data = data;                        
                }).error(function (data,status,headers,config) {
                    console.log('Error updating step '+step.id);
                });
        }
        
        function launchSubChecklist(step) {
            $http.post('rest/checklists'+step.subChecklist+'?parent='+$location.search().id)
                .success(function (data,status,headers,config) {
                    $window.location.href = './checklist.html?id='+data;                      
                }).error(function (data,status,headers,config) {
                    console.log('Error creating new checklist');
                });  
        }
        
        function revalidate(step) {
            $scope.checkResults[step.id] = {};
            $http.put('rest/checklists/'+$location.search().id+"/"+step.id+'/validate')
                .success(function (data,status,headers,config) {
                    $scope.data = data;                        
                }).error(function (data,status,headers,config) {
                    console.log('Error updating step '+step.id);
                });
        }
        // =================================================
        // overview operations
        // =================================================
        function getChecklists() {
            var hash=window.location.hash;  
            var url = 'rest/checklists'; // default, get all recent checklists
            if(hash !== '') {
                // add a filter, either on tag or on milestone
                url += '?'+hash.substring(1); // strip the #
            }
            $http.get(url)
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
        
        function createTagClouds() {
            $http.get('rest/tags')
                .success(function (data,status,headers,config) {                    
                    for(var i=0;i<data.length;i++) {
                        data[i]['handlers'] = {};
                        data[i]['handlers']['click'] =  function() {
                                                            angular.element('#tags').scope().openOverviewForTag(this)                          
                        };
                    }
                    $('#tags').jQCloud(data);            
                }).error(function (data,status,headers,config) {
                    console.log('Error getting rest/checklist/tags/list');
                });
            $http.get('rest/milestones')
                .success(function (data,status,headers,config) {
                    for(var i=0;i<data.length;i++) {
                        data[i]['handlers'] = {};
                        data[i]['handlers']['click'] =  function() {
                                                            angular.element('#milestones').scope().openOverviewForMilestone(this)                          
                        };
                    }
                    $('#milestones').jQCloud(data);            
                }).error(function (data,status,headers,config) {
                    console.log('Error getting rest/checklist/milestones/list');
                });
        }
        
        function openOverviewForTag(tag) {
            window.open('checklistOverview.html#tag='+tag.innerText);
        }
        function openOverviewForMilestone(milestone) {
            window.open('checklistOverview.html#milestone='+milestone.innerText);
        }
        // =================================================
        // reload function
        // =================================================
        function toggleRefresh(state) {          
          if(state) {
              $scope.refresh = $interval(function () {
                  $http.get('rest/checklists/'+$location.search().id)
                .success(function (data,status,headers,config) {
                    console.log("Data loaded");
                    $scope.data = data;
                    reposition();
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
        $scope.showAnswerTextBox = showAnswerTextBox;
        $scope.showAnswerChecklists = showAnswerChecklists;
        $scope.showAnswerRadioButton= showAnswerRadioButton;
        $scope.showRevalidateButton = showRevalidateButton;
        $scope.getSubchecklistClass = getSubchecklistClass;
        
        $scope.updateAction   = updateAction;
        $scope.addErrorAction = addErrorAction;
        $scope.addAnswer      = addAnswer;
        $scope.setCheckResult = setCheckResult;
        $scope.addTag         = addTag;
        $scope.setStepOption  = setStepOption;
        $scope.revalidate     = revalidate;
        $scope.launchSubChecklist = launchSubChecklist;        
        
        $scope.getChecklists = getChecklists;
        $scope.getClassForChecklist = getClassForChecklist;   
        $scope.createTagClouds = createTagClouds;
        $scope.openOverviewForTag = openOverviewForTag;
        $scope.openOverviewForMilestone = openOverviewForMilestone;
        
        $scope.toggleRefresh = toggleRefresh;     
    }
    );

   
})();