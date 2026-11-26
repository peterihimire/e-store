package com.benkih.estore.product.service;

import com.benkih.estore.product.dto.response.ImageDto;
import com.benkih.estore.product.entity.Image;
import com.benkih.estore.product.entity.Product;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface ImageService {
    Image getImageBySlug(String slug);

    void deleteImageById(String slug);

    Product getProductEntityBySlug(String slug);

    List<ImageDto> addImages(List<MultipartFile> files, String slug);

    void updateImage(MultipartFile file, String imageSlug);
}
