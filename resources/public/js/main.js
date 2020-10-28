var post = function($this, name, val) {
    if(!val) {val = $this.val();}
    data = {};
    data[name] = val;
    $.ajax( {
	type: "POST",
	url: "/" + name,
	data: JSON.stringify(data),
	success: console.log("ok"),
	contentType: "application/json; charset=utf-8",
	dataType: "json"
    });
}

$("#year").change(function() {
    post($(this), "year");
    location.reload();
});

$("#standard").change(function() {
    post($(this), "standard");
    location.reload();
});

$("#gas").change(function() {
    post($(this), "gas")
});
$("#mode").change(function() {
    post($(this), "mode")
});
$("#maintainer").change(function() {
    post($(this), "maintainer")
});
$("#reset").click(function() {
    post($(this), "reset", true)
    location.reload();
});    
