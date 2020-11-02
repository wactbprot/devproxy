
var post = function($this, path, val, row, key) {
    data = {"value": val}
    if (typeof row !== 'undefined') {
	data["row"] = row;
    }

    if (typeof key !== 'undefined') {
	    data["key"] = key;
    }
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
    post($this, "year", $this.val());
    location.reload();
});
$("#standard").change(function() {
    var $this = $(this);
    post($this, "standard",$this.val());
    location.reload();
});
$("#gas").change(function() {
    var $this = $(this);
    post($this, "gas",$this.val())
});
$("#mode").change(function() {
    var $this = $(this);
    post($this, "mode",$this.val())
});

$("#maintainer").change(function() {
    var $this = $(this);
    post($this, "maintainer",$this.val())
});

//----------------------------------
$(".device").change(function() {
    var $this = $(this);
    row = $this.data("row")
    post($this, "device/"+ row, $this.val(), row)
    location.reload();
});

//----------------------------------
$(".reset").click(function() {
    var $this = $(this);
    row = $this.data("row")
    post($this, "reset", true, row)
    location.reload();
});

$(".id").change(function() {
    var $this = $(this),
	row = $this.data("row");
    post($this, "id", $this.val(), row);
});
$(".branch").change(function() {
    var $this = $(this),
	row = $this.data("row");
    post($this, "branch", $this.val(), row);
});
$(".fullscale").change(function() {
    var $this = $(this),
	row = $this.data("row");
    post($this, "fullscale", $this.val(), row);
});
$(".defaults").change(function() {
    var $this = $(this),
	row = $this.data("row"),
        key = $this.data("key");
    post($this, "default/" + row, $this.val(), row, key);
});
