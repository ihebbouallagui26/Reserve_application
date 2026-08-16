package com.rezkna.identity.partner;

import com.rezkna.common.exception.BadRequestException;
import com.rezkna.common.exception.ResourceNotFoundException;
import com.rezkna.common.response.ApiResponse;
import com.rezkna.identity.exception.ConflictException;
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
public class PartnerController {

    private static final List<String> ALLOWED_ROLES = List.of("OWNER", "HOST");

    private final PartnerUserRepository partnerUserRepository;
    private final PartnerTokenService tokenService;
    private final PasswordEncoder passwordEncoder;

    public PartnerController(PartnerUserRepository partnerUserRepository,
                              PartnerTokenService tokenService,
                              PasswordEncoder passwordEncoder) {
        this.partnerUserRepository = partnerUserRepository;
        this.tokenService = tokenService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/partner/login")
    public ApiResponse<PartnerLoginResponse> login(@Valid @RequestBody PartnerLoginRequest request) {
        String email = request.email().trim().toLowerCase();

        List<PartnerUser> matches = partnerUserRepository.findByEmail(email).stream()
                .filter(PartnerUser::isActive)
                .filter(u -> u.getPasswordHash() != null && passwordEncoder.matches(request.password(), u.getPasswordHash()))
                .toList();

        if (matches.isEmpty()) {
            throw new BadCredentialsException("Invalid email or password");
        }

        PartnerUser user;
        if (request.propertyId() != null && !request.propertyId().isBlank()) {
            user = matches.stream()
                    .filter(u -> request.propertyId().equals(u.getPropertyId()))
                    .findFirst()
                    .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));
        } else if (matches.size() == 1) {
            user = matches.get(0);
        } else {
            throw new ConflictException("Multiple properties found for this account - propertyId is required");
        }

        user.setLastLogin(Instant.now());
        PartnerUser saved = partnerUserRepository.save(user);
        String token = tokenService.generateToken(saved);

        List<PartnerRestaurantRef> restaurants = matches.stream()
                .map(u -> new PartnerRestaurantRef(u.getPropertyId(), u.getRole()))
                .toList();

        return ApiResponse.success(new PartnerLoginResponse(token, PartnerUserView.from(saved), saved.getPropertyId(), restaurants));
    }

    @GetMapping("/partner/me")
    public ApiResponse<PartnerMeResponse> me(@RequestHeader("Authorization") String authorization) {
        PartnerUser user = requireUser(authorization);
        return ApiResponse.success(new PartnerMeResponse(PartnerUserView.from(user), user.getPropertyId()));
    }

    @GetMapping("/partner/staff")
    public ApiResponse<List<PartnerUserView>> listStaff(@RequestHeader("Authorization") String authorization) {
        PartnerPrincipal principal = tokenService.requireClaims(authorization);
        List<PartnerUserView> staff = partnerUserRepository.findByPropertyId(principal.propertyId()).stream()
                .map(PartnerUserView::from)
                .toList();
        return ApiResponse.success(staff);
    }

    @PostMapping("/partner/staff")
    public ApiResponse<PartnerUserView> createStaff(@RequestHeader("Authorization") String authorization,
                                                       @Valid @RequestBody CreateStaffRequest request) {
        PartnerPrincipal principal = tokenService.requireClaims(authorization);
        requireOwner(principal);

        String email = request.email().trim().toLowerCase();
        String role = request.role() != null ? request.role().toUpperCase() : "HOST";
        if (!ALLOWED_ROLES.contains(role)) {
            throw new BadRequestException("Invalid role - must be OWNER or HOST");
        }
        if (partnerUserRepository.existsByEmailAndPropertyId(email, principal.propertyId())) {
            throw new ConflictException("A staff account with this email already exists for this property");
        }

        PartnerUser user = new PartnerUser();
        user.setEmail(email);
        user.setPropertyId(principal.propertyId());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setName(request.name());
        user.setRole(role);
        user.setActive(true);
        user.setCreatedAt(Instant.now());

        PartnerUser saved = partnerUserRepository.save(user);
        return ApiResponse.success(PartnerUserView.from(saved));
    }

    @DeleteMapping("/partner/staff/{id}")
    public ApiResponse<String> deleteStaff(@RequestHeader("Authorization") String authorization,
                                             @PathVariable String id) {
        PartnerPrincipal principal = tokenService.requireClaims(authorization);
        requireOwner(principal);

        PartnerUser user = partnerUserRepository.findById(id)
                .filter(u -> principal.propertyId().equals(u.getPropertyId()))
                .orElseThrow(() -> new ResourceNotFoundException("Staff account not found"));

        partnerUserRepository.delete(user);
        return ApiResponse.success(id);
    }

    private PartnerUser requireUser(String authorizationHeader) {
        PartnerPrincipal principal = tokenService.requireClaims(authorizationHeader);
        return partnerUserRepository.findById(principal.userId())
                .orElseThrow(() -> new BadCredentialsException("Account no longer exists"));
    }

    private void requireOwner(PartnerPrincipal principal) {
        if (!"OWNER".equals(principal.role())) {
            throw new AccessDeniedException("OWNER role required");
        }
    }
}
