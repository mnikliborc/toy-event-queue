var ws = null;
function connect() {
	var ws = new WebSocket("ws://localhost:9000/connect");
	
	ws.onopen = function(){
		console.log("Websocket open")
	};
	ws.onmessage = function(evt){ 
	   var received_msg = evt.data;
	   $("#events").append("<li>"+received_msg+"</li>")
	};
	ws.onclose = function() {
	   console.log("Connection is closed..."); 
	};
	
	return ws;
}

$(function(){
	$("button#auth").click(function(){
		var username = $("#username").val();
		$.get("/auth/" + username, function() {
			ws = connect();
		});
	});
	
	$("button#register").click(function(){
		var username = $("#username").val();
		var eventType = $("#eventType").val();
		$.get("/events/register/" + username +"/" + eventType);
	});
	
	$("button#unregister").click(function(){
		var username = $("#username").val();
		var eventType = $("#eventType").val();
		$.get("/events/unregister/" + username +"/" + eventType);
	});
	
	$("button#send").click(function(){
		var eventType = $("#eventType").val();
		ws.send(eventType);
	});
	
	var username = $("#username").val();
	if (username != "") {
		ws = connect();
	}
});
