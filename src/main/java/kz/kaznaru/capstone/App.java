package kz.kaznaru.capstone;

import java.time.Clock;
import kz.kaznaru.capstone.infra.DemoData;
import kz.kaznaru.capstone.infra.InMemoryDoctorRepository;
import kz.kaznaru.capstone.infra.InMemoryPatientRepository;
import kz.kaznaru.capstone.infra.InMemorySlotRepository;

/** Composition root: the ONLY place allowed to wire concrete classes together. */
public final class App {
    private App() { }

    public static void main(String[] args) {
        InMemoryDoctorRepository doctors = new InMemoryDoctorRepository();
        DemoData.seed(doctors, new InMemoryPatientRepository(), new InMemorySlotRepository(), Clock.systemDefaultZone());
        doctors.findAll().forEach(d -> System.out.println(d.id() + ". " + d.fullName() + " — " + d.specialty()));
    }
}
