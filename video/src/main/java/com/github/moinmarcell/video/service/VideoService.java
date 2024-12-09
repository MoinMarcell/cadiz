package com.github.moinmarcell.video.service;

import com.github.moinmarcell.video.exception.StoreFailedException;
import com.mongodb.client.gridfs.model.GridFSFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Objects;

import static org.springframework.data.mongodb.core.query.Query.query;
import static org.springframework.data.mongodb.gridfs.GridFsCriteria.whereFilename;

@Service
public class VideoService {

	private final AsyncVideoService asyncVideoService;
	private final GridFsTemplate gridFsTemplate;

	@Autowired
	public VideoService(AsyncVideoService asyncVideoService, GridFsTemplate gridFsTemplate) {
		this.asyncVideoService = asyncVideoService;
		this.gridFsTemplate = gridFsTemplate;
	}

	@Async
	public void uploadVideo(MultipartFile file) {
		if (!isVideoMp4(file)) {
			throw new IllegalArgumentException("Only video/mp4 files are allowed");
		}
		try {
			asyncVideoService.storeVideo(file);
		} catch (IOException e) {
			throw new StoreFailedException(e.getLocalizedMessage());
		}
	}

	public byte[] getVideo(String filename) throws IOException {
		GridFSFile gridFSFile = gridFsTemplate.findOne(query(whereFilename().is(filename)));

		GridFsResource resource = gridFsTemplate.getResource(gridFSFile);

		return resource.getInputStream().readAllBytes();
	}

	private boolean isVideoMp4(MultipartFile file) {
		return Objects.equals(file.getContentType(), "video/mp4");
	}

}
