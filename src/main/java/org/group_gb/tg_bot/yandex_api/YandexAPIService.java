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


    public String getForcast(Integer daysForecast, Double lat, Double lon) {
        Integer df;

        WebClient webClient = WebClient
                .builder()
                .baseUrl("https://api.weather.yandex.ru/v2")
                .build();

        String forcastText = "";


        try {

            if (daysForecast == null) {
                df = 1;
            } else if (daysForecast == -1) {
                df = 1;
            } else {
                df = daysForecast;
            }

            log.info("Request Yandex param lat " + lat.toString() + "lon " + lon.toString());

            String responseJson = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/forecast")
                            .queryParam("lat", lat.toString())
                            .queryParam("lon", lon.toString())
                            .queryParam("lang", "ru_RU")
                            .queryParam("limit", df)
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

            if (daysForecast == null) {
                JsonObject fact = jsonObjectAlt.getAsJsonObject("fact");
                forcastText += "Сейчас: \n" + genForecastText(fact) + "\n" + "\n";
            } else if (daysForecast == -1) {
                forcastText += "Прогноз по часам:" + "\n";
                for (JsonElement item : jsonObjectAlt.get("forecasts").getAsJsonArray().get(0).getAsJsonObject().get("hours").getAsJsonArray()) {
                    String hour = item.getAsJsonObject().get("hour").getAsString();
                    String condition = item.getAsJsonObject().get("condition").getAsString();
                    forcastText += (hour.length() == 1 ? "0" + hour : hour) + ":00 - " + condition + "\n";
                }
            } else {
                for (JsonElement item : jsonObjectAlt.get("forecasts").getAsJsonArray()) {

                    String date = item.getAsJsonObject().get("date").getAsString();
                    JsonObject dayShort = item.getAsJsonObject().getAsJsonObject("parts").getAsJsonObject("day_short");
                    JsonObject nightShort = item.getAsJsonObject().getAsJsonObject("parts").getAsJsonObject("night_short");
                    forcastText += date + " \n" + "Ночью: \n" + genForecastText(nightShort) + "Днем: \n" + genForecastText(dayShort) + "\n" + "\n";
                }

            }
            return forcastText;

        } catch (RuntimeException e) {
            throw new YandexApiException(e.getMessage());
        }

    }

    public String getWeatherChangeRecommendation(Integer daysForecast, Double lat, Double lon) {
        WebClient webClient = WebClient
                .builder()
                .baseUrl("https://api.weather.yandex.ru/v2")
                .build();

        String weatherChangeRecommendationText = "Рекоммендации о перепадах:\n";

        try {
            log.info("Request Yandex param lat " + lat.toString() + "lon " + lon.toString());

            String responseJson = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/forecast")
                            .queryParam("lat", lat.toString())
                            .queryParam("lon", lon.toString())
                            .queryParam("lang", "ru_RU")

                            .queryParam("limit", daysForecast)
                            .queryParam("hours", "false")
                            .queryParam("extra", "true")

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

            if (Math.abs(todaysTemperature - tomorrowsTemperature) >= 9)
                weatherChangeRecommendationText += "будет перепад, приобретите лекарство";
            else weatherChangeRecommendationText += "перепадов не ожидается";

            return weatherChangeRecommendationText;
        } catch (RuntimeException e) {
            throw new YandexApiException(e.getMessage());
        }

    }

    public String genForecastText(JsonObject jsonObject) {
        String genText = "";
        String conditionRus = "";
        String windDirRus = "";
        String condition = jsonObject.getAsJsonObject().get("condition").getAsString();
        switch (condition) {
            case "clear":
                conditionRus = "ясно";
                break;
            case "partly-cloudy":
                conditionRus = "малооблачно";
                break;
            case "cloudy":
                conditionRus = "облачно с прояснениями";
                break;
            case "overcast":
                conditionRus = "пасмурно";
                break;
            case "drizzle":
                conditionRus = "морось";
                break;
            case "light-rain":
                conditionRus = "небольшой дождь";
                break;
            case "rain":
                conditionRus = "дождь";
                break;
            case "moderate-rain":
                conditionRus = "умеренно сильный дождь";
                break;
            case "heavy-rain":
                conditionRus = "сильный дождь";
                break;
            case "continuous-heavy-rain":
                conditionRus = "длительный сильный дождь";
                break;
            case "showers":
                conditionRus = "ливень";
                break;
            case "wet-snow":
                conditionRus = "дождь со снегом";
                break;
            case "light-snow":
                conditionRus = "небольшой снег";
                break;
            case "snow":
                conditionRus = "снег";
                break;
            case "snow-showers":
                conditionRus = "снегопад";
                break;
            case "hail":
                conditionRus = "град";
                break;
            case "thunderstorm":
                conditionRus = "гроза";
                break;
            case "thunderstorm-with-rain":
                conditionRus = "дождь с грозой";
                break;
            case "thunderstorm-with-hail":
                conditionRus = "дождь с градом";
                break;
        }

        String temp = jsonObject.getAsJsonObject().get("temp").getAsString();
        String feelsLike = jsonObject.getAsJsonObject().get("feels_like").getAsString();
        String windSpeed = jsonObject.getAsJsonObject().get("wind_speed").getAsString();
        String windGust = jsonObject.getAsJsonObject().get("wind_gust").getAsString();
        String windDir = jsonObject.getAsJsonObject().get("wind_dir").getAsString();
        switch (windDir) {
            case "nw":
                windDirRus = " северо-западное";
                break;
            case "n":
                windDirRus = "северное";
                break;
            case "ne":
                windDirRus = "северо-восточное";
                break;
            case "e":
                windDirRus = "восточное";
                break;
            case "se":
                windDirRus = "юго-восточное";
                break;
            case "s":
                windDirRus = "южное";
                break;
            case "sw":
                windDirRus = "юго-западное";
                break;
            case "w":
                windDirRus = "западное";
                break;
            case "с":
                windDirRus = "штиль";
                break;
        }

        String pressureMM = jsonObject.getAsJsonObject().get("pressure_mm").getAsString();
        String humidity = jsonObject.getAsJsonObject().get("humidity").getAsString();

        genText = conditionRus + " \n" +
                "Температура воздуха " + temp + "°C(ощущается как " + feelsLike + "°C) \n" +
                "Направление ветра " + windDirRus + ", скорость " + windSpeed + "м/c, порывы " + windGust + "м/c \n" +
                "Атмосферное давление " + pressureMM + "мм рт.ст., влажность воздуха " + humidity + "% \n";
        return genText;
    }
}






