package com.scidef.betfair.api;

import com.betfair.publicapi.types.exchange.v5.BetTypeEnum;

import java.util.Date;

/**
 * A class representing a bet that can be placed via the Betfair APIs.
 * <p/>
 * User: tompearson
 * Date: 17/01/2013
 */
public class Bet {

    private Long betId;
    private String runnerName;
    private int marketId;
    private int selectionId;
    private BetTypeEnum betType;
    private double price;
    private Double size;
    private double amountAvailable;
    private Double amountMatched;
    private String location;
    private String marketDesc;
    private Date eventTime;
    private Date timePlaced;
    private Integer eventOutcome; // 0: no place; 1: first; 2: second; etc
    private Boolean nonRunner;
    private Double profitOrLoss;
    private Integer numberOfRunners;

    /**
     * No-arg constructor
     */
    public Bet() {
    }

    /**
     * Basic constructor taking arguments that will be known ahead of placing the bet
     *
     * @param marketId the id of the market
     * @param selectionId the id of the desired runner or selection within the market
     * @param betType the bet type (back, lay or equivalent Asian Handicap types)
     * @param price the price (odds) you want to set for the bet
     * @param amountAvailable the amount that was available at the time of the bet
     * @param eventTime the time at which the event takes place
     * @param location the location of the event
     * @param marketDesc the market description for the event
     */
    public Bet(int marketId, int selectionId, BetTypeEnum betType, double price, double amountAvailable,
               Date eventTime, String location, String marketDesc) {
        this.marketId = marketId;
        this.selectionId = selectionId;
        this.betType = betType;
        this.price = price;
        this.amountAvailable = amountAvailable;
        this.eventTime = eventTime;
        this.location = location;
        this.marketDesc = marketDesc;
    }

    public Long getBetId() {
        return betId;
    }

    public void setBetId(Long betId) {
        this.betId = betId;
    }

    public String getRunnerName() {
        return runnerName;
    }

    public void setRunnerName(String runnerName) {
        this.runnerName = runnerName;
    }

    public int getMarketId() {
        return marketId;
    }

    public void setMarketId(int marketId) {
        this.marketId = marketId;
    }

    public int getSelectionId() {
        return selectionId;
    }

    public void setSelectionId(int selectionId) {
        this.selectionId = selectionId;
    }

    public BetTypeEnum getBetType() {
        return betType;
    }

    public void setBetType(BetTypeEnum betType) {
        this.betType = betType;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public Double getSize() {
        return size;
    }

    public void setSize(Double size) {
        this.size = size;
    }

    public double getAmountAvailable() {
        return amountAvailable;
    }

    public void setAmountAvailable(double amountAvailable) {
        this.amountAvailable = amountAvailable;
    }

    public Double getAmountMatched() {
        return amountMatched;
    }

    public void setAmountMatched(Double amountMatched) {
        this.amountMatched = amountMatched;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getMarketDesc() {
        return marketDesc;
    }

    public void setMarketDesc(String marketDesc) {
        this.marketDesc = marketDesc;
    }

    public Date getEventTime() {
        return eventTime;
    }

    public void setEventTime(Date eventTime) {
        this.eventTime = eventTime;
    }

    public Date getTimePlaced() {
        return timePlaced;
    }

    public void setTimePlaced(Date timePlaced) {
        this.timePlaced = timePlaced;
    }

    public Integer getEventOutcome() {
        return eventOutcome;
    }

    public void setEventOutcome(Integer eventOutcome) {
        this.eventOutcome = eventOutcome;
    }

    public Boolean getNonRunner() {
        return nonRunner;
    }

    public void setNonRunner(Boolean nonRunner) {
        this.nonRunner = nonRunner;
    }

    public Double getProfitOrLoss() {
        return profitOrLoss;
    }

    public void setProfitOrLoss(Double profitOrLoss) {
        this.profitOrLoss = profitOrLoss;
    }

    public Integer getNumberOfRunners() {
        return numberOfRunners;
    }

    public void setNumberOfRunners(Integer numberOfRunners) {
        this.numberOfRunners = numberOfRunners;
    }

    @Override
    public String toString() {
        return "Bet{" +
                "betId=" + betId +
                ", runnerName='" + runnerName + '\'' +
                ", marketId=" + marketId +
                ", selectionId=" + selectionId +
                ", betType=" + betType +
                ", price=" + price +
                ", size=" + size +
                ", amountAvailable=" + amountAvailable +
                ", amountMatched=" + amountMatched +
                ", location='" + location + '\'' +
                ", marketDesc='" + marketDesc + '\'' +
                ", eventTime=" + eventTime +
                ", timePlaced=" + timePlaced +
                ", eventOutcome=" + eventOutcome +
                ", nonRunner=" + nonRunner +
                ", profitOrLoss=" + profitOrLoss +
                ", numberOfRunners=" + numberOfRunners +
                '}';
    }
}
