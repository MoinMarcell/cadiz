package com.github.moinmarcell.video.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;

@Configuration
public class MongoConfig {

	@Bean
	public GridFsTemplate gridFsTemplate(MongoDatabaseFactory mongoDatabaseFactory, MappingMongoConverter converter) {
		return new GridFsTemplate(mongoDatabaseFactory, converter, "videos");
	}

}
