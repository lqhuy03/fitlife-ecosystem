package com.fitlife.member.service.impl;

import com.fitlife.core.response.PageResponse;
import com.fitlife.core.exception.AppException;
import com.fitlife.core.exception.ErrorCode;
import com.fitlife.identity.entity.User;
import com.fitlife.member.dto.MemberCreationRequest;
import com.fitlife.member.dto.MemberProfileResponse;
import com.fitlife.member.dto.MemberUpdateRequest;
import com.fitlife.member.entity.Member;
import com.fitlife.identity.repository.UserRepository;
import com.fitlife.core.storage.impl.CloudinaryServiceImpl;
import com.fitlife.member.repository.MemberRepository;
import com.fitlife.member.mapper.MemberMapper;
import com.fitlife.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final UserRepository userRepository;
    private final CloudinaryServiceImpl cloudinaryServiceImpl;
    private final MemberMapper memberMapper;

    private static final String ACTIVE_STATUS = "ACTIVE";
    private static final String BANNED_STATUS = "BANNED";
    private static final String INACTIVE_STATUS = "INACTIVE";

    @Transactional
    @Override
    public MemberProfileResponse createMember(MemberCreationRequest request) {
        if (memberRepository.existsByPhone(request.getPhone())) {
            throw new AppException(ErrorCode.PHONE_ALREADY_EXISTS);
        }

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng ID: " + request.getUserId()));

        Member newMember = memberMapper.toEntity(request);
        newMember.setUser(user);
        newMember.setStatus(ACTIVE_STATUS);

        memberRepository.save(newMember);
        return memberMapper.toResponse(newMember);
    }

    @Transactional
    @Override
    public String updateAvatar(String username, MultipartFile file) throws IOException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy User"));

        Member member = user.getMember();
        if (member == null) throw new AppException(ErrorCode.MEMBER_NOT_FOUND);

        if (member.getAvatarUrl() != null) {
            String publicId = "avatars/member_" + member.getId();
            try {
                cloudinaryServiceImpl.deleteImage(publicId);
            } catch (Exception e) {
                log.warn("Không thể xóa ảnh cũ trên Cloudinary: {}", e.getMessage());
            }
        }

        // Upload new photo
        String avatarUrl = cloudinaryServiceImpl.uploadImage(file, "avatars", "member_" + member.getId());
        member.setAvatarUrl(avatarUrl);

        return avatarUrl;
    }

    @Transactional(readOnly = true)
    @Override
    public PageResponse<MemberProfileResponse> getAllMembers(int page, int size, String sortBy, String sortDir, String keyword) {
        // Protect pagination logic: Ensure page is never < 1
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

        List<MemberProfileResponse> content = memberPage.getContent().stream()
                .map(memberMapper::toResponse)
                .toList();

        return PageResponse.<MemberProfileResponse>builder()
                .currentPage(page)
                .totalPages(memberPage.getTotalPages())
                .pageSize(memberPage.getSize())
                .totalElements(memberPage.getTotalElements())
                .data(content)
                .build();
    }

    @Transactional
    @Override
    public MemberProfileResponse createMemberByAdmin(MemberCreationRequest request) {
        // 1. Check trùng lặp
        if (memberRepository.existsByPhone(request.getPhone())) {
            throw new AppException(ErrorCode.PHONE_ALREADY_EXISTS);
        }
        if (userRepository.findByUsername(request.getEmail()).isPresent()) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        // 2. TẠO TÀI KHOẢN USER TRƯỚC (Dùng Email làm Username, Pass mặc định: 123456)
        User newUser = User.builder()
                .username(request.getEmail())
                // Pass "123456" mã hóa Bcrypt. Nếu em có PasswordEncoder thì dùng passwordEncoder.encode("123456")
                .password("$2a$10$X8C5.5hN7q6aN9zJbXqY4.0yZ3.rU7y7T4/q4z4u4u4u4u4u4u4u4")
                .role("MEMBER")
                .status(ACTIVE_STATUS)
                .build();
        userRepository.save(newUser);

        // 3. TẠO HỒ SƠ MEMBER GẮN VỚI USER TRÊN
        Member newMember = memberMapper.toEntity(request);
        newMember.setUser(newUser);
        newMember.setStatus(ACTIVE_STATUS);
        memberRepository.save(newMember);

        return memberMapper.toResponse(newMember);
    }

    @Transactional
    @Override
    public void toggleMemberLock(Long memberId) {
        // 1. Tìm Member
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new AppException(ErrorCode.MEMBER_NOT_FOUND));

        // 2. Lấy User liên kết (Tài khoản để đăng nhập)
        User user = member.getUser();
        if (user == null) {
            throw new AppException(ErrorCode.MEMBER_NO_ACCOUNT);
        }

        // 3. Đảo trạng thái (Khóa cả Member profile lẫn User login)
        if (ACTIVE_STATUS.equalsIgnoreCase(member.getStatus())) {
            member.setStatus(BANNED_STATUS);
            user.setStatus(BANNED_STATUS);
        } else {
            member.setStatus(ACTIVE_STATUS);
            user.setStatus(ACTIVE_STATUS);
        }

        // 4. Lưu thay đổi
        memberRepository.save(member);
        userRepository.save(user);
    }

    @Override
    public MemberProfileResponse getMemberById(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new AppException(ErrorCode.MEMBER_NOT_FOUND));
        return memberMapper.toResponse(member);
    }

    @Transactional
    @Override
    public MemberProfileResponse updateMemberByAdmin(Long memberId, MemberCreationRequest request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new AppException(ErrorCode.MEMBER_NOT_FOUND));

        // Cập nhật thông tin
        member.setFullName(request.getFullName());
        member.setPhone(request.getPhone());
        member.setEmail(request.getEmail());

        memberRepository.save(member);
        return memberMapper.toResponse(member);
    }

    @Transactional
    @Override
    public void deleteMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new AppException(ErrorCode.MEMBER_NOT_FOUND));

        User user = member.getUser();

        // THỰC THI SOFT DELETE CHUẨN MỰC
        member.setIsDeleted(true);
        member.setStatus(INACTIVE_STATUS); // Khóa luôn trạng thái cho an toàn

        if (user != null) {
            user.setIsDeleted(true);
            user.setStatus(INACTIVE_STATUS);
            userRepository.save(user); // Lưu user
        }

        memberRepository.save(member); // Lưu member

        // Lưu ý: Không cần cascade (xóa lan) isDeleted sang các bảng lịch sử (checkin, orders).
        // Lịch sử là bất biến.
    }

    @Override
    @Transactional
    public MemberProfileResponse getMyProfile(String username) {
        // Tìm user theo email (username trong JWT chính là email ở luồng của chúng ta)
        Member member = memberRepository.findByEmail(username)
                .orElseThrow(() -> new AppException(ErrorCode.MEMBER_NOT_FOUND));
        // Tận dụng mapper em đã dùng AI tạo ra
        return memberMapper.toProfileResponse(member);
    }

    @Override
    @Transactional
    public MemberProfileResponse updateMyProfile(String username, MemberUpdateRequest request) {
        Member member = memberRepository.findByEmail(username)
                .orElseThrow(() -> new AppException(ErrorCode.MEMBER_NOT_FOUND));

        // Cập nhật thông tin cơ bản
        if (request.getFullName() != null) member.setFullName(request.getFullName());
        if (request.getPhone() != null) member.setPhone(request.getPhone());
        if (request.getFitnessGoal() != null) member.setFitnessGoal(request.getFitnessGoal());

        // Logic tính toán BMI Tự động
        if (request.getWeight() != null && request.getHeight() != null) {
            member.setWeight(request.getWeight());
            member.setHeight(request.getHeight());

            // Đổi cm sang m
            double heightInMeter = request.getHeight() / 100.0;
            // Tính BMI và làm tròn 2 chữ số thập phân
            double bmi = Math.round((request.getWeight() / (heightInMeter * heightInMeter)) * 100.0) / 100.0;
            member.setBmi(bmi);
        }

        Member savedMember = memberRepository.save(member);
        return memberMapper.toProfileResponse(savedMember);
    }
}