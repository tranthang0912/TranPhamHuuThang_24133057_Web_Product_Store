package com.baitap03.repository;

import com.baitap03.model.User;

public interface IUserRepository {

    void insert(User user);

    void update(User user);

    User findById(int userId);

    User updateProfile(int userId, String fullname, String phone, String image);

    User findByUsername(String username);

    User findByEmail(String email);

    User findByIdentifier(String identifier);
}
