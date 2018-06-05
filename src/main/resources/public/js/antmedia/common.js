var websocketURL;
var streamId;
var server;
var playerURL;
var localWebRTCAdaptor;
var remoteWebRTCAdaptor;
var hlsPlayer;

function initiateCommon(onSuccess)
{
	$.get("/settings/server", function(data, status){
		server = data;	
		websocketURL = "ws://" + server + ":8081/WebRTCAppEE";
		if (location.protocol.startsWith("https")) {
			websocketURL = "wss://" + server + ":8082/WebRTCAppEE";
		}
		
		console.log("server @ "+server);
		$("span#serverAdr").html(server);
		onSuccess();
    });
}

function initiatePublisher(){
	var sdpConstraints = {
			OfferToReceiveAudio : false,
			OfferToReceiveVideo : false

	};

	var mediaConstraints = {
			video : true,
			audio : true
	};

	var pc_config = null;

	var webRTCAdaptor = new WebRTCAdaptor({
		websocket_url : websocketURL,
		mediaConstraints : mediaConstraints,
		peerconnection_config : pc_config,
		sdp_constraints : sdpConstraints,
		localVideoId : "localVideo",
		debug:true,
		callback : function(info, description) {
			if (info == "initialized") {
				console.log("initialized");
			} else if (info == "publish_started") {
				//stream is being published
				console.log("publish started");
			} else if (info == "publish_finished") {
				//stream is being finished
				console.log("publish finished");
			}
			else if (info == "closed") {
				//console.log("Connection closed");
				if (typeof description != "undefined") {
					console.log("Connecton closed: " + JSON.stringify(description));
				}
			}
		},
		callbackError : function(error, message) {
			//some of the possible errors, NotFoundError, SecurityError,PermissionDeniedError

			console.log("error callback: " +  JSON.stringify(error));
			var errorMessage = JSON.stringify(error);
			if (typeof message != "undefined") {
				errorMessage = message;
			}
			var errorMessage = JSON.stringify(error);
			if (error.indexOf("NotFoundError") != -1) {
				errorMessage = "Camera or Mic are not found or not allowed in your device";
			}
			else if (error.indexOf("NotReadableError") != -1 || error.indexOf("TrackStartError") != -1) {
				errorMessage = "Camera or Mic is being used by some other process that does not let read the devices";
			}
			else if(error.indexOf("OverconstrainedError") != -1 || error.indexOf("ConstraintNotSatisfiedError") != -1) {
				errorMessage = "There is no device found that fits your video and audio constraints. You may change video and audio constraints"
			}
			else if (error.indexOf("NotAllowedError") != -1 || error.indexOf("PermissionDeniedError") != -1) {
				errorMessage = "You are not allowed to access camera and mic.";
			}
			else if (error.indexOf("TypeError") != -1) {
				errorMessage = "Video/Audio is required";
			}

			alert(errorMessage);
		}
	});

	return webRTCAdaptor;
}

function initiatePlayer(){
	var pc_config = null;

	var sdpConstraints = {
		OfferToReceiveAudio : true,
		OfferToReceiveVideo : true

	};
	var mediaConstraints = {
		video : false,
		audio : false
	};
	
	var webRTCAdaptor = new WebRTCAdaptor({
		websocket_url : websocketURL,
		mediaConstraints : mediaConstraints,
		peerconnection_config : pc_config,
		sdp_constraints : sdpConstraints,
		remoteVideoId : "remoteVideo",
		isPlayMode: true,
		debug: true,
		callback : function(info, description) {
			if (info == "initialized") {
				console.log("initialized");
			} else if (info == "play_started") {
				//joined the stream
				console.log("play started");
			
			} else if (info == "play_finished") {
				//leaved the stream
				console.log("play finished");
			}
			else if (info == "closed") {
				//console.log("Connection closed");
				if (typeof description != "undefined") {
					console.log("Connecton closed: " + JSON.stringify(description));
				}
			}
		},
		callbackError : function(error) {
			//some of the possible errors, NotFoundError, SecurityError,PermissionDeniedError

			console.log("error callback: " + JSON.stringify(error));
			alert(JSON.stringify(error));
		}
	});
	return webRTCAdaptor;
}


function playRTMP(file)
{
	var type = file.type;
	//rtmpPlayer = document.querySelector('video');
	var videoNode = $('#rtmp').get(0);
	var canPlay = videoNode.canPlayType(type);
	if (canPlay)
	{
		var fileURL = URL.createObjectURL(file);
		videoNode.src = fileURL;
		$("#publisherStatus").html("Playing from local");
	}
}

function stopRTMP()
{
	$('#rtmp').get(0).pause();
}

function playHLS(srcFile){
	
//  if (this.isEnterpriseEdition) {
//      srcFile = 'localhost:5080/LiveApp/streams/demo_rtmp_hls_adaptive.m3u8'; 
//  }
	
	var timer;
	var counter = 1;
	var clip = {
      sources: [{
          type: 'application/x-mpegurl',
          src: srcFile,
      }]
  };

	var playing = false;
	hlsPlayer = flowplayer('#video-player', {
              autoplay: true,
              live: true,
              title: "demo Stream",
              clip : clip,
          }).on("ready", function () {
          	playing = true;
          	clearInterval(timer);
          	$("#playerStatus").html("Live Playing");
            });
      
  timer = setInterval(function () {
  	if(!playing)
  		{
  			$("#playerStatus").html("Trying to get stream..."+(counter++));
  			hlsPlayer.error = hlsPlayer.loading = false;

  			hlsPlayer.load(clip);
  		}
    }, 1000);    
}

function stopHLS()
{
	hlsPlayer.stop();
}

function createQR()
{
	var qrcode = new QRCode("qrcode", {
	    text: playerURL,
	    width: 128,
	    height: 128,
	    colorDark : "#000000",
	    colorLight : "#ffffff",
	    correctLevel : QRCode.CorrectLevel.H
	});
}

function uploadFile(file) {
	 var formData = new FormData();
     formData.append('file', file);
     $.ajax({
         url: "/upload",
         dataType: 'script',
         cache: false,
         contentType: false,
         processData: false,
         data: formData,
         type: 'post'
     });	
     //$.post("/upload", formData);
}