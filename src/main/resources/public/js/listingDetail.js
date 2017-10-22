$(function() {

    var arr = window.location.href.split("/");
    var id = arr[arr.length -1].replace(".html");

    var listingsData = ko.observable([]);
    $.getJSON("/api/item/" + id, function(data){
        var ViewModel = function(){
            var self = this;
            self.item = data
        };

        var viewModel = new ViewModel()
        ko.applyBindings(viewModel);
    });
});