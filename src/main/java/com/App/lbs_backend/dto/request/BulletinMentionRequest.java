package com.App.lbs_backend.dto.request;

import lombok.Data;

@Data
public class BulletinMentionRequest {
    private Boolean tableauHonneur;
    private Boolean felicitations;
    private Boolean encouragement;
    private Boolean avertissement;
    private String decisionConseil;
    private String observationDirecteur;
}
