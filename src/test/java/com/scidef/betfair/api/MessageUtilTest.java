package com.scidef.betfair.api;

import com.betfair.publicapi.types.global.v3.MarketSummary;
import org.junit.Test;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import java.util.List;

import static com.scidef.betfair.api.MessageUtil.getMarketNameWithTime;
import static com.scidef.betfair.api.MessageUtil.parseCompressedMarketPrices;
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
        String s = "101442426~GBP~ACTIVE~0~1~BLA\\:asd~true~5.0~1274885395669~~Y:4056154~0~24778.26~1.81~" +
                "~54.1~false~3.12~1.89~~|1.79~54.64~L~1~1.78~82.0~L~2~1.77~117.55~L~3~|1.81~18.46" +
                "~B~1~1.82~103.26~B~2~1.83~127.83~B~3~:3793446~1~2427.42~9.0~~11.1~false~1.54~8.54~" +
                "~|8.8~109.03~L~1~8.2~18.28~L~2~8.0~25.0~L~3~|9.2~47.95~B~1~9.4~20.14~B~2~10.0~15" +
                ".0~B~3~:4020999~2~736.98~15.0~~6.9~false~10.72~14.0~~|14.5~12.81~L~1~14.0~16.68~L~" +
                "2~13.5~17.44~L~3~|17.5~6.0~B~1~18.0~4.84~B~2~19.0~39.4~B~3~:3932069~3~490.22~15.0~" +
                "~6.3~false~2.17~12.88~~|15.0~3.99~L~1~14.5~11.03~L~2~13.5~8.15~L~3~|18.5~6.0~B~1~19.0~" +
                "3.0~B~2~20.0~7.2~B~3~:3826855~4~1002.72~24.0~~4.5~false~2.61~18.26~~|24.0~25.27~L~1~23" +
                ".0~3.47~L~2~22.0~8.62~L~3~|25.0~2.0~B~1~26.0~6.12~B~2~30.0~75.0~B~3~:3797698~5~545.1" +
                "4~26.0~~3.7~false~12.24~24.14~~|26.0~3.0~L~1~25.0~42.21~L~2~24.0~9.01~L~3~|27.0~2.16~B" +
                "~1~28.0~5.74~B~2~29.0~14.42~B~3~:4074501~6~344.34~40.0~~2.6~false~8.69~31.36~~|38.0~4.0" +
                "~L~1~36.0~15.84~L~2~32.0~3.59~L~3~|42.0~4.36~B~1~46.0~4.91~B~2~55.0~2.0~B~3~:4156516~7~" +
                "519.12~40.0~~2.4~false~11.36~22.27~~|36.0~5.33~L~1~34.0~6.24~L~2~32.0~3.05~L~3~|44.0~14.89" +
                "~B~1~50.0~10.0~B~2~55.0~2.26~B~3~:3855286~8~434.44~55.0~~2.3~false~3.49~38.78~~|50.0~10.59~" +
                "L~1~48.0~7.06~L~2~40.0~2.86~L~3~|55.0~15.8~B~1~60.0~20.0~B~2~80.0~50.0~B~3~:4115114~9~267.78" +
                "~65.0~~1.8~false~5.82~18.7~~|60.0~11.96~L~1~55.0~2.0~L~2~50.0~5.86~L~3~|75.0~18.6~B~1~" +
                "95.0~2.0~B~2~100.0~10.0~B~3~:424541~10~181.78~70.0~~1.7~false~14.0~45.21~~|70.0~10.99" +
                "~L~1~48.0~2.29~L~2~46.0~2.48~L~3~|100.0~3.0~B~1~110.0~6.0~B~2~130.0~6.0~B~3~:3962620" +
                "~11~127.1~85.0~~1.4~false~14.0~53.91~~|85.0~2.0~L~1~80.0~2.0~L~2~75.0~2.0~L~3~|90.0~8" +
                ".51~B~1~100.0~2.0~B~2~110.0~5.0~B~3~:4299096~12~341.86~95.0~~1.1~false~14.0~72.77~~|95.0" +
                "~27.73~L~1~85.0~32.69~L~2~80.0~16.0~L~3~|100.0~5.05~B~1~110.0~2.0~B~2~120.0~2.0~B~3~";

        List<RunnerPricesWrapper> l = parseCompressedMarketPrices(s);

        assertThat(l.get(0).getRunnerPrices().getSelectionId(), is(4056154));
        assertThat(l.get(0).getBestPricesToBack().get(0).getAmountAvailable(), is(54.64));
        assertThat(l.get(0).getBestPricesToLay().get(1).getPrice(), is(1.82));
    }

}
