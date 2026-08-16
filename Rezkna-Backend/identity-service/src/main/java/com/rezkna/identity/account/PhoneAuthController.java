package com.rezkna.identity.account;

import com.rezkna.common.exception.BadRequestException;
import com.rezkna.common.response.ApiResponse;
import com.rezkna.identity.exception.TooManyAttemptsException;
import com.rezkna.identity.sms.SmsSender;
import jakarta.validation.Valid;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@RestController
@Validated
public class PhoneAuthController {

    private static final int OTP_EXPIRY_MINUTES = 10;
    private static final int MAX_ATTEMPTS = 5;

    private final PhoneVerificationCodeRepository codeRepository;
    private final DinerAccountRepository accountRepository;
    private final DinerTokenService tokenService;
    private final SmsSender smsSender;
    private final PasswordEncoder passwordEncoder;
    private final OtpGenerator otpGenerator;

    public PhoneAuthController(PhoneVerificationCodeRepository codeRepository,
                                DinerAccountRepository accountRepository,
                                DinerTokenService tokenService,
                                SmsSender smsSender,
                                PasswordEncoder passwordEncoder,
                                OtpGenerator otpGenerator) {
        this.codeRepository = codeRepository;
        this.accountRepository = accountRepository;
        this.tokenService = tokenService;
        this.smsSender = smsSender;
        this.passwordEncoder = passwordEncoder;
        this.otpGenerator = otpGenerator;
    }

    @PostMapping("/phone/start")
    public ApiResponse<PhoneStartResponse> start(@Valid @RequestBody PhoneStartRequest request) {
        String phone = PhoneNormalizer.normalize(request.phone());
        if (phone == null) {
            throw new BadRequestException("Invalid phone number");
        }

        codeRepository.deleteByPhone(phone);

        String code = otpGenerator.generate();
        PhoneVerificationCode entity = new PhoneVerificationCode();
        entity.setPhone(phone);
        entity.setCodeHash(passwordEncoder.encode(code));
        entity.setExpiresAt(Instant.now().plus(OTP_EXPIRY_MINUTES, ChronoUnit.MINUTES));
        entity.setAttempts(0);
        entity.setCreatedAt(Instant.now());
        codeRepository.save(entity);

        smsSender.send(phone, "Your Rezkna verification code is " + code + ". It expires in " + OTP_EXPIRY_MINUTES + " minutes.");

        return ApiResponse.success(new PhoneStartResponse(phone, OTP_EXPIRY_MINUTES));
    }

    @PostMapping("/phone/verify")
    public ApiResponse<PhoneVerifyResponse> verify(@Valid @RequestBody PhoneVerifyRequest request) {
        String phone = PhoneNormalizer.normalize(request.phone());
        if (phone == null) {
            throw new BadRequestException("Invalid phone number");
        }

        PhoneVerificationCode entity = codeRepository.findByPhone(phone)
                .orElseThrow(() -> new BadCredentialsException("Invalid or expired code"));

        if (entity.getExpiresAt().isBefore(Instant.now())) {
            codeRepository.deleteByPhone(phone);
            throw new BadCredentialsException("Invalid or expired code");
        }

        if (entity.getAttempts() >= MAX_ATTEMPTS) {
            codeRepository.deleteByPhone(phone);
            throw new TooManyAttemptsException("Too many attempts - request a new code");
        }

        if (!passwordEncoder.matches(request.code(), entity.getCodeHash())) {
            int attempts = entity.getAttempts() + 1;
            if (attempts >= MAX_ATTEMPTS) {
                codeRepository.deleteByPhone(phone);
                throw new TooManyAttemptsException("Too many attempts - request a new code");
            }
            entity.setAttempts(attempts);
            codeRepository.save(entity);
            throw new BadCredentialsException("Invalid or expired code");
        }

        codeRepository.deleteByPhone(phone);

        Optional<DinerAccount> existing = accountRepository.findByPhone(phone);
        boolean isNew = existing.isEmpty();
        DinerAccount account = existing.orElseGet(() -> {
            DinerAccount created = new DinerAccount();
            created.setPhone(phone);
            created.setPhoneTail(PhoneNormalizer.tail(phone));
            created.setName(request.name());
            created.setProvider("phone");
            Instant now = Instant.now();
            created.setCreatedAt(now);
            created.setLastLogin(now);
            return created;
        });

        account.setLastLogin(Instant.now());
        DinerAccount saved = accountRepository.save(account);
        String token = tokenService.generateToken(saved);
        return ApiResponse.success(new PhoneVerifyResponse(token, DinerAccountView.from(saved), isNew));
    }
}
