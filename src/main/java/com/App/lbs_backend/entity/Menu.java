package com.App.lbs_backend.entity;

import com.App.lbs_backend.core.AuditableEntity;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.App.lbs_backend.core.Timestamps;

@Entity
@Table(name = "lbs_menu", schema = "lbs")
@AttributeOverrides({
    @AttributeOverride(name = "modifierLe", column = @Column(name = "lbs_menu_modifier_le")),
    @AttributeOverride(name = "modifierPar", column = @Column(name = "lbs_menu_modifier_par", length = 100))
})
public class Menu extends AuditableEntity implements Timestamps {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "lbs_menu_uuid", length = 50, unique = true, nullable = false)
    private String uuid;

    @Column(name = "lbs_menu_code", length = 20)
    private String code;

    @Column(name = "lbs_menu_description", length = 500)
    private String description;

    @Column(name = "lbs_menu_path", length = 200)
    private String path;

    @Column(name = "lbs_menu_ordre")
    private Integer ordre;

    @Column(name = "lbs_menu_titre", length = 100)
    private String titre;

    @Column(name = "lbs_menu_enfant_id")
    private Long menuEnfantId;

    // ===== RELATIONS =====
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lbs_menu_enfant_id", insertable = false, updatable = false)
    private Menu menuParent;

    @OneToMany(mappedBy = "menuParent", fetch = FetchType.LAZY)
    private List<Menu> listeMenuEnfant;

    @OneToMany(mappedBy = "menu", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProfilMenu> listeProfilMenu = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (this.uuid == null) this.uuid = UUID.randomUUID().toString();
    }

    public Menu() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }

    public Integer getOrdre() { return ordre; }
    public void setOrdre(Integer ordre) { this.ordre = ordre; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public Long getMenuEnfantId() { return menuEnfantId; }
    public void setMenuEnfantId(Long menuEnfantId) { this.menuEnfantId = menuEnfantId; }

    public Menu getMenuParent() { return menuParent; }
    public void setMenuParent(Menu menuParent) { this.menuParent = menuParent; }

    public List<Menu> getListeMenuEnfant() { return listeMenuEnfant; }
    public void setListeMenuEnfant(List<Menu> listeMenuEnfant) { this.listeMenuEnfant = listeMenuEnfant; }

    public List<ProfilMenu> getListeProfilMenu() { return listeProfilMenu; }
    public void setListeProfilMenu(List<ProfilMenu> listeProfilMenu) { this.listeProfilMenu = listeProfilMenu; }
}