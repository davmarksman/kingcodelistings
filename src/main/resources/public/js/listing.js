$(function() {

    var listingsData = ko.observable([]);
    $.getJSON("/api/list", function(data){

        var count = 0;
        var currentRow = [];
        var all = [];
        for(var i = 0; i< data.length; i ++){
            count = count + 1;
            currentRow.push(data[i])
            if(count === 4){
                all.push(currentRow.slice(0));
                currentRow = [];
            }
        }
        if(count != 4){
            all.push(currentRow.slice(0));
        }

        listingsData(all)
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