package com.github.moinmarcell.video.controller;

import com.github.moinmarcell.video.service.VideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.util.List;
import java.util.Map;

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
	public ResponseEntity<StreamingResponseBody> getVideoStream(@PathVariable String filename, @RequestHeader HttpHeaders headers) throws IOException {
		Map<String, Object> video = videoService.getVideo(filename);
		GridFsResource resource = (GridFsResource) video.get("content");

		long fileLength = resource.contentLength();
		List<HttpRange> ranges = headers.getRange();

		if (ranges.isEmpty()) {
			// Full file request
			return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"").contentType(MediaType.valueOf(resource.getContentType())).body(outputStream ->
					resource.getInputStream().transferTo(outputStream));
		}

		// Handle range requests
		HttpRange range = ranges.getFirst();
		long start = range.getRangeStart(fileLength);
		long end = range.getRangeEnd(fileLength);
		long rangeLength = end - start + 1;

		return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
				.header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"").header(HttpHeaders.CONTENT_RANGE, "bytes " + start + "-" + end + "/" + fileLength).contentType(MediaType.valueOf(resource.getContentType())).contentLength(rangeLength).body(outputStream -> {
					try (var inputStream = resource.getInputStream()) {
						long skipped = 0;
						while (skipped < start) {
							long remaining = start - skipped;
							long actualSkipped = inputStream.skip(remaining);
							if (actualSkipped <= 0) {
								throw new IOException("Unable to skip to the requested start position.");
							}
							skipped += actualSkipped;
						}

						byte[] buffer = new byte[8192];
						long bytesToWrite = rangeLength;

						while (bytesToWrite > 0) {
							int bytesRead = inputStream.read(buffer, 0, (int) Math.min(buffer.length, bytesToWrite));

							if (bytesRead != -1) {
								try {
									outputStream.write(buffer, 0, bytesRead);
									bytesToWrite -= bytesRead;
								} catch (IOException e) {
									bytesToWrite = 0;
								}
							} else {
								bytesToWrite = 0;
							}
						}

					}
				});
	}

}
