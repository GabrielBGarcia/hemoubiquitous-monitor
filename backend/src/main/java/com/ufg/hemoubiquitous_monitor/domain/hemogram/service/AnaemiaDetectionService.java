package com.ufg.hemoubiquitous_monitor.domain.hemogram.service;

import com.ufg.hemoubiquitous_monitor.domain.hemogram.model.AnaemiaStatus;
import com.ufg.hemoubiquitous_monitor.domain.hemogram.model.Hemoglobin;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Calendar;
import java.util.Date;

@Service
public class AnaemiaDetectionService {
    /**
     * Critérios OMS para anemia (sem gestantes):
     * - Homens adultos: Hb < 13,0 g/dL
     * - Mulheres adultas: Hb < 12,0 g/dL
     * - Crianças 6m-5a: Hb < 11,0 g/dL
     * - Crianças 5-12a: Hb < 11,5 g/dL
     * - Adolescentes 12-15a: Hb < 12,0 g/dL
     *
     * Espera que Hemoglobin contenha valor em g/L e Patient tenha sexo/idade.
     */
    public AnaemiaStatus detect(Hemoglobin hemoglobin, String gender, Date birthDate) {
        double hb = hemoglobin.getValueInGramsPerLiter();
        int age = -1;
        if (birthDate != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(birthDate);
            int birthYear = cal.get(Calendar.YEAR);
            int birthMonth = cal.get(Calendar.MONTH);
            int birthDay = cal.get(Calendar.DAY_OF_MONTH);
            Calendar now = Calendar.getInstance();
            int years = now.get(Calendar.YEAR) - birthYear;
            if (now.get(Calendar.MONTH) < birthMonth || (now.get(Calendar.MONTH) == birthMonth && now.get(Calendar.DAY_OF_MONTH) < birthDay)) {
                years--;
            }
            age = years;
        }
        String g = gender != null ? gender.toLowerCase() : "";

        boolean hasAnaemia = false;
        if (age >= 0 && age < 5) {
            hasAnaemia = hb < 110.0;
        } else if (age >= 5 && age < 12) {
            hasAnaemia = hb < 115.0;
        } else if (age >= 12 && age < 15) {
            hasAnaemia = hb < 120.0;
        } else if (g.startsWith("f") && age >= 15) {
            hasAnaemia = hb < 120.0;
        } else if (g.startsWith("m") && age >= 15) {
            hasAnaemia = hb < 130.0;
        }
        // Default: não anêmico
        return new AnaemiaStatus(hasAnaemia, Instant.now());
    }
}
