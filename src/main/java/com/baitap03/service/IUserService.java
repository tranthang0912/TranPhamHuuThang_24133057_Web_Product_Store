package com.baitap03.service;

import com.baitap03.model.User;

public interface IUserService {

    boolean register(User user);

    User findById(int userId);

    User updateProfile(int userId, String fullname, String phone, String image);

    User login(
            String identifier,
            String password
    );

    User findByIdentifier(String identifier);

    User findByEmail(String email);

    boolean activateAccount(String email, String otp);

    boolean prepareActivationOtp(String email, String otp);

    boolean preparePasswordResetOtp(String email, String otp);

    boolean resetPassword(String email, String otp, String newPassword);
}
