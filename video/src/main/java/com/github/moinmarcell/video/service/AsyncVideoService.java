package com.github.moinmarcell.video.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class AsyncVideoService {

	private final GridFsTemplate gridFsTemplate;

	@Autowired
	public AsyncVideoService(GridFsTemplate gridFsTemplate) {
		this.gridFsTemplate = gridFsTemplate;
	}

	@Async
	public void storeVideo(MultipartFile file) throws IOException {
		gridFsTemplate.store(file.getInputStream(), file.getOriginalFilename(), file.getContentType());
	}
}
