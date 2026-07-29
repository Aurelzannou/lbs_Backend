package com.App.lbs_backend.dto.response;

import lombok.Data;

@Data
public class ClasseMatiereANoterResponse {
    private Long classeId;
    private String classeLibelle;
    private Long matiereId;
    private String matiereLibelle;
}
