package org.group_gb.tg_bot.yandexAPI;


import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class YandexAPIService {

    private final WebClient webClient;

    public YandexAPIService() {
        this.webClient =  WebClient
                .builder()
                .baseUrl("https://api.weather.yandex.ru/v2")
                .build();;
    }

    @Value("${yandex.pogoda.token}")
    String token;

    public String getForcast(Double lat, Double lon) {

        String forcastText = "Прогноз погоды по часам:\n";

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
                .header("X-Yandex-API-Key",token)
                .retrieve()
                .bodyToMono(String.class)
                .block();
        //TODO 2 Сделать проверку на статус код сообщения

        JsonObject jsonObjectAlt = JsonParser.parseString(responseJson).getAsJsonObject();

        for (JsonElement item: jsonObjectAlt.get("forecasts").getAsJsonArray().get(0).getAsJsonObject().get("hours").getAsJsonArray()) {

           String hour = item.getAsJsonObject().get("hour").getAsString();
           String condition = item.getAsJsonObject().get("condition").getAsString();

           forcastText += (hour.length() ==1 ? "0"+hour :hour) + ":00 - " + condition +"\n";

        }

        return forcastText;

    }


}
