!function(e,t){function n(){var e=this;e.getZipFile=function(e){var t="hiddenDownloader",n=document.getElementById(t);null===n&&(n=document.createElement("iframe"),n.id=t,n.style.display="none",document.body.appendChild(n)),n.src=ANALYSIS_PAGE.URL.download+e}}function o(e,t){function n(){return e.get(ANALYSIS_PAGE.URLS.status).then(function(e){return e.data})}function o(e){var o=t(function(){n().then(function(n){100===parseInt(n.percentComplete)&&(t.cancel(o),window.location.reload()),e(n)})},5e3)}var r=this;return r.getAnalysisState=function(e){n().then(function(t){e(t),100!==parseInt(t.percentComplete)&&o(e)})},r}function r(e){function t(){return e.getAnalysisState(function(e){o.state=e.state,o.stateLang=e.stateLang,o.percentage=parseFloat(e.percentComplete),o.stateClass=n(o.state)})}function n(e){return"analysis__alert--"+e.toLowerCase()}var o=this;o.percentage=0,t()}e.module("irida.analysis",[]).service("AnalysisService",["$http","$interval",o]).directive("phylocanvas",function c(){return{restrict:"E",transclude:!0,replace:!0,template:"<div><div ng-transclude></div>{{shape}}</div>",scope:{shape:"@"},controller:["$scope",function(e){e.shape=shape}]}}).directive("phylocanvasControls",function(){return{require:"^phylocanvas",replace:!0,transclude:!0,template:'<div><div class="btn-group" role="group"><button ng-repeat="control in controls" ng-click="select(control)" class="btn btn-default btn-sm" ng-class="{active: control.selected}">{{control.text}}</button></div><div ng-transclude></div></div>',restrict:"E",controller:["$scope",function(t){t.controls=[],t.shape="circular",t.select=function(n){e.forEach(t.controls,function(t){t.selected=e.equals(n,t)})},this.addControl=function(e){0===t.controls.length&&(e.selected=!0),t.controls.push(e)}}]}}).directive("phylocanvasControl",function(){return{require:"^phylocanvasControls",restrict:"E",replace:!0,template:"",scope:{shape:"@",text:"@"},link:function(e,t,n,o){o.addControl(e)}}}).directive("phylocanvasBody",function(){return{require:"^phylocanvas",scope:{newick:"@",id:"@"},link:function(n,o){e.element(o).css({height:"500px",width:"100%"});var r=new t.Tree(n.id,{});r.load(n.newick),n.$watch(function(){return n.$parent.shape},function(e,t){console.log(e,t),t!==e&&r.setTreeType(e)})}}}).controller("FileDownloadController",[n]).controller("StateController",["AnalysisService",r])}(window.angular,window.PhyloCanvas);