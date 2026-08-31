package com.App.lbs_backend.dto.request;

import com.App.lbs_backend.core.http.request.FormRequest;

/**
 * Marqueur générique requis par MasterController<E,R,F> — le journal de caisse n'accepte
 * aucune écriture directe (voir MouvementCaisseController), donc ce type n'est jamais rempli.
 */
public class MouvementCaisseRequest implements FormRequest {
}
