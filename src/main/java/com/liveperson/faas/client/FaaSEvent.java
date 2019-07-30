package com.liveperson.faas.client;

public enum FaaSEvent {
    ChatPostSurveyEmailTranscript("denver_post_survey_email_transcript"),
    MessagingNewConversation("controllerbot_messaging_new_conversation"),
    MessagingConversationEnd("controllerbot_messaging_conversation_end"),
    MessagingConversationIdle("controllerbot_messaging_conversation_idle"),
    MessagingConversationRouting("controllerbot_messaging_conversation_routing"),
    MessagingConversationMid("controllerbot_messaging_mid_conversation_msg"),
    MessagingParticipantChange("controllerbot_messaging_participants_change"),
    MessagingTTR("controllerbot_messaging_ttr"),
    MessagingSurveyStarted("surveybot_messaging_survey_started"),
    MessagingSurveyEnded("'surveybot_messaging_survey_ended");

    private final String eventId;

    private FaaSEvent(final String eventId){
        this.eventId = eventId;
    }

    @Override
    public String toString() {
        return eventId;
    }
}
