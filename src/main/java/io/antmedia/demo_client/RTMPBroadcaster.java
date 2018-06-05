package io.antmedia.demo_client;
import static org.bytedeco.javacpp.avcodec.*;
import static org.bytedeco.javacpp.avformat.*;
import static org.bytedeco.javacpp.avutil.*;
import static org.bytedeco.javacpp.swscale.*;

import java.io.File;

import org.bytedeco.javacpp.*;
import org.bytedeco.javacpp.avcodec.AVCodecParameters;
import org.bytedeco.javacpp.avcodec.AVPacket;
import org.bytedeco.javacpp.avformat.AVFormatContext;
import org.bytedeco.javacpp.avformat.AVIOContext;
import org.bytedeco.javacpp.avformat.AVOutputFormat;
import org.bytedeco.javacpp.avutil.AVDictionary;




public class RTMPBroadcaster {
	public static RTMPBroadcaster instance = new RTMPBroadcaster();

	AVOutputFormat ofmt = null;
	AVFormatContext ifmt_ctx = new AVFormatContext(null);
	AVFormatContext ofmt_ctx = new AVFormatContext(null);
	AVPacket pkt = new AVPacket();
	String in_filename, out_filename;
	int ret, i;
	int videoindex=-1;
	int frame_index=0;
	long start_time=0;
	Thread thread;

	private boolean continueStreaming;	

	public void broadcast(String name, String outURL)
	{
		thread = new Thread()
		{
			public void run() {
				startBroadcasting(name, outURL);
			};
		};
		thread.start();
	}

	void startBroadcasting(String fileName, String outURL)
	{
		in_filename  = "temp/"+fileName;
		out_filename = outURL;
		continueStreaming =true;
		
		String[] token = fileName.split("\\.");
		String extension = token[token.length-1];
		
		System.out.println("Broadcast:"+in_filename+" to "+out_filename);
		
		av_register_all();
		//Network
		avformat_network_init();
		//Input
		if ((ret = avformat_open_input(ifmt_ctx, in_filename, av_find_input_format(extension), (AVDictionary) null)) < 0) {
			System.err.println("Could not open input file.");
			close();
			return;
		}
		
		if ((ret = avformat_find_stream_info(ifmt_ctx, (AVDictionary)null)) < 0) {
			System.err.println("Failed to retrieve input stream information");
			close();
			return;
		}
		
		for(i=0; i<ifmt_ctx.nb_streams(); i++) 
			if(ifmt_ctx.streams(i).codec().codec_type() == AVMEDIA_TYPE_VIDEO){
				videoindex=i;
				break;
			}

		av_dump_format(ifmt_ctx, 0, in_filename, 0);

		//Output

		avformat_alloc_output_context2(ofmt_ctx, null, "flv", out_filename); //RTMP
		//avformat_alloc_output_context2(&ofmt_ctx, NULL, "mpegts", out_filename);//UDP

		if (ofmt_ctx == null) {
			System.err.println("Could not create output context");
			ret = AVERROR_UNKNOWN;
			close();
			return;
		}
		
		ofmt = ofmt_ctx.oformat();
		for (i = 0; i < ifmt_ctx.nb_streams(); i++) {
			//Create output AVStream according to input AVStream
			AVStream in_stream = ifmt_ctx.streams(i);
			
			AVStream out_stream = avformat_new_stream(ofmt_ctx, in_stream.codec().codec());
			
			if (out_stream == null) {
				System.err.println("Failed allocating output stream");
				ret = AVERROR_UNKNOWN;
				close();
				return;
			}
			avcodec_parameters_copy(out_stream.codecpar(), in_stream.codecpar());
			
			//Copy the settings of AVCodecContext
			//ret = avcodec_copy_context(out_stream.codec(), in_stream.codec());
			if (ret < 0) {
				System.err.println( "Failed to copy context from input to output stream codec context");
				close();
				return;
			}
			out_stream.codecpar().codec_tag(0);
			if ((ofmt_ctx.oformat().flags() & AVFMT_GLOBALHEADER) != 0)
			{
				int tmp = out_stream.codec().flags();
				out_stream.codec().flags(tmp | CODEC_FLAG_GLOBAL_HEADER);
			}
		}
		
		//Dump Format------------------
		av_dump_format(ofmt_ctx, 0, out_filename, 1);
		AVIOContext pb = new AVIOContext(null);
		//Open output URL
		if ((ofmt.flags() & AVFMT_NOFILE) == 0) {
			ret = avio_open(pb, out_filename, AVIO_FLAG_WRITE);
			if (ret < 0) {
				System.err.println("Could not open output URL '%s'");
				close();
				return;
			}
		}
		ofmt_ctx.pb(pb);
		
		AVDictionary optionsDictionary = new AVDictionary();
		
		//Write file header
		ret = avformat_write_header(ofmt_ctx, optionsDictionary);
		if (ret < 0) {
			System.err.println("Error occurred when opening output URL");
			close();
			return;
		}
		start_time= System.currentTimeMillis()*1000; //in us
		while (continueStreaming) {
			AVStream in_stream, out_stream;
			//Get an AVPacket
			ret = av_read_frame(ifmt_ctx, pkt);
			
			if (ret < 0)
				break;
			//FIX：No PTS (Example: Raw H.264)
			//Simple Write PTS
			if(pkt.pts()==AV_NOPTS_VALUE){
				
				//Write PTS
				AVRational time_base1=ifmt_ctx.streams(videoindex).time_base();
				//Duration between 2 frames (us)
				double calc_duration=(double)AV_TIME_BASE/av_q2d(ifmt_ctx.streams(videoindex).r_frame_rate());
				//Parameters
				pkt.pts((long)((double)(frame_index*calc_duration)/(double)(av_q2d(time_base1)*AV_TIME_BASE)));
				pkt.dts(pkt.pts());
				pkt.duration((long)((double)calc_duration/(double)(av_q2d(time_base1)*AV_TIME_BASE)));
			}
			//Important:Delay
			if(pkt.stream_index()==videoindex){
				AVRational time_base=ifmt_ctx.streams(videoindex).time_base();
				
				AVRational time_base_q= new AVRational().num(1).den(AV_TIME_BASE);
				long pts_time = av_rescale_q(pkt.dts(), time_base, time_base_q);
				long now_time = System.currentTimeMillis()*1000 - start_time;
				if (pts_time > now_time)
					//System.out.println("Frame:"+frame_index+" PTS:"+pkt.pts()+" DTS:"+pkt.dts()+" Duration:"+pkt.duration()+" Now:"+now_time+" Time Base:"+time_base.den()+ " AV Time Base:"+AV_TIME_BASE+" ptst:"+pts_time);
					try {
						//int delay = (pts_time- now_time)/1000;
						int delay = 40;
						Thread.sleep(delay);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
			}

			in_stream  = ifmt_ctx.streams(pkt.stream_index());
			out_stream = ofmt_ctx.streams(pkt.stream_index());
			
			if(pkt.stream_index()==videoindex){
				//System.out.println("Frame:"+frame_index+" PTS:"+pkt.pts()+" DTS:"+pkt.dts()+" Duration:"+pkt.duration());
			}
			
			
			
			// copy packet 
			//Convert PTS/DTS
			pkt.pts (av_rescale_q_rnd(pkt.pts(), in_stream.time_base(), out_stream.time_base(), (AV_ROUND_NEAR_INF|AV_ROUND_PASS_MINMAX)));
			pkt.dts (av_rescale_q_rnd(pkt.dts(), in_stream.time_base(), out_stream.time_base(), (AV_ROUND_NEAR_INF|AV_ROUND_PASS_MINMAX)));
			pkt.duration (av_rescale_q(pkt.duration(), in_stream.time_base(), out_stream.time_base()));
			pkt.pos (-1);
			
			
			//Print to Screen
			if(pkt.stream_index()==videoindex){
				//System.out.println("oo Frame:"+frame_index+" PTS:"+pkt.pts()+" DTS:"+pkt.dts()+" Duration:"+pkt.duration());
				frame_index++;
			}
			//ret = av_write_frame(ofmt_ctx, &pkt);
			ret = av_interleaved_write_frame(ofmt_ctx, pkt);
			if (ret < 0) {
				System.err.println("Error muxing packet");
				break;
			}

			av_free_packet(pkt);

		}
		
		//Write file trailer
		av_write_trailer(ofmt_ctx);
	}

	void close()
	{
		/* close input */
		if (ifmt_ctx != null && ((ifmt_ctx.flags() & AVFMT_NOFILE) == 0))
		{
			avformat_close_input(ifmt_ctx);
			avformat_free_context(ifmt_ctx);
		}
		/* close output */
		if (ofmt_ctx != null && ((ofmt.flags() & AVFMT_NOFILE) == 0))
		{
			avio_close(ofmt_ctx.pb());
			avformat_free_context(ofmt_ctx);
		}
	}

	public String stop() {
		System.out.println("Stop");
		thread.stop();
		continueStreaming = false;
		close();
		return "OK";
	}
}


