package com.scidef.betfair.sample;

import com.scidef.betfair.api.ExchangeAPI;
import com.scidef.betfair.api.GlobalAPI;
import com.scidef.betfair.api.HorseRacing;
import com.scidef.betfair.api.exception.BetfairException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.regex.Pattern;

/**
 * A simple sample use case of betfair-horses.
 * <p/>
 * User: tompearson
 * Date: 20/01/2013
 */
public class SampleApp {

    public static final String USERNAME = ""; // fill in Betfair username here
    public static final String PASSWORD = ""; // fill in Betfair password here

    public static void main(String[] args) throws BetfairException {
        // setup the HorseRacing class for use
        GlobalAPI globalAPI = new GlobalAPI();
        ExchangeAPI exchangeAPI = new ExchangeAPI(globalAPI);
        HorseRacing horseRacing = new HorseRacing(USERNAME, PASSWORD, globalAPI, exchangeAPI);

        // include only events that meet the pattern for standard races
        horseRacing.setEventInclusionPatterns(
                Collections.singletonList(Pattern.compile("^[A-Za-z]*? [0-9]*?[a-z][a-z] [A-Za-z]*?$"))
        );

        // exclude non-flat horse racing markets
        List<Pattern> marketExclusionPatterns = new ArrayList<Pattern>(4);
        marketExclusionPatterns.add(Pattern.compile("To Be Placed"));
        marketExclusionPatterns.add(Pattern.compile("Hrd"));
        marketExclusionPatterns.add(Pattern.compile("Chs"));
        marketExclusionPatterns.add(Pattern.compile("NHF"));
        horseRacing.setMarketExclusionPatterns(marketExclusionPatterns);

        // login and then retrieve and pretty-print the events map for today
        horseRacing.login();
        HorseRacing.prettyPrintEventsMapToStdout(
                horseRacing.getEventsMap(new GregorianCalendar())
        );
    }

}
