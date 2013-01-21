package com.scidef.betfair.api;

import com.betfair.publicapi.types.global.v3.MarketSummary;
import org.junit.Test;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import java.util.List;

import static com.scidef.betfair.api.MessageUtil.getMarketNameWithTime;
import static com.scidef.betfair.api.MessageUtil.parseCompressedMarketPrices;
import static com.scidef.betfair.api.TestConstant.COMPRESSED_MARKET_DATA;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * Tests for the methods in <code>MessageUtils</code>.
 * <p/>
 * User: tompearson
 * Date: 20/01/2013
 */
public class MessageUtilTest {

    @Test
    public void testGetMarketNameWithTime() throws DatatypeConfigurationException {
        MarketSummary summary = new MarketSummary();
        summary.setMarketName("Doodle");
        summary.setStartTime(DatatypeFactory.newInstance().newXMLGregorianCalendar(
                2000, 1, 1, 13, 0, 0, 0, 0
        ));

        String nameWithTime = getMarketNameWithTime(summary);

        assertThat(nameWithTime, is("Doodle 13:00"));
    }

    @Test
    public void testCompressedPricesParsing() {
        List<RunnerPricesWrapper> l = parseCompressedMarketPrices(COMPRESSED_MARKET_DATA);

        assertThat(l.get(0).getRunnerPrices().getSelectionId(), is(4056154));
        assertThat(l.get(0).getBestPricesToBack().get(0).getAmountAvailable(), is(54.64));
        assertThat(l.get(0).getBestPricesToLay().get(1).getPrice(), is(1.82));
    }

}
