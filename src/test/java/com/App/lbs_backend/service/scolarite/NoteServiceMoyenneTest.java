package com.App.lbs_backend.service.scolarite;

import com.App.lbs_backend.entity.Note;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

/**
 * Vérifie la règle de calcul de la moyenne d'une matière telle que définie par l'école :
 *
 *  - moyenne des interros  = somme des interros ÷ nombre d'interros organisées (non composée = 0)
 *  - moyenne des devoirs   = somme des devoirs  ÷ nombre de devoirs organisés   (non composé  = 0)
 *  - moyenne de la matière = (moyenne des interros + moyenne des devoirs) ÷ 2
 *  - conduite              = la note unique (0 si non notée)
 */
class NoteServiceMoyenneTest {

    private static Note note(String type, int numero, Double valeur) {
        Note n = new Note();
        n.setTypeEvaluation(type);
        n.setNumero(numero);
        n.setValeur(valeur);
        return n;
    }

    private static Double moyenneMatiere(List<Note> notesEleve, List<Note> notesClasse, boolean conduite) {
        int nbInterros = conduite ? 1
                : NoteService.nombreEvaluationsOrganisees(notesClasse, NoteService.INTERROGATION, NoteService.MAX_INTERROGATIONS);
        int nbDevoirs = conduite ? 0
                : NoteService.nombreEvaluationsOrganisees(notesClasse, NoteService.DEVOIR, NoteService.MAX_DEVOIRS);
        Double moyInterros = NoteService.calculerMoyenneInterrogations(notesEleve, nbInterros);
        Double moyDevoirs = conduite ? null
                : NoteService.moyenneComposante(notesEleve, NoteService.DEVOIR, nbDevoirs);
        return NoteService.calculerMoyenneMatiere(moyInterros, moyDevoirs, conduite);
    }

    @Test
    void cas_nominal_50_50_interros_devoirs() {
        // 2 interros (12 et 14 -> moyenne 13), 2 devoirs (15 et 11 -> moyenne 13)
        List<Note> notes = List.of(
                note(NoteService.INTERROGATION, 1, 12.0),
                note(NoteService.INTERROGATION, 2, 14.0),
                note(NoteService.DEVOIR, 1, 15.0),
                note(NoteService.DEVOIR, 2, 11.0));
        assertThat(moyenneMatiere(notes, notes, false)).isCloseTo(13.0, within(1e-9));
    }

    @Test
    void devoir_non_compose_compte_zero() {
        // interros -> 12 ; la classe a organisé 2 devoirs, l'élève n'a composé que le 1er (14)
        List<Note> notesClasse = List.of(
                note(NoteService.INTERROGATION, 1, 12.0),
                note(NoteService.DEVOIR, 1, 14.0),
                note(NoteService.DEVOIR, 2, 9.0));   // un autre élève a composé le devoir 2
        List<Note> notesEleve = List.of(
                note(NoteService.INTERROGATION, 1, 12.0),
                note(NoteService.DEVOIR, 1, 14.0));
        // moy interros = 12 ; moy devoirs = (14 + 0) / 2 = 7 ; matière = (12 + 7) / 2 = 9.5
        assertThat(moyenneMatiere(notesEleve, notesClasse, false)).isCloseTo(9.5, within(1e-9));
    }

    @Test
    void interro_manquante_compte_zero() {
        // la classe a fait 3 interros ; l'élève n'a que la 1re (18)
        List<Note> notesClasse = List.of(
                note(NoteService.INTERROGATION, 1, 18.0),
                note(NoteService.INTERROGATION, 2, 10.0),
                note(NoteService.INTERROGATION, 3, 10.0),
                note(NoteService.DEVOIR, 1, 12.0),
                note(NoteService.DEVOIR, 2, 12.0));
        List<Note> notesEleve = List.of(
                note(NoteService.INTERROGATION, 1, 18.0),
                note(NoteService.DEVOIR, 1, 12.0),
                note(NoteService.DEVOIR, 2, 12.0));
        // moy interros = (18 + 0 + 0) / 3 = 6 ; moy devoirs = 12 ; matière = (6 + 12) / 2 = 9
        assertThat(moyenneMatiere(notesEleve, notesClasse, false)).isCloseTo(9.0, within(1e-9));
    }

    @Test
    void matiere_jamais_evaluee_vaut_zero() {
        assertThat(moyenneMatiere(List.of(), List.of(), false)).isEqualTo(0.0);
    }

    @Test
    void une_seule_composante_organisee_vaut_la_moyenne() {
        // Uniquement des interros, aucun devoir organisé
        List<Note> notes = List.of(
                note(NoteService.INTERROGATION, 1, 10.0),
                note(NoteService.INTERROGATION, 2, 16.0));
        assertThat(moyenneMatiere(notes, notes, false)).isCloseTo(13.0, within(1e-9));
    }

    @Test
    void conduite_est_la_note_unique() {
        List<Note> notes = List.of(note(NoteService.INTERROGATION, 1, 17.0));
        assertThat(moyenneMatiere(notes, notes, true)).isCloseTo(17.0, within(1e-9));
    }

    @Test
    void conduite_non_notee_vaut_zero() {
        assertThat(moyenneMatiere(List.of(), List.of(), true)).isEqualTo(0.0);
    }
}
