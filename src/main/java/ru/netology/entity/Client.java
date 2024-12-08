package ru.netology.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "clients", schema = "cloud")
@Data
@NoArgsConstructor
public class Client {

    @Id
    @Column(name = "login", nullable = false, unique = true)
    private String login;

    @Column(name = "password", nullable = false)
    private String password;

    public Client(String login, String password) {
        this.login = login;
        this.password = password;
    }
}

