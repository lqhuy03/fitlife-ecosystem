package com.fitlife.service;

import com.fitlife.dto.MemberCreationRequest;
import com.fitlife.dto.MemberResponse;
import com.fitlife.entity.Member;
import com.fitlife.entity.User;
import com.fitlife.repository.MemberRepository;
import com.fitlife.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final UserRepository userRepository; // Inject UserRepository here!
    private final CloudinaryService cloudinaryService;

    public MemberResponse createMember(MemberCreationRequest request) {
        // 1. Validate if phone number already exists
        if (memberRepository.existsByPhone(request.getPhone())) {
            throw new RuntimeException("Phone number already registered.");
        }

        // 2. Fetch the User entity from DB (Handle Cross-Repository mapping)
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + request.getUserId()));

        // 3. Map DTO to Entity
        Member newMember = Member.builder()
                .user(user)
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .email(request.getEmail())
                .status("ACTIVE")
                .build();

        // 4. Save to Database
        Member savedMember = memberRepository.save(newMember);

        // 5. Map Entity back to Response DTO
        return MemberResponse.builder()
                .id(savedMember.getId())
                .userId(savedMember.getUser().getId())
                .fullName(savedMember.getFullName())
                .phone(savedMember.getPhone())
                .email(savedMember.getEmail())
                .status(savedMember.getStatus())
                .build();
    }

    @Transactional
    public String updateAvatar(String username, MultipartFile file) throws IOException {
        // 1. Tìm Member đang đăng nhập
        Member member = memberRepository.findByUserUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hội viên"));

        // 2. Đẩy ảnh lên Cloudinary và lấy URL
        String avatarUrl = cloudinaryService.uploadImage(file);

        // 3. Cập nhật vào Database
        member.setAvatarUrl(avatarUrl);
        memberRepository.save(member);

        return avatarUrl;
    }
}