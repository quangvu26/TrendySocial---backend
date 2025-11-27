package com.example.trendy_chat.demo;

import com.cloudinary.utils.ObjectUtils;
import com.example.trendy_chat.util.CloudinaryUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class CloudinaryDemo implements CommandLineRunner {
    @Autowired
    private CloudinaryUtil cloudinaryUtil;

    @Override
    public void run(String... args) throws Exception {
        // 1. Upload an image
        Map params1 = ObjectUtils.asMap(
                "use_filename", true,
                "unique_filename", false,
                "overwrite", true
        );
        System.out.println("Uploading image...");
        Map uploadResult = cloudinaryUtil.upload(
                "https://cloudinary-devs.github.io/cld-docs-assets/assets/images/coffee_cup.jpg",
                params1
        );
        System.out.println("Upload result: " + uploadResult);

        // 2. Get asset details
        Map params2 = ObjectUtils.asMap(
                "quality_analysis", true
        );
        System.out.println("Getting asset details...");
        Map details = cloudinaryUtil.getResource("coffee_cup", params2);
        System.out.println("Asset details: " + details);

        // 3. Get transformed image tag
        String imageTag = cloudinaryUtil.getTransformedImageTag("coffee_cup", 300, 400);
        System.out.println("Transformed image tag: " + imageTag);
    }
}
