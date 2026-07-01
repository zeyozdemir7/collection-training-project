package lv.bootcamp.shelter.service;

import lv.bootcamp.shelter.model.Animal;
import lv.bootcamp.shelter.service.data.ShelterReportData;


import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Optional;


public class ReportExportService {

    public void writeReport(Path outputPath, ShelterReportData reportData) {
        // TODO Step 4:
        // 1) Write upload-report.txt in required format.
        // 2) Include generated date, imported/skipped totals.
        // 3) Include unique species and per-species breakdown.
        // 4) Include oldest animal per species.
        // 5) Include animalsNeedingVetInput as name(species), name2(species2).
        // 6) Use UTF-8 and try-with-resources.
        try {
            if (outputPath.getParent() != null) {
                Files.createDirectories(outputPath.getParent());
            }
            try (BufferedWriter writer = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8)) {
                writeLine(writer, "Shelter Intake Report");
                writeLine(writer, "Generated: " + LocalDate.now());
                writeLine(writer, "");
                writeLine(writer, "Import summary:");
                writeLine(writer, " Total imported: " + reportData.importResult().allAnimals().size());
                writeLine(writer, " Total skipped: " + reportData.importResult().skippedRows());
                writeLine(writer, "");
                writeLine(writer, "Unique species: " + String.join(", ", reportData.uniqueSpecies()));
                writeLine(writer, "");
                writeLine(writer, "Per-species breakdown:");

                for (String species : reportData.uniqueSpecies()) {
                    int total = reportData.animalsBySpecies().get(species).size();
                    long vaccinated = reportData.vaccinatedCountBySpecies().getOrDefault(species, 0L);
                    long unvaccinated = reportData.unvaccinatedCountBySpecies().getOrDefault(species, 0L);
                    writeLine(writer, species + ": " + total
                    + ", vaccinated: " + vaccinated
                    + ", unvaccinated: " + unvaccinated);
                }
                writeLine(writer, "");
                writeLine(writer, "Oldest per Species: ");

                for (String species : reportData.uniqueSpecies()) {
                    Optional<Animal> oldest = reportData.oldestBySpecies().get(species);
                    if (oldest == null || oldest.isEmpty()){
                        writeLine(writer, species + ": none");
                    } else  {
                        Animal animal = oldest.get();
                        writeLine(writer, species + ": " + animal.getName() + " (" + animal.getAge() + ") ");
                    }
                }
                writeLine(writer, "");
                writeLine(writer, "Needs vet input: "
                + String.join(", ", reportData.animalsNeedingVetInput()));

            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to write the report" + outputPath, e);
        }


    }

    private void writeLine(BufferedWriter writer, String line) throws IOException {
        writer.write(line);
        writer.newLine();
    }
}
