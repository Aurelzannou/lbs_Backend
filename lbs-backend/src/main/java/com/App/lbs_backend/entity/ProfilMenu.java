package com.App.lbs_backend.entity;

import com.App.lbs_backend.core.AuditableEntity;
import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "lbs_profil_menu", schema = "lbs")
@AttributeOverrides({
    @AttributeOverride(name = "modifierLe", column = @Column(name = "lbs_prme_modifier_le")),
    @AttributeOverride(name = "modifierPar", column = @Column(name = "lbs_prme_modifier_par", length = 100))
})
public class ProfilMenu extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "lbs_prme_uuid", length = 50, unique = true, nullable = false)
    private String uuid;

    @Column(name = "lbs_prme_profil_id")
    private Integer profilId;

    @Column(name = "lbs_prme_code", length = 10)
    private String code;

    @Column(name = "lbs_prme_menu_id")
    private Integer menuId;

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

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }

    public Integer getProfilId() { return profilId; }
    public void setProfilId(Integer profilId) { this.profilId = profilId; }

    public Integer getMenuId() { return menuId; }
    public void setMenuId(Integer menuId) { this.menuId = menuId; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public Profil getProfil() { return profil; }
    public void setProfil(Profil profil) { this.profil = profil; }

    public Menu getMenu() { return menu; }
    public void setMenu(Menu menu) { this.menu = menu; }
}