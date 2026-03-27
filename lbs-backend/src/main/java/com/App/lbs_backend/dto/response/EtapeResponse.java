package com.App.lbs_backend.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class EtapeResponse {
    private Integer id;
    private String uuid;
    private String code;
    private String libelle;
    private Integer ordre;
    private Boolean actif;
    private LocalDateTime modifierLe;
    private String modifierPar;
}
