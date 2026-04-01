package com.App.lbs_backend.entity;

import com.App.lbs_backend.core.AuditableEntity;
import jakarta.persistence.*;
import java.util.UUID;

import com.App.lbs_backend.core.Timestamps;

@Entity
@Table(name = "lbs_profil_menu", schema = "lbs")
@AttributeOverrides({
    @AttributeOverride(name = "modifierLe", column = @Column(name = "lbs_prme_modifier_le")),
    @AttributeOverride(name = "modifierPar", column = @Column(name = "lbs_prme_modifier_par", length = 100))
})
public class ProfilMenu extends AuditableEntity implements Timestamps {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "lbs_prme_uuid", length = 50, unique = true, nullable = false)
    private String uuid;

    @Column(name = "lbs_prme_profil_id")
    private Long profilId;

    @Column(name = "lbs_prme_code", length = 10)
    private String code;

    @Column(name = "lbs_prme_menu_id")
    private Long menuId;

    @Column(name = "lbs_prme_token", length = 500)
    private String token;

    // ===== RELATIONS =====
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lbs_prme_profil_id", insertable = false, updatable = false)
    private Profil profil;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lbs_prme_menu_id", insertable = false, updatable = false)
    private Menu menu;

    @PrePersist
    public void prePersist() {
        if (this.uuid == null) this.uuid = UUID.randomUUID().toString();
    }

    public ProfilMenu() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }

    public Long getProfilId() { return profilId; }
    public void setProfilId(Long profilId) { this.profilId = profilId; }

    public Long getMenuId() { return menuId; }
    public void setMenuId(Long menuId) { this.menuId = menuId; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public Profil getProfil() { return profil; }
    public void setProfil(Profil profil) { this.profil = profil; }

    public Menu getMenu() { return menu; }
    public void setMenu(Menu menu) { this.menu = menu; }
}