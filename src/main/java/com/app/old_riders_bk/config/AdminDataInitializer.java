package com.app.old_riders_bk.config;

import com.app.old_riders_bk.admin.entity.Admin;
import com.app.old_riders_bk.admin.entity.AdminRole;
import com.app.old_riders_bk.admin.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AdminDataInitializer implements CommandLineRunner {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (!adminRepository.existsByEmail("superadmin@oldriders.app")) {
            Admin superAdmin = Admin.builder()
                    .fullName("Head System Administrator")
                    .email("superadmin@oldriders.app")
                    .password(passwordEncoder.encode("SuperAdmin2026!"))
                    .role(AdminRole.SUPER_ADMIN)
                    .isActive(true)
                    .build();
            adminRepository.save(superAdmin);
            System.out.println(">>> Initial Super Admin seeded: superadmin@oldriders.app / SuperAdmin2026!");
        }
    }
}