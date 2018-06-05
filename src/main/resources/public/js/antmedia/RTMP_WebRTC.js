$( document ).ready(function() {
	$("#main").load("p/main.html", function(){
		$("#guide").load("g/rtmp_webrtc_guide.html");
		$("#left").load("p/rtmp_publisher.html");
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
		streamId = "demo_rtmp_webrtc";
		playerURL = "http://"+server+":5080/WebRTCAppEE/player.html?name="+streamId;
		
		remoteWebRTCAdaptor = initiatePlayer();
	});
}

function playFile(file)
{
	uploadFile(file);
	playRTMP(file);
	streamFileRTMP(file.name);
}

function streamFileRTMP(fileName)
{
	$.ajax({
        url: "/rtmp_webrtc?name="+fileName, 
        success: function(result){
        	$("#controlStatus").html(fileName+ "is broadcasting with rtmp");
        	
        	timer = setInterval(function () {
        		remoteWebRTCAdaptor.play(streamId);
    	    	clearInterval(timer);
    		}, 3000);
        	
        	createQR();
        }
    });
}


function camera()
{
	
}

function stop()
{
	$.ajax({
        url: "/stop", 
        success: function(result){
        	stopRTMP();
        	remoteWebRTCAdaptor.stop(streamId);
        }
    });
}

