package dev.rox;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class InfoTest {
    @Test void staffRanks() {
        assertTrue(Info.staffLike("[Admin] Steve"));
        assertTrue(Info.staffLike("Helper Alex"));
        assertFalse(Info.staffLike("Steve"));
        assertFalse(Info.staffLike(null));
    }
    @Test void clockStartsAtSix() {
        assertEquals("06:00", Info.clock(0));
        assertEquals("12:00", Info.clock(6000));
        assertEquals("00:00", Info.clock(18000));
        assertEquals("06:30", Info.clock(24000 * 5 + 500));
        assertEquals("05:58", Info.clock(-17));
    }
    @Test void topTruncates() {
        assertEquals("a, b", Info.top(List.of("a", "b"), 3));
        assertEquals("a, b (+2 more)", Info.top(List.of("a", "b", "c", "d"), 2));
    }
}
