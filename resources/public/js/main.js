$("#year").change(function() {
    var year = $(this).val();
    if(! year.startsWith("select")){
	$.ajax({
	    type: "POST",
	    url: "/year",
	    data: {"year": year},
	    success: console.log("ok"),
	    dataType: "json"
	});
    }
});
