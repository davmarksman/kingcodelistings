$(function() {
    var ViewModel = function(){
        var self = this;

        var a = {
            title:"hi",
            message: "message text",
            imagePath: "/api/image/1"
        }
        self.listing = [a,a];


    };

    var viewModel = new ViewModel()


    ko.applyBindings(viewModel);
});