(function () {
  "use strict";
  /**
   * Main controller for the phylogenomics pipeline page.
   * @param $http AngularJS http object
   * @constructor
   */
  function PhylogenomicsController($http) {
    var vm = this;
    /*
     * Whether or not the page is waiting for a response from the server.
     */
    vm.loading = false;

    /**
     * Launch the phylogenomics pipeline
     */
    vm.launch = function () {
      var
      // reference file id
      ref = Number(angular.element('option:selected').val()),
      // User defined name for the pipeline
      name = angular.element('#pipeline-name').val(),
      // All the selected sample single or pair-end files
      radioBtns = angular.element("input[type='radio']:checked"),
      // Holds all the ids for the selected single-end
      single = [],
      // Holds all the ids for the selected paired-end
      paired = [];

      if (name === null || name.length === 0) {
        vm.error = PIPELINE.required;
      } else {
        // Hide the launch buttons and display a message that it has been sent.
        vm.loading = true;

        // Get a list of paired and single end files to run.
        _.forEach(radioBtns, function (c) {
          c = $(c);
          if (c.attr('data-type') === 'single_end') {
            single.push(Number(c.val()));
          }
          else {
            paired.push(Number(c.val()));
          }
        });

        $http.post(PIPELINE.url, {ref: ref, single: single, paired: paired, name: name})
          .success(function (data) {
            if (data.result === 'success') {
              vm.success = true;
            }
            else {
              vm.error = data.error;
            }
          });
      }
    };
  }

  angular.module('irida.pipelines.phylogenomics', [])
    .controller('PhylogenomicsController', ['$http', PhylogenomicsController])
  ;
})();