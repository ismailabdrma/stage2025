package com.example.stage2025.entity;


import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Getter
@Setter
@Entity
@Table(name = "admins")
@NoArgsConstructor
public class Admin extends User {

}
