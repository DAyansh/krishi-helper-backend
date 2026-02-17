package com.ayansh.Backend.Model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.action.internal.OrphanRemovalAction;

import java.util.Set;
import java.util.prefs.Preferences;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Roles {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long roleId;

    @ToString.Exclude
    @Enumerated(EnumType.STRING)
    @Column(length = 20, name = "role_name")
    private AppRole roleName;

    public Roles(AppRole roleName) {
        this.roleName = roleName;

    }

}
