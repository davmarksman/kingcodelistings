$(function() {

    var listingsData = ko.observable([]);
    $.getJSON("/api/list", function(data){
        listingsData(data)
    });

    var ViewModel = function(){
        var self = this;
//
//        var a = {
//            title:"hi",
//            message: "message text",
//            imagePath: "/api/image/1"
//        }
//        self.listing = [a,a];
        self.listing = listingsData;
    };

    var viewModel = new ViewModel()


    ko.applyBindings(viewModel);
});