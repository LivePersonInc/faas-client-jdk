package com.liveperson.faas.client;

public enum FaaSEvent {
    DenverPostSurveyEmailTranscript("denver_post_survey_email_transcript"),
    CRMWidget("crm_widget"),
    ControllerBotMessagingNewConversation("controllerbot_messaging_new_conversation");

    private final String eventId;

    private FaaSEvent(final String eventId){
        this.eventId = eventId;
    }

    @Override
    public String toString() {
        return eventId;
    }
}
