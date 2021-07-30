
var post = (path, data, callback) => { 
    if(! callback){callback = () => {console.log("ok")}}
    
    $.ajax( {
	type: "POST",
	url: "/" + path,
	data: JSON.stringify(data),
	success: callback,
	contentType: "application/json; charset=utf-8",
	dataType: "json"
    });
}
//----------------------------------
$("#year").change( e => {
    var $this = $(e.currentTarget);
    post( "year", {"value": $this.val()}, () => {location.reload();});
});
$("#standard").change( e => {
    var $this = $(e.currentTarget);
    post( "standard",{"value": $this.val()}, () => {location.reload();});
});
$("#n").change( e => {
    var $this = $(e.currentTarget);
    post( "n", {"value": $this.val()}, () => {location.reload();});
});

$("#gas").change( e => {
    var $this = $(e.currentTarget);
    post( "gas", {"value": $this.val()});
});

$("#mode").change( e => {
    var $this = $(e.currentTarget);
    post( "mode", {"value": $this.val()});
});

$("#maintainer").change( e => {
    var $this = $(e.currentTarget);
    post( "maintainer", {"value": $this.val()});
});

//----------------------------------
$(".run").click( e => {
    var $this = $(e.currentTarget),
	row = $this.data("row"),
	data = {"value": $("#task_" + row).val(), "row": row};
    $("#device-stdout_" + row).val("...will post to server");
    post( "run", data);
    
});

//----------------------------------
$(".device").change(e => {
    var $this = $(e.currentTarget),
	row =  $this.data("row"),
	data = {"value": $this.val(), "row": row};
    post( "device/"+ row, data, () => {location.reload(); });
});

//----------------------------------
$(".reset").click( e => {
    var $this = $(e.currentTarget),
	row = $this.data("row"),
	data = {"value": true, "row": row};
    post( "reset", data, () => {location.reload()});
});

$(".id").change( e => {
    var $this = $(e.currentTarget),
	data = {"value": $this.val(), "row": $this.data("row")};
    post( "id", data);
});

$(".branch").change( e => {
    var $this = $(e.currentTarget),
	data = {"value": $this.val(),"row": $this.data("row")};
    post( "branch", data);
});

$(".fullscale").change( e => {
    var $this = $(e.currentTarget),
	data = {"value": $this.val(),"row": $this.data("row")};
    post( "fullscale", data);
});

$(".port").change( e => {
    var $this = $(e.currentTarget),
	data = {"value": $this.val(),"row": $this.data("row")};
    post( "port", data);
});

$(".opx").change( e => {
    var $this = $(e.currentTarget),
	data = {"value": $this.val(),"row": $this.data("row")};
    post( "opx", data);
});

$(".defaults").change( e => {
    var $this = $(e.currentTarget),
	row = $this.data("row"),
        key = $this.data("key"),
	data = {"value": $this.val(), "row": row, "key": key};
    post( "default/" + row, data);
});

var ensure_type = (v, t) => {
    var ret = v;
    if (t == "string") ret = v;
    if (t == "float") ret = parseFloat(v);
    if (t == "integer") ret = parseInt(v);
    
    return ret
}

$(".input-value").change( e => {
    var $this = $(e.currentTarget),
	data = {"value": ensure_type($this.val(), $this.data("type")),
		"row": $this.data("row"),
		"key": $this.data("key"),
		"taskname": $this.data("taskname")};
    post( "man_input", data);
});

$(".ready-button").click( e => {
    var $this = $(e.currentTarget),
	data = {"value": true,
		"row": $this.data("row"),
		"key": "Ready",
		"taskname": $this.data("taskname")};
    post( "ready_button", data);
});
