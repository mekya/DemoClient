package io.antmedia.demo_client;


import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.stereotype.*;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
@EnableAutoConfiguration
public class App {

    @RequestMapping("/rtmp_hls")
    @ResponseBody
    String rtmpHLS(@RequestParam("name") String name) {
        String url = "rtmp://"+Settings.instance.getServerAddress()+"/LiveApp/demo_rtmp_hls";
		RTMPBroadcaster.instance.broadcast(name, url );
    	return "OK";
    }
    
    @RequestMapping("/rtmp_webrtc")
    @ResponseBody
    String rtmpWebRTC(@RequestParam("name") String name) {
        String url = "rtmp://"+Settings.instance.getServerAddress()+"/WebRTCAppEE/demo_rtmp_webrtc";
    	RTMPBroadcaster.instance.broadcast(name, url);
    	return "OK";
    }

    @GetMapping("/settings/server")
    @ResponseBody
    String getServer() {
    	return Settings.instance.getServerAddress();
    }
    
    @PostMapping("/settings/server")
    @ResponseBody
    String setServer(@RequestBody String ip) {
    	Settings.instance.setServerAddress(ip);
    	return "OK";
    }
    
    @RequestMapping("/stop")
    @ResponseBody
    String stop() {
    	return RTMPBroadcaster.instance.stop();
    }
    
    @PostMapping("/upload")
    @ResponseBody
    String upload(@RequestParam("file") MultipartFile file, Model model) throws IOException {
    	File tempDir = Utils.getTempDir();
    	System.out.println("upload:"+file.getOriginalFilename());
    	 if (!file.getOriginalFilename().isEmpty()) {
             BufferedOutputStream outputStream = new BufferedOutputStream(
                   new FileOutputStream(new File(tempDir, file.getOriginalFilename())));
             outputStream.write(file.getBytes());
             outputStream.flush();
             outputStream.close();

             model.addAttribute("msg", "File uploaded successfully.");
          } else {
             model.addAttribute("msg", "Please select a valid file..");
          }
    	return "OK";
    }

	public static void main(String[] args) throws Exception {
        SpringApplication.run(App.class, args);
    }
}