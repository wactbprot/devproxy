
var post = function($this, path, val, row) {
    data = {"value": val}
    if (typeof row !== 'undefined') {
	data["row"] = row;
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
