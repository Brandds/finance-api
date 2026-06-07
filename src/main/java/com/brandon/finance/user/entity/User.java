package com.brandon.finance.user.entity;

import java.util.List;

import com.brandon.finance.category.entity.Category;
import com.brandon.finance.expense.entity.Expense;
import com.brandon.finance.shared.base.entity.AuditableEntity;
import com.brandon.finance.user.enums.Role;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Entity
@Setter
@Table(name = "users")
public class User extends AuditableEntity {

     @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(nullable = true)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Role role;

    @Column(nullable = true, unique = true, length = 255)
    private String googleId;

    @Column(nullable = true, length = 50)
    private String oauthProvider;

    @Column(nullable = true, length = 1024)
    private String googlePictureUrl;

    @OneToMany(mappedBy = "user")
    private List<Expense> expenses;

    @OneToMany(mappedBy = "user")
    private List<Category> categories;

    @Column(nullable = false, unique = true, length = 11)
    private String cpf;

    public User(Long id){
        super(id);
    }
    public User(String name, String email, String password, Role role, String cpf) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.cpf = cpf;
    }

    public void updatePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    public void updateProfile(String name, String email) {
        this.name = name;
        this.email = email;
    }

    public void setGoogleId(String googleId) {
        this.googleId = googleId;
        this.oauthProvider = "GOOGLE";
    }

    public void updateFromGoogleInfo(String name, String picture) {
        this.name = name;
        this.googlePictureUrl = picture;
    }

    public String getOAuthProvider() {
        return this.googleId != null ? "GOOGLE" : "LOCAL";
    }
    
}
