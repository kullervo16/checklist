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

  app.config(['$httpProvider', function ($httpProvider) {

    $httpProvider.interceptors.push(function authInterceptor($q) {
      return {
        request: function (config) {
          if (window._keycloak.token) {
            var deferred = $q.defer();
            window._keycloak.updateToken(30).success(function () {
              config.headers               = config.headers || {};
              config.headers.Authorization = 'Bearer ' + window._keycloak.token;

              deferred.resolve(config);
            }).error(function () {
              location.reload();
            });
            return deferred.promise;
          } else {
            return config;
          }
        }
      };
    });

    $httpProvider.interceptors.push(function errorInterceptor($rootScope, $q) {
      return {
        responseError: function (response) {
          if (!response.config.ignoreAuthModule) {
            switch (response.status) {
              case 0: // cors issue
              case 401:
                console.log("Received 401.... re-authenticate");
                var deferred = $q.defer();
                $rootScope.$broadcast('event:auth-loginRequired', response);
                return deferred.promise;
              case 403:
                $rootScope.$broadcast('event:auth-forbidden', response);
                break;
            }
          }
          return $q.reject(response);
        }
      };
    });
  }]);

  app.config(['$httpProvider', function ($httpProvider) {
    var token                                              = window._keycloak.token;
    $httpProvider.defaults.headers.common['Authorization'] = 'BEARER ' + token;
  }]);


  app.filter("htmlEncode", ['$sce', function ($sce) {
    return function (htmlCode) {
      return htmlCode.replace(/&/g, "&amp;").replace(/</g, "&lt;");
    }
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
      var newText     = "";
      var protocolPos = text.indexOf(protocol);
      while (protocolPos >= 0) {
        newText += text.substring(0, protocolPos);
        var url              = text.substring(protocolPos);
        var nextSpacePos     = url.indexOf(" ");
        var nextEndOfLinePos = url.indexOf("\n");
        var endOfUrlPos      = nextSpacePos < nextEndOfLinePos ? nextSpacePos : nextEndOfLinePos;
        if (endOfUrlPos > 0) {
          url = url.substring(0, endOfUrlPos);
        }
        newText += "<a href=\"" + url + "\" target=\"_blank\">" + url + "</a>";
        text        = text.substring(protocolPos + url.length);
        protocolPos = text.indexOf(protocol);
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

    $scope.isAdmin     = isAdmin;
    $scope.canModify   = canModify;
    $scope.getUserName = getUserName;
    $scope.getRoles    = getRoles;
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
          if (step.id == getStepIdFromHash()) {
            return "list-group selectedStep";
          } else {
            return "list-group";
          }
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
        $scope.filter               = '';
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
          var stepClass = "step unknown";
          if (step.state === 'OK') {
            stepClass = "step ok";
          } else if (step.state === 'EXECUTED') {
            stepClass = "step executed";
          } else if (step.state === 'IN_PROGRESS') {
            stepClass = "step inProgress";
          } else if (step.state === 'NOT_YET_APPLICABLE') {
            stepClass = "step notYetApplicable";
          } else if (step.state === 'NOT_APPLICABLE') {
            stepClass = "step notApplicable";
          } else if (step.state === 'ABORTED') {
            stepClass = "step aborted";
          } else if (step.state === 'EXECUTION_FAILED'
                     || step.state === 'CHECK_FAILED'
                     || step.state === 'CHECK_FAILED_NO_COMMENT'
                     || step.state === 'EXECUTION_FAILED_NO_COMMENT') {
            stepClass = "step nok";
          }
          if (step.id == getStepIdFromHash()) {
            return stepClass + " selectedStep";
          } else {
            return stepClass;
          }
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

        function gotoChecklist(cl,stepId) {
          $window.location = 'checklist.html?id=' + cl + (stepId == null ? "" : "#" + stepId);
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

        function repositionToLastUpdatedStep() {

          var lastUpdatedStep = null;

          for (var i = 0; i < $scope.data.steps.length; i++) {

            var step = $scope.data.steps[i];

            if (step.lastUpdate != null) {

              if (lastUpdatedStep == null || step.lastUpdate >= lastUpdatedStep.lastUpdate) {
                lastUpdatedStep = step;
              }
            }
          }

          if (lastUpdatedStep != null) {
            repositionTo(lastUpdatedStep.id);
          }
        }

        function repositionTo(stepId) {

          if (stepId == null) {
            $window.location.hash = "";
          } else {
            $window.location.hash = stepId;
          }

          $anchorScroll();
        }

        function repositionIfNeeded(step) {
          if (step.id === getLastStep().id) {
            if ($scope.hasToBeRepositionedToStep != null) {
              repositionTo($scope.hasToBeRepositionedToStep)
              $scope.hasToBeRepositionedToStep = null;
            }
          }
          // Return true to allow this function to be in ng-if with other functions (separated by &&)
          return true;
        }

        function repositionToStep(step) {

          if (step == null) {
            $window.location.hash = "";
          } else {
            $window.location.hash = step.id;
          }

          $anchorScroll();
        }

        function repositionToNextStep() {

          var stepId = $window.location.hash;

          if ("" === stepId || "#" === stepId) {
            stepId = null;
          } else {
            stepId = stepId.slice(1);
          }

          repositionTo(getStepAfter(getStepById(stepId)).id);
        }

        function repositionToPreviousStep() {

          var stepId = $window.location.hash;

          if ("" === stepId) {
            stepId = null;
          } else {
            stepId = stepId.slice(1);
          }

          repositionTo(getStepBefore(getStepById(stepId)).id);
        }

        function repositionToNextUnfinishedStep() {
          repositionToStep(getUnfinishedStepAfter(getStepIdFromHash()));
        }

        function repositionToPreviousUnfinishedStep() {
          repositionToStep(getUnfinishedStepBefore(getStepById(getStepIdFromHash())));
        }

        function getStepIdFromHash() {

          var stepId = $window.location.hash;

          return "" === stepId || "#" === stepId ? null : stepId.slice(1);
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

        // Apply the "normal" action to the selected step
        function applyNormalAction() {

          // Find the selected step
          var step = getStepById(getStepIdFromHash());

          // If no step selected
          if (step == null) {
            return;
          }

          // If the step is an action
          if (step.action != null) {

            // If the status is UNKNOWN and if the action is startable
            if (step.state === "UNKNOWN" && step.actionExpected === true) {
              startAction(step);
              return;
            }

            if (step.state === "IN_PROGRESS") {
              updateAction(step, true);
              return;
            }

            if (step.state === "EXECUTED") {
              setCheckResult(step, null, true);
              return;
            }

            return;
          }

          // If the step is a question
          if (step.question != null) {

            if (step.complete) {
              return;
            }

            if (step.answerType === "text") {
              document.getElementById(step.id + "_textarea").focus();
              return;
            }

            if (step.answerType === "onlyOne") {

              var selectedOption;
              var selectedElement;
              var nbOptions = step.options.length;

              for (var i = 0; i < nbOptions; i++) {

                var optionElementWalker = document.getElementById(step.id + "_option_" + i);

                if (optionElementWalker.checked) {
                  selectedOption  = i;
                  selectedElement = optionElementWalker;
                  i               = nbOptions;
                }
              }

              if (selectedOption == null) {
                selectedElement         = document.getElementById(step.id + "_option_0");
                selectedElement.checked = true;
              }

              if (selectedElement != null) {
                selectedElement.click();
                selectedElement.focus();
              }

              return;
            }

            if (step.answerType === "multiple") {
              document.getElementById(step.id + "_checkbox_0").focus();
            }

            return;
          }

          if (step.subChecklist != null) {

            // If this checklist has already been instancied
            if (step.child == null) {

              if (step.actionExpected) {
                launchSubChecklist(step);
              }
            } else {
              gotoChecklist(step.child);
            }
          }
        }

        function selectNextRadioButton(optionElement) {

          var cleanedElementId = optionElement.id.replace("_option", "");
          var stepId           = cleanedElementId.substring(0, cleanedElementId.lastIndexOf("_"));
          var optionIndex      = cleanedElementId.substring(cleanedElementId.lastIndexOf("_") + 1);
          var step             = getStepById(stepId);

          optionIndex++;

          if (optionIndex >= step.options.length) {
            optionIndex = 0;
          }

          var newOptionElement = document.getElementById(stepId + "_option_" + optionIndex);
          newOptionElement.click();
          newOptionElement.focus();
        }

        function selectPreviousRadioButton(optionElement) {

          var cleanedElementId = optionElement.id.replace("_option", "");
          var stepId           = cleanedElementId.substring(0, cleanedElementId.lastIndexOf("_"));
          var optionIndex      = cleanedElementId.substring(cleanedElementId.lastIndexOf("_") + 1);
          var step             = getStepById(stepId);

          optionIndex--;

          if (optionIndex < 0) {
            optionIndex = step.options.length - 1;
          }

          var newOptionElement = document.getElementById(stepId + "_option_" + optionIndex);
          newOptionElement.click();
          newOptionElement.focus();
        }

        function selectNextCheckbox(checkbox) {

          var cleanedElementId = checkbox.id.replace("_checkbox", "");
          var stepId           = cleanedElementId.substring(0, cleanedElementId.lastIndexOf("_"));
          var optionIndex      = cleanedElementId.substring(cleanedElementId.lastIndexOf("_") + 1);
          var step             = getStepById(stepId);

          optionIndex++;

          if (optionIndex >= step.options.length) {
            optionIndex = 0;
          }

          var newCheckbox = document.getElementById(stepId + "_checkbox_" + optionIndex);
          newCheckbox.focus();
        }

        function selectPreviousCheckbox(checkbox) {

          var cleanedElementId = checkbox.id.replace("_checkbox", "");
          var stepId           = cleanedElementId.substring(0, cleanedElementId.lastIndexOf("_"));
          var optionIndex      = cleanedElementId.substring(cleanedElementId.lastIndexOf("_") + 1);
          var step             = getStepById(stepId);

          optionIndex--;

          if (optionIndex < 0) {
            optionIndex = step.options.length - 1;
          }

          var newCheckbox = document.getElementById(stepId + "_checkbox_" + optionIndex);
          newCheckbox.focus();
        }

        // =================================================
        // Backend update operations
        // =================================================

        function startAction(step) {
          $http.put('rest/checklists/' + $location.search().id + "/" + step.id + "/start")
               .success(function (data, status, headers, config) {
                 $scope.data = data;
               }).error(function (data, status, headers, config) {
            console.log('Error starting step ' + step.id);
          });
        }

        function getStepAfter(step) {

          var firstStep = $scope.data.steps[0];

          if (step == null) {
            return firstStep;
          }

          for (var i = 0; i < $scope.data.steps.length; i++) {

            var stepWalker = $scope.data.steps[i];

            if (stepWalker.id === step.id) {

              if (i < $scope.data.steps.length - 1) {
                return $scope.data.steps[++i];
              } else {
                return firstStep;
              }
            }
          }

          // We should never reach this line
          return firstStep;
        }

        function getStepBefore(step) {

          // If no step is provided, we return the last step
          if (step == null) {
            return getLastStep();
          }

          var previousStep = getLastStep();

          for (var i = 0; i < $scope.data.steps.length; i++) {

            var stepWalker = $scope.data.steps[i];

            if (stepWalker.id === step.id) {
              return previousStep;
            }

            previousStep = stepWalker;
          }

          // We should never reach this code
          return getLastStep();
        }

        function getLastStep() {
          return $scope.data.steps[$scope.data.steps.length - 1];
        }

        function getUnfinishedStepAfter(stepId, data) {

          if (data == null) {
            data = $scope.data;
          }

          var i = getStepPosById(stepId);

          if (i == null) {
            return getFirstUnfinishedStep();
          }

          for (i++; i < data.steps.length; i++) {

            var stepWalker = data.steps[i];

            if (isActionExpected(stepWalker)) {
              return stepWalker;
            }
          }

          return getFirstUnfinishedStep();
        }

        function getUnfinishedStepBefore(step) {

          var i = getStepPos(step);

          if (i == null) {
            i = $scope.data.steps.length;
          }

          for (i--; i >= 0; i--) {

            var stepWalker = $scope.data.steps[i];

            if (isActionExpected(stepWalker)) {
              return stepWalker;
            }
          }

          return step == null ? null : getUnfinishedStepBefore(null);
        }

        function getFirstUnfinishedStep(data) {

          if (data == null) {
            data = $scope.data;
          }

          for (var i = 0; i < data.steps.length; i++) {

            var stepWalker = data.steps[i];

            if (isActionExpected(stepWalker)) {
              return stepWalker;
            }
          }

          return null;
        }

        function isActionExpected(step) {
          return step.actionExpected;
        }

        function getStepById(stepId, data) {

          if (data == null) {
            data = $scope.data;
          }

          if (stepId == null) {
            return null;
          }

          for (var i = 0; i < data.steps.length; i++) {

            var stepWalker = data.steps[i];

            if (stepWalker.id === stepId) {
              return stepWalker;
            }
          }

          return null;
        }

        function getStepPos(step) {

          if (step == null) {
            return null;
          }

          return getStepPosById(step.id);
        }

        function getStepPosById(stepId) {

          if (stepId == null) {
            return null;
          }

          for (var i = 0; i < $scope.data.steps.length; i++) {

            if ($scope.data.steps[i].id === stepId) {
              return i;
            }
          }

          return null;
        }

        /**
         * Action result for a step, send to the backend and reload.
         */
        function updateAction(step, result) {
          $http.put('rest/checklists/' + $location.search().id + "/" + step.id + "/actionresults/" + result)
               .success(function (data, status, headers, config) {
                 $scope.data = data;
                 if (getStepIdFromHash() === step.id && getStepById(step.id).complete) {
                   repositionToNextUnfinishedStep();
                 }
               }).error(function (data, status, headers, config) {
            console.log('Error updating step ' + step.id);
          });
        }

        function addErrorAction(step, error) {
          $http.post('rest/checklists/' + $location.search().id + "/" + step.id + '/errors', error)
               .success(function (data, status, headers, config) {
                 $scope.data = data;
               }).error(function (data, status, headers, config) {
            console.log('Error updating step ' + step.id);
          });
        }

        function setAnswers(step, answer) {
          $http.put('rest/checklists/' + $location.search().id + "/" + step.id + '/answers', answer)
               .success(function (data, status, headers, config) {
                 setHasToBeRepositionedToStepIfNeeded(step, data);
                 $scope.data = data;
               }).error(function (data, status, headers, config) {
            console.log('Error updating step ' + step.id);
          });
        }

        function setAnswersByRadioButton(radioButton) {

          var cleanedElementId = radioButton.id.replace("_option", "");
          var stepId           = cleanedElementId.substring(0, cleanedElementId.lastIndexOf("_"));
          var step             = getStepById(stepId);

          setAnswers(step, step.answers[0]);
        }

        function setAnswersByCheckbox(checkbox) {

          var cleanedElementId = checkbox.id.replace("_checkbox", "");
          var stepId           = cleanedElementId.substring(0, cleanedElementId.lastIndexOf("_"));
          var step             = getStepById(stepId);

          setAnswers(step, step.answers);
        }

        function setHasToBeRepositionedToStepIfNeeded(step, data) {
          if (getStepById(step.id, data).complete && step.id === getStepIdFromHash()) {
            $scope.hasToBeRepositionedToStep = getUnfinishedStepAfter(step.id, data).id;
          }
        }

        function updateAnswer(step, answer, event) {
          if (event.target.checked) {
            if (step.answers.indexOf(answer) === -1) {
              step.answers.push(answer);
            }
          } else {
            var indexOfAnswer = step.answers.indexOf(answer);
            if (indexOfAnswer !== -1) {
              step.answers.splice(indexOfAnswer, 1);
            }
          }
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
                 }
               }).error(function (data, status, headers, config) {
            console.log('Error adding a tag ' + step.id);
          });
        }

        // If check === -1, then update the next check
        function setCheckResult(step, check, result) {

          if (!(step.id in $scope.checkResults)) {
            $scope.checkResults[step.id] = {};
          }

          var stepMap = $scope.checkResults[step.id];

          if (check == null) {
            // A (probably not necessary) test to make sure that all checks are not yet done
            if (Object.keys(stepMap).length < step.checks.length) {
              // Look for the first check that is not in the done checks
              for (var i = 0; i < step.checks.length; i++) {
                // If the check has not yet been done
                if (stepMap[step.checks[i]] == null) {
                  stepMap[step.checks[i]] = result;
                  // Skip the loop
                  i                       = step.checks.length;
                }
              }
            }
          } else {
            stepMap[check] = result;
          }

          if (Object.keys(stepMap).length === step.checks.length) {
            // all results are in... update the backend
            var combinedResult = true;
            for (var key in $scope.checkResults[step.id]) {
              combinedResult &= $scope.checkResults[step.id][key];
            }
            $http.put('rest/checklists/' + $location.search().id + "/" + step.id + "/checkresults/" + (combinedResult === 1))
                 .success(function (data, status, headers, config) {
                   $scope.data = data;
                   if (getStepIdFromHash() === step.id && getStepById(step.id).complete) {
                     repositionToNextUnfinishedStep();
                   }
                 }).error(function (data, status, headers, config) {
              console.log('Error updating step ' + step.id);
            });
          }
        }

        function canModify() {
          for (var i = 0; i < $window._keycloak.realmAccess.roles.length; i++) {
            if ($window._keycloak.realmAccess.roles[i] === 'modify') {
              return true;
            }
          }
          return false;
        }

        function launchSubChecklist(step) {

          if (canModify() && step.actionExpected) {

            $http.post('rest/checklists' + step.subChecklist + '?parent=' + $location.search().id + '&step=' + step.id)
                 .success(function (data, status, headers, config) {
                   $window.location.href = './checklist.html?id=' + data;
                 })
                 .error(function (data, status, headers, config) {
                   console.log('Error creating new checklist');
                 });
          }
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
          if( step != null && step.reopenable) {
            $scope.checkResults[step.id] = {};
            $http.put('rest/checklists/' + $location.search().id + "/" + step.id + '/reopen')
                 .success(function (data, status, headers, config) {
                   $scope.data = data;
                 }).error(function (data, status, headers, config) {
              console.log('Error updating step ' + step.id);
            });
          }
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
        
        function filterTags() {            
            filterAndDisplayTagData();
        }

        function addTagToSelection(tag) {
          if ($scope.tagSelection === '') {
            $scope.tagSelection = tag.innerText;
          } else {
            $scope.tagSelection = $scope.tagSelection + "," + tag.innerText;
          }
          $scope.filter = '';
          
          createTagCloud('rest/tags?filter=' + $scope.tagSelection);
        }
        
        function filterAndDisplayTagData() {
            var data = [];
            var nbEntries = $scope.rawTags.entries.length;
            for (var i = 0; i < nbEntries; i++) {
                if($scope.filter === '' || $scope.rawTags.entries[i].text.indexOf($scope.filter) >= 0) {
                    data.push($scope.rawTags.entries[i]);
                }
            }
            
            nbEntries       = data.length;            
            if (nbEntries == 0) {
               openOverview();
            }
            $('#tags').empty();
             $('#tags').jQCloud('destroy');
             for (var i = 0; i < nbEntries; i++) {
               var entriesWalker                  = data[i];
               entriesWalker['handlers']          = {};
               entriesWalker['handlers']['click'] = function () {
                 angular.element('#tags').scope().addTagToSelection(this)
               };
             }
             $('#tags').jQCloud(data);
        }

        function createTagCloud(path) {
          $http.get(path)
               .success(function (data, status, headers, config) {                 
                 $scope.rawTags = data;
                 $scope.tagSelection = $scope.arrayToComaSeparatedString(data.selection);                                  
                 filterAndDisplayTagData();
                 
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
                     repositionToLastUpdatedStep();
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
        $scope.setAnswers         = setAnswers;
        $scope.updateAnswer       = updateAnswer;
        $scope.setCheckResult     = setCheckResult;
        $scope.addTag             = addTag;
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
        $scope.filterTags              = filterTags;
        $scope.addTagToSelection       = addTagToSelection;
        $scope.addMileStoneToSelection = addMileStoneToSelection;
        $scope.instantiateFromTemplate = instantiateFromTemplate;

        $scope.toggleRefresh                      = toggleRefresh;
        $scope.getStepById                        = getStepById;
        $scope.repositionIfNeeded                 = repositionIfNeeded;
        $scope.repositionToNextStep               = repositionToNextStep;
        $scope.repositionToPreviousStep           = repositionToPreviousStep;
        $scope.repositionToNextUnfinishedStep     = repositionToNextUnfinishedStep;
        $scope.repositionToPreviousUnfinishedStep = repositionToPreviousUnfinishedStep;
        $scope.applyNormalAction                  = applyNormalAction;
        $scope.selectNextRadioButton              = selectNextRadioButton;
        $scope.selectPreviousRadioButton          = selectPreviousRadioButton;
        $scope.setAnswersByRadioButton            = setAnswersByRadioButton;
        $scope.selectNextCheckbox                 = selectNextCheckbox;
        $scope.selectPreviousCheckbox             = selectPreviousCheckbox;
        $scope.setAnswersByCheckbox               = setAnswersByCheckbox;
        $scope.getStepIdFromHash                  = getStepIdFromHash;

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