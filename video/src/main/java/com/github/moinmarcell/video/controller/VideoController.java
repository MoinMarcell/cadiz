package com.github.moinmarcell.video.controller;

import com.github.moinmarcell.video.service.VideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/videos")
public class VideoController {

	private final VideoService videoService;

	@Autowired
	public VideoController(VideoService videoService) {
		this.videoService = videoService;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void uploadVideo(
			@RequestParam("file") MultipartFile file
	) {
		videoService.uploadVideo(file);
	}

	@GetMapping("/{filename}")
	public ResponseEntity<byte[]> getVideo(@PathVariable String filename) throws IOException {
		byte[] content = videoService.getVideo(filename);

		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
				.contentType(MediaType.MULTIPART_FORM_DATA)
				.body(content);
	}

}
