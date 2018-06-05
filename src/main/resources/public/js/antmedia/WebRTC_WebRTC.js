$( document ).ready(function() {
	$("#main").load("p/main.html", function(){
		$("#guide").load("g/webrtc_webrtc_guide.html");
		$("#left").load("p/webrtc_publisher.html");
		$("#right").load("p/webrtc_player.html");
		$("#footer").load("p/footer.html");
		$("#control").load("p/control.html", function(){
			initiate();
		});
	});
});

function initiate()
{
	initiateCommon(function (){
		streamId = "demo_webrtc_webrtc";
		playerURL = "http://"+server+":5080/WebRTCAppEE/player.html?name="+streamId;
		
		localWebRTCAdaptor = initiatePublisher();
		remoteWebRTCAdaptor = initiatePlayer();
	});
}

function camera()
{
	localWebRTCAdaptor.openCamera(function (){
		localWebRTCAdaptor.publish(streamId);
		timer = setInterval(function () {
	    	remoteWebRTCAdaptor.play(streamId);
	    	clearInterval(timer);
		}, 1000);   
	});
	
	createQR();
}

function playFile(file)
{
	
}

function stop()
{
	localWebRTCAdaptor.stop(streamId);
	localWebRTCAdaptor.turnOffLocalCamera();
	remoteWebRTCAdaptor.stop(streamId);
}