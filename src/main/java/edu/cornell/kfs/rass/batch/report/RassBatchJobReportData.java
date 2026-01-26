package edu.cornell.kfs.rass.batch.report;

import java.util.List;
import java.util.Map;

public class RassBatchJobReportData {
    protected int numberOfAgenciesCreated;
    protected int numberOfAgenciesUpdated;
    protected int numberOfAgenciesWithBusinessRuleFailures;
    protected List<String> agenciesInRASSfeedNotRequiringUpdatingInKFS;

    protected int numberOfAwardsCreated;
    protected int numberOfAwardsUpdated;
    protected int numberOfAwardsWithBusinessRuleFailures;
    protected List<String> awardsInRASSFeedNotRequiringUpdatingInKFS;

    protected Map<String, List<String>> agenciesErrorMessages;
    protected Map<String, String> successfullyProcessedAgenciesDocumentNumbers;
    protected Map<String, List<String>> awardErrorMessages;
    protected Map<String, String> successfullyProcessedAwardsDocumentNumbers;

    public int getNumberOfAgenciesCreated() {
        return numberOfAgenciesCreated;
    }

    public void setNumberOfAgenciesCreated(int numberOfAgenciesCreated) {
        this.numberOfAgenciesCreated = numberOfAgenciesCreated;
    }

    public int getNumberOfAgenciesUpdated() {
        return numberOfAgenciesUpdated;
    }

    public void setNumberOfAgenciesUpdated(int numberOfAgenciesUpdated) {
        this.numberOfAgenciesUpdated = numberOfAgenciesUpdated;
    }

    public int getNumberOfAgenciesWithBusinessRuleFailures() {
        return numberOfAgenciesWithBusinessRuleFailures;
    }

    public void setNumberOfAgenciesWithBusinessRuleFailures(int numberOfAgenciesWithBusinessRuleFailures) {
        this.numberOfAgenciesWithBusinessRuleFailures = numberOfAgenciesWithBusinessRuleFailures;
    }

    public List<String> getAgenciesInRASSfeedNotRequiringUpdatingInKFS() {
        return agenciesInRASSfeedNotRequiringUpdatingInKFS;
    }

    public void setAgenciesInRASSfeedNotRequiringUpdatingInKFS(List<String> agenciesInRASSfeedNotRequiringUpdatingInKFS) {
        this.agenciesInRASSfeedNotRequiringUpdatingInKFS = agenciesInRASSfeedNotRequiringUpdatingInKFS;
    }

    public int getNumberOfAwardsCreated() {
        return numberOfAwardsCreated;
    }

    public void setNumberOfAwardsCreated(int numberOfAwardsCreated) {
        this.numberOfAwardsCreated = numberOfAwardsCreated;
    }

    public int getNumberOfAwardsUpdated() {
        return numberOfAwardsUpdated;
    }

    public void setNumberOfAwardsUpdated(int numberOfAwardsUpdated) {
        this.numberOfAwardsUpdated = numberOfAwardsUpdated;
    }

    public int getNumberOfAwardsWithBusinessRuleFailures() {
        return numberOfAwardsWithBusinessRuleFailures;
    }

    public void setNumberOfAwardsWithBusinessRuleFailures(int numberOfAwardsWithBusinessRuleFailures) {
        this.numberOfAwardsWithBusinessRuleFailures = numberOfAwardsWithBusinessRuleFailures;
    }

    public List<String> getAwardsInRASSFeedNotRequiringUpdatingInKFS() {
        return awardsInRASSFeedNotRequiringUpdatingInKFS;
    }

    public void setAwardsInRASSFeedNotRequiringUpdatingInKFS(List<String> awardsInRASSFeedNotRequiringUpdatingInKFS) {
        this.awardsInRASSFeedNotRequiringUpdatingInKFS = awardsInRASSFeedNotRequiringUpdatingInKFS;
    }

    public Map<String, List<String>> getAgenciesErrorMessages() {
        return agenciesErrorMessages;
    }

    public void setAgenciesErrorMessages(Map<String, List<String>> agenciesErrorMessages) {
        this.agenciesErrorMessages = agenciesErrorMessages;
    }

    public Map<String, String> getSuccessfullyProcessedAgenciesDocumentNumbers() {
        return successfullyProcessedAgenciesDocumentNumbers;
    }

    public void setSuccessfullyProcessedAgenciesDocumentNumbers(Map<String, String> successfullyProcessedAgenciesDocumentNumbers) {
        this.successfullyProcessedAgenciesDocumentNumbers = successfullyProcessedAgenciesDocumentNumbers;
    }

    public Map<String, List<String>> getAwardErrorMessages() {
        return awardErrorMessages;
    }

    public void setAwardErrorMessages(Map<String, List<String>> awardErrorMessages) {
        this.awardErrorMessages = awardErrorMessages;
    }

    public Map<String, String> getSuccessfullyProcessedAwardsDocumentNumbers() {
        return successfullyProcessedAwardsDocumentNumbers;
    }

    public void setSuccessfullyProcessedAwardsDocumentNumbers(Map<String, String> successfullyProcessedAwardsDocumentNumbers) {
        this.successfullyProcessedAwardsDocumentNumbers = successfullyProcessedAwardsDocumentNumbers;
    }
}
