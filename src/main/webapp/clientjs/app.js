(function () {

  var app = angular.module('checklist', ['ui.bootstrap', 'ngResource']);
  
  app.config(['$locationProvider', function ($locationProvider) {
    $locationProvider.html5Mode({
      enabled: true,
      requireBase: false
    });
  }]);

  app.directive('ngEnter', function () {
    return function (scope, element, attrs) {
      element.bind("keydown keypress", function (event) {
        if (event.which === 13) {
          scope.$apply(function () {
            scope.$eval(attrs.ngEnter);
          });
          event.preventDefault();
        }
      });
    };
  });
  
  app.config(['$httpProvider', function($httpProvider) {
    var token = window._keycloak.token;     
    $httpProvider.defaults.headers.common['Authorization'] = 'BEARER ' + token;
  }]);


  app.filter("trust", ['$sce', function ($sce) {
    return function (htmlCode) {
      return $sce.trustAsHtml(htmlCode);
    }
  }]);

  app.filter("createLinks", ['$sce', function ($sce) {
    return function (text) {
      text = parseProtocol("http://", text);
      return parseProtocol("https://", text);
    };

    function parseProtocol(protocol, text) {
      var newText = "";
      while (text.indexOf(protocol) >= 0) {
        newText += text.substring(0, text.indexOf(protocol));
        var url = text.substring(text.indexOf(protocol));
        if (url.indexOf(" ") > 0) {
          // normal case... other words after
          url = url.substring(0, url.indexOf(" "));
        } else {
          // border case... text ends with URL
          // TODO: is it really necessary ???
          url = url;
        }
        newText += "<a href=\"" + url + "\" target=\"_blank\">" + url + "</a>";
        text = text.substring(text.indexOf(protocol) + url.length);
      }
      // now set the remainer (which may be the complete string when no URLs in it :-)
      newText += text;
      return newText;
    }
  }]);


  app.controller('userController', function ($scope, $http, $window) {

          
        function getUserName() {
            return $window._keycloak.idTokenParsed.name;
        }
        
        function getRoles() {
            return $window._keycloak.realmAccess.roles;
        }
          
        function isAdmin() {            
            for (var i = 0; i < $window._keycloak.realmAccess.roles.length; i++) {
              if ($window._keycloak.realmAccess.roles[i] === 'admin') {
                return true;
              }
            }

            return false;
        }
        function canModify() {            
            for (var i = 0; i < $window._keycloak.realmAccess.roles.length; i++) {
              if ($window._keycloak.realmAccess.roles[i] === 'modify') {
                return true;
              }
            }            
            return false;
        }
        $scope.isAdmin = isAdmin;
        $scope.canModify = canModify;
        $scope.getUserName = getUserName;
        $scope.getRoles = getRoles;
  });

  app.controller('templateController', function ($scope, $http, $window) {
        // init stuff... get data from backend
        var init = function () {
          if ($scope.subCLshown === undefined) {
            $scope.subCLshown = false;
          }
          $http.get('rest/templates')
               .success(function (data, status, headers, config) {
                 $scope.rawItems = data;
                 toggleShowSubchecklists($scope.subCLshown, false);
               }).error(function (data, status, headers, config) {
            console.log('Error getting rest/templates');
          });
        };

        function toggleShowSubchecklists(show, apply) {
          if (show) {
            $scope.items      = $scope.rawItems;
            $scope.subCLshown = true;
          } else {
            $scope.subCLshown = false;
            $scope.items      = [];
            for (var i = 0; i < $scope.rawItems.length; i++) {
              if (!$scope.rawItems[i].subchecklistOnly) {
                $scope.items.push($scope.rawItems[i]);
              }
            }
          }
          if (apply) {
            $scope.$apply();
          }
        }

        // function definitions
        function createChecklist(templateId) {
          $http.post('rest/checklists' + templateId)
               .success(function (data, status, headers, config) {
                 $window.location.href = './checklist.html?id=' + data;
               }).error(function (data, status, headers, config) {
            console.log('Error creating new checklist');
          });

        }

        function getClassForMilestone(milestone) {
          if (milestone.reached) {
            return "label label-success";
          } else {
            return "label label-default";
          }
        }

        function showTemplate(id) {
          $window.location.href = './checklist.html?id=' + id + "&mode=template";
        }

        function showStats(id) {
          $http.get('rest/templates' + id + '/stats')
               .success(function (data, status, headers, config) {
                 $scope.stats = data;
                 $('#stats').modal('show');
               }).error(function (data, status, headers, config) {
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
          if (nameInputNeeded) {
            $('#templateName').removeAttr('disabled');
          } else {
            $('#templateName').attr({
              'disabled': 'disabled'
            });
          }
          $('#myModal').modal('show');
        }

        function uploadFile() {

          if ($scope.files == null || $scope.files.length == 0) {
            alert("You have to select a file !");
            return;
          }

          if ($scope.templateName == null || $scope.templateName.trim().length == 0) {
            alert("You have to give an ID !\nExample: /directory/template_name");
            return;
          }

          var fd = new FormData();
          //Take the first selected file
          fd.append("file", $scope.files[0]);

          if (!$scope.templateName.startsWith("/")) {
            $scope.templateName = "/" + $scope.templateName;
          }

          if ($scope.templateName.split("/").length !== 3) {
            alert("Current layout requires a single grouping folder and a template name.\nExample: /directory/template_name");
            return;
          }

          hideModal();
          $http.put('rest/templates' + $scope.templateName, fd, {
            withCredentials: true,
            headers: {'Content-Type': undefined},
            transformRequest: angular.identity
          }).success(function (data, status, headers, config) {
                $scope.uploadValidationData = data;
                if (data.length > 0) {
                  showModal();
                } else {
                  hideModal();
                  init();
                }
              }
          ).error(function (data, status, headers, config) {
            console.log("Error uploading file")
          });
        }

        function getClassForStep(step) {
          if (step.successRate === 100) {
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
          $scope.templateName      = id;
          $('#templateName').value = id;
          showModal(false);
        }

        function downloadTemplate(id) {
          var hiddenElement = document.createElement('a');

          hiddenElement.href     = './rest/templates' + id + '/content';
          hiddenElement.target   = '_blank';
          hiddenElement.download = id.substr(id.lastIndexOf('/') + 1) + '.yml';
          hiddenElement.click();
        }

        function deleteTemplate(id) {
          if (confirm("Are you sure you want to delete template " + id + "? This action cannot be undone...")) {
            $http.delete('rest/templates' + id)
                 .success(function (data, status, headers, config) {
                   $http.get('rest/templates')
                        .success(function (data, status, headers, config) {
                          $scope.rawItems = data;
                          toggleShowSubchecklists($scope.subCLshown, false);
                        }).error(function (data, status, headers, config) {
                     console.log('Error getting rest/templates');
                   });
                 }).error(function (data, status, headers, config) {
              if (status === 409) {
                alert(data.description);
                console.log(data.description);
              } else {
                console.log('Unexpected error in deleteTemplate(id) !');
              }
            });
          }
        }

        $scope.createChecklist         = createChecklist;
        $scope.getClassForMilestone    = getClassForMilestone;
        $scope.uploadFile              = uploadFile;
        $scope.setTemplateName         = setTemplateName;
        $scope.setFile                 = setFile;
        $scope.hideModal               = hideModal;
        $scope.showModal               = showModal;
        $scope.showTemplate            = showTemplate;
        $scope.showStats               = showStats;
        $scope.getClassForStep         = getClassForStep;
        $scope.uploadTemplate          = uploadTemplate;
        $scope.downloadTemplate        = downloadTemplate;
        $scope.deleteTemplate          = deleteTemplate;
        $scope.toggleShowSubchecklists = toggleShowSubchecklists;
        init();
      }
  );


  app.controller('checklistController', function ($scope, $http, $window, $location, $anchorScroll, $interval) {
        // =================================================
        // init stuff... get data from backend
        // =================================================
        $scope.mode         = $location.search().mode;
        $scope.refreshState = false;
        if ($location.search().refresh !== undefined) {
          toggleRefresh($location.search().refresh);
        }

        $scope.tagSelection         = '';
        $scope.milestoneSelection   = '';
        $scope.hideClosedChecklists = false;
        if ($scope.mode === 'template') {
          // if mode is template, we show a template in the checklist view (but in readonly)
          $http.get('rest/templates' + $location.search().id)
               .success(function (data, status, headers, config) {
                 console.log("Data loaded");
                 $scope.data = data;
               }).error(function (data, status, headers, config) {
            console.log('Error getting rest/checklist/get');
          });
        } else {
          $scope.mode = 'checklist';
          $http.get('rest/checklists/' + $location.search().id)
               .success(function (data, status, headers, config) {
                 console.log("Data loaded");
                 $scope.data = data;
                 if (!data.specificTagSet || !data.uniqueTagcombination) {
                   showModal('#tagModal');
                 }
               }).error(function (data, status, headers, config) {
            console.log('Error getting rest/checklist/get');
          });
        }
        $scope.checkResults = {};

        // =================================================
        // CSS class calculation
        // =================================================
        function getClassForMilestone(milestone) {
          if (milestone.reached) {
            return "label label-success";
          } else {
            return "label label-default";
          }
        }

        function getClassForStep(step) {
          if (step.state === 'OK') {
            return "step ok";
          } else if (step.state === 'EXECUTED') {
            return "step executed";
          } else if (step.state === 'IN_PROGRESS') {
            return "step inProgress";
          } else if (step.state === 'NOT_YET_APPLICABLE') {
            return "step notYetApplicable";
          } else if (step.state === 'NOT_APPLICABLE') {
            return "step notApplicable";
          } else if (step.state === 'ABORTED') {
            return "step aborted";
          } else if (step.state === 'EXECUTION_FAILED'
                     || step.state === 'CHECK_FAILED'
                     || step.state === 'CHECK_FAILED_NO_COMMENT'
                     || step.state === 'EXECUTION_FAILED_NO_COMMENT') {
            return "step nok";
          }
          return "step unknown";
        }

        // =================================================
        // Visibility determination
        // =================================================

        function showErrorDialog(step) {
          return (step.state === 'EXECUTION_FAILED_NO_COMMENT' || step.state === 'CHECK_FAILED_NO_COMMENT') && $scope.mode !== 'template';
        }

        function showActionButtons(step) {
          return step.state === 'IN_PROGRESS' && step.action && $scope.mode !== 'template' && !step.options;
        }

        function showChecks(step) {
          return step.state === 'EXECUTED' || ($scope.mode === 'template' && step.checks != null && step.checks.length > 0);
        }

        function showCheckButtons(step, check) {
          if ($scope.mode === 'template') {
            return false;
          }
          if (!(step.id in $scope.checkResults)) {
            return true; // no results yet, so definitely show
          }
          return !(check in $scope.checkResults[step.id]); // only when no result yet for that check
        }

        function showSubchecklist(step) {
          return step.subChecklist != null;
        }

        function getSubchecklistClass() {
          return $scope.mode === 'template' ? "btn btn-default disabled" : "btn btn-default";
        }

        function showMainBody() {
          return ($scope.data !== undefined && $scope.data.specificTagSet && $scope.data.uniqueTagcombination) || $scope.mode === 'template';
        }

        function showProgressBar() {
          return $scope.mode !== 'template';
        }

        function showOptions(step) {
          return step.options && ($scope.mode === 'template' || !step.selectedOption) && !step.answerType && !(step.state === 'ABORTED' || step.state === 'NOT_YET_APPLICABLE');
        }

        function showRevalidateButton(step) {
          return step.state === 'CHECK_FAILED';
        }

        function showReopenButton(step) {
          return step.reopenable;
        }

        function showAnswerChecklists(step) {
          return step.answerType === 'multiple' && step.state === 'UNKNOWN';
        }

        function showAnswerRadioButton(step) {
          return step.answerType === 'onlyOne' && step.state === 'UNKNOWN';
        }

        function showAnswerTextBox(step) {
          return step.answerType === 'text' && step.state === 'UNKNOWN';
        }

        function gotoChecklist(cl) {
          $window.location = 'checklist.html?id=' + cl;
        }

        function gotoTemplate(templateId) {
          $window.location = 'checklist.html?mode=template&id=' + window.encodeURIComponent(templateId);
        }

        function hideModal(modalId) {
          $(modalId).modal('hide');
          $('body').removeClass('modal-open');
          $('.modal-backdrop').remove();
        }

        function showModal(modalId) {
          $(modalId).modal('show');
        }

        function showErrors(step) {
          $scope.shownStep = step;
          showModal('#errorModal');
        }

        function reposition() {
          var activeStep = undefined;
          // for some odd reason, the links don't work for the last 3 steps... go to complete then
          for (var i = 0; i < $scope.data.steps.length - 3; i++) {
            var step = $scope.data.steps[i];
            if (!step.complete) {
              activeStep = step;
              break;
            }
          }

          if (activeStep === undefined) {
            $window.location.hash = 'complete';
            console.log('Start new repositioning cycle');
            $interval(function () {
              console.log('repos');
              $anchorScroll();
            }, 500, 3);
          } else {
            $window.location.hash = activeStep.id;
            console.log('Start new repositioning cycle');
            $interval(function () {
              console.log('repos');
              $anchorScroll();
            }, 500, 3);
          }
        }

        function repositionTo(step) {
          $window.location.hash = step.id;
          $anchorScroll();
        }

        function showGoBackToParent() {
          return $scope.data && $scope.data.parent && $scope.data.progress === 100;
        }

        function showStartProgress(step) {
          return step.action != null && step.state === 'UNKNOWN' && $scope.mode !== 'template';
        }

        function showDocumentation(step) {
          return step.documentation != null;
        }

        // =================================================
        // Backend update operations
        // =================================================

        function startAction(step) {
          $http.put('rest/checklists/' + $location.search().id + "/" + step.id + "/start")
               .success(function (data, status, headers, config) {
                 $scope.data = data;
                 repositionTo(step);
               }).error(function (data, status, headers, config) {
            console.log('Error starting step ' + step.id);
          });
        }

        /**
         * Action result for a step, send to the backend and reload.
         */
        function updateAction(step, result) {
          $http.put('rest/checklists/' + $location.search().id + "/" + step.id + "/actionresults/" + result)
               .success(function (data, status, headers, config) {
                 $scope.data = data;
                 reposition();
               }).error(function (data, status, headers, config) {
            console.log('Error updating step ' + step.id);
          });
        }

        function addErrorAction(step, error) {
          $http.post('rest/checklists/' + $location.search().id + "/" + step.id + '/errors', error)
               .success(function (data, status, headers, config) {
                 $scope.data = data;
                 reposition();
               }).error(function (data, status, headers, config) {
            console.log('Error updating step ' + step.id);
          });
        }

        function addAnswer(step, answer) {
          $http.post('rest/checklists/' + $location.search().id + "/" + step.id + '/answers', answer)
               .success(function (data, status, headers, config) {
                 $scope.data = data;
                 reposition();
               }).error(function (data, status, headers, config) {
            console.log('Error updating step ' + step.id);
          });
        }

        function addTag(tag) {
          $http.put('rest/checklists/' + $location.search().id + "/tags/" + tag)
               .success(function (data, status, headers, config) {
                 $scope.newTag = '';
                 $scope.data   = data;
                 if (!data.specificTagSet || !data.uniqueTagcombination) {
                   showModal('#tagModal');
                 } else {
                   hideModal('#tagModal');
                   reposition();
                 }
               }).error(function (data, status, headers, config) {
            console.log('Error adding a tag ' + step.id);
          });
        }

        function setCheckResult(step, check, result) {

          if (!(step.id in $scope.checkResults)) {
            $scope.checkResults[step.id] = {};
          }

          var stepMap    = $scope.checkResults[step.id];
          stepMap[check] = result;

          if (Object.keys(stepMap).length === step.checks.length) {
            // all results are in... update the backend
            var combinedResult = true;
            for (var key in $scope.checkResults[step.id]) {
              combinedResult &= $scope.checkResults[step.id][key];
            }
            $http.put('rest/checklists/' + $location.search().id + "/" + step.id + "/checkresults/" + (combinedResult === 1))
                 .success(function (data, status, headers, config) {
                   $scope.data = data;
                   reposition();
                 }).error(function (data, status, headers, config) {
              console.log('Error updating step ' + step.id);
            });
          }
        }

        function setStepOption(step, choice) {
          if (isInChecklistMode()) {
            $http.put('rest/checklists/' + $location.search().id + "/" + step.id + "/options/" + choice)
                 .success(function (data, status, headers, config) {
                   $scope.data = data;
                   reposition();
                 }).error(function (data, status, headers, config) {
              console.log('Error updating step ' + step.id);
            });
          }
        }

        function launchSubChecklist(step) {
          $http.post('rest/checklists' + step.subChecklist + '?parent=' + $location.search().id + '&step=' + step.id)
               .success(function (data, status, headers, config) {
                 $window.location.href = './checklist.html?id=' + data;
               }).error(function (data, status, headers, config) {
            console.log('Error creating new checklist');
          });
        }

        function revalidate(step) {
          $scope.checkResults[step.id] = {};
          $http.put('rest/checklists/' + $location.search().id + "/" + step.id + '/validate')
               .success(function (data, status, headers, config) {
                 $scope.data = data;
               }).error(function (data, status, headers, config) {
            console.log('Error updating step ' + step.id);
          });
        }

        function reopen(step) {
          $scope.checkResults[step.id] = {};
          $http.put('rest/checklists/' + $location.search().id + "/" + step.id + '/reopen')
               .success(function (data, status, headers, config) {
                 $scope.data = data;
                 repositionTo(step);
               }).error(function (data, status, headers, config) {
            console.log('Error updating step ' + step.id);
          });
        }

        function deleteChecklist() {
          if (confirm("Are you sure you want to delete this checklist and its sub-checklists? This operation cannot be undone...")) {
            var parent = $scope.data.parent;
            $http.delete('rest/checklists/' + $location.search().id)
                 .success(function (data, status, headers, config) {
                   if (parent == null) {
                     $window.location.href = './checklistOverview.html'
                   } else {
                     $window.location.href = './checklist.html?id=' + parent;
                   }
                 }).error(function (data, status, headers, config) {
              console.log('Error deleting checklist ' + $location.search().id);
            });
          }
        }

        function closeChecklist() {
          if (confirm("Are you sure you want to close this checklist? All unfinished steps will be set to aborted. This operation cannot be undone...")) {
            $http.post('rest/checklists/' + $location.search().id + '/actions/close')
                 .success(function (data, status, headers, config) {
                   if ($scope.data.parent == null) {
                     $window.location.href = './checklistOverview.html'
                   } else {
                     $window.location.href = './checklist.html?id=' + $scope.data.parent;
                   }
                 }).error(function (data, status, headers, config) {
              alert('Error closing checklist ' + $location.search().id);
              console.log('Error closing checklist ' + $location.search().id);
              $window.location.href = './checklist.html?id=' + $scope.data.id;
            });
          }
        }

        function removeTagFromChecklist(tag) {
          if ($scope.mode === 'checklist') {
            if (confirm("Are you sure you want to remove tag '" + tag + "' from this checklist? You can always add it again later...")) {
              $http.delete('rest/checklists/' + $location.search().id + '/tags/' + tag)
                   .success(function (data, status, headers, config) {
                     $scope.data = data;
                     if (!data.specificTagSet || !data.uniqueTagcombination) {
                       showModal('#tagModal');
                     }
                   }).error(function (data, status, headers, config) {
                alert(data.error);
                console.log('Error removing tag ' + tag + ' from checklist ' + $location.search().id);
              });
            }
          }
        }

        function instantiateFromTemplate() {
          $http.post('rest/checklists' + $location.search().id)
               .success(function (data, status, headers, config) {
                 $window.location.href = './checklist.html?id=' + data;
               }).error(function (data, status, headers, config) {
            console.log('Error creating new checklist');
          });
        }

        // =================================================
        // overview operations
        // =================================================
        function getChecklists() {
          var hash = window.location.hash;
          var url  = 'rest/checklists'; // default, get all recent checklists
          // Tags form the tags hash param
          var filteredTags;
          if (hash !== '') {
            var hashParams   = hash.substring(1);
            // add a filter, either on tag or on milestone
            url += '?' + hashParams; // strip the #
            var tagsParamPos = hashParams.indexOf("tags=");
            if (tagsParamPos != -1) {
              tagsParamPos += 5;
              var tagsParamEndPos = hashParams.indexOf("&", tagsParamPos);
              filteredTags        = hashParams.substring(tagsParamPos, tagsParamEndPos == -1 ? hashParams.length : tagsParamEndPos).split(",");
            }
          }
          $http.get(url)
               .success(function (data, status, headers, config) {
                 if (data.length == 1) {
                   window.open("checklist.html?id=" + data[0].uuid, "_self");
                 }
                 // Remove the filtered tags from each checklist
                 if (filteredTags != null) {
                   for (var i = 0; i < data.length; i++) {
                     var tags = data[i].tags;
                     for (var j = 0; j < tags.length;) {
                       if (filteredTags.includes(tags[j])) {
                         tags.splice(j, 1);
                       } else {
                         j++;
                       }
                     }
                   }
                 }
                 $scope.rawchecklists = data;
                 toggleClosedFilter($scope.hideClosedChecklists, false);
               }).error(function (data, status, headers, config) {
            console.log('Error listing checklists');
          });
        }

        function toggleClosedFilter(hide, apply) {
          if (hide) {
            $scope.hideClosedChecklists = true;
            $scope.checklists           = [];
            for (var i = 0; i < $scope.rawchecklists.length; i++) {
              if (!$scope.rawchecklists[i].complete) {
                $scope.checklists.push($scope.rawchecklists[i]);
              }
            }
          } else {
            $scope.hideClosedChecklists = false;
            $scope.checklists           = $scope.rawchecklists;
          }
          if (apply) {
            $scope.$apply();
          }
        }

        function getClassForChecklist(checklist) {
          if (checklist.complete) {
            return "list-group-item list-group-item-success";
          } else {
            return "list-group-item list-group-item-info";
          }
        }

        function createTagClouds() {
          clearTagSelection();
          clearMilestoneSelection();
        }

        function openOverview() {
          window.open('checklistOverview.html#tags=' + $scope.tagSelection + "&milestones=" + $scope.milestoneSelection, '_self');
        }

        function addTagToSelection(tag) {
          if ($scope.tagSelection === '') {
            $scope.tagSelection = tag.innerText;
          } else {
            $scope.tagSelection = $scope.tagSelection + "," + tag.innerText;
          }
          createTagCloud('rest/tags?filter=' + $scope.tagSelection);
        }

        function createTagCloud(path) {
          $http.get(path)
               .success(function (data, status, headers, config) {
                 $scope.tagSelection = $scope.arrayToComaSeparatedString(data.selection);
                 var nbEntries       = data.entries.length;
                 if (nbEntries == 0) {
                   openOverview();
                 }
                 $('#tags').empty();
                 $('#tags').jQCloud('destroy');
                 for (var i = 0; i < nbEntries; i++) {
                   var entriesWalker                  = data.entries[i];
                   entriesWalker['handlers']          = {};
                   entriesWalker['handlers']['click'] = function () {
                     angular.element('#tags').scope().addTagToSelection(this)
                   };
                 }
                 $('#tags').jQCloud(data.entries);
               }).error(function (data, status, headers, config) {
            console.log('Error getting rest/tags');
          });
        }

        function arrayToComaSeparatedString(array) {
          var string    = "";
          var separator = "";
          if (array != null) {
            var arrayLength = array.length;
            for (var i = 0; i < arrayLength; i++) {
              string += separator + array[i];
              separator = ",";
            }
          }
          return string;
        }

        function addMileStoneToSelection(milestone) {
          if ($scope.milestoneSelection === '') {
            $scope.milestoneSelection = milestone.innerText;
          } else {
            $scope.milestoneSelection = $scope.milestoneSelection + "," + milestone.innerText;
          }
          $scope.$apply();
          createMilestoneCloud('rest/milestones?filter=' + $scope.milestoneSelection);
        }

        function createMilestoneCloud(path) {
          $http.get(path)
               .success(function (data, status, headers, config) {
                 $('#milestones').empty();
                 $('#milestones').jQCloud('destroy');
                 for (var i = 0; i < data.length; i++) {
                   data[i]['handlers']          = {};
                   data[i]['handlers']['click'] = function () {
                     angular.element('#milestones').scope().addMileStoneToSelection(this)
                   };
                 }
                 $('#milestones').jQCloud(data);
               }).error(function (data, status, headers, config) {
            console.log('Error getting rest/milestones');
          });
        }

        function clearTagSelection() {
          $scope.tagSelection = '';
          createTagCloud('rest/tags');
        }

        function clearMilestoneSelection() {
          $scope.milestoneSelection = '';
          createMilestoneCloud('rest/milestones');
        }

        // =================================================
        // reload function
        // =================================================
        function toggleRefresh(state) {
          if (state) {
            $scope.refresh = $interval(function () {
              $http.get('rest/checklists/' + $location.search().id)
                   .success(function (data, status, headers, config) {
                     console.log("Data loaded");
                     $scope.data = data;
                     reposition();
                   }).error(function (data, status, headers, config) {
                console.log('Error getting rest/checklist/get');
              });
            }, 5000);
          } else {
            $interval.cancel($scope.refresh);
          }
          $scope.refreshState = state;
        }

        // =================================================
        // misc operations
        // =================================================
        function isInChecklistMode() {
          return $scope.mode !== 'template';
        }

        $scope.getClassForMilestone = getClassForMilestone;
        $scope.getClassForStep      = getClassForStep;

        $scope.isInChecklistMode      = isInChecklistMode;
        $scope.showActionButtons      = showActionButtons;
        $scope.showErrorDialog        = showErrorDialog;
        $scope.showChecks             = showChecks;
        $scope.showCheckButtons       = showCheckButtons;
        $scope.showSubchecklist       = showSubchecklist;
        $scope.showOptions            = showOptions;
        $scope.showMainBody           = showMainBody;
        $scope.showProgressBar        = showProgressBar;
        $scope.showAnswerChecklists   = showAnswerChecklists;
        $scope.showAnswerRadioButton  = showAnswerRadioButton;
        $scope.showAnswerTextBox      = showAnswerTextBox;
        $scope.showGoBackToParent     = showGoBackToParent;
        $scope.showStartProgress      = showStartProgress;
        $scope.showDocumentation      = showDocumentation;
        $scope.gotoChecklist          = gotoChecklist;
        $scope.gotoTemplate           = gotoTemplate;
        $scope.showModal              = showModal;
        $scope.hideModal              = hideModal;
        $scope.showErrors             = showErrors;
        $scope.showRevalidateButton   = showRevalidateButton;
        $scope.showReopenButton       = showReopenButton;
        $scope.getSubchecklistClass   = getSubchecklistClass;
        $scope.toggleClosedFilter     = toggleClosedFilter;
        $scope.removeTagFromChecklist = removeTagFromChecklist;

        $scope.startAction        = startAction;
        $scope.updateAction       = updateAction;
        $scope.addErrorAction     = addErrorAction;
        $scope.addAnswer          = addAnswer;
        $scope.setCheckResult     = setCheckResult;
        $scope.addTag             = addTag;
        $scope.setStepOption      = setStepOption;
        $scope.revalidate         = revalidate;
        $scope.reopen             = reopen;
        $scope.deleteChecklist    = deleteChecklist;
        $scope.closeChecklist     = closeChecklist;
        $scope.launchSubChecklist = launchSubChecklist;

        $scope.getChecklists           = getChecklists;
        $scope.getClassForChecklist    = getClassForChecklist;
        $scope.createTagClouds         = createTagClouds;
        $scope.clearTagSelection       = clearTagSelection;
        $scope.clearMilestoneSelection = clearMilestoneSelection;
        $scope.openOverview            = openOverview;
        $scope.addTagToSelection       = addTagToSelection;
        $scope.addMileStoneToSelection = addMileStoneToSelection;
        $scope.instantiateFromTemplate = instantiateFromTemplate;

        $scope.toggleRefresh = toggleRefresh;

        $scope.arrayToComaSeparatedString = arrayToComaSeparatedString;
      }
  );

  app.controller('tagController', function ($scope, $http, $window) {
        // =================================================
        // init stuff... get data from backend
        // =================================================

        $http.get('rest/tags')
             .success(function (data, status, headers, config) {
               load(data);
             }).error(function (data, status, headers, config) {
          console.log('Error getting rest/tags');
        });

        function load(data) {
          $scope.rawData      = data.entries;
          $scope.beginLetters = [];
          $scope.groupedData  = {};
          // now calculate the begin letters and split
          for (var i = 0; i < $scope.rawData.length; i++) {
            var beginLetter = $scope.rawData[i].text.toUpperCase().substring(0, 1);
            if (!contains($scope.beginLetters, beginLetter)) {
              $scope.beginLetters.push(beginLetter);
              $scope.groupedData[beginLetter] = [];
            }
            $scope.groupedData[beginLetter].push($scope.rawData[i]);
          }
          $scope.beginLetters.sort();
          for (var i = 0; i < $scope.beginLetters.length; i++) {
            $scope.groupedData[$scope.beginLetters[i]].sort();
          }
          console.log($scope.beginLetters);
          console.log($scope.groupedData);
        }

        var contains = function (haystack, needle) {
          var indexOf = Array.prototype.indexOf;
          return indexOf.call(haystack, needle) > -1;
        };

        function editTag(tag) {
          $scope.selectedTag            = tag;
          $scope.selectedMergeCandidate = undefined;
          $scope.mergeCandidates        = [];
          for (var i = 0; i < $scope.rawData.length; i++) {
            var candidate = $scope.rawData[i].text;
            if (candidate !== tag) {
              $scope.mergeCandidates.push(candidate);
            }
          }
          $scope.mergeCandidates.sort();
          //$scope.selectedAction = undefined; // keep the last one, browser remembers the last selection as well.. user can still say no in the confirmation
          $scope.newName                = undefined;
          $scope.selectedMergeCandidate = undefined;
          $('#editModal').modal('show');
        }

        function actionOK() {
          if ($scope.selectedAction === undefined) return false;
          if ($scope.selectedAction === 'merge') return $scope.selectedMergeCandidate !== undefined;
          if ($scope.selectedAction === 'rename') return $scope.newName !== undefined;
          return true;
        }

        function setAction(action) {
          $scope.selectedAction = action;
          console.log("Selected action = " + action);
        }

        function checkEditState(modal) {
          if (actionOK()) {
            if (confirm("Are you sure you want to " + $scope.selectedAction + " " + $scope.selectedTag + "? This action cannot be undone...")) {
              var command = 'rest/tags/' + $scope.selectedTag;
              if ($scope.selectedAction === 'merge') {
                command += '?newName=' + $scope.selectedMergeCandidate;
              } else if ($scope.selectedAction === 'rename') {
                command += '?newName=' + $scope.newName;
              }
              $http.delete(command).success(function (data, status, headers, config) {
                $http.get('rest/tags')
                     .success(function (data, status, headers, config) {
                       load(data);
                     }).error(function (data, status, headers, config) {
                  console.log('Error getting rest/tags');
                });
              }).error(function (data, status, headers, config) {
                if (status === 409) {
                  alert(data.description);
                  console.log(data.description);
                } else {
                  console.log('Unexpected error in delete tag !');
                }
              });

            }
            $('#editModal').modal('hide');
            $('body').removeClass('modal-open');
            $('.modal-backdrop').remove();
          }
        }

        $scope.editTag        = editTag;
        $scope.actionOK       = actionOK;
        $scope.setAction      = setAction;
        $scope.checkEditState = checkEditState;
      }
  );
})();