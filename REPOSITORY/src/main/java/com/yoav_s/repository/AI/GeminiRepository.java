package com.yoav_s.repository.AI;

import android.graphics.Bitmap;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.ChatFutures;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.BlockThreshold;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.ai.client.generativeai.type.GenerationConfig;
import com.google.ai.client.generativeai.type.HarmCategory;
import com.google.ai.client.generativeai.type.SafetySetting;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.yoav_s.repository.BASE.AI.BaseAiRepository;
import com.yoav_s.repository.BuildConfig;
//import com.repository.yoav_s.BuildConfig;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;

public class GeminiRepository implements BaseAiRepository {

    // (כל הקוד שכתבנו קודם לכן נשאר כאן - המשתנים, הבנאי וכו')
    private final GenerativeModelFutures model;
    private final ChatFutures chat;
    private final Executor mainExecutor;

    public GeminiRepository(Executor executor) {
        // 1. שמירת ה-Executor של ה-Thread הראשי
        this.mainExecutor = executor;

        // 2. יצירת אובייקט הגדרות להתנהגות המודל (GenerationConfig)
        GenerationConfig.Builder configBuilder = new GenerationConfig.Builder();
        configBuilder.temperature = 0.9f; // רמת יצירתיות גבוהה יותר
        configBuilder.topK = 16;
        configBuilder.topP = 0.1f;
        GenerationConfig generationConfig = configBuilder.build();

        // 3. יצירת רשימת הגדרות בטיחות (SafetySettings)
        List<SafetySetting> safetySettings = Collections.singletonList(
                new SafetySetting(HarmCategory.HARASSMENT, BlockThreshold.ONLY_HIGH)
        );

        // 4. יצירת אובייקט המודל הראשי של Gemini
        GenerativeModel geminiModel = new GenerativeModel(
                "gemini-2.0-flash",         // שם המודל המדויק שמצאנו שעובד
                BuildConfig.GEMINI_API_KEY, // מפתח ה-API הסודי שלנו
                generationConfig,           // הגדרות ההתנהגות שיצרנו
                safetySettings              // הגדרות הבטיחות שיצרנו
        );

        /*
        GenerativeModel geminiModel = new GenerativeModel(
                "gemini-2.0-flash",         // שם המודל המדויק שמצאנו שעובד
                "GEMINI_API_KEY",                     // מפתח ה-API הסודי שלנו
                generationConfig,                     // הגדרות ההתנהגות שיצרנו
                safetySettings                        // הגדרות הבטיחות שיצרנו
        );
         */


        // 5. עטיפת המודל בממשק Java Futures והגדרת אובייקט הצ'אט
        this.model = GenerativeModelFutures.from(geminiModel);
        this.chat = this.model.startChat(Collections.emptyList());        // ... (הבנאי המלא שלנו עם הגדרות המודל "gemini-2.0-flash") ...
    }

    @Override
    public ListenableFuture<String> generateText(String prompt) {
        Content content = new Content.Builder().addText(prompt).build();
        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);
        // We use Futures.transform to convert the full response into just a String
        return Futures.transform(response, GenerateContentResponse::getText, mainExecutor);
    }

    @Override
    public ListenableFuture<String> generateTextFromImage(String prompt, Bitmap image) {
        Content content = new Content.Builder().addText(prompt).addImage(image).build();
        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);
        return Futures.transform(response, GenerateContentResponse::getText, mainExecutor);
    }

    @Override
    public ListenableFuture<String> continueChat(String prompt) {
        Content content = new Content.Builder().addText(prompt).build();
        ListenableFuture<GenerateContentResponse> response = chat.sendMessage(content);
        return Futures.transform(response, GenerateContentResponse::getText, mainExecutor);
    }

    @Override
    public ListenableFuture<String> continueChatWithImage(String prompt, Bitmap image) {
        // 1. בונים את אובייקט ה-Content שיכיל גם את הטקסט וגם את התמונה.
        // הסדר לא משנה, ה-SDK יודע לטפל בזה.
        Content content = new Content.Builder()
                .addText(prompt)
                .addImage(image)
                .build();

        // 2. שולחים את ה-Content באמצעות אובייקט ה-chat.
        // אובייקט ה-chat ידאג לצרף את ההודעה החדשה (שכוללת תמונה)
        // להיסטוריית השיחה הקיימת ולשלוח הכל יחד ל-Gemini.
        ListenableFuture<GenerateContentResponse> response = chat.sendMessage(content);

        // 3. מבצעים טרנספורמציה כדי לקבל רק את הטקסט מהתשובה,
        // בדיוק כמו שעשינו במתודות האחרות.
        return Futures.transform(response, GenerateContentResponse::getText, mainExecutor);
    }
}
