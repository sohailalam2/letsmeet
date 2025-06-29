//package com.yourkoder.letsmeet.processmanager.ai.service;
//
//import com.yourkoder.letsmeet.domain.meet.model.GeneratedMeetingData;
//import com.yourkoder.letsmeet.domain.meet.valueobject.GeneratedMeetingInfo;
//import com.yourkoder.letsmeet.processmanager.meet.valueobject.Transcript;
//import io.quarkus.test.junit.QuarkusTest;
//import jakarta.inject.Inject;
//import org.junit.jupiter.api.Tag;
//import org.junit.jupiter.api.Test;
//
//import java.util.HashMap;
//
//@QuarkusTest
//@Tag("unit")
//class BedrockSummarizationServiceTest {
//
//    @Inject
//    AiSummarizationService summarizationService;
//
//    @Test
//    void test() throws Exception {
//
//        GeneratedMeetingData summarizationResult = summarizationService.summarizeText(Transcript.fromString(
//                        trnascript
//                ),
//                500, new HashMap<>());
//
//        System.out.println(summarizationResult.getMeetingSummary());
//        System.out.println(summarizationResult.getActionItems());
//        System.out.println(summarizationResult.getModelID());
//        System.out.println(summarizationResult.getProcessingTimeMillis());
//    }
//
//}
