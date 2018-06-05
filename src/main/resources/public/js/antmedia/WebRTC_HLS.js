$( document ).ready(function() {
	$("#main").load("p/main.html", function(){
		$("#guide").load("g/webrtc_hls_guide.html");
		$("#left").load("p/webrtc_publisher.html");
		$("#right").load("p/hls_player.html");
		$("#footer").load("p/footer.html");
		$("#control").load("p/control.html", function(){
			initiate();
		});
	});
});


function initiate()
{
	initiateCommon(function (){
		streamId = "demo_webrtc_hls";
		playerURL = "http://"+server+":5080/WebRTCAppEE/play.html?name="+streamId+"&autoplay=true";
		
		localWebRTCAdaptor = initiatePublisher();
	});
}

function camera()
{
	localWebRTCAdaptor.openCamera(function (){
		localWebRTCAdaptor.publish(streamId);
		srcFile = 'http://'+server+':5080/WebRTCAppEE/streams/'+streamId+'_adaptive.m3u8';
		playHLS(srcFile);   
		createQR();
	});
}

function playFile(file)
{
	
}
function stop()
{
	localWebRTCAdaptor.stop(streamId);
	localWebRTCAdaptor.turnOffLocalCamera();
	stopHLS();
}