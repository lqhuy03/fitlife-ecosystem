package com.fitlife.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private final Cloudinary cloudinary;

    /**
     * @param file       File ảnh từ Client
     * @param folderName Tên thư mục con (Vd: "avatars", "packages")
     * @param publicId   Tên file cố định (Vd: "member_1") để ghi đè ảnh cũ
     */
    public String uploadImage(MultipartFile file, String folderName, String publicId) throws IOException {
        try {
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "fitlife/" + folderName, // Tổ chức thư mục
                            "public_id", publicId,             // Định danh file
                            "overwrite", true,                 // Ghi đè nếu đã tồn tại
                            "resource_type", "image"
                    ));
            return uploadResult.get("secure_url").toString();

        } catch (IOException e) {
            throw new RuntimeException("Lỗi khi tải ảnh lên đám mây: " + e.getMessage());
        }
    }
}