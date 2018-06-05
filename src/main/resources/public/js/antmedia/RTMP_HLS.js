$( document ).ready(function() {
	$("#main").load("p/main.html", function(){
		$("#guide").load("g/rtmp_hls_guide.html");
		$("#left").load("p/rtmp_publisher.html");
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
		streamId = "demo_rtmp_hls";
		playerURL = "http://"+server+":5080/LiveApp/play.html?name="+streamId+"&autoplay=true";
	});
}


function playFile(file)
{
	uploadFile(file);
	playRTMP(file);
	streamFileRTMP(file.name);
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
        	stopHLS();
        }
    });
}

function streamFileRTMP(fileName)
{
	var srcFile = "http://"+server+":5080/LiveApp/streams/"+streamId+".m3u8";
	
	$.ajax({
        url: "/rtmp_hls?name="+fileName, 
        success: function(result){
        	$("#controlStatus").html(fileName+ " is broadcasting with rtmp");
        	
        	playHLS(srcFile);
        	createQR();
        }
    });
}
