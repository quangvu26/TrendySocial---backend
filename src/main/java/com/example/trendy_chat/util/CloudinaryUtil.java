package com.example.trendy_chat.util;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
public class CloudinaryUtil {
    private final Cloudinary cloudinary;

    @Autowired
    public CloudinaryUtil(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    public Map upload(Object fileOrUrl, Map options) throws IOException {
        return cloudinary.uploader().upload(fileOrUrl, options);
    }

    public Map getResource(String publicId, Map options) throws Exception {
        return cloudinary.api().resource(publicId, options);
    }

    public String getTransformedImageTag(String publicId, int width, int height) {
        return cloudinary.url().transformation(
                new com.cloudinary.Transformation()
                        .crop("pad")
                        .width(width)
                        .height(height)
                        .background("auto:predominant")
        ).imageTag(publicId);
    }
}
