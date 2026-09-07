package com.baitap03.model;

import java.io.Serializable;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(
    name = "Users",
    schema = "dbo"
)
public class User implements Serializable {

    private static final long serialVersionUID = 1L;


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "UserId")
    private int userid;


    @Column(
        name = "Username",
        nullable = false,
        unique = true,
        columnDefinition = "nvarchar(50)"
    )
    private String username;


    @Column(
        name = "FullName",
        nullable = false,
        columnDefinition = "nvarchar(100)"
    )
    private String fullname;

    @Column(name = "Phone", length = 20)
    private String phone;

    @Column(name = "Images", length = 255)
    private String images;

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getImages() {
        return images;
    }

    public void setImages(String images) {
        this.images = images;
    }


    @Column(
        name = "Password",
        nullable = false,
        columnDefinition = "nvarchar(255)"
    )
    private String password;

    @Column(
        name = "Email",
        columnDefinition = "nvarchar(150)"
    )
    private String email;

    @Column(name = "IsActive")
    private Boolean active;

    @Column(name = "ActivationOtpHash", length = 64)
    private String activationOtpHash;

    @Column(name = "ActivationOtpExpiresAt")
    private LocalDateTime activationOtpExpiresAt;

    @Column(name = "ResetOtpHash", length = 64)
    private String resetOtpHash;

    @Column(name = "ResetOtpExpiresAt")
    private LocalDateTime resetOtpExpiresAt;


    // Constructor không tham số
    // JPA bắt buộc phải có
    public User() {
    }


    // Constructor đầy đủ
    public User(
            int userid,
            String username,
            String fullname,
            String password) {

        this.userid = userid;
        this.username = username;
        this.fullname = fullname;
        this.password = password;
    }


    // Getter / Setter UserId

    public int getUserid() {

        return userid;
    }

    public void setUserid(int userid) {

        this.userid = userid;
    }


    // Getter / Setter Username

    public String getUsername() {

        return username;
    }

    public void setUsername(String username) {

        this.username = username;
    }


    // Getter / Setter FullName

    public String getFullname() {

        return fullname;
    }

    public void setFullname(String fullname) {

        this.fullname = fullname;
    }


    // Getter / Setter Password

    public String getPassword() {

        return password;
    }

    public void setPassword(String password) {

        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isActive() {
        // Bản ghi cũ chưa có cột IsActive sẽ nhận NULL và tiếp tục đăng nhập được.
        // Tài khoản đăng ký mới luôn được service gán false cho tới khi nhập OTP.
        return active == null || active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getActivationOtpHash() {
        return activationOtpHash;
    }

    public void setActivationOtpHash(String activationOtpHash) {
        this.activationOtpHash = activationOtpHash;
    }

    public LocalDateTime getActivationOtpExpiresAt() {
        return activationOtpExpiresAt;
    }

    public void setActivationOtpExpiresAt(LocalDateTime activationOtpExpiresAt) {
        this.activationOtpExpiresAt = activationOtpExpiresAt;
    }

    public String getResetOtpHash() {
        return resetOtpHash;
    }

    public void setResetOtpHash(String resetOtpHash) {
        this.resetOtpHash = resetOtpHash;
    }

    public LocalDateTime getResetOtpExpiresAt() {
        return resetOtpExpiresAt;
    }

    public void setResetOtpExpiresAt(LocalDateTime resetOtpExpiresAt) {
        this.resetOtpExpiresAt = resetOtpExpiresAt;
    }
}
