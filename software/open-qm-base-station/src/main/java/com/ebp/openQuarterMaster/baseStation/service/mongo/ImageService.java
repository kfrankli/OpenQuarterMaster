package com.ebp.openQuarterMaster.baseStation.service.mongo;

import com.ebp.openQuarterMaster.lib.core.media.Image;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoClient;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.inject.Inject;
import javax.validation.Validator;

public class ImageService extends MongoService<Image> {

    private Validator validator;

    ImageService() {//required for DI
        super(null, null, null, null, null, false, null);
    }

    @Inject
    ImageService(
            Validator validator,
            ObjectMapper objectMapper,
            MongoClient mongoClient,
            @ConfigProperty(name = "quarkus.mongodb.database")
                    String database
    ) {
        super(
                objectMapper,
                mongoClient,
                database,
                Image.class,
                false
        );
        this.validator = validator;
    }
}