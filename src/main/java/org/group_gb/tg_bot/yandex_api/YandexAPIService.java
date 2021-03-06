package org.group_gb.tg_bot.yandex_api;


import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.extern.slf4j.Slf4j;
import org.group_gb.tg_bot.exceptions.YandexApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class YandexAPIService {

    public YandexAPIService() {

    }

    @Value("${yandex.pogoda.token}")
    String token;


    public String getForcast(Integer numberCommand, Double lat, Double lon) {
        Integer daysForecast;

        WebClient webClient = WebClient
                .builder()
                .baseUrl("https://api.weather.yandex.ru/v2")
                .build();

        String forcastText = "";


        try {

            if (numberCommand == -3) {
                daysForecast = 1;
            } else if (numberCommand == -1) {
                daysForecast = 1;
            }else if (numberCommand == -2){
                daysForecast = 2;
            } else {
                daysForecast = numberCommand;
            }

            log.info("Request Yandex param lat " + lat.toString() + "lon " + lon.toString());

            String responseJson = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/forecast")
                            .queryParam("lat", lat.toString())
                            .queryParam("lon", lon.toString())
                            .queryParam("lang", "ru_RU")
                            .queryParam("limit", daysForecast)
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

            if (numberCommand == -3) {
                JsonObject fact = jsonObjectAlt.getAsJsonObject("fact");
                forcastText += "????????????: \n" + genForecastText(fact) + "\n" + "\n";
            } else if (numberCommand == -1) {
                forcastText += "?????????????? ???? ??????????:" + "\n";
                for (JsonElement item : jsonObjectAlt.get("forecasts").getAsJsonArray().get(0).getAsJsonObject().get("hours").getAsJsonArray()) {
                    String hour = item.getAsJsonObject().get("hour").getAsString();
                    String condition = item.getAsJsonObject().get("condition").getAsString();
                    forcastText += (hour.length() == 1 ? "0" + hour : hour) + ":00 - " + condition + "\n";
                }
            } else if (numberCommand == -2) {
                forcastText = "?????????????????????????? ?? ??????????????????:\n";

                List<Integer> temperatures = new ArrayList<>();
                for (JsonElement item : jsonObjectAlt.get("forecasts").getAsJsonArray()) {
                    int dayTemperature = Integer.parseInt(item.getAsJsonObject().get("parts").getAsJsonObject().get("day_short").getAsJsonObject().get("temp").getAsString());
                    temperatures.add(dayTemperature);
                }
                int todaysTemperature = temperatures.get(0);
                int tomorrowsTemperature = temperatures.get(1);

                if (Math.abs(todaysTemperature - tomorrowsTemperature) >= 9)
                    forcastText += "?????????? ??????????????, ?????????????????????? ??????????????????";
                else forcastText += "?????????????????? ???? ??????????????????";

            } else {
                for (JsonElement item : jsonObjectAlt.get("forecasts").getAsJsonArray()) {

                    String date = item.getAsJsonObject().get("date").getAsString();
                    JsonObject dayShort = item.getAsJsonObject().getAsJsonObject("parts").getAsJsonObject("day_short");
                    JsonObject nightShort = item.getAsJsonObject().getAsJsonObject("parts").getAsJsonObject("night_short");
                    forcastText += date + " \n" + "??????????: \n" + genForecastText(nightShort) + "????????: \n" + genForecastText(dayShort) + "\n" + "\n";
                }

            }

            return forcastText;

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
                conditionRus = "????????";
                break;
            case "partly-cloudy":
                conditionRus = "??????????????????????";
                break;
            case "cloudy":
                conditionRus = "?????????????? ?? ????????????????????????";
                break;
            case "overcast":
                conditionRus = "????????????????";
                break;
            case "drizzle":
                conditionRus = "????????????";
                break;
            case "light-rain":
                conditionRus = "?????????????????? ??????????";
                break;
            case "rain":
                conditionRus = "??????????";
                break;
            case "moderate-rain":
                conditionRus = "???????????????? ?????????????? ??????????";
                break;
            case "heavy-rain":
                conditionRus = "?????????????? ??????????";
                break;
            case "continuous-heavy-rain":
                conditionRus = "???????????????????? ?????????????? ??????????";
                break;
            case "showers":
                conditionRus = "????????????";
                break;
            case "wet-snow":
                conditionRus = "?????????? ???? ????????????";
                break;
            case "light-snow":
                conditionRus = "?????????????????? ????????";
                break;
            case "snow":
                conditionRus = "????????";
                break;
            case "snow-showers":
                conditionRus = "????????????????";
                break;
            case "hail":
                conditionRus = "????????";
                break;
            case "thunderstorm":
                conditionRus = "??????????";
                break;
            case "thunderstorm-with-rain":
                conditionRus = "?????????? ?? ????????????";
                break;
            case "thunderstorm-with-hail":
                conditionRus = "?????????? ?? ????????????";
                break;
        }

        String temp = jsonObject.getAsJsonObject().get("temp").getAsString();
        String feelsLike = jsonObject.getAsJsonObject().get("feels_like").getAsString();
        String windSpeed = jsonObject.getAsJsonObject().get("wind_speed").getAsString();
        String windGust = jsonObject.getAsJsonObject().get("wind_gust").getAsString();
        String windDir = jsonObject.getAsJsonObject().get("wind_dir").getAsString();
        switch (windDir) {
            case "nw":
                windDirRus = " ????????????-????????????????";
                break;
            case "n":
                windDirRus = "????????????????";
                break;
            case "ne":
                windDirRus = "????????????-??????????????????";
                break;
            case "e":
                windDirRus = "??????????????????";
                break;
            case "se":
                windDirRus = "??????-??????????????????";
                break;
            case "s":
                windDirRus = "??????????";
                break;
            case "sw":
                windDirRus = "??????-????????????????";
                break;
            case "w":
                windDirRus = "????????????????";
                break;
            case "??":
                windDirRus = "??????????";
                break;
        }

        String pressureMM = jsonObject.getAsJsonObject().get("pressure_mm").getAsString();
        String humidity = jsonObject.getAsJsonObject().get("humidity").getAsString();

        genText = conditionRus + " \n" +
                "?????????????????????? ?????????????? " + temp + "??C(?????????????????? ?????? " + feelsLike + "??C) \n" +
                "?????????????????????? ?????????? " + windDirRus + ", ???????????????? " + windSpeed + "??/c, ???????????? " + windGust + "??/c \n" +
                "?????????????????????? ???????????????? " + pressureMM + "???? ????.????., ?????????????????? ?????????????? " + humidity + "% \n";
        return genText;
    }
}






