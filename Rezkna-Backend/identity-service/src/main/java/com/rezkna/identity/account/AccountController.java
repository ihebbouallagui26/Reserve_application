package com.rezkna.identity.account;

import com.rezkna.common.response.ApiResponse;
import com.rezkna.identity.account.social.SocialLoginVerifier;
import com.rezkna.identity.account.social.SocialProfile;
import com.rezkna.identity.exception.ConflictException;
import jakarta.validation.Valid;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Optional;

@RestController
@Validated
public class AccountController {

    private final DinerAccountRepository accountRepository;
    private final DinerTokenService tokenService;
    private final PasswordEncoder passwordEncoder;
    private final SocialLoginVerifier socialLoginVerifier;

    public AccountController(DinerAccountRepository accountRepository,
                              DinerTokenService tokenService,
                              PasswordEncoder passwordEncoder,
                              SocialLoginVerifier socialLoginVerifier) {
        this.accountRepository = accountRepository;
        this.tokenService = tokenService;
        this.passwordEncoder = passwordEncoder;
        this.socialLoginVerifier = socialLoginVerifier;
    }

    @PostMapping("/register")
    public ApiResponse<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        String email = request.email().trim().toLowerCase();
        if (accountRepository.existsByEmail(email)) {
            throw new ConflictException("An account with this email already exists");
        }

        DinerAccount account = new DinerAccount();
        account.setEmail(email);
        account.setPasswordHash(passwordEncoder.encode(request.password()));
        account.setName(request.name());
        String normalizedPhone = PhoneNormalizer.normalize(request.phone());
        account.setPhone(normalizedPhone);
        account.setPhoneTail(PhoneNormalizer.tail(normalizedPhone));
        account.setCity(request.city());
        account.setPreferences(request.preferences());
        account.setProvider("local");
        Instant now = Instant.now();
        account.setCreatedAt(now);
        account.setLastLogin(now);

        DinerAccount saved = accountRepository.save(account);
        String token = tokenService.generateToken(saved);
        return ApiResponse.success(new AuthResponse(token, DinerAccountView.from(saved)));
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        String email = request.email().trim().toLowerCase();
        DinerAccount account = accountRepository.findByEmail(email)
                .filter(a -> a.getPasswordHash() != null && passwordEncoder.matches(request.password(), a.getPasswordHash()))
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        account.setLastLogin(Instant.now());
        DinerAccount saved = accountRepository.save(account);
        String token = tokenService.generateToken(saved);
        return ApiResponse.success(new AuthResponse(token, DinerAccountView.from(saved)));
    }

    @PostMapping("/social")
    public ApiResponse<AuthResponse> social(@Valid @RequestBody SocialLoginRequest request) {
        SocialProfile profile = socialLoginVerifier.verify(request.provider(), request.token());

        Optional<DinerAccount> existing = accountRepository.findByProviderAndProviderId(profile.provider(), profile.providerId());
        DinerAccount account;
        if (existing.isPresent()) {
            account = existing.get();
        } else {
            String normalizedEmail = profile.email() != null ? profile.email().trim().toLowerCase() : null;
            if (normalizedEmail != null && accountRepository.existsByEmail(normalizedEmail)) {
                throw new ConflictException("An account with this email already exists");
            }
            account = new DinerAccount();
            account.setEmail(normalizedEmail);
            account.setName(request.name() != null ? request.name() : profile.name());
            account.setProvider(profile.provider());
            account.setProviderId(profile.providerId());
            Instant now = Instant.now();
            account.setCreatedAt(now);
            account.setLastLogin(now);
        }

        account.setLastLogin(Instant.now());
        DinerAccount saved = accountRepository.save(account);
        String token = tokenService.generateToken(saved);
        return ApiResponse.success(new AuthResponse(token, DinerAccountView.from(saved)));
    }

    @GetMapping("/me")
    public ApiResponse<DinerAccountView> me(@RequestHeader("Authorization") String authorization) {
        return ApiResponse.success(DinerAccountView.from(requireAccount(authorization)));
    }

    @PostMapping("/me")
    public ApiResponse<DinerAccountView> updateMe(@RequestHeader("Authorization") String authorization,
                                                    @RequestBody UpdateProfileRequest request) {
        DinerAccount account = requireAccount(authorization);

        if (request.name() != null) {
            account.setName(request.name());
        }
        if (request.phone() != null) {
            String normalized = PhoneNormalizer.normalize(request.phone());
            account.setPhone(normalized);
            account.setPhoneTail(PhoneNormalizer.tail(normalized));
        }
        if (request.city() != null) {
            account.setCity(request.city());
        }
        if (request.preferences() != null) {
            account.setPreferences(request.preferences());
        }
        if (request.birthday() != null) {
            account.setBirthday(request.birthday());
        }
        if (request.allergies() != null) {
            account.setAllergies(request.allergies());
        }
        if (request.diets() != null) {
            account.setDiets(request.diets());
        }

        DinerAccount saved = accountRepository.save(account);
        return ApiResponse.success(DinerAccountView.from(saved));
    }

    @GetMapping("/preferences")
    public ApiResponse<PreferencesResponse> getPreferences(@RequestHeader("Authorization") String authorization) {
        DinerAccount account = requireAccount(authorization);
        return ApiResponse.success(new PreferencesResponse(
                account.isNotifySms(), account.isNotifyEmail(), account.isMarketingOptIn()));
    }

    @PostMapping("/preferences")
    public ApiResponse<PreferencesResponse> updatePreferences(@RequestHeader("Authorization") String authorization,
                                                                 @RequestBody PreferencesRequest request) {
        DinerAccount account = requireAccount(authorization);
        account.setNotifySms(request.notifySms());
        account.setNotifyEmail(request.notifyEmail());
        account.setMarketingOptIn(request.marketingOptIn());
        DinerAccount saved = accountRepository.save(account);
        return ApiResponse.success(new PreferencesResponse(
                saved.isNotifySms(), saved.isNotifyEmail(), saved.isMarketingOptIn()));
    }

    private DinerAccount requireAccount(String authorizationHeader) {
        String accountId = tokenService.requireAccountId(authorizationHeader);
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new BadCredentialsException("Account no longer exists"));
    }
}
