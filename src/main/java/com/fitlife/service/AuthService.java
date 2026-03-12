package com.fitlife.service;

import com.fitlife.dto.*;
import com.fitlife.entity.Member;
import com.fitlife.entity.User;
import com.fitlife.repository.MemberRepository;
import com.fitlife.repository.UserRepository;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final MemberRepository memberRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    // Logic Register: Create User + Member
    @Transactional
    public String register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username đã tồn tại!");
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword())) //  BCrypt
                .role("MEMBER")
                .status("ACTIVE")
                .build();
        User savedUser = userRepository.save(user);

        Member member = Member.builder()
                .user(savedUser)
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .email(request.getEmail())
                .status("ACTIVE")
                .avatarUrl(null)
                .build();
        memberRepository.save(member);

        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            emailService.sendWelcomeEmail(request.getEmail(), request.getFullName());
            System.out.println("Đã đẩy lệnh gửi email chào mừng vào luồng chạy ngầm cho: " + request.getEmail());
        }

        return "Đăng ký thành công!";
    }

    // Logic Login: Authenticate Spring Security + Generate JWT
    public LoginResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        var user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found after auth!"));

        // Print cards JWT
        String token = jwtService.generateToken(user);

        return LoginResponse.builder()
                .token(token)
                .username(user.getUsername())
                .role(user.getRole())
                .build();
    }

// FUNCTION FORGOT PASSWORD
    // Create code OTP random 6 numbers
    private String generateOtp() {
        int randomPin = (int) (Math.random() * 900000) + 100000;
        return String.valueOf(randomPin);
    }

    // 1. Stream forgot password (Create OTP and send mail)
    @Transactional
    public String forgotPassword(ForgotPasswordRequest request) {
        Member member = memberRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản với email này!"));

        User user = member.getUser();

        String otp = generateOtp();
        user.setResetToken(otp);
        user.setResetTokenExpiry(LocalDateTime.now().plusMinutes(5));
        userRepository.save(user);

        emailService.sendPasswordResetEmail(request.getEmail(), otp);

        return "Mã OTP đã được gửi đến email của bạn. Vui lòng kiểm tra hộp thư!";
    }

    // 2. Stream reset password (Check OTP and reset new password)
    @Transactional
    public String resetPassword(ResetPasswordRequest request) {
        Member member = memberRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản với email này!"));

        User user = member.getUser();

        if (user.getResetToken() == null || !user.getResetToken().equals(request.getOtp())) {
            throw new RuntimeException("Mã OTP không chính xác!");
        }

        if (user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Mã OTP đã hết hạn! Vui lòng yêu cầu gửi lại.");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));

        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        userRepository.save(user);

        return "Khôi phục mật khẩu thành công! Bạn có thể đăng nhập bằng mật khẩu mới.";
    }

    //  Register by Google (OAUTH2)

    private static final String GOOGLE_CLIENT_ID = "YOUR_GOOGLE_CLIENT_ID.apps.googleusercontent.com";

    @Transactional
    public LoginResponse googleLogin(GoogleLoginRequest request) {
        try {
            // 1. Cấu hình bộ xác minh của Google
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                    // Chỉ chấp nhận Token được tạo ra cho ứng dụng của em (Bỏ qua đoạn verify ClientId tạm thời để test dễ hơn nếu chưa có ClientID thật)
                    // .setAudience(Collections.singletonList(GOOGLE_CLIENT_ID))
                    .build();

            // 2. Xác minh Token do Frontend gửi lên
            GoogleIdToken idToken = verifier.verify(request.getToken());
            if (idToken == null) {
                throw new RuntimeException("Google Token không hợp lệ hoặc đã hết hạn!");
            }

            // 3. Lấy thông tin từ Google
            GoogleIdToken.Payload payload = idToken.getPayload();
            String email = payload.getEmail();
            String name = (String) payload.get("name");
            String pictureUrl = (String) payload.get("picture");

            // Lấy phần đầu của email làm username tạm thời
            String username = email.split("@")[0];

            // 4. Kiểm tra xem người dùng đã tồn tại trong Database chưa
            User user = userRepository.findByUsername(username).orElse(null);

            if (user == null) {
                // NẾU LÀ NGƯỜI MỚI -> TỰ ĐỘNG ĐĂNG KÝ (Auto-Register)
                // Sinh mật khẩu ngẫu nhiên vì họ đăng nhập bằng Google
                String randomPassword = java.util.UUID.randomUUID().toString().substring(0, 8);

                user = User.builder()
                        .username(username)
                        .password(passwordEncoder.encode(randomPassword))
                        .role("MEMBER")
                        .status("ACTIVE")
                        .build();
                user = userRepository.save(user);

                Member member = Member.builder()
                        .user(user)
                        .fullName(name)
                        .email(email)
                        .phone(null) // Đăng nhập Google không có SĐT
                        .status("ACTIVE")
                        .avatarUrl(pictureUrl) // Lấy luôn Avatar của Google
                        .build();
                memberRepository.save(member);

                // Gửi email chào mừng luôn
                emailService.sendWelcomeEmail(email, name);
            }

            // 5. Sinh JWT Token của hệ thống FitLife (Giống hệt đăng nhập thường)
            String jwtToken = jwtService.generateToken(user);

            return LoginResponse.builder()
                    .token(jwtToken)
                    .username(user.getUsername())
                    .role(user.getRole())
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("Lỗi xác thực Google: " + e.getMessage());
        }
    }
}