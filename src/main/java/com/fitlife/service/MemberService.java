package com.fitlife.service;

import com.fitlife.dto.MemberCreationRequest;
import com.fitlife.dto.MemberResponse;
import com.fitlife.dto.PageResponse;
import com.fitlife.entity.Member;
import com.fitlife.entity.User;
import com.fitlife.repository.MemberRepository;
import com.fitlife.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService;

    @Transactional
    public MemberResponse createMember(MemberCreationRequest request) {
        if (memberRepository.existsByPhone(request.getPhone())) {
            throw new RuntimeException("Số điện thoại đã được đăng ký.");
        }

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng ID: " + request.getUserId()));

        Member newMember = Member.builder()
                .user(user)
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .email(request.getEmail())
                .status("ACTIVE")
                .build();

        memberRepository.save(newMember);
        return mapToMemberResponse(newMember);
    }

    @Transactional
    public String updateAvatar(String username, MultipartFile file) throws IOException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy User"));

        Member member = user.getMember();
        if (member == null) throw new RuntimeException("Không tìm thấy thông tin hội viên");

        // Gợi ý: Xóa ảnh cũ trên Cloudinary nếu có để tiết kiệm dung lượng
        // if (member.getAvatarUrl() != null) { cloudinaryService.deleteImage(publicId); }

        String avatarUrl = cloudinaryService.uploadImage(file, "avatars", "member_" + member.getId());
        member.setAvatarUrl(avatarUrl);
        // Không nhất thiết phải gọi save() vì có @Transactional, Hibernate sẽ tự dirty check
        return avatarUrl;
    }

    @Transactional(readOnly = true)
    public PageResponse<MemberResponse> getAllMembers(int page, int size, String sortBy, String sortDir, String keyword) {
        // Bảo vệ logic phân trang: Đảm bảo page không bao giờ < 1
        int pageIndex = Math.max(0, page - 1);

        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(pageIndex, size, sort);
        Page<Member> memberPage;

        if (keyword != null && !keyword.trim().isEmpty()) {
            memberPage = memberRepository.findByFullNameContainingIgnoreCase(keyword.trim(), pageable);
        } else {
            memberPage = memberRepository.findAll(pageable);
        }

        List<MemberResponse> content = memberPage.getContent().stream()
                .map(this::mapToMemberResponse) // Tách hàm mapping cho sạch code
                .toList();

        return PageResponse.<MemberResponse>builder()
                .currentPage(page)
                .totalPages(memberPage.getTotalPages())
                .pageSize(memberPage.getSize())
                .totalElements(memberPage.getTotalElements())
                .data(content)
                .build();
    }

    // Hàm bổ trợ để tái sử dụng logic mapping
    private MemberResponse mapToMemberResponse(Member member) {
        return MemberResponse.builder()
                .id(member.getId())
                .userId(member.getUser() != null ? member.getUser().getId() : null)
                .fullName(member.getFullName())
                .email(member.getEmail())
                .phone(member.getPhone())
                .status(member.getStatus())
                .avatarUrl(member.getAvatarUrl())
                .build();
    }
}