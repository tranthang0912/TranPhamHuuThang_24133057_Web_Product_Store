package com.baitap03.service;

import java.time.LocalDateTime;
import java.util.Locale;

import com.baitap03.model.User;
import com.baitap03.repository.IUserRepository;
import com.baitap03.repository.UserRepository;
import com.baitap03.util.Constants;
import com.baitap03.util.OtpUtil;
import com.baitap03.util.PasswordUtil;

public class UserServiceImpl
        implements IUserService {

    public IUserRepository userRepository =
            new UserRepository();

    @Override
    public User findById(int userId) {
        return userRepository.findById(userId);
    }

    @Override
    public User updateProfile(int userId, String fullname, String phone, String image) {
        if (fullname == null || fullname.isBlank() || fullname.trim().length() > 100) {
            throw new IllegalArgumentException("Họ tên phải có từ 1 đến 100 ký tự.");
        }
        String normalizedPhone = phone == null ? "" : phone.trim();
        if (!normalizedPhone.matches("\\+?[0-9]{9,15}")) {
            throw new IllegalArgumentException("Số điện thoại phải có từ 9 đến 15 chữ số, có thể bắt đầu bằng +.");
        }
        return userRepository.updateProfile(userId, fullname.trim(), normalizedPhone, image);
    }


    @Override
    public boolean register(User user) {

        User existing =
                userRepository.findByUsername(
                        user.getUsername()
                );

        User emailOwner = userRepository.findByEmail(user.getEmail());

        if (existing != null || emailOwner != null) {

            return false;
        }

        user.setUsername(user.getUsername().trim());
        user.setEmail(user.getEmail().trim().toLowerCase(Locale.ROOT));
        user.setPassword(PasswordUtil.hash(user.getPassword()));
        user.setActive(false);

        userRepository.insert(user);

        return true;
    }


    @Override
    public User login(
            String identifier,
            String password) {

        User user =
                userRepository.findByIdentifier(
                        identifier == null ? null : identifier.trim()
                );

        if (user == null || !user.isActive()) {

            return null;
        }

        if (!PasswordUtil.verify(password, user.getPassword())) {

            return null;
        }

        if (PasswordUtil.needsUpgrade(user.getPassword())) {
            user.setPassword(PasswordUtil.hash(password));
            userRepository.update(user);
        }

        return user;
    }

    @Override
    public User findByIdentifier(String identifier) {
        if (identifier == null || identifier.isBlank()) {
            return null;
        }
        return userRepository.findByIdentifier(identifier.trim());
    }

    @Override
    public User findByEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        return userRepository.findByEmail(email.trim());
    }

    @Override
    public boolean activateAccount(String email, String otp) {
        User user = findByEmail(email);
        if (user == null || user.isActive()
                || user.getActivationOtpExpiresAt() == null
                || LocalDateTime.now().isAfter(user.getActivationOtpExpiresAt())
                || !OtpUtil.matches(otp, user.getActivationOtpHash())) {
            return false;
        }
        user.setActive(true);
        user.setActivationOtpHash(null);
        user.setActivationOtpExpiresAt(null);
        userRepository.update(user);
        return true;
    }

    @Override
    public boolean prepareActivationOtp(String email, String otp) {
        User user = findByEmail(email);
        if (user == null || user.isActive()) {
            return false;
        }
        user.setActivationOtpHash(OtpUtil.hash(otp));
        user.setActivationOtpExpiresAt(
                LocalDateTime.now().plusMinutes(Constants.OTP_EXPIRE_MINUTES)
        );
        userRepository.update(user);
        return true;
    }

    @Override
    public boolean preparePasswordResetOtp(String email, String otp) {
        User user = findByEmail(email);
        if (user == null || !user.isActive()) {
            return false;
        }
        user.setResetOtpHash(OtpUtil.hash(otp));
        user.setResetOtpExpiresAt(
                LocalDateTime.now().plusMinutes(Constants.OTP_EXPIRE_MINUTES)
        );
        userRepository.update(user);
        return true;
    }

    @Override
    public boolean resetPassword(String email, String otp, String newPassword) {
        User user = findByEmail(email);
        if (user == null || !user.isActive()
                || user.getResetOtpExpiresAt() == null
                || LocalDateTime.now().isAfter(user.getResetOtpExpiresAt())
                || !OtpUtil.matches(otp, user.getResetOtpHash())) {
            return false;
        }
        user.setPassword(PasswordUtil.hash(newPassword));
        user.setResetOtpHash(null);
        user.setResetOtpExpiresAt(null);
        userRepository.update(user);
        return true;
    }
}
