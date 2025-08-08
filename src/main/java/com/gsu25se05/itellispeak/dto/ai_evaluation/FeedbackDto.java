package com.gsu25se05.itellispeak.dto.ai_evaluation;

public class FeedbackDto {
    private KnowledgeFeedback knowledge;
    private CommunicationFeedback communication;
    private String conclusion;

    public FeedbackDto() {}

    public FeedbackDto(KnowledgeFeedback knowledge, CommunicationFeedback communication, String conclusion) {
        this.knowledge = knowledge;
        this.communication = communication;
        this.conclusion = conclusion;
    }

    // Getters and setters
    public KnowledgeFeedback getKnowledge() {
        return knowledge;
    }

    public void setKnowledge(KnowledgeFeedback knowledge) {
        this.knowledge = knowledge;
    }

    public CommunicationFeedback getCommunication() {
        return communication;
    }

    public void setCommunication(CommunicationFeedback communication) {
        this.communication = communication;
    }

    public String getConclusion() {
        return conclusion;
    }

    public void setConclusion(String conclusion) {
        this.conclusion = conclusion;
    }

    public static class KnowledgeFeedback {
        private String correctness;
        private String improvement;
        private String strengths;

        public KnowledgeFeedback() {}

        public KnowledgeFeedback(String correctness, String improvement, String strengths) {
            this.correctness = correctness;
            this.improvement = improvement;
            this.strengths = strengths;
        }

        // Getters and setters
        public String getCorrectness() {
            return correctness;
        }

        public void setCorrectness(String correctness) {
            this.correctness = correctness;
        }

        public String getImprovement() {
            return improvement;
        }

        public void setImprovement(String improvement) {
            this.improvement = improvement;
        }

        public String getStrengths() {
            return strengths;
        }

        public void setStrengths(String strengths) {
            this.strengths = strengths;
        }
    }

    public static class CommunicationFeedback {
        private String clarity;
        private String conciseness;
        private String terminology;

        public CommunicationFeedback() {}

        public CommunicationFeedback(String clarity, String conciseness, String terminology) {
            this.clarity = clarity;
            this.conciseness = conciseness;
            this.terminology = terminology;
        }

        // Getters and setters
        public String getClarity() {
            return clarity;
        }

        public void setClarity(String clarity) {
            this.clarity = clarity;
        }

        public String getConciseness() {
            return conciseness;
        }

        public void setConciseness(String conciseness) {
            this.conciseness = conciseness;
        }

        public String getTerminology() {
            return terminology;
        }

        public void setTerminology(String terminology) {
            this.terminology = terminology;
        }
    }
}
