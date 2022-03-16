package org.group_gb.tg_bot.yandex_api;


import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.extern.slf4j.Slf4j;
import org.group_gb.tg_bot.exceptions.YandexApiException;
import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class YandexAPIService {

    public YandexAPIService() {

    }

    @Value("${yandex.pogoda.token}")
    String token;

    public String getForcast(Double lat, Double lon) throws YandexApiException {

        WebClient  webClient =  WebClient
                .builder()
                .baseUrl("https://api.weather.yandex.ru/v2")
                .build();

        String forcastText = "Прогноз погоды по часам:\n";


        try {

            log.info( "Request Yandex param lat " + lat.toString() + "lon "+lon.toString());

            String responseJson = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/forecast")
                            .queryParam("lat", lat.toString())
                            .queryParam("lon", lon.toString())
                            .queryParam("lang", "ru_RU")
                            .queryParam("limit", "1")
                            .queryParam("hours", "true")
                            .queryParam("extra", "true")
                            .build())
                    .header("X-Yandex-API-Key", token)
                    .retrieve()
//                    .onStatus(
//                            HttpStatus.INTERNAL_SERVER_ERROR::equals,
//                            response -> response.bodyToMono(String.class).map(YandexApiExeption::new))
//                    .onStatus(
//                            HttpStatus.BAD_REQUEST::equals,
//                            response -> response.bodyToMono(String.class).map(YandexApiExeption::new))
//                    .onStatus(
//                            HttpStatus.FORBIDDEN::equals,
//                            response -> response.bodyToMono(String.class).map(YandexApiExeption::new))
                    .bodyToMono(String.class)
                    .block();

            log.info(responseJson);

            JsonObject jsonObjectAlt = JsonParser.parseString(responseJson).getAsJsonObject();

            for (JsonElement item: jsonObjectAlt.get("forecasts").getAsJsonArray().get(0).getAsJsonObject().get("hours").getAsJsonArray()) {

                String hour = item.getAsJsonObject().get("hour").getAsString();
                String condition = item.getAsJsonObject().get("condition").getAsString();
                forcastText += (hour.length() ==1 ? "0"+hour :hour) + ":00 - " + condition +"\n";
            }

            return forcastText;
        }catch (RuntimeException e){
            throw new YandexApiException(e.getMessage());
        }

    }

    public String getWeatherChangeRecommendation(Double lat, Double lon) {
        WebClient  webClient =  WebClient
                .builder()
                .baseUrl("https://api.weather.yandex.ru/v2")
                .build();

        String weatherChangeRecommendationText = "Рекоммендации о перепадах:\n";
        String responseJson = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/forecast")
                        .queryParam("lat", lat.toString())
                        .queryParam("lon", lon.toString())
                        .queryParam("lang", "ru_RU")
                        .queryParam("limit", "2")
                        .queryParam("date", "true")
                        .queryParam("hours", "false")
                        .queryParam("extra", "false")
                        .build())
                .header("X-Yandex-API-Key", token)
                .retrieve()
                .bodyToMono(String.class)
                .block();


        JsonObject jsonObjectAlt = JsonParser.parseString(responseJson).getAsJsonObject();
        List<Integer> temperatures = new ArrayList<>();
        for (JsonElement item : jsonObjectAlt.get("forecasts").getAsJsonArray()) {
            int dayTemperature = Integer.parseInt(item.getAsJsonObject().get("parts").getAsJsonObject().get("day_short").getAsJsonObject().get("temp").getAsString());
            temperatures.add(dayTemperature);
        }
        int todaysTemperature = temperatures.get(0);
        int tomorrowsTemperature = temperatures.get(1);

        if (Math.abs(todaysTemperature-tomorrowsTemperature)>=9)
            weatherChangeRecommendationText += "будет перепад, приобретите лекарство";
        else weatherChangeRecommendationText +="перепадов не ожидается";

        return weatherChangeRecommendationText;

    }

}