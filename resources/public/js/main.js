$("#year").change(function() {
    var year = $(this).val();
    if(! year.startsWith("select")){
	$.ajax({
	    type: "POST",
	    url: "/year",
	    data: JSON.stringify({"year": year}),
	    success: console.log("ok"),
	    contentType: "application/json; charset=utf-8",
	    dataType: "json"
	});
    }
});
