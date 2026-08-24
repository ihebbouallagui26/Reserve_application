package com.rezkna.identity.platform;

import com.rezkna.common.exception.ResourceNotFoundException;
import com.rezkna.common.response.ApiResponse;
import com.rezkna.identity.exception.ConflictException;
import com.rezkna.identity.partner.PartnerUser;
import com.rezkna.identity.partner.PartnerUserRepository;
import com.rezkna.identity.partner.PartnerUserView;
import jakarta.validation.Valid;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@Validated
public class PlatformController {

    private final PlatformUserRepository platformUserRepository;
    private final PartnerUserRepository partnerUserRepository;
    private final PlatformTokenService tokenService;
    private final PasswordEncoder passwordEncoder;

    public PlatformController(PlatformUserRepository platformUserRepository,
                               PartnerUserRepository partnerUserRepository,
                               PlatformTokenService tokenService,
                               PasswordEncoder passwordEncoder) {
        this.platformUserRepository = platformUserRepository;
        this.partnerUserRepository = partnerUserRepository;
        this.tokenService = tokenService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/platform/login")
    public ApiResponse<PlatformLoginResponse> login(@Valid @RequestBody PlatformLoginRequest request) {
        String email = request.email().trim().toLowerCase();

        PlatformUser user = platformUserRepository.findByEmail(email)
                .filter(PlatformUser::isActive)
                .filter(u -> u.getPasswordHash() != null && passwordEncoder.matches(request.password(), u.getPasswordHash()))
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        user.setLastLogin(Instant.now());
        PlatformUser saved = platformUserRepository.save(user);
        String token = tokenService.generateToken(saved);

        return ApiResponse.success(new PlatformLoginResponse(token, PlatformUserView.from(saved)));
    }

    @GetMapping("/platform/me")
    public ApiResponse<PlatformUserView> me(@RequestHeader("Authorization") String authorization) {
        return ApiResponse.success(PlatformUserView.from(requireUser(authorization)));
    }

    @GetMapping("/platform/partners")
    public ApiResponse<List<PartnerUserView>> listPartners(@RequestHeader("Authorization") String authorization) {
        requirePlatformOwner(authorization);

        List<PartnerUserView> partners = partnerUserRepository.findAll().stream()
                .map(PartnerUserView::from)
                .toList();
        return ApiResponse.success(partners);
    }

    @PostMapping("/platform/partners")
    public ApiResponse<PartnerUserView> createPartner(@RequestHeader("Authorization") String authorization,
                                                        @Valid @RequestBody PlatformCreatePartnerRequest request) {
        requirePlatformOwner(authorization);

        String email = request.email().trim().toLowerCase();
        if (partnerUserRepository.existsByEmailAndPropertyId(email, request.propertyId())) {
            throw new ConflictException("A partner account with this email already exists for this property");
        }

        PartnerUser partner = new PartnerUser();
        partner.setEmail(email);
        partner.setPropertyId(request.propertyId());
        partner.setPasswordHash(passwordEncoder.encode(request.password()));
        partner.setName(request.name());
        partner.setRole("OWNER");
        partner.setActive(true);
        partner.setCreatedAt(Instant.now());

        PartnerUser saved = partnerUserRepository.save(partner);
        return ApiResponse.success(PartnerUserView.from(saved));
    }

    @PatchMapping("/platform/partners/{id}/status")
    public ApiResponse<PartnerUserView> updatePartnerStatus(@RequestHeader("Authorization") String authorization,
                                                              @PathVariable String id,
                                                              @Valid @RequestBody PartnerStatusRequest request) {
        requirePlatformOwner(authorization);

        PartnerUser partner = partnerUserRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Partner not found"));
        partner.setActive(request.active());

        PartnerUser saved = partnerUserRepository.save(partner);
        return ApiResponse.success(PartnerUserView.from(saved));
    }

    private PlatformUser requireUser(String authorizationHeader) {
        PlatformPrincipal principal = tokenService.requireClaims(authorizationHeader);
        requirePlatformOwner(principal);
        return platformUserRepository.findById(principal.userId())
                .orElseThrow(() -> new BadCredentialsException("Account no longer exists"));
    }

    private void requirePlatformOwner(String authorizationHeader) {
        requirePlatformOwner(tokenService.requireClaims(authorizationHeader));
    }

    private void requirePlatformOwner(PlatformPrincipal principal) {
        if (!"PLATFORM_OWNER".equals(principal.role())) {
            throw new AccessDeniedException("PLATFORM_OWNER role required");
        }
    }
}
