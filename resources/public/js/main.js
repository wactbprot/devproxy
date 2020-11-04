
var post = function($this, path, data) {
    $.ajax( {
	type: "POST",
	url: "/" + path,
	data: JSON.stringify(data),
	success: console.log("ok"),
	contentType: "application/json; charset=utf-8",
	dataType: "json"
    });
}
//----------------------------------
$("#year").change(function() {
    var $this = $(this);
    post($this, "year", {"value": $this.val()} );
    location.reload();
});
$("#standard").change(function() {
    var $this = $(this);
    post($this, "standard",{"value": $this.val()});
    location.reload();
});
$("#n").change(function() {
    var $this = $(this);
    post($this, "n", {"value": $this.val()} );
    location.reload();
});

$("#gas").change(function() {
    var $this = $(this);
    post($this, "gas", {"value": $this.val()})
});
$("#mode").change(function() {
    var $this = $(this);
    post($this, "mode", {"value": $this.val()})
});

$("#maintainer").change(function() {
    var $this = $(this);
    post($this, "maintainer",{"value": $this.val()})
});

//----------------------------------
$(".run").click(function() {
    var $this = $(this),
	row = $this.data("row");
    post($this, "run", {"value": $("#task_" + row).val(),
			"row": row});
    $("#device-stdout_" + row).val("...post to server");
});

//----------------------------------
$(".device").change(function() {
    var $this = $(this),
	row = $this.data("row");
    post($this, "device/"+ row, {"value": $this.val(),
				 "row": row})
    location.reload();
});

//----------------------------------
$(".reset").click(function() {
    var $this = $(this),
	row = $this.data("row");
    post($this, "reset", {"value": true,
			  "row": row})
    location.reload();
});

$(".id").change(function() {
    var $this = $(this),
	row = $this.data("row");
    post($this, "id", {"value": $this.val(),
		       "row": row});
});
$(".branch").change(function() {
    var $this = $(this),
	row = $this.data("row");
    post($this, "branch", {"value": $this.val(),
			   "row": row});
});
$(".fullscale").change(function() {
    var $this = $(this),
	row = $this.data("row");
    post($this, "fullscale", {"value": $this.val(),
			      "row": row});
});
$(".defaults").change(function() {
    var $this = $(this),
	row = $this.data("row"),
        key = $this.data("key");
    post($this, "default/" + row, {"value": $this.val(),
				   "row": row,
				   "key": key});
});
