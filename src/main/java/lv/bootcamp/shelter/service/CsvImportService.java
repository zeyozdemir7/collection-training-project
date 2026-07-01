package lv.bootcamp.shelter.service;

import lombok.extern.slf4j.Slf4j;
import lv.bootcamp.shelter.model.Animal;
import lv.bootcamp.shelter.service.data.ImportResult;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Slf4j
public class CsvImportService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public ImportResult importAnimals(Path inputPath) {
        log.info("Starting import from {}", inputPath);

        List<Animal> allAnimals = new ArrayList<>();
        int skippedRows = 0;

        // TODO Step 1:

        try {
            List<String> lines = Files.readAllLines(inputPath, StandardCharsets.UTF_8);
            for (int i = 1; i < lines.size(); i++) {
                String line = lines.get(i).trim();
                if (line.isEmpty()) {
                    continue;
                }
                int rowNumber = i + 1;
                String[] fields = line.split(",", -1);

                if (fields.length != 5) {
                    log.warn("Skipped malformed row {} , expected 5 columns, got {}", rowNumber, fields.length);
                    skippedRows++;
                    continue;
                }
                String name = fields[0].trim();
                String species = fields[1].trim();
                String ageValue = fields[2].trim();
                String vaccinatedValue = fields[3].trim();
                String intakeDateValue = fields[4].trim();

                if (name.isEmpty() || species.isEmpty()){
                    log.warn("Skipping malformed row {} , name and species are empty", rowNumber);
                    skippedRows++;
                    continue;
                }
                Integer age;
                if (ageValue.isEmpty()) {
                    age = null;
                } else  {
                    try {
                        age = Integer.parseInt(ageValue);
                        if (age <= 0){
                            log.warn("Skipping malformed row {} , age value must be positive, got '{}' ", rowNumber, ageValue);
                            skippedRows++;
                            continue;
                        }
                    } catch (NumberFormatException e) {
                        log.warn("Skipping malformed row {}: invalid age '{}' ", rowNumber, ageValue);
                        skippedRows++;
                        continue;
                    }
                }
                Boolean vaccinated = vaccinatedStatus(vaccinatedValue, rowNumber);
                if (vaccinated == null) {
                    skippedRows++;
                    continue;
                }
                LocalDate intakeDate = intakeDateStatus(intakeDateValue, rowNumber);
                if (intakeDate == null) {
                    skippedRows++;
                    continue;
                }
                allAnimals.add(new Animal(name, species, age, vaccinated, intakeDate));
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read intake file: " + inputPath, e);
        }

        return new ImportResult(allAnimals, skippedRows);
    }

    private Boolean vaccinatedStatus(String vaccinatedValue, int rowNumber) {
        if ("true".equalsIgnoreCase(vaccinatedValue)) {
            return true;
        }
        if ("false".equalsIgnoreCase(vaccinatedValue)) {
            return false;
        }
        log.warn("Skipping malformed row {}: invalid vaccinated value '{}'", rowNumber, vaccinatedValue);
        return null;
    }

    private LocalDate intakeDateStatus(String intakeDateValue, int rowNumber) {
        try {
            return LocalDate.parse(intakeDateValue, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            log.warn("Skipping malformed row {}: invalid intakeDate '{}'", rowNumber, intakeDateValue);
            return null;
        }
    }

}
