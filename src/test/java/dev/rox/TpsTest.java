package dev.rox;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TpsTest {
    /** Client ticks every 1/20 s; the server sends the age every 20 of ITS ticks, i.e. every 20*20/tps client ticks. */
    private static double simulate(double serverTps, int clientTicks) {
        Tps t = new Tps();
        long local = 0, server = 0;
        double sinceUpdate = 0;
        for (int i = 0; i < clientTicks; i++) {
            local++;
            sinceUpdate += serverTps / 20.0;           // server ticks that happened during this client tick
            if (sinceUpdate >= 20) { sinceUpdate -= 20; server += 20; local = server; }
            t.sample(local);
        }
        return t.value();
    }

    @Test void healthyServerReads20() { assertTrue(simulate(20, 2000) > 19.9); }
    @Test void halfSpeedServerReadsAboutTen() { double v = simulate(10, 1600); assertTrue(v > 9 && v < 11.5, "got " + v); }
    @Test void slowServerReadsLow() { double v = simulate(5, 2400); assertTrue(v > 4 && v < 7, "got " + v); }
    @Test void estimateHandlesEdges() {
        assertTrue(Tps.estimate(0) == 20 && Tps.estimate(5) == 20);
        assertTrue(Tps.estimate(-20) == 10);
        assertTrue(Tps.estimate(-100000) >= 0.5);
    }
    @Test void worldChangeJumpIsIgnoredAndResetWorks() {
        Tps t = new Tps();
        t.sample(100); t.sample(101); t.sample(5000);       // far jump forward: ignored
        t.sample(10);                                         // huge jump back (new world): ignored
        assertTrue(t.value() == 20);
        t.reset();
        assertTrue(t.value() == 20);
    }
    @Test void recoversWhenCorrectionsStop() {
        Tps t = new Tps();
        long age = 1000;
        for (int i = 0; i < 6; i++) { t.sample(age); age += 1; t.sample(age - 20 + 1 - 1); age = age - 20 + 1; }   // some corrections
        double low = t.value();
        assertTrue(low < 20);
        for (int i = 0; i < 2000; i++) t.sample(++age);
        assertTrue(t.value() > low);
    }
}
