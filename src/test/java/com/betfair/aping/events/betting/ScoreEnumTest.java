package com.betfair.aping.events.betting;

import com.betfair.aping.com.betfair.aping.events.betting.ScoreEnum;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by markwilliams on 8/25/14.
 */
public class ScoreEnumTest {
    @Test
    public void scoreEnumTest(){
        ScoreEnum scoreEnum = ScoreEnum.NIL_NIL;
        assertEquals("0 - 0", scoreEnum.toString());
        assertEquals(ScoreEnum.NIL_NIL, scoreEnum);
        assertEquals("NIL_NIL", scoreEnum.getName());
    }

    @Test
    public void scoreEnumFromStringTest(){
        ScoreEnum scoreEnum = ScoreEnum.fromString("Any Unquoted");
        assertEquals("Any Unquoted", scoreEnum.toString());
        assertEquals(ScoreEnum.ANY_UNQUOTED, scoreEnum);
        assertEquals("ANY_UNQUOTED", scoreEnum.getName());
    }
}
